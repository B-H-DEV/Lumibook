export class AudioEngine {
    constructor() {
        this.ctx = null;
        this.masterGain = null;
        this.compressor = null;
        this.analyser = null;
        this.soundNodes = {}; 
        this.buffers = {};

        // Moteur de temps absolu (Lookahead Scheduling via Web Worker)
        this.scheduledEvents = [];
        this.lookahead = 2.5; // Prévoir 2.5 sec à l'avance pour contrer le throttling (1s) d'Android
        this.worker = null;
    }

    initSchedulerWorker() {
        // Si le scheduler est déjà actif, on ne resynchronise PAS les événements
        // (sinon on ré-étale les nextTime à chaque play() et on crée des doublons
        // de notes / des bursts). On se contente de vérifier que le contexte tourne.
        if (this.worker || this.schedulerTimer) {
            console.log("Scheduler already active, skipping resync");
            return;
        }

        // ANTI-BURST : on resynchronise les événements UNIQUEMENT quand on
        // redémarre réellement le scheduler (après un stop ou un suspend).
        this.resyncScheduledEvents();
        // Réactivation du Web Worker pour meilleure stabilité audio
        // Fallback vers setInterval si le Worker échoue
        try {
            const workerScript = `
                self.onmessage = function(e) {
                    setInterval(() => {
                        self.postMessage('tick');
                    }, 100);
                };
            `;
            const blob = new Blob([workerScript], { type: 'application/javascript' });
            const workerUrl = URL.createObjectURL(blob);
            
            this.worker = new Worker(workerUrl);
            this.worker.onmessage = () => {
                this.processScheduledEvents();
            };
            this.worker.postMessage('start');
            console.log("Web Worker scheduler started successfully");
        } catch (e) {
            console.warn("Web Worker failed, falling back to setInterval:", e);
            this.schedulerTimer = setInterval(() => {
                this.processScheduledEvents();
            }, 100);
        }
    }

    stopScheduler() {
        console.log("Stopping audio scheduler...");
        if (this.worker) {
            this.worker.terminate();
            this.worker = null;
            console.log("Web Worker terminated");
        }
        if (this.schedulerTimer) {
            clearInterval(this.schedulerTimer);
            this.schedulerTimer = null;
            console.log("setInterval scheduler stopped");
        }
    }

    /**
     * ANTI-CRASH : vérifie que le moteur audio est dans un état sain.
     * Retourne false si le contexte est fermé ou si le scheduler est mort.
     * Utilisé par le bridge pour décider s'il faut réinitialiser.
     */
    isHealthy() {
        if (!this.ctx) return false;
        if (this.ctx.state === 'closed') return false;
        // Si le scheduler est censé tourner mais qu'il n'y a ni worker ni timer,
        // c'est qu'il est mort.
        if (!this.worker && !this.schedulerTimer) return false;
        return true;
    }

    /**
     * ANTI-CRASH : réinitialise complètement le moteur audio.
     * À appeler si isHealthy() retourne false (ex: après un crash du render
     * process WebView ou un AudioContext fermé).
     */
    async hardReset() {
        console.log("AudioEngine.hardReset() - Rebuilding audio engine");
        try {
            this.stopScheduler();
        } catch (e) {}
        try {
            if (this.ctx && this.ctx.state !== 'closed') {
                await this.ctx.close();
            }
        } catch (e) {}
        this.ctx = null;
        this.masterGain = null;
        this.compressor = null;
        this.analyser = null;
        this.soundNodes = {};
        this.buffers = {};
        this.scheduledEvents = [];
        this.worker = null;
        this.schedulerTimer = null;
        await this.init();
        console.log("AudioEngine.hardReset() - Done");
    }

    registerGenerativeEvent(id, generateCallback, initialOffset = 0) {
        this.scheduledEvents.push({
            id: id,
            nextTime: (this.ctx ? this.ctx.currentTime : 0) + initialOffset,
            generate: generateCallback
        });
    }

    processScheduledEvents() {
        // ANTI-CRASH : on vérifie que le contexte existe ET n'est pas fermé.
        // Un AudioContext fermé (closed) ne peut plus rien jouer et ferait
        // planter silencieusement tout le scheduler.
        if (!this.ctx || this.ctx.state === 'closed') return;
        if (this.ctx.state !== 'running') return;
        const now = this.ctx.currentTime;

        this.scheduledEvents.forEach(event => {
            // ANTI-BURST : Si l'événement a accumulé trop de retard (ex: après une
            // suspension du AudioContext), on le resynchronise sur "now" au lieu de
            // rattraper toutes les notes manquées d'un coup (ce qui créerait un burst).
            if (event.nextTime < now - 0.5) {
                event.nextTime = now;
            }

            // ANTI-BURST RENFORCÉ : on limite à 4 événements par tick et par piste.
            // Avant, un retard important pouvait déclencher jusqu'à 64 notes d'un coup
            // (=> saturation CPU + burst audio = pops). On préfère "sauter" les notes
            // manquées plutôt que de les rattraper.
            let safety = 0;
            while (event.nextTime < now + this.lookahead && safety < 4) {
                const master = this.soundNodes[event.id];
                const isMuted = !master || master.userVolume <= 0.001;

                let delayInSeconds = event.generate(event.nextTime, isMuted);
                // Sécurité : un délai nul ou négatif ferait boucler à l'infini.
                if (!(delayInSeconds > 0)) delayInSeconds = 0.1;
                event.nextTime += delayInSeconds;
                safety++;
            }

            // Si on a atteint la limite, on resynchronise pour ne pas accumuler
            // un retard qui provoquerait un burst au tick suivant.
            if (safety >= 4 && event.nextTime < now) {
                event.nextTime = now + 0.05;
            }
        });
    }

    /**
     * Resynchronise tous les événements planifiés en les ÉTALANT aléatoirement
     * dans la fenêtre de lookahead. À appeler après un resume() du AudioContext.
     *
     * ANTI-BURST : on ne met surtout PAS tous les nextTime à "now", sinon tous
     * les événements se déclencheraient au même instant (burst). On les répartit
     * sur la durée du lookahead pour retrouver un flux naturel.
     */
    resyncScheduledEvents() {
        if (!this.ctx) return;
        const now = this.ctx.currentTime;
        const spread = Math.max(0.1, this.lookahead * 0.9);
        this.scheduledEvents.forEach(event => {
            // Décalage aléatoire réparti sur la fenêtre de lookahead
            event.nextTime = now + Math.random() * spread;
        });
    }

    async init() {
        console.log("AudioEngine.init() - Starting initialization");
        const AudioContextClass = window.AudioContext || window.webkitAudioContext;
        try {
            this.ctx = new AudioContextClass();
            console.log("AudioContext created successfully:", this.ctx);
        } catch (e) {
            console.error("AudioContext initialization failed:", e);
            // On continue quand même pour ne pas bloquer l'app
            return;
        }

        // Compresseur
        this.compressor = this.ctx.createDynamicsCompressor();
        this.compressor.threshold.value = -12;
        this.compressor.ratio.value = 12;

        // Master (initialisé à mi-hauteur 50%)
        // ANTI-POP : on démarre à 0 et on fade-in doucement pour éviter tout clic au resume()
        this.masterGain = this.ctx.createGain();
        this.masterGain.gain.value = 0.0;
        // ANTI-CONFLIT : on mémorise la valeur cible pour getVolume().
        this.masterGain.gain.__targetVolume = 0.5;
        this.masterGain.gain.setTargetAtTime(0.5, this.ctx.currentTime, 0.05);

        // --- BUS DE RÉVERBÉRATION ---
        this.reverbNode = this.ctx.createConvolver();
        this.reverbNode.buffer = this.createReverbBuffer(2.0, 2.0);

        this.reverbGain = this.ctx.createGain();
        this.reverbGain.gain.value = 0.5;

        this.reverbNode.connect(this.reverbGain);
        this.reverbGain.connect(this.compressor);

        // --- FILTRE GLOBAL DOUX / CLAIR ---
        this.toneFilter = this.ctx.createBiquadFilter();
        this.toneFilter.type = 'lowpass';
        this.toneFilter.frequency.value = 12000;

        // 2ème filtre pour gérer le bas (high-pass subtil)
        this.lofiLowFilter = this.ctx.createBiquadFilter();
        this.lofiLowFilter.type = 'highpass';
        this.lofiLowFilter.frequency.value = 20; // neutre ~ inaudible

        // ANTI-POP : DC blocker en sortie master. Les buffers de bruit (brown/pink)
        // peuvent avoir un offset DC résiduel qui provoque un "pop" à chaque boucle.
        this.dcBlocker = this.ctx.createBiquadFilter();
        this.dcBlocker.type = 'highpass';
        this.dcBlocker.frequency.value = 20;
        this.dcBlocker.Q.value = 0.7;

        // Chaîne : Comp -> Tone -> LofiLow -> DCBlocker -> Master -> Analyser -> Destination
        this.analyser = this.ctx.createAnalyser();
        this.analyser.fftSize = 2048;

        this.compressor.connect(this.toneFilter);
        this.toneFilter.connect(this.lofiLowFilter);
        this.lofiLowFilter.connect(this.dcBlocker);
        this.dcBlocker.connect(this.masterGain);
        this.masterGain.connect(this.analyser);
        this.analyser.connect(this.ctx.destination);

        // Buffers
        this.buffers.white = this.createNoiseBuffer('white');
        this.buffers.pink = this.createNoiseBuffer('pink');
        this.buffers.brown = this.createNoiseBuffer('brown');

        this.setLofiMode('off');
        console.log("Starting initSounds()...");
        this.initSounds();
        console.log("initSounds() completed");
        console.log("Starting initSchedulerWorker()...");
        this.initSchedulerWorker(); // Démarrage du métronome absolu via Web Worker
        console.log("initSchedulerWorker() completed");

        if (this.ctx.state === 'suspended') {
            await this.ctx.resume();
        }
    }

    setLofiMode(mode) {
        if (!this.ctx || !this.toneFilter || !this.lofiLowFilter) return;

        const now = this.ctx.currentTime;

        if (mode === 'off') {
            this.toneFilter.type = 'lowpass';
            this.toneFilter.frequency.setTargetAtTime(12000, now, 0.2);
            this.lofiLowFilter.type = 'highpass';
            this.lofiLowFilter.frequency.setTargetAtTime(20, now, 0.2);
        } else if (mode === 'soft') {
            // un peu plus doux qu'avant
            this.toneFilter.type = 'lowpass';
            this.toneFilter.frequency.setTargetAtTime(7000, now, 0.2);

            this.lofiLowFilter.type = 'highpass';
            this.lofiLowFilter.frequency.setTargetAtTime(50, now, 0.2);
        } else if (mode === 'deep') {
            // “Let’s go deeper” : grosse couverture
            this.toneFilter.type = 'lowpass';
            this.toneFilter.frequency.setTargetAtTime(4000, now, 0.2);

            this.lofiLowFilter.type = 'highpass';
            this.lofiLowFilter.frequency.setTargetAtTime(80, now, 0.2);
        }
    }

    setTone(val) {
        if (!this.toneFilter) return;

        // val entre 0 et 1
        // 0   => low-pass très doux (1500 Hz)
        // 0.5 => neutre (12000 Hz low-pass)
        // 1   => high-pass pour éclaircir (300 Hz)
        const now = this.ctx.currentTime;

        if (val < 0.5) {
            const t = val / 0.5; // 0 -> 1
            const freq = 1500 + t * (12000 - 1500);
            this.toneFilter.type = 'lowpass';
            this.toneFilter.frequency.setTargetAtTime(freq, now, 0.1);
        } else {
            const t = (val - 0.5) / 0.5; // 0 -> 1
            const freq = 50 + t * (300 - 50);
            this.toneFilter.type = 'highpass';
            this.toneFilter.frequency.setTargetAtTime(freq, now, 0.1);
        }
    }
    createNoiseBuffer(type) {
        const bufferSize = 4 * this.ctx.sampleRate;
        const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
        const output = buffer.getChannelData(0);

        let b0 = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0, b5 = 0, b6 = 0;
        let lastOut = 0;

        for (let i = 0; i < bufferSize; i++) {
            const white = Math.random() * 2 - 1;
            if (type === 'white') {
                output[i] = white;
            } else if (type === 'pink') {
                b0 = 0.99886 * b0 + white * 0.0555179;
                b1 = 0.99332 * b1 + white * 0.0750759;
                b2 = 0.96900 * b2 + white * 0.1538520;
                b3 = 0.86650 * b3 + white * 0.3104856;
                b4 = 0.55000 * b4 + white * 0.5329522;
                b5 = -0.7616 * b5 - white * 0.0168980;
                output[i] = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362;
                output[i] *= 0.11;
                b6 = white * 0.115926;
            } else if (type === 'brown') {
                output[i] = (lastOut + (0.02 * white)) / 1.02;
                lastOut = output[i];
                output[i] *= 3.5;
            }
        }

        // Fondu croisé d'égalisation (crossfade 500 échantillons) pour boucle sans aucun clic/pop
        const fadeSamples = 500;
        for (let i = 0; i < fadeSamples; i++) {
            const alpha = i / fadeSamples;
            output[i] = output[i] * alpha + output[bufferSize - fadeSamples + i] * (1 - alpha);
        }

        // ANTI-POP : suppression de l'offset DC résiduel (moyenne non nulle).
        // Un offset DC provoque un "pop" à chaque boucle du buffer.
        let sum = 0;
        for (let i = 0; i < bufferSize; i++) sum += output[i];
        const dcOffset = sum / bufferSize;
        if (Math.abs(dcOffset) > 0.0001) {
            for (let i = 0; i < bufferSize; i++) output[i] -= dcOffset;
        }

        return buffer;
    }

    // Génère une réponse impulsionnelle synthétique optimisée pour la réverbération
    createReverbBuffer(duration = 0.8, decay = 2.0) {
        const sampleRate = this.ctx.sampleRate;
        const length = Math.floor(sampleRate * duration);
        const impulse = this.ctx.createBuffer(2, length, sampleRate);
        const left = impulse.getChannelData(0);
        const right = impulse.getChannelData(1);

        for (let i = 0; i < length; i++) {
            const n = duration - i / sampleRate;
            const env = Math.pow(Math.max(0, n / duration), decay);
            left[i] = (Math.random() * 2 - 1) * env;
            right[i] = (Math.random() * 2 - 1) * env;
        }
        return impulse;
    }

    // Nouvelle méthode pour ajuster le niveau de la reverb
    setReverbLevel(val) {
        if (this.reverbGain) {
            // Lissage pour éviter les "clics" sonores lors du changement
            this.reverbGain.gain.setTargetAtTime(val, this.ctx.currentTime, 0.1);
        }
    }
                
    initSounds() {
        this.createNoiseSource('rain', 'pink', 'lowpass', 800, false, 0.1, 0.6);
        this.createBirds('birds');
        this.createClock('clock'); 
        this.createNoiseSource('waterfall', 'brown', 'lowpass', 600);
        this.createNoiseSource('stream', 'white', 'highpass', 2000, false, 1, 0.05); 
        this.createNoiseSource('ocean', 'pink', 'lowpass', 350, true, 0.05, 2.5); 
        this.createNoiseSource('wind', 'pink', 'bandpass', 400, true, 0.15); 

        this.createCricketsHigh('crickets'); 
        this.createOwl('owl');
        this.createPurrGrainy('purr');
        this.createNoiseSource('fan', 'brown', 'lowpass', 400, true, 8.0, 3.5);
        this.createCyberNight('night');
        this.createVinylSource('vinyl');
        this.createHeartbeat('heartbeat');

        this.createPureSine('solfeggio', 528, 0.07); 
        this.createPureSine('schumann', 50, 0.8); 
        this.createDroneSource('alpha', 100, 108); 
        this.createDroneSource('theta', 150, 155); 
        this.createDroneSource('delta', 60, 62);   
        this.createDroneSource('gamma', 200, 240); 
        this.createPureSine('subbass', 45, 1.0);

        this.createMelodyGenerator('bamboo', 'bamboo'); 
        this.createMelodyGenerator('metal', 'metal');
        this.createMelodyGenerator('handpan', 'handpan'); 
        this.createMelodyGenerator('kalimba', 'kalimba'); 
        this.createMelodyGenerator('bells', 'bells');
        this.createMelodyGenerator('panflute', 'panflute'); 
        this.createMelodyGenerator('harp', 'harp'); 

        this.createChoir('choir');
        this.createStrings('strings');
        this.createIceTexture('ice');
        this.createCrystalDrops('crystal'); 
        this.createPadSource('space');
        this.createSingingBowl('bowl_mid', 220); 
        this.createMagneticField('magnetic'); 
    }

    createClock(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        this.soundNodes[id] = master; master.connect(this.compressor);

        const filter = this.ctx.createBiquadFilter(); filter.type = 'bandpass'; filter.frequency.value = 800; filter.Q.value = 2;
        const filter2 = this.ctx.createBiquadFilter(); filter2.type = 'bandpass'; filter2.frequency.value = 600; filter2.Q.value = 2;
        filter.connect(master);
        filter2.connect(master);

        this.registerGenerativeEvent(id, (t, isMuted) => {
            if (!isMuted) {
                try {
                    const noise = this.ctx.createBufferSource(); noise.buffer = this.buffers['white'];
                    const g = this.ctx.createGain(); g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(1, t+0.005); g.gain.exponentialRampToValueAtTime(0.001, t+0.05);
                    noise.connect(g); g.connect(filter); noise.start(t); noise.stop(t+0.1);
                    
                    const t2 = t + 1.0;
                    const noise2 = this.ctx.createBufferSource(); noise2.buffer = this.buffers['white'];
                    const g2 = this.ctx.createGain(); g2.gain.setValueAtTime(0, t2); g2.gain.linearRampToValueAtTime(0.8, t2+0.005); g2.gain.exponentialRampToValueAtTime(0.001, t2+0.05);
                    noise2.connect(g2); g2.connect(filter2); noise2.start(t2); noise2.stop(t2+0.1);
                } catch(e) {}
            }
            return 2.0; // 2 secondes
        });
    }

    createBirds(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); 
        master.gain.value = 0.0001; 
        master.userVolume = 0;
        this.soundNodes[id] = master; 
        
        // NOUVEAU : Création du Panner
        const panner = this.ctx.createStereoPanner();
        panner.pan.value = 0; // Au centre par défaut
        
        // Routage : Master -> Panner -> Compressor (et Reverb)
        master.connect(panner);
        panner.connect(this.compressor);

        // Envoi Reverb (si vous l'aviez mis)
        const reverbSend = this.ctx.createGain();
        reverbSend.gain.value = 0.5;
        panner.connect(reverbSend);
        reverbSend.connect(this.reverbNode);

        this.registerGenerativeEvent(id, (t, isMuted) => {
            const nextDelay = (Math.random() * 2000 + 800) / 1000; // secondes
            if (!isMuted) {
                try {
                    const newPan = (Math.random() * 2) - 1;
                    panner.pan.setTargetAtTime(newPan, t, 0.1);

                    const osc = this.ctx.createOscillator(); osc.type = 'sine';
                    const startFreq = 2000 + Math.random() * 1000;
                    osc.frequency.setValueAtTime(startFreq, t); 
                    osc.frequency.exponentialRampToValueAtTime(startFreq/2, t + 0.1);
                    
                    const g = this.ctx.createGain(); 
                    g.gain.setValueAtTime(0, t); 
                    g.gain.linearRampToValueAtTime(0.6, t + 0.01); 
                    g.gain.exponentialRampToValueAtTime(0.001, t + 0.1);
                    
                    osc.connect(g); 
                    g.connect(master); 
                    osc.start(t); 
                    osc.stop(t + 0.15);
                } catch(e) {}
            }
            return nextDelay;
        });
    }


    createOwl(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        this.soundNodes[id] = master; 
        // NOUVEAU : Création du Panner
        const panner = this.ctx.createStereoPanner();
        panner.pan.value = 0; // Au centre par défaut
        
        // Routage : Master -> Panner -> Compressor (et Reverb)
        master.connect(panner);
        panner.connect(this.compressor);
        // On limite l'envoi reverb à 50%
        const reverbSend = this.ctx.createGain();
        reverbSend.gain.value = 0.50; 
        master.connect(reverbSend);
        reverbSend.connect(this.reverbNode);
        this.registerGenerativeEvent(id, (t, isMuted) => {
            const nextDelay = (Math.random() * 4000 + 2000) / 1000;
            if (!isMuted) {
                try {
                    const osc = this.ctx.createOscillator(); osc.type = 'sine';
                    osc.frequency.setValueAtTime(400, t); osc.frequency.linearRampToValueAtTime(300, t + 0.4);
                    const g = this.ctx.createGain();
                    g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.25, t + 0.1); g.gain.linearRampToValueAtTime(0.1, t + 0.2); 
                    g.gain.linearRampToValueAtTime(0.25, t + 0.3); g.gain.exponentialRampToValueAtTime(0.001, t + 0.8);
                    osc.connect(g); g.connect(master); osc.start(t); osc.stop(t + 1);
                } catch(e) {}
            }
            return nextDelay;
        });
    }

    createHeartbeat(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        this.soundNodes[id] = master; master.connect(this.compressor);
        this.registerGenerativeEvent(id, (t, isMuted) => {
            if (!isMuted) {
                try {
                    const osc = this.ctx.createOscillator(); osc.frequency.setValueAtTime(60, t); osc.frequency.exponentialRampToValueAtTime(30, t + 0.1);
                    const g = this.ctx.createGain(); g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(1.5, t + 0.05); g.gain.exponentialRampToValueAtTime(0.001, t + 0.4);
                    osc.connect(g); g.connect(master); osc.start(t); osc.stop(t + 0.5);
                    
                    const t2 = t + 0.3;
                    const osc2 = this.ctx.createOscillator(); osc2.frequency.setValueAtTime(50, t2); osc2.frequency.exponentialRampToValueAtTime(25, t2 + 0.15);
                    const g2 = this.ctx.createGain(); g2.gain.setValueAtTime(0, t2); g2.gain.linearRampToValueAtTime(1.0, t2 + 0.05); g2.gain.exponentialRampToValueAtTime(0.001, t2 + 0.5);
                    osc2.connect(g2); g2.connect(master); osc2.start(t2); osc2.stop(t2 + 0.6);
                } catch(e) {}
            }
            return 1.5;
        });
    }

    createVinylSource(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        const hiss = this.ctx.createBufferSource(); hiss.buffer = this.buffers['pink']; hiss.loop = true;
        const hissFilter = this.ctx.createBiquadFilter(); hissFilter.type = 'highpass'; hissFilter.frequency.value = 4000;
        const hissGain = this.ctx.createGain(); hissGain.gain.value = 0.03;
        hiss.connect(hissFilter); hissFilter.connect(hissGain); hissGain.connect(master); hiss.start();
        this.soundNodes[id] = master; master.connect(this.compressor);
        const bpFilter = this.ctx.createBiquadFilter(); bpFilter.type = 'bandpass'; bpFilter.frequency.value = 2500; bpFilter.Q.value = 5;
        bpFilter.connect(master);

        this.registerGenerativeEvent(id, (t, isMuted) => {
            const nextDelay = (Math.random() * 1200 + 600) / 1000;
            if (!isMuted) {
                try {
                    const noise = this.ctx.createBufferSource(); noise.buffer = this.buffers['white'];
                    const g = this.ctx.createGain();
                    g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.04, t + 0.002); g.gain.exponentialRampToValueAtTime(0.0001, t + 0.03);
                    noise.connect(g); g.connect(bpFilter); noise.start(t); noise.stop(t + 0.04);
                } catch(e) {}
            }
            return nextDelay;
        });
    }

    createCrystalDrops(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        this.soundNodes[id] = master; master.connect(this.compressor);
        master.connect(this.reverbNode); // Envoie le son mélodique dans la reverb
        this.registerGenerativeEvent(id, (t, isMuted) => {
            const nextDelay = (Math.random() * 1000 + 500) / 1000;
            if (!isMuted) {
                try {
                    const osc = this.ctx.createOscillator(); osc.frequency.value = 1500 + Math.random() * 2000; osc.type = 'sine';
                    const g = this.ctx.createGain(); g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.1, t + 0.02); g.gain.exponentialRampToValueAtTime(0.001, t + 0.8);
                    osc.connect(g); g.connect(master); osc.start(t); osc.stop(t + 0.9);
                } catch(e) {}
            }
            return nextDelay;
        });
    }

    createCyberNight(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain();
        master.gain.value = 0.0001;
        master.userVolume = 0;
        this.soundNodes[id] = master;

        // Compresseur + très peu de reverb pour l'effet "collé au composant"
        master.connect(this.compressor);
        const reverbSend = this.ctx.createGain();
        reverbSend.gain.value = 0.15; // Reverb encore diminuée pour un son bien "sec"
        master.connect(reverbSend);
        reverbSend.connect(this.reverbNode);

        // 1. LE SOUFFLE DE CONNECTION (bruit blanc hyper filtré)
        const dataHiss = this.ctx.createBufferSource();
        dataHiss.buffer = this.buffers['white'];
        dataHiss.loop = true;
        
        // Filtre passe-haut extrême pour ne garder que le grésillement numérique
        const hissFilter = this.ctx.createBiquadFilter();
        hissFilter.type = 'highpass';
        hissFilter.frequency.value = 12000; 

        const hissGain = this.ctx.createGain();
        hissGain.gain.value = 0.2; // Volume de base du grésillement

        dataHiss.connect(hissFilter);
        hissFilter.connect(hissGain);
        hissGain.connect(master);
        dataHiss.start();

        // NOUVEAU: Création globale du panner
        const burstPanner = this.ctx.createStereoPanner();
        burstPanner.connect(master);

        // 2. SOUFFLE SPATIAL DOUX
        const generateBurst = (t, isMuted) => {
            const nextDelay = (Math.random() * 2500 + 1000) / 1000;
            if (!isMuted) {
                try {
                    const baseFreq = 2000 + Math.random() * 1500;
                    const osc = this.ctx.createOscillator();
                    osc.type = 'sine';
                    osc.frequency.setValueAtTime(baseFreq, t);

                    const g = this.ctx.createGain();
                    g.gain.setValueAtTime(0, t);
                    g.gain.linearRampToValueAtTime(0.03, t + 0.01);
                    g.gain.exponentialRampToValueAtTime(0.0001, t + 0.12);

                    burstPanner.pan.setTargetAtTime((Math.random() * 1.6) - 0.8, t, 0.01);

                    osc.connect(g);
                    g.connect(burstPanner);

                    osc.start(t);
                    osc.stop(t + 0.15);
                } catch(e) {}
            }
            return nextDelay;
        };

        this.registerGenerativeEvent(id, generateBurst, 0.0);
        this.registerGenerativeEvent(id, generateBurst, 0.5);
    }

    createMelodyGenerator(id, instrumentType) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        this.soundNodes[id] = master; 
        
        // Routage vers Compressor ET Reverb
        master.connect(this.compressor);
        master.connect(this.reverbNode);

        // Gamme Pentatonique (très relaxante)
        const scale = [261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25];
        
        // NOUVEAU : On garde en mémoire la dernière note jouée (index)
        let currentNoteIndex = Math.floor(scale.length / 2);

        // NOUVEAU: un seul Panner pour toute la piste (gain RAM massif)
        const trackPanner = this.ctx.createStereoPanner();
        trackPanner.connect(master);

        this.registerGenerativeEvent(id, (t, isMuted) => {
            let nextDelay = 0;
            if(instrumentType === 'bells' || instrumentType === 'kalimba') nextDelay = (Math.random() * 500 + 200) / 1000;
            else if (instrumentType === 'metal') nextDelay = (Math.random() * 3000 + 2000) / 1000;
            else nextDelay = (Math.random() * 1500 + 500) / 1000;

            if (!isMuted) {
                try {
                    // NOUVEAU : Marche aléatoire (Random Walk)
                    // Au lieu de prendre n'importe quelle note, on monte d'une note, on descend d'une note, ou on reste sur place.
                    const step = Math.floor(Math.random() * 3) - 1; // Donne -1, 0 ou +1
                    currentNoteIndex += step;
                    
                    // On s'assure de ne pas sortir du tableau
                    if (currentNoteIndex < 0) currentNoteIndex = 1;
                    if (currentNoteIndex >= scale.length) currentNoteIndex = scale.length - 2;

                    const freq = scale[currentNoteIndex];

                    // Spatialisation sur le panner global
                    trackPanner.pan.setTargetAtTime((Math.random() * 0.8) - 0.4, t, 0.05);
                    
                    if (instrumentType === 'bamboo') {
                        // "WOOD": Octave down (freq * 0.5)
                        const osc1 = this.ctx.createOscillator(); osc1.type = 'sine'; osc1.frequency.value = freq * 0.5;
                        const osc2 = this.ctx.createOscillator(); osc2.type = 'sine'; osc2.frequency.value = (freq * 0.5) * 2.6; 
                        const g = this.ctx.createGain();
                        g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.6, t+0.01); g.gain.exponentialRampToValueAtTime(0.001, t+0.4); 
                        osc1.connect(g); osc2.connect(g); g.connect(trackPanner);
                        osc1.start(t); osc1.stop(t+0.5); osc2.start(t); osc2.stop(t+0.5);

                        const noise = this.ctx.createBufferSource(); noise.buffer = this.buffers['white'];
                        const nFilter = this.ctx.createBiquadFilter(); nFilter.type = 'highpass'; nFilter.frequency.value = 1000;
                        const nGain = this.ctx.createGain(); nGain.gain.setValueAtTime(0, t); nGain.linearRampToValueAtTime(0.4, t+0.005); nGain.exponentialRampToValueAtTime(0.001, t+0.05);
                        noise.connect(nFilter); nFilter.connect(nGain); nGain.connect(master);
                        noise.start(t); noise.stop(t+0.1);
                    } 
                    else if (instrumentType === 'metal') {
                        const carrier = this.ctx.createOscillator(); carrier.frequency.value = freq;
                        const mod = this.ctx.createOscillator(); mod.frequency.value = freq * 2.4; 
                        const modG = this.ctx.createGain(); modG.gain.value = 225;
                        const g = this.ctx.createGain();
                        mod.connect(modG); modG.connect(carrier.frequency);
                        carrier.connect(g); g.connect(trackPanner);
                        g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.3, t + 0.02); g.gain.exponentialRampToValueAtTime(0.001, t + 3.0); 
                        carrier.start(t); mod.start(t); carrier.stop(t+3.1); mod.stop(t+3.1);
                    }
                    else if (instrumentType === 'handpan') {
                        const g = this.ctx.createGain();
                        g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.35, t + 0.03); g.gain.exponentialRampToValueAtTime(0.001, t + 3.0); 
                        const osc1 = this.ctx.createOscillator(); osc1.type = 'sine'; osc1.frequency.value = freq;
                        const osc2 = this.ctx.createOscillator(); osc2.type = 'sine'; osc2.frequency.value = freq * 2;
                        const osc3 = this.ctx.createOscillator(); osc3.type = 'sine'; osc3.frequency.value = freq * 3; 
                        const g1 = this.ctx.createGain(); g1.gain.value = 1.0;
                        const g2 = this.ctx.createGain(); g2.gain.value = 0.5;
                        const g3 = this.ctx.createGain(); g3.gain.value = 0.25;
                        osc1.connect(g1); g1.connect(g); osc2.connect(g2); g2.connect(g); osc3.connect(g3); g3.connect(g);
                        g.connect(trackPanner);
                        osc1.start(t); osc2.start(t); osc3.start(t);
                        osc1.stop(t+3.1); osc2.stop(t+3.1); osc3.stop(t+3.1);
                    }
                    else if (instrumentType === 'kalimba') {
                        const osc = this.ctx.createOscillator(); osc.type = 'sine'; osc.frequency.setValueAtTime(freq, t);
                        const g = this.ctx.createGain();
                        const click = this.ctx.createOscillator(); click.type = 'square'; click.frequency.value = 100; 
                        const clickG = this.ctx.createGain(); clickG.gain.setValueAtTime(0.05, t); clickG.gain.exponentialRampToValueAtTime(0.0001, t+0.02);
                        click.connect(clickG); clickG.connect(master); click.start(t); click.stop(t+0.03);
                        osc.connect(g); g.connect(trackPanner);
                        g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.4, t + 0.01); g.gain.exponentialRampToValueAtTime(0.001, t + 1.5); 
                        osc.start(t); osc.stop(t+1.6);
                    }
                    else if (instrumentType === 'bells') {
                        const osc = this.ctx.createOscillator(); osc.type = 'sine'; osc.frequency.value = freq * 4; 
                        const g = this.ctx.createGain();
                        osc.connect(g); g.connect(trackPanner);
                        g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.1, t + 0.01); g.gain.exponentialRampToValueAtTime(0.001, t + 1.0);
                        osc.start(t); osc.stop(t+1.1);
                    }
                    else if (instrumentType === 'panflute') {
                        // FIXED: Pure sound, low noise.
                        const pitch = freq / 2; 
                        const osc = this.ctx.createOscillator(); osc.type = 'triangle'; osc.frequency.value = pitch;
                        
                        const vib = this.ctx.createOscillator(); vib.frequency.value = 5; 
                        const vibG = this.ctx.createGain(); vibG.gain.value = 6; // Deep vibrato
                        vib.connect(vibG); vibG.connect(osc.frequency); vib.start(t); vib.stop(t+2.5);

                        // Subtle Chiff
                        const noise = this.ctx.createBufferSource(); noise.buffer = this.buffers['white'];
                        const nFilter = this.ctx.createBiquadFilter(); nFilter.type = 'bandpass'; nFilter.frequency.value = pitch * 3; nFilter.Q.value = 5; 
                        const g = this.ctx.createGain();
                        const noiseG = this.ctx.createGain(); noiseG.gain.value = 0.4; // Reduced
                        const toneG = this.ctx.createGain(); toneG.gain.value = 0.6; 
                        
                        // Lowpass tone
                        const toneFilter = this.ctx.createBiquadFilter(); toneFilter.type = 'lowpass'; toneFilter.frequency.value = pitch * 1.5;
                        osc.connect(toneFilter); toneFilter.connect(toneG); toneG.connect(g);
                        noise.connect(nFilter); nFilter.connect(noiseG); noiseG.connect(g);
                        g.connect(trackPanner);
                        
                        g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(0.6, t + 0.4); 
                        g.gain.linearRampToValueAtTime(0.4, t + 1.0); g.gain.linearRampToValueAtTime(0, t + 2.5);   
                        osc.start(t); noise.start(t); osc.stop(t+2.6); noise.stop(t+2.6);
                    }
                    else if (instrumentType === 'harp') {
                        const delay = Math.random() * 0.1;
                        const start = t + delay;
                        const g = this.ctx.createGain();
                        g.gain.setValueAtTime(0, start); g.gain.linearRampToValueAtTime(0.3, start + 0.1); g.gain.exponentialRampToValueAtTime(0.001, start + 3.0); 
                        const osc1 = this.ctx.createOscillator(); osc1.type = 'triangle'; osc1.frequency.value = freq;
                        const osc2 = this.ctx.createOscillator(); osc2.type = 'sine'; osc2.frequency.value = freq * 1.002; 
                        const lp = this.ctx.createBiquadFilter(); lp.type = 'lowpass'; lp.frequency.value = freq * 2;
                        osc1.connect(lp); lp.connect(g); osc2.connect(g); g.connect(trackPanner);
                        osc1.start(start); osc2.start(start); osc1.stop(start+3.1); osc2.stop(start+3.1);
                    }
                } catch(e) {}
            }
            return nextDelay;
        });
    }

    createMagneticField(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        this.soundNodes[id] = master; master.connect(this.compressor);
        master.connect(this.reverbNode);
        const osc = this.ctx.createOscillator(); osc.type = 'sine'; osc.frequency.value = 60;
        const osc2 = this.ctx.createOscillator(); osc2.type = 'triangle'; osc2.frequency.value = 60.5;
        const lfo = this.ctx.createOscillator(); lfo.frequency.value = 0.1;
        const lfoG = this.ctx.createGain(); lfoG.gain.value = 80; // amplitude réduite
        const filter = this.ctx.createBiquadFilter(); filter.type = 'lowpass'; filter.frequency.value = 200;
        // ANTI-ARTEFACT : on borne la fréquence du filtre entre 60 et 340 Hz
        // pour éviter qu'elle ne devienne négative (ce qui crée des clics).
        // On ajoute un offset DC de 200Hz pour que le LFO module AUTOUR de 200Hz
        // (200 ± 80 => 120..280 Hz) au lieu de partir de 0.
        const lfoOffset = this.ctx.createConstantSource();
        lfoOffset.offset.value = 200;
        lfoOffset.connect(filter.frequency);
        lfo.connect(lfoG); lfoG.connect(filter.frequency);
        osc.connect(filter); osc2.connect(filter); filter.connect(master);
        osc.start(); osc2.start(); lfo.start(); lfoOffset.start();
    }
    createCricketsHigh(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        const src = this.ctx.createBufferSource(); src.buffer = this.buffers['white']; src.loop = true;
        const filter = this.ctx.createBiquadFilter(); filter.type = 'bandpass'; filter.frequency.value = 5500; filter.Q.value = 8;
        const tremolo = this.ctx.createOscillator(); tremolo.frequency.value = 30; tremolo.type = 'triangle';
        const tremoloGain = this.ctx.createGain(); tremoloGain.gain.value = 0.2; // amplitude réduite
        const modGain = this.ctx.createGain();
        modGain.gain.value = 0.4; // <-- C'est ici que tu réduis le volume de base (par ex. à 40%)
        src.connect(filter); filter.connect(modGain); modGain.connect(master);
        // ANTI-DISTORSION : on module AUTOUR de 0.4 (offset) au lieu de partir de 0,
        // sinon le gain peut devenir négatif et créer des artefacts/clics.
        // On ajoute un ConstantSource pour garantir un offset positif.
        const modOffset = this.ctx.createConstantSource();
        modOffset.offset.value = 0.4;
        modOffset.connect(modGain.gain);
        tremolo.connect(tremoloGain); tremoloGain.connect(modGain.gain);
        src.start(); tremolo.start(); modOffset.start();
        this.soundNodes[id] = master; master.connect(this.compressor);
        const reverbSend = this.ctx.createGain();
        reverbSend.gain.value = 0.5;
        master.connect(reverbSend);
        reverbSend.connect(this.reverbNode);
    }
    createPurrGrainy(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        const src = this.ctx.createBufferSource(); src.buffer = this.buffers['pink']; src.loop = true;
        const filter = this.ctx.createBiquadFilter(); filter.type = 'lowpass'; filter.frequency.value = 250; 
        const grainOsc = this.ctx.createOscillator(); grainOsc.type = 'sawtooth'; grainOsc.frequency.value = 26; 
        const grainFilter = this.ctx.createBiquadFilter(); grainFilter.type = 'lowpass'; grainFilter.frequency.value = 100; 
        const grainGain = this.ctx.createGain(); grainGain.gain.value = 0.4; 
        src.connect(filter); filter.connect(master);
        grainOsc.connect(grainFilter); grainFilter.connect(grainGain); grainGain.connect(master);
        const breathLfo = this.ctx.createOscillator(); breathLfo.frequency.value = 0.2; 
        const breathLfoGain = this.ctx.createGain(); breathLfoGain.gain.value = 0.3; 
        const swellingNode = this.ctx.createGain(); swellingNode.gain.value = 0.7;
        // ANTI-CLIC : on ne déconnecte PAS master (ça coupe le son sèchement).
        // On route simplement master -> swellingNode -> compressor.
        master.connect(swellingNode); swellingNode.connect(this.compressor);
        breathLfo.connect(breathLfoGain); breathLfoGain.connect(swellingNode.gain);
        src.start(); grainOsc.start(); breathLfo.start();
        this.soundNodes[id] = master;
    }
    createSingingBowl(id, freq) {
        // Le gain contrôlé par l'utilisateur
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const gain = this.ctx.createGain(); 
        gain.gain.value = 0.0001; 
        gain.userVolume = 0;
        
        // NOUVEAU : Un réducteur global (On divise la puissance du bol par 4 pour l'intégrer au mix)
        const masterReducer = this.ctx.createGain();
        masterReducer.gain.value = 0.25; 
        masterReducer.connect(gain);

        // Les harmoniques : fondamentale (1), medium (2.7), aiguë (5.2)
        const ratios = [1, 2.7, 5.2]; 
        
        ratios.forEach((r, index) => {
            const osc = this.ctx.createOscillator(); 
            osc.type = 'sine'; 
            osc.frequency.value = freq * r; 
            
            // NOUVEAU : On gère le volume de chaque harmonique indépendamment
            const oscGain = this.ctx.createGain();
            
            if (index === 0) {
                oscGain.gain.value = 1.0;   // La note de base prend toute la place
            } else if (index === 1) {
                oscGain.gain.value = 0.3;   // La 1ère résonance est 3x moins forte
            } else {
                oscGain.gain.value = 0.1;   // Le petit tintement aigu est très discret
            }
            
            osc.connect(oscGain);
            oscGain.connect(masterReducer);
            osc.start(); 
        });
        
        gain.connect(this.compressor); 
        this.soundNodes[id] = gain; 
        gain.connect(this.reverbNode);
    }
    createNoiseSource(id, bufferType, filterType, freq, modulated=false, modSpeed=0.1, gainBoost=1.0) {
        const src = this.ctx.createBufferSource(); 
        src.buffer = this.buffers[bufferType]; 
        src.loop = true;
        
        const filter = this.ctx.createBiquadFilter(); 
        filter.type = filterType; 
        filter.frequency.value = freq;
        if(filterType === 'bandpass') filter.Q.value = 0.5;
        
        const preGain = this.ctx.createGain(); preGain.gain.value = gainBoost;
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement, sinon setTargetAtTime
        // met une éternité à décoller à cause de la courbe exponentielle).
        const gain = this.ctx.createGain(); gain.gain.value = 0.0001; gain.userVolume = 0;
        
        // NOUVEAU : Spatialisation lente
        const panner = this.ctx.createStereoPanner();
        
        // Routage
        src.connect(filter); 
        filter.connect(preGain); 
        preGain.connect(gain); 
        gain.connect(panner);
        panner.connect(this.compressor); 

        // NOUVEAU : Envoi subtil dans la reverb pour les sons d'ambiance de la pièce
        if (id === 'fan' || id === 'clock') {
            const reverbSend = this.ctx.createGain();
            reverbSend.gain.value = 0.3; // 40% de reverb pour le fan et la clock
            panner.connect(reverbSend);
            reverbSend.connect(this.reverbNode);
        }
        // Si c'est du vent, l'océan, OU LE VENTILATEUR, on ajoute un mouvement 3D
        if (id === 'wind' || id === 'ocean' || id === 'fan') {
            const panLfo = this.ctx.createOscillator();
            
            // Le ventilo tourne plus vite que l'océan (ex: 0.15 = 1 cycle toutes les ~6.5 secondes)
            panLfo.frequency.value = (id === 'fan') ? 0.10 : 0.05; 
            
            // ANTI-CLIC : on limite l'amplitude du LFO à 0.8 max pour ne pas
            // saturer le panner (qui accepte -1..1) et on lisse la modulation.
            const panLfoGain = this.ctx.createGain();
            panLfoGain.gain.value = 0.8;
            panLfo.connect(panLfoGain);
            panLfoGain.connect(panner.pan);
            panLfo.start();
        }
        // Le reste du code existant...
        src.start();
        if (modulated) {
            const lfo = this.ctx.createOscillator(); lfo.frequency.value = modSpeed;
            const lfoGain = this.ctx.createGain(); lfoGain.gain.value = freq * 0.3;
            lfo.connect(lfoGain); lfoGain.connect(filter.frequency); lfo.start();
        }
        this.soundNodes[id] = gain;
    }

    createDroneSource(id, baseFreq, beatFreq) {
        const gain = this.ctx.createGain(); 
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        gain.gain.value = 0.0001; 
        gain.userVolume = 0;
        
        // ANTI-POP : fade-in très court au démarrage pour éviter le clic
        const now = this.ctx.currentTime;
        gain.gain.setValueAtTime(0.0001, now);
        
        // NOUVEAU : Création d'un "Splitter" (Séparateur stéréo)
        const merger = this.ctx.createChannelMerger(2);

        // Oscillateur GAUCHE (Fréquence de base)
        const oscLeft = this.ctx.createOscillator(); 
        oscLeft.type = 'sine'; // Le sinus est le plus pur pour les battements binauraux
        oscLeft.frequency.value = baseFreq;
        
        // Oscillateur DROIT (Fréquence de base + différence)
        const oscRight = this.ctx.createOscillator(); 
        oscRight.type = 'sine';
        oscRight.frequency.value = beatFreq;

        // Connexion à gauche (canal 0) et à droite (canal 1)
        oscLeft.connect(merger, 0, 0);
        oscRight.connect(merger, 0, 1);

        // Un filtre passe-bas doux pour que le son soit rond et agréable
        const filter = this.ctx.createBiquadFilter(); 
        filter.type = 'lowpass'; 
        filter.frequency.value = 300;
        
        merger.connect(filter); 
        filter.connect(gain); 
        
        // Les ondes binaurales ne doivent PAS aller dans la reverb, 
        // sinon les phases se mélangent et l'effet cérébral est annulé.
        gain.connect(this.compressor); 
        
        oscLeft.start(); 
        oscRight.start();
        
        this.soundNodes[id] = gain;
    }

    // On ajoute maxVol avec une valeur par défaut de 1.0
    createPureSine(id, freq, maxVol = 1.0) {
        const osc = this.ctx.createOscillator(); 
        osc.type = 'sine'; 
        osc.frequency.value = freq;
        
        // Le reducer utilise maintenant le paramètre maxVol
        const reducer = this.ctx.createGain();
        reducer.gain.value = maxVol; 
        
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const gain = this.ctx.createGain(); 
        gain.gain.value = 0.0001; 
        gain.userVolume = 0;
        
        osc.connect(reducer); 
        reducer.connect(gain); 
        
        gain.connect(this.compressor);
        gain.connect(this.reverbNode);
        osc.start(); 
        this.soundNodes[id] = gain;
    }
    createPadSource(id) {
        const osc = this.ctx.createOscillator();
        osc.type = 'sawtooth';
        osc.frequency.value = 80;

        const filter = this.ctx.createBiquadFilter();
        filter.type = 'lowpass';
        filter.Q.value = 5;

        // NOUVEAU : Réducteur de volume à la source
        const padGain = this.ctx.createGain();
        padGain.gain.value = 0.25; // On réduit drastiquement la puissance de base à 25%

        const lfo = this.ctx.createOscillator();
        lfo.frequency.value = 0.2;
        const lfoGain = this.ctx.createGain();
        lfoGain.gain.value = 300;

        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const gain = this.ctx.createGain();
        gain.gain.value = 0.0001;
        gain.userVolume = 0;

        // ROUTAGE : osc -> filter -> padGain -> gain(master)
        osc.connect(filter);
        filter.connect(padGain); 
        padGain.connect(gain);

        gain.connect(this.compressor);
        gain.connect(this.reverbNode);

        lfo.connect(lfoGain);
        lfoGain.connect(filter.frequency);

        osc.start();
        lfo.start();

        this.soundNodes[id] = gain;
    }
    createChoir(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain();
        master.gain.value = 0.0001;
        master.userVolume = 0;

        const osc = this.ctx.createOscillator();
        osc.type = 'sawtooth';
        osc.frequency.value = 110;

        const f1 = this.ctx.createBiquadFilter();
        f1.type = 'bandpass';
        f1.frequency.value = 350;
        f1.Q.value = 4;

        const f2 = this.ctx.createBiquadFilter();
        f2.type = 'bandpass';
        f2.frequency.value = 800;
        f2.Q.value = 4;

        const mix = this.ctx.createGain();
        // MODIFICATION : Gain divisé par 2 (0.25 au lieu de 0.5)
        mix.gain.value = 0.25; 

        osc.connect(f1);
        osc.connect(f2);
        f1.connect(mix);
        f2.connect(mix);
        mix.connect(master);

        const lfo = this.ctx.createOscillator();
        lfo.frequency.value = 0.1;
        const lfoG = this.ctx.createGain();
        lfoG.gain.value = 2;

        lfo.connect(lfoG);
        lfoG.connect(osc.frequency);

        osc.start();
        lfo.start();

        master.connect(this.compressor);
        this.soundNodes[id] = master;
        // Envoie le son mélodique dans la reverb
        master.connect(this.reverbNode);
    }
    createStrings(id) {
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain();
        master.gain.value = 0.0001;
        master.userVolume = 0;

        // NOUVEAU : On ajoute un gain global très faible pour la piste entière
        const padGain = this.ctx.createGain();
        padGain.gain.value = 0.10; // Réduit le volume des violons de 85% à la source !

        [220, 221, 329.6, 331].forEach(f => {
            const osc = this.ctx.createOscillator();
            osc.type = 'sawtooth';
            osc.frequency.value = f;
            
            const lp = this.ctx.createBiquadFilter();
            lp.type = 'lowpass';
            lp.frequency.value = 600;
            
            // On connecte au padGain plutôt qu'au master directement
            osc.connect(lp);
            lp.connect(padGain);
            osc.start();
        });

        // Le padGain va ensuite dans le master
        padGain.connect(master);

        master.connect(this.compressor);
        this.soundNodes[id] = master;

        // Envoie le son mélodique dans la reverb
        master.connect(this.reverbNode);
    }
    createIceTexture(id) {
        const src = this.ctx.createBufferSource(); src.buffer = this.buffers['white']; src.loop = true;
        const filter = this.ctx.createBiquadFilter(); filter.type = 'highpass'; filter.frequency.value = 6000; filter.Q.value = 5; 
        const mod = this.ctx.createOscillator(); mod.frequency.value = 0.2;
        const modG = this.ctx.createGain(); modG.gain.value = 2000;
        // NOUVEAU : On ajoute un réducteur de gain à 20%
        const reducer = this.ctx.createGain();
        reducer.gain.value = 0.15; 
        
        // ANTI-POP : on démarre à 0.0001 (jamais 0 exactement).
        const master = this.ctx.createGain(); master.gain.value = 0.0001; master.userVolume = 0;
        
        // Le filtre passe d'abord par le réducteur avant d'aller dans le master
        src.connect(filter); 
        filter.connect(reducer); 
        reducer.connect(master);
        
        src.start(); mod.start();
        master.connect(this.compressor); this.soundNodes[id] = master;
        master.connect(this.reverbNode);
    }
    setVolume(id, val) {
        if(this.soundNodes[id]) {
            // ANTI-POP : time constant plus long (0.05s) pour lisser les transitions
            // rapides (ex: sliders de l'auto-shuffle qui bougent à 20fps).
            this.soundNodes[id].gain.setTargetAtTime(val, this.ctx.currentTime, 0.05);
            this.soundNodes[id].userVolume = val;
        }
    }

    /**
     * ANTI-POP : fade-in global du master.
     * À appeler après un resume() du AudioContext ou après un reset massif de
     * pistes, pour éviter le clic de démarrage brutal.
     */
    fadeMasterIn(duration = 0.15) {
        if (!this.masterGain || !this.ctx) return;
        const now = this.ctx.currentTime;
        const g = this.masterGain.gain;
        // ANTI-CONFLIT : on cible la valeur demandée par l'utilisateur (target),
        // pas 0.5 en dur. Sinon le fade-in écrase le volume choisi.
        const target = (g.__targetVolume !== undefined) ? g.__targetVolume : 0.5;
        g.cancelScheduledValues(now);
        g.setValueAtTime(Math.max(0.0001, g.value), now);
        g.linearRampToValueAtTime(Math.max(0.0001, target), now + duration);
    }

    /**
     * ANTI-POP : fade-out global du master.
     * À appeler avant un suspend() du AudioContext ou un reset massif de pistes.
     */
    fadeMasterOut(duration = 0.15) {
        if (!this.masterGain || !this.ctx) return;
        const now = this.ctx.currentTime;
        const g = this.masterGain.gain;
        g.cancelScheduledValues(now);
        g.setValueAtTime(Math.max(0.0001, g.value), now);
        g.linearRampToValueAtTime(0.0001, now + duration);
    }

    // --- BREATHING GUIDE ---
    
    startBreathingGuide() {
        if (this.breathingOsc) return;
        
        this.breathingGain = this.ctx.createGain();
        this.breathingGain.gain.value = 0;
        
        // Onde sinusoïdale très douce pour le drone
        this.breathingOsc = this.ctx.createOscillator();
        this.breathingOsc.type = 'sine';
        this.breathingOsc.frequency.value = 100;
        
        this.breathingOsc.connect(this.breathingGain);
        this.breathingGain.connect(this.compressor);
        // On envoie aussi dans la reverb pour enrober l'audio
        this.breathingGain.connect(this.reverbNode);
        
        this.breathingOsc.start(this.ctx.currentTime);
    }

    stopBreathingGuide() {
        if (this.breathingOsc) {
            this.breathingGain.gain.setTargetAtTime(0, this.ctx.currentTime, 0.5);
            this.breathingOsc.stop(this.ctx.currentTime + 1);
            this.breathingOsc = null;
            this.breathingGain = null;
        }
    }

    updateBreathingGuide(phaseIndex, progress) {
        if (!this.breathingOsc) this.startBreathingGuide();
        
        // Calcule l'échelle (0 à 1) basée sur la phase
        let scale = 0;
        if (phaseIndex === 0) scale = progress;                  // Inhale
        else if (phaseIndex === 1) scale = 1;                    // Hold
        else if (phaseIndex === 2) scale = 1 - progress;         // Exhale
        else if (phaseIndex === 3) scale = 0;                    // Hold out
        
        // Courbe douce pour l'audio
        let smoothScale = (1 - Math.cos(scale * Math.PI)) / 2;
        
        const now = this.ctx.currentTime;
        // Le volume monte jusqu'à 0.3 et le pitch de 100 Hz à 120 Hz
        if (this.breathingGain) {
            this.breathingGain.gain.setTargetAtTime(smoothScale * 0.3, now, 0.1);
        }
        if (this.breathingOsc) {
            this.breathingOsc.frequency.setTargetAtTime(80 + (smoothScale * 30), now, 0.1);
        }
    }
    
    resetBreathingGuide() {
        this.stopBreathingGuide();
    }

    triggerBell() {
        if (!this.ctx) return;
        const now = this.ctx.currentTime;
        
        const osc = this.ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(528, now); // Note de base (Solfeggio 528Hz)
        
        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0, now);
        gain.gain.linearRampToValueAtTime(0.08, now + 0.01);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 3);
        
        osc.connect(gain);
        gain.connect(this.reverbNode); // Dans la reverb 
        gain.connect(this.compressor); 

        // Harmonique supérieure pour l'effet "bol tibétain" ou "cloche"
        const osc2 = this.ctx.createOscillator();
        osc2.type = 'sine';
        osc2.frequency.setValueAtTime(528 * 2.7, now);
        
        const gain2 = this.ctx.createGain();
        gain2.gain.setValueAtTime(0, now);
        gain2.gain.linearRampToValueAtTime(0.02, now + 0.01);
        gain2.gain.exponentialRampToValueAtTime(0.001, now + 2);
        
        osc2.connect(gain2);
        gain2.connect(this.reverbNode);
        
        osc.start(now);
        osc.stop(now + 3.1);
        osc2.start(now);
        osc2.stop(now + 2.1);
    }
}
