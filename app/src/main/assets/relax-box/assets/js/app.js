import { AudioEngine } from './audio.js';
import { VisualEngine } from './visuals.js';
import { BreathingEngine } from './breathing.js';
import { TutorialManager } from './tutorial.js';

lucide.createIcons();

const app = {
    audio: new AudioEngine(),
    visuals: null,
    breathing: null,
    isPanelOpen: true,
    isLightTheme: false,
    
    tracks: [
        { id: 'rain', label: 'Pluie Acide', group: 'nature' },
        { id: 'birds', label: 'Forêt & Oiseaux', group: 'nature' },
        { id: 'clock', label: 'Vieille Horloge', group: 'nature' }, 
        { id: 'waterfall', label: 'Cascade', group: 'nature' },
        { id: 'stream', label: 'Ruisseau Cristal', group: 'nature' },
        { id: 'ocean', label: 'Océan (Boost)', group: 'nature' },
        { id: 'wind', label: 'Vent Solaire', group: 'nature' },
        { id: 'crickets', label: 'Chant Criquets', group: 'atmosphere' },
        { id: 'owl', label: 'Hibou Nocturne', group: 'atmosphere' },
        { id: 'purr', label: 'Ronronnement', group: 'atmosphere' }, 
        { id: 'fan', label: 'Ventilation', group: 'atmosphere' },
        { id: 'night', label: 'Nuit Cyber', group: 'atmosphere' },
        { id: 'vinyl', label: 'Vinyle Lofi', group: 'atmosphere' },
        { id: 'heartbeat', label: 'Battement Cœur', group: 'atmosphere' },
        { id: 'solfeggio', label: '528Hz (Love)', group: 'drone' }, 
        { id: 'schumann', label: '7.83Hz (Terre)', group: 'drone' }, 
        { id: 'alpha', label: 'Ondes Alpha', group: 'drone' },
        { id: 'theta', label: 'Ondes Theta', group: 'drone' },
        { id: 'delta', label: 'Ondes Delta', group: 'drone' },
        { id: 'gamma', label: 'Ondes Gamma', group: 'drone' },
        { id: 'subbass', label: 'Sub-Bass Pure', group: 'drone' },
        { id: 'bamboo', label: 'Carillon Bois', group: 'melody' },
        { id: 'metal', label: 'Carillon Métal', group: 'melody' },
        { id: 'handpan', label: 'Handpan', group: 'melody' },
        { id: 'kalimba', label: 'Kalimba', group: 'melody' },
        { id: 'bells', label: 'Clochettes', group: 'melody' },
        { id: 'panflute', label: 'Flûte Psyché', group: 'melody' },
        { id: 'harp', label: 'Harpe Éolienne', group: 'melody' },
        { id: 'choir', label: 'Chœurs Éthérés', group: 'synth' }, 
        { id: 'strings', label: 'Violons Lents', group: 'synth' }, 
        { id: 'crystal', label: 'Cristaux', group: 'synth' }, 
        { id: 'ice', label: 'Glace', group: 'synth' }, 
        { id: 'space', label: 'Pad Cosmique', group: 'synth' },
        { id: 'bowl_mid', label: 'Bol Tibétain', group: 'synth' }, 
        { id: 'magnetic', label: 'Brouillard Mag.', group: 'synth' },
    ],

    isAutoShuffle: false,
    currentShuffleMode: 'global', // 'global', 'sleep', 'focus' ou 'zen'
    autoShuffleInterval: null,
    shuffleTargets: {},
    sliderAnimationId: null,
    // Variables du Sleep Timer
    sleepTimerInterval: null,
    sleepTimerEndTime: null,
    // Variables de monitoring
    metricsInterval: null,

        // --- PRESETS ---
    defaultPresets: {
        'sleep-deep': {
            name: 'Sommeil profond',
            tracks: [
                { id: 'rain',      val: 0.12 },
                { id: 'crickets',  val: 0.18 },
                { id: 'bamboo',    val: 0.15 },
                { id: 'handpan',   val: 0.08 },
                { id: 'alpha',     val: 0.20 },
                { id: 'subbass',   val: 0.10 },
            ],
            reverb: 0.6,
            timerMinutes: 45
        },
        'forest-night': {
            name: 'Forêt nocturne',
            tracks: [
                { id: 'stream',    val: 0.18 },
                { id: 'birds',     val: 0.10 },
                { id: 'owl',       val: 0.12 },
                { id: 'crickets',  val: 0.20 },
                { id: 'vinyl',     val: 0.05 },
            ],
            reverb: 0.4,
            timerMinutes: 0
        },
        'focus-alpha': {
            name: 'Focus Alpha',
            tracks: [
                { id: 'wind',      val: 0.12 },
                { id: 'space',     val: 0.18 },
                { id: 'alpha',     val: 0.25 },
                { id: 'gamma',     val: 0.10 },
            ],
            reverb: 0.3,
            timerMinutes: 0
        },
        'theta-dreams': {
            name: 'Theta Dreams',
            tracks: [
                { id: 'ocean',     val: 0.16 },
                { id: 'choir',     val: 0.10 },
                { id: 'harp',      val: 0.12 },
                { id: 'theta',     val: 0.22 },
            ],
            reverb: 0.5,
            timerMinutes: 30
        }
    },

    userPresets: {},          // chargé / sauvegardé via localStorage
    selectedUserSlot: null,   // slot C1..C4 sélectionné,

    applyPresetObject: function(preset, startFromZero = false) {
        if (!preset) return;

        // 1) Reset doux (volumes à 0) - sauf si startFromZero est false (on garde la position actuelle)
        if (startFromZero) {
            this.tracks.forEach(t => this.updateSlider(t.id, 0));
        }

        // 2) Appliquer les volumes
        if (Array.isArray(preset.tracks)) {
            preset.tracks.forEach(s => {
                this.updateSlider(s.id, s.val);
            });
        }

        // 3) Reverb globale
        if (typeof preset.reverb === 'number') {
            const revSlider = document.getElementById('slider-reverb');
            const revLabel = document.getElementById('val-reverb');
            if (revSlider) {
                revSlider.value = preset.reverb;
                if (revLabel) {
                    revLabel.innerText = Math.round(preset.reverb * 100) + '%';
                }
                if (this.audio && this.audio.setReverbLevel) {
                    this.audio.setReverbLevel(preset.reverb);
                }
            }
        }

        // 4) Sleep timer (on ne démarre pas automatiquement, on place juste le slider + label)
        if (typeof preset.timerMinutes === 'number') {
            const tSlider = document.getElementById('slider-timer');
            const tLabel  = document.getElementById('val-timer');
            if (tSlider && tLabel) {
                const minVal = Math.max(0, Math.min(120, preset.timerMinutes));
                tSlider.value = minVal;
                tLabel.innerText = minVal === 0 ? window.i18n.t('app.disabled') : (minVal + ' min');
            }
        }
    },

    applyPresetById: function(id, startFromZero = true) {
        const p = this.defaultPresets[id];
        if (p) this.applyPresetObject(p, startFromZero);
    },

    loadUserPresetsFromStorage: function() {
        try {
            const raw = localStorage.getItem('relaxbox_user_presets');
            if (raw) {
                this.userPresets = JSON.parse(raw);
            }
        } catch (e) {
            console.warn('Erreur chargement presets custom', e);
        }
    },

    saveUserPresetsToStorage: function() {
        try {
            localStorage.setItem('relaxbox_user_presets', JSON.stringify(this.userPresets));
        } catch (e) {
            console.warn('Erreur sauvegarde presets custom', e);
        }
    },

    captureCurrentStateAsPreset: function() {
        const preset = {
            tracks: [],
            reverb: 0.5,
            timerMinutes: 0
        };

        // Volumes
        this.tracks.forEach(track => {
            const el = document.getElementById(`slider-${track.id}`);
            if (!el) return;
            const val = parseFloat(el.value) || 0;
            if (val > 0.001) {
                preset.tracks.push({ id: track.id, val });
            }
        });

        // Reverb
        const revSlider = document.getElementById('slider-reverb');
        if (revSlider) {
            preset.reverb = parseFloat(revSlider.value) || 0.5;
        }

        // Timer
        const tSlider = document.getElementById('slider-timer');
        if (tSlider) {
            preset.timerMinutes = parseInt(tSlider.value, 10) || 0;
        }

        return preset;
    },

    applyUserPresetSlot: function(slot) {
        const p = this.userPresets[slot];
        if (p) {
            this.applyPresetObject(p);
            const status = document.getElementById('presetStatus');
            if (status) status.innerText = window.i18n.t('app.presetLoaded') + `C${slot}`;
        }
    },

    saveCurrentToUserSlot: function() {
        if (!this.selectedUserSlot) {
            const status = document.getElementById('presetStatus');
            if (status) status.innerText = window.i18n.t('app.selectSlotFirst');
            return;
        }

        const p = this.captureCurrentStateAsPreset();
        this.userPresets[this.selectedUserSlot] = p;
        this.saveUserPresetsToStorage();

        const status = document.getElementById('presetStatus');
        if (status) status.innerText =
            `Preset sauvegardé dans C${this.selectedUserSlot}`;
    },
    
    // --- FONCTIONS DE CONTRÔLE UI DU SHUFFLE ---

    openShuffleMenu: function() {
        const menu = document.getElementById('shuffleMenu');
        if (menu) {
            menu.classList.remove('opacity-0', 'pointer-events-none');
        }
    },

    closeShuffleMenu: function() {
        const menu = document.getElementById('shuffleMenu');
        if (menu) {
            menu.classList.add('opacity-0', 'pointer-events-none');
        }
    },

    stopAutoShuffle: function() {
        this.isAutoShuffle = false;
        clearInterval(this.autoShuffleInterval);
        clearTimeout(this.sliderAnimationId);
        
        const btn = document.getElementById('autoShuffleBtn');
        if (btn) {
            btn.classList.remove('bg-purple-900/50', 'text-white', 'btn-breathing');
            btn.classList.add('text-purple-400');
            // Remet l'icône Play
            btn.innerHTML = '<i data-lucide="play" class="w-3 h-3"></i><i data-lucide="shuffle" class="w-4 h-4"></i>';
            lucide.createIcons();
        }
    },

    startAutoShuffleWithMode: function(mode) {
        // 1. Fermer le menu
        this.closeShuffleMenu();

        // 2. Définir le nouveau mode et activer l'état
        this.currentShuffleMode = mode;
        this.isAutoShuffle = true;

        // 3. Appliquer le filtre LoFi selon l'ambiance si la fonction existe
        if (this.audio && this.audio.setLofiMode) {
            if (mode === 'sleep') this.audio.setLofiMode('deep');
            else if (mode === 'focus') this.audio.setLofiMode('off');
            // Si global ou zen, on ne touche pas au lofi de l'utilisateur
        }

        // 4. Mettre tous les sliders à 0 pour un démarrage complètement aléatoire
        this.tracks.forEach(track => {
            this.updateSlider(track.id, 0);
        });

        // 5. Générer les cibles vers lesquelles les sliders vont glisser (en partant de zéro)
        this.generateShuffleTargets();
        
        // 6. Lancer la boucle d'intervalle et l'animation
        // Nettoyer d'abord si on relançait par-dessus un autre mode
        if (this.autoShuffleInterval) clearInterval(this.autoShuffleInterval);
        this.autoShuffleInterval = setInterval(() => { this.generateShuffleTargets(); }, 30000);
        
        // Lancer l'animation visuelle
        this.animateSliders();
        
        // 7. Mettre à jour le bouton Auto-Shuffle visuellement
        const btn = document.getElementById('autoShuffleBtn');
        if (btn) {
            btn.classList.add('bg-purple-900/50', 'text-white', 'btn-breathing');
            btn.classList.remove('text-purple-400');
            // Met l'icône Stop (Carré) + Shuffle pour indiquer qu'on peut l'arrêter
            btn.innerHTML = '<i data-lucide="square" class="w-3 h-3 fill-current"></i><i data-lucide="shuffle" class="w-4 h-4"></i>';
            lucide.createIcons();
        }
        
        console.log("Auto-shuffle started in mode:", mode, "with random seed:", Date.now());
    },

    // La fonction de génération des cibles :
    generateShuffleTargets: function() {
        // 1. D'abord, on détermine combien de pistes maximum on veut allumer au total
        // (entre 4 et 7 pistes actives en même temps c'est l'idéal pour ne pas faire "bouillie")
        let targetActiveCount = 0;
        if (this.currentShuffleMode === 'global') targetActiveCount = Math.floor(Math.random() * 4) + 4; // 4 à 7 pistes
        else if (this.currentShuffleMode === 'sleep') targetActiveCount = Math.floor(Math.random() * 3) + 3; // 3 à 5 pistes
        else if (this.currentShuffleMode === 'focus') targetActiveCount = Math.floor(Math.random() * 3) + 3; // 3 à 5 pistes
        else if (this.currentShuffleMode === 'zen') targetActiveCount = Math.floor(Math.random() * 4) + 4; // 4 à 7 pistes

        // 2. On trie les pistes aléatoirement pour ne pas toujours privilégier les premières
        let shuffledTracks = [...this.tracks].sort(() => 0.5 - Math.random());
        
        let currentActiveCount = 0;

        // 3. On parcours nos pistes mélangées
        shuffledTracks.forEach(track => {
            let chance = 0;
            let maxVol = 0.25;

            // --- DÉFINITION DES PROBABILITÉS D'ÊTRE CHOISI ---
            if (this.currentShuffleMode === 'global') {
                chance = 0.4; // Baissé à 40% (au lieu de 70%)
                if (track.group === 'melody') chance = 0.6; 
                maxVol = 0.25;
                
            } else if (this.currentShuffleMode === 'sleep') {
                if (track.group === 'nature') { chance = 0.5; maxVol = 0.2; }
                else if (track.group === 'atmosphere') { chance = 0.5; maxVol = 0.15; }
                else if (track.group === 'drone') { chance = 0.6; maxVol = 0.25; }
                if (track.id === 'birds' || track.id === 'fan' || track.id === 'magnetic') chance = 0;
                
            } else if (this.currentShuffleMode === 'focus') {
                if (track.id === 'alpha' || track.id === 'gamma' || track.id === 'theta') { chance = 0.7; maxVol = 0.3; }
                else if (track.id === 'wind' || track.id === 'waterfall' || track.id === 'ocean' || track.id === 'fan') { chance = 0.6; maxVol = 0.15; }
                else if (track.id === 'clock' || track.id === 'heartbeat') { chance = 0.3; maxVol = 0.1; }
                
            } else if (this.currentShuffleMode === 'zen') {
                if (track.group === 'melody' || track.group === 'synth') { chance = 0.6; maxVol = 0.3; }
                else if (track.group === 'nature') { chance = 0.3; maxVol = 0.15; }
                else if (track.id === 'solfeggio') { chance = 0.8; maxVol = 0.2; }
            }

            // --- DÉCISION FINALE ---
            // On joue la piste SI elle passe la probabilité ET qu'on n'a pas atteint la limite de pistes actives
            let target = 0;
            
            if (Math.random() < chance && currentActiveCount < targetActiveCount) {
                target = Math.random() * maxVol;
                // On s'assure que si la piste est choisie, elle a au moins un petit volume (ex: 5%)
                if (target < 0.05) target = 0.05 + (Math.random() * 0.1); 
                currentActiveCount++;
            }
            
            const el = document.getElementById('slider-' + track.id);
            const start = el ? parseFloat(el.value) : 0;
            
            this.shuffleTargets[track.id] = {
                start: start,
                target: target,
                startTime: performance.now(),
                duration: 15000 + Math.random() * 10000 
            };
        });
    },
    animateSliders: function() {
        if (!this.isAutoShuffle) return;
        
        const now = performance.now();
        
        this.tracks.forEach(track => {
            const tInfo = this.shuffleTargets[track.id];
            if (!tInfo) return;
            
            const elapsed = now - tInfo.startTime;
            let progress = elapsed / tInfo.duration;
            
            if (progress > 1) progress = 1;
            
            // Lissage du mouvement (Easing in/out) pour faire plus naturel
            const ease = progress * progress * (3 - 2 * progress);
            const currentVal = tInfo.start + (tInfo.target - tInfo.start) * ease;
            
            const el = document.getElementById(`slider-${track.id}`);
            if (el && Math.abs(el.value - currentVal) > 0.001) {
                el.value = currentVal;
                // Met à jour l'interface visuelle
                const valEl = document.getElementById(`val-${track.id}`);
                if (valEl) valEl.innerText = Math.floor(currentVal * 100) + '%';
                // Met à jour le moteur audio
                if (this.audio && typeof this.audio.setVolume === 'function') {
                    this.audio.setVolume(track.id, currentVal);
                }
            }
        });
        
        // Continue la boucle d'animation tant que le mode est actif
        // Utilisation de setTimeout au lieu de requestAnimationFrame pour que la boucle ne soit pas tuée quand l'écran s'éteint
        this.sliderAnimationId = setTimeout(() => this.animateSliders(), 50); // ~20fps
    },

    startSleepTimer: function(minutes) {
        // 1. On nettoie tout timer existant
        if (this.sleepTimerInterval) clearInterval(this.sleepTimerInterval);
        const countdownEl = document.getElementById('timer-countdown');
        
        // 2. Si on met le curseur à 0 (Désactivé)
        if (minutes === 0) {
            countdownEl.classList.add('hidden');
            // On s'assure que le volume global est remis à 100% si on annule un fadeout en cours
            if (this.audio && this.audio.masterGain) {
                this.audio.masterGain.gain.cancelScheduledValues(this.audio.ctx.currentTime);
                this.audio.masterGain.gain.setTargetAtTime(1.0, this.audio.ctx.currentTime, 0.5);
            }
            return;
        }

        // 3. Calcul de l'heure de fin
        this.sleepTimerEndTime = Date.now() + (minutes * 60 * 1000);
        countdownEl.classList.remove('hidden');

        // 4. La boucle qui vérifie chaque seconde
        this.sleepTimerInterval = setInterval(() => {
            const now = Date.now();
            const remainingMs = this.sleepTimerEndTime - now;

            // Affichage du temps restant
            if (remainingMs > 0) {
                const totalSeconds = Math.floor(remainingMs / 1000);
                const mins = Math.floor(totalSeconds / 60);
                const secs = totalSeconds % 60;
                countdownEl.innerText = `${window.i18n.t('app.stopIn')} ${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;

                // --- LE FADE OUT (Les 2 dernières minutes) ---
                // Si on entre dans les 120 dernières secondes, on commence à baisser le volume global doucement
                if (remainingMs <= 120000 && this.audio && this.audio.masterGain) {
                    const volumeRatio = remainingMs / 120000; // Va de 1.0 à 0.0
                    // On ne force le volume que s'il est plus bas que le volume actuel pour ne pas le remonter
                    if (this.audio.masterGain.gain.value > volumeRatio) {
                         this.audio.masterGain.gain.setTargetAtTime(volumeRatio, this.audio.ctx.currentTime, 0.5);
                    }
                }
            } 
        // 5. FIN DU TEMPS
            else {
                clearInterval(this.sleepTimerInterval);
                countdownEl.innerText = window.i18n.t('app.standby');

                // 1. On coupe le son
                this.reset();
                
                setTimeout(() => {
                if (this.audio && this.audio.ctx) {
                    this.audio.ctx.suspend();
                }
                countdownEl.innerText = window.i18n.t('app.audioStopped');
                
                const sliderTimer = document.getElementById('slider-timer');
                if (sliderTimer) {
                    sliderTimer.value = 0;
                    document.getElementById('val-timer').innerText = window.i18n.t('app.disabled');
                }
                
                const bgAudio = document.getElementById('bg-audio');
                if (bgAudio) {
                    bgAudio.pause();
                }

                // --- NOUVEAUTÉS POUR LA BATTERIE ---
                
                // 2. On arrête les calculs 2D/WebGL et le BreathingEngine
                if (this.visuals) {
                    this.visuals.stop();
                }
                if (this.breathing && this.breathing.isActive) {
                    this.breathing.stop();
                    const btn = document.getElementById('breathingNavBtn');
                    if (btn) {
                        btn.classList.add('text-purple-400', 'border-purple-500/50');
                        btn.classList.remove('bg-purple-600', 'text-white', 'border-purple-400', 'shadow-[0_0_15px_rgba(168,85,247,0.5)]');
                    }
                    const bOverlay = document.getElementById('breathingOverlay');
                    if (bOverlay) {
                        bOverlay.style.opacity = '0';
                        setTimeout(() => bOverlay.classList.add('hidden'), 500);
                    }
                }

                // 3. On force l'écran noir tout de suite (économise les écrans OLED)
                // On suppose que wakeManager est déclaré globalement dans ton fichier
                if (typeof wakeManager !== 'undefined') {
                    wakeManager.overlay.classList.add('active'); // Rend l'écran noir
                    wakeManager.releaseWakeLock(); // Permet au téléphone de s'éteindre !
                }

                // 4. On prépare l'écran de réveil EN DESSOUS de l'écran noir
                const wakeupOverlay = document.getElementById('wakeupOverlay');
                if (wakeupOverlay) {
                    wakeupOverlay.classList.remove('hidden');
                    wakeupOverlay.style.opacity = '1';
                }
                }, 5000); // 5 secondes de délai de grâce pour la Reverb
            }        
        }, 1000);
    },


    init: function() {
        this.renderSliders();
        window.tutorialManager = new TutorialManager();

        // Démarrer le monitoring des métriques système (toutes les 30 secondes)
        this.metricsInterval = setInterval(() => {
            this.logSystemMetrics("Periodic check");
        }, 30000);

        const startOverlay = document.getElementById('startOverlay');
        const controlsPanel = document.getElementById('controlsPanel');
        const bgAudio = document.getElementById('bg-audio');

        const performStart = async (presetId) => {
            if (this._hasStarted) {
                if (presetId) {
                    const low = presetId.toLowerCase();
                    if (['global', 'sleep', 'focus', 'zen'].includes(low)) {
                        this.startAutoShuffleWithMode(low);
                    } else {
                        this.applyPresetById(presetId);
                    }
                }
                return;
            }
            this._hasStarted = true;

            // 1. INITIALISER LE MOTEUR AUDIO
            try {
                if (!this.audio.ctx) {
                    await this.audio.init();
                }
                if (this.audio.ctx && this.audio.ctx.state === 'suspended') {
                    await this.audio.ctx.resume();
                }
            } catch (e) {
                console.warn('AudioEngine init error:', e);
            }

            // Démarrer la balise audio en arrière-plan
            if (bgAudio) {
                try {
                    bgAudio.currentTime = 0;
                    const p = bgAudio.play();
                    if (p !== undefined) p.catch(() => {});
                } catch (e) {}
            }

            // 2. CONFIGURER LA MEDIA SESSION
            if ('mediaSession' in navigator) {
                try {
                    navigator.mediaSession.metadata = new MediaMetadata({
                        title: 'Relax-Box',
                        artist: 'Générateur de relaxation',
                        album: 'B-H.DEV',
                        artwork: [
                            { src: 'assets/favicon.png', sizes: '512x512', type: 'image/png' }
                        ]
                    });

                    navigator.mediaSession.setActionHandler('play', async () => {
                        if (this.audio && this.audio.ctx) await this.audio.ctx.resume();
                        if (bgAudio) bgAudio.play();
                        navigator.mediaSession.playbackState = 'playing';
                    });

                    navigator.mediaSession.setActionHandler('pause', async () => {
                        if (this.audio && this.audio.ctx) await this.audio.ctx.suspend();
                        if (bgAudio) bgAudio.pause();
                        navigator.mediaSession.playbackState = 'paused';
                    });

                    navigator.mediaSession.playbackState = 'playing';
                } catch (e) {}
            }

            // 3. LANCER LES VISUELS
            try {
                if (!this.visuals && this.audio) {
                    this.visuals = new VisualEngine('mainCanvas', this.audio);
                    if (this.visuals.modes && this.visuals.modes.length > 0) {
                        const numStandardModes = 4; // On bloque aux 4 premiers visuels pour l'aléatoire global
                        this.visuals.currentModeIndex = Math.floor(Math.random() * numStandardModes);
                    }
                    this.visuals.draw();
                }
            } catch (e) {
                console.warn('Visuals error:', e);
            }

            // 4. CACHER L'OVERLAY ET AFFICHER LA CONSOLE DE CONTRÔLE
            if (startOverlay) {
                startOverlay.style.opacity = '0';
                startOverlay.style.display = 'none';
                if (bgAudio) bgAudio.style.display = 'none';
            }
            if (controlsPanel) {
                controlsPanel.classList.remove('translate-y-[120%]', 'opacity-0');
                controlsPanel.style.transform = 'none';
                controlsPanel.style.opacity = '1';
            }

            // 5. CHARGER LE MODE OU PRESET DEMANDE (seulement si un preset est spécifié)
            const chosenPreset = presetId || this._pendingPresetId || 'global';
            if (presetId) {
                setTimeout(() => {
                    const low = presetId.toLowerCase();
                    if (['global', 'sleep', 'focus', 'zen'].includes(low)) {
                        this.startAutoShuffleWithMode(low);
                    } else {
                        this.applyPresetById(presetId);
                    }
                }, 300);
            }

            if (window.LumibookBridge) {
                window.LumibookBridge._started = true;
                window.LumibookBridge._currentPresetId = chosenPreset;
                window.LumibookBridge.notifyState();
            }
        };

        this.startApp = performStart;

        if (bgAudio) {
            bgAudio.addEventListener('play', () => performStart());
        }
        const startPlayBtn = document.getElementById('startPlayBtn');
        if (startPlayBtn) {
            startPlayBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                performStart();
            });
        }
        if (startOverlay) {
            startOverlay.addEventListener('click', (e) => {
                if (e.target === startOverlay) {
                    performStart();
                }
            });
        }

        // Démarrage automatique de l'initialisation (sans lancer de mode)
        setTimeout(() => {
            if (!this._hasStarted) {
                performStart(null); // Initialise sans preset
            }
        }, 500);

        // --- LOGIQUE AUTO-SHUFFLE ---
        const autoShuffleBtn = document.getElementById('autoShuffleBtn');
        if (autoShuffleBtn) {
            autoShuffleBtn.addEventListener('click', (e) => {
                e.stopPropagation(); // Évite que le document.click ferme tout de suite le menu
                
                // Réveiller le système même si l'événement ne remonte pas ---
                if (typeof wakeManager !== 'undefined') {
                    wakeManager.resetIdleTimer(); // Relance le chrono de l'écran noir
                    if (!wakeManager.wakeLock) {
                        // S'il n'y a pas encore de Wake Lock actif, on le demande
                        wakeManager.requestWakeLock();
                    }
                }
                // -------------------------------------------------------------------------
                if (app.isAutoShuffle) {
                    app.stopAutoShuffle();
                } else {
                    app.openShuffleMenu();
                }
            });
        }

        // Boutons à l'intérieur du menu
        document.querySelectorAll('.shuffle-mode-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const mode = btn.getAttribute('data-mode');
                app.startAutoShuffleWithMode(mode);
            });
        });

        // Clic à l'extérieur ferme le menu
        document.addEventListener('click', () => {
            if (window.app) window.app.closeShuffleMenu();
            const langMenu = document.getElementById('langMenu');
            if (langMenu) langMenu.classList.add('opacity-0', 'pointer-events-none');
        });

        // --- LANGUAGE MODAL LOGIC ---
        const langBtn = document.getElementById('langBtn');
        const langMenu = document.getElementById('langMenu');
        if (langBtn && langMenu) {
            langBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                if (window.app) window.app.closeShuffleMenu();
                langMenu.classList.remove('hidden');
                
                if (langMenu.classList.contains('pointer-events-none')) {
                    langMenu.classList.remove('pointer-events-none');
                    langMenu.style.display = 'flex';
                    setTimeout(() => langMenu.style.opacity = '1', 10);
                } else {
                    langMenu.style.opacity = '0';
                    setTimeout(() => {
                        langMenu.classList.add('pointer-events-none');
                        langMenu.style.display = 'none';
                    }, 200);
                }
            });

            document.querySelectorAll('.lang-opt-btn').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const lang = btn.getAttribute('data-lang');
                    if (window.i18n) window.i18n.setLanguage(lang);
                    langMenu.style.opacity = '0';
                    setTimeout(() => {
                        langMenu.classList.add('pointer-events-none');
                        langMenu.style.display = 'none';
                    }, 200);
                });
            });
        }

        // --- INFO MODAL LOGIC ---
        const infoBtn = document.getElementById('infoBtn');
        const infoOverlay = document.getElementById('infoOverlay');
        const closeInfoBtn = document.getElementById('closeInfoBtn');
        const replayTutorialBtn = document.getElementById('replayTutorialBtn');

        if (infoBtn && infoOverlay) {
            infoBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                infoOverlay.classList.remove('hidden');
                setTimeout(() => infoOverlay.style.opacity = '1', 50);
            });
        }
        
        if (closeInfoBtn && infoOverlay) {
            closeInfoBtn.addEventListener('click', () => {
                infoOverlay.style.opacity = '0';
                setTimeout(() => infoOverlay.classList.add('hidden'), 500);
            });
        }

        if (replayTutorialBtn && window.tutorialManager) {
            replayTutorialBtn.addEventListener('click', () => {
                infoOverlay.style.opacity = '0';
                setTimeout(() => {
                    infoOverlay.classList.add('hidden');
                    window.tutorialManager.currentStep = 0;
                    window.tutorialManager.startTutorial();
                }, 500);
            });
        }


        // Écouteur pour le bouton de réveil après le Sleep Timer
        const wakeupBtn = document.getElementById('wakeupBtn');
        const wakeupOverlay = document.getElementById('wakeupOverlay');

        if (wakeupBtn && wakeupOverlay) {
        wakeupBtn.addEventListener('click', async () => {
            // 1. On réveille la carte mère audio
            if (this.audio && this.audio.ctx) {
                if (this.audio.ctx.state === 'suspended') {
                    await this.audio.ctx.resume();
                }
            }

            const bgAudio = document.getElementById('bg-audio');
            if (bgAudio) {
                bgAudio.play();
            }

            // 2. On s'assure que le volume global est bien à fond
            if (this.audio && this.audio.masterGain) {
                this.audio.masterGain.gain.cancelScheduledValues(this.audio.ctx.currentTime);
                this.audio.masterGain.gain.setTargetAtTime(1.0, this.audio.ctx.currentTime, 0.1);
            }

            // --- NOUVEAUTÉ : RELANCE VISUELS ET BATTERIE ---
            // 3. Relancer les visuels
            if (this.visuals) {
                this.visuals.start();
            }

            // 4. Reprendre le Wake Lock pour empêcher la veille
            if (typeof wakeManager !== 'undefined') {
                wakeManager.requestWakeLock();
                // On s'assure que l'écran noir est bien enlevé au cas où
                wakeManager.overlay.classList.remove('active'); 
            }

            // RESET DU SLEEP TIMER DANS L'UI ---
            const countdownEl = document.getElementById('timer-countdown');
            if (countdownEl) {
                countdownEl.innerText = ""; // On enlève le texte "Audio arrêté"
            }
            
            const sliderTimer = document.getElementById('slider-timer');
            const valTimer = document.getElementById('val-timer');
            if (sliderTimer && valTimer) {
                sliderTimer.value = 0;
                valTimer.innerText = window.i18n.t('app.disabled');
            }

            // On s'assure que les variables internes de l'app sont bien nettoyées
            app.sleepTimerEndTime = null;
            if (app.sleepTimerInterval) {
                clearInterval(app.sleepTimerInterval);
                app.sleepTimerInterval = null;
            }
            
            // 5. On ferme l'overlay de réveil proprement
            wakeupOverlay.style.opacity = '0';
            setTimeout(() => {
                wakeupOverlay.classList.add('hidden');
            }, 500);

        });
        }
        
        // 1. Logique du bouton Global Settings
        const settingsBtn = document.getElementById('globalSettingsBtn');
        const settingsPanel = document.getElementById('globalSettingsPanel');

        if (settingsBtn && settingsPanel) {
            settingsBtn.addEventListener('click', () => {
                const isClosed = settingsPanel.classList.contains('grid-rows-[0fr]');
                
                // Bonus Accordéon : Ferme l'autre panneau
                const presetsPanel = document.getElementById('presetsPanel');
                if (presetsPanel && isClosed) {
                    presetsPanel.classList.add('grid-rows-[0fr]');
                    presetsPanel.classList.remove('grid-rows-[1fr]');
                }

                if (isClosed) {
                    settingsPanel.classList.remove('grid-rows-[0fr]');
                    settingsPanel.classList.add('grid-rows-[1fr]');
                } else {
                    settingsPanel.classList.add('grid-rows-[0fr]');
                    settingsPanel.classList.remove('grid-rows-[1fr]');
                }
                updatePanelButtonStates();
            });
        }

        // 2. Logique du bouton Presets
        const presetsBtn = document.getElementById('presetsBtn');
        const presetsPanel = document.getElementById('presetsPanel');

        if (presetsBtn && presetsPanel) {
            presetsBtn.addEventListener('click', () => {
                const isClosed = presetsPanel.classList.contains('grid-rows-[0fr]');
                
                // Bonus Accordéon : Ferme l'autre panneau
                const settingsPanel = document.getElementById('globalSettingsPanel');
                if (settingsPanel && isClosed) {
                    settingsPanel.classList.add('grid-rows-[0fr]');
                    settingsPanel.classList.remove('grid-rows-[1fr]');
                }

                if (isClosed) {
                    presetsPanel.classList.remove('grid-rows-[0fr]');
                    presetsPanel.classList.add('grid-rows-[1fr]');
                } else {
                    presetsPanel.classList.add('grid-rows-[0fr]');
                    presetsPanel.classList.remove('grid-rows-[1fr]');
                }
                updatePanelButtonStates();
            });
        }

        function setupLofiSwitch() {
            const btnOff = document.getElementById('lofi-off');
            const btnSoft = document.getElementById('lofi-soft');
            const btnDeep = document.getElementById('lofi-deep');

            if (!btnOff || !btnSoft || !btnDeep) return;

            const setActive = (mode) => {
                // On retire l'état sélectionné partout
                [btnOff, btnSoft, btnDeep].forEach(b => {
                    b.classList.remove('settings-selected');
                });

                // On identifie le bouton actif
                let activeBtn = mode === 'soft' ? btnSoft : (mode === 'deep' ? btnDeep : btnOff);
                
                // On lui ajoute la classe sélectionnée
                activeBtn.classList.add('settings-selected');
                
                if (app.audio && app.audio.setLofiMode) {
                    app.audio.setLofiMode(mode);
                }
            };

            btnOff.addEventListener('click', () => setActive('off'));
            btnSoft.addEventListener('click', () => setActive('soft'));
            btnDeep.addEventListener('click', () => setActive('deep'));

            // Mode par défaut au chargement
            setActive('off');
        }

        // après l'init de app.audio
        setupLofiSwitch();

        // Écouteur pour la Reverb Globale
        const reverbSlider = document.getElementById('slider-reverb');
        if (reverbSlider) {
            reverbSlider.addEventListener('input', (e) => {
                const val = parseFloat(e.target.value);
                document.getElementById('val-reverb').innerText = Math.floor(val * 100) + '%';
                
                // On met à jour la reverb dans l'AudioEngine
                // this.audio pointe vers l'instance de AudioEngine
                if (this.audio && this.audio.reverbGain) {
                    this.audio.reverbGain.gain.setTargetAtTime(val, this.audio.ctx.currentTime, 0.1);
                }
            });
        }

        // --- INIT BREATHING ENGINE ---
        this.breathing = new BreathingEngine(this);

        const breathingNavBtn = document.getElementById('breathingNavBtn');
        const breathingOverlay = document.getElementById('breathingOverlay');
        const closeBreathingBtn = document.getElementById('closeBreathingBtn');
        const startBreathingBtn = document.getElementById('startBreathingBtn');
        const bubblesContainer = document.getElementById('bubblesContainer');

        const updateBreathingBtnUI = (isActive) => {
            if (!breathingNavBtn) return;
            if (isActive) {
                breathingNavBtn.classList.remove('text-purple-400', 'border-purple-500/50');
                breathingNavBtn.classList.add('bg-purple-600', 'text-white', 'border-purple-400', 'shadow-[0_0_15px_rgba(168,85,247,0.5)]');
            } else {
                breathingNavBtn.classList.add('text-purple-400', 'border-purple-500/50');
                breathingNavBtn.classList.remove('bg-purple-600', 'text-white', 'border-purple-400', 'shadow-[0_0_15px_rgba(168,85,247,0.5)]');
            }
        };

        if (breathingNavBtn && breathingOverlay) {
            breathingNavBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                // Fermer les autres menus
                this.closeShuffleMenu();
                
                // Si déjà actif, un 2ème clic arrête l'exercice
                if (this.breathing && this.breathing.isActive) {
                    this.breathing.stop();
                    updateBreathingBtnUI(false);
                    // On rétablit le tableau de bord visuel normal
                    if (this.visuals && this.visuals.currentModeIndex >= this.visuals.modes.length) {
                        this.visuals.currentModeIndex = 0;
                    }
                    return;
                }
                
                // Sinon, ouvre le panneau d'options
                breathingOverlay.classList.remove('hidden');
                setTimeout(() => {
                    breathingOverlay.style.opacity = '1';
                }, 10);
            });

            closeBreathingBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                breathingOverlay.style.opacity = '0';
                setTimeout(() => {
                    breathingOverlay.classList.add('hidden');
                }, 500);
            });

            startBreathingBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                const pattern = document.getElementById('breathingPatternSelect').value;
                const useAudio = document.getElementById('breathingAudioGuide').checked;
                
                const visualChoice = document.getElementById('breathingVisualSelect').value || 'fractal';
                const durationChoice = parseInt(document.getElementById('breathingDurationSelect').value || '0', 10);
                
                // Si on était pas en lecture, on force
                const bgAudio = document.getElementById('bg-audio');
                if (bgAudio && bgAudio.paused) {
                    bgAudio.play();
                }

                // Démarrage du moteur
                this.breathing.start(pattern, useAudio, durationChoice);
                updateBreathingBtnUI(true);

                // Cache l'overlay
                breathingOverlay.style.opacity = '0';
                setTimeout(() => {
                    breathingOverlay.classList.add('hidden');
                }, 500);
                
                // On met éventuellement le mode de visualisation choisi
                if (this.visuals) {
                    const shaderIndex = this.visuals.modes.indexOf(visualChoice);
                    if (shaderIndex !== -1 && this.visuals.currentModeIndex !== shaderIndex) {
                        this.visuals.currentModeIndex = shaderIndex;
                        this.visuals.updateCanvasVisibility();
                    }
                }
            });
        }

        // Hook up les callbacks du BreathingEngine pour l'UI
        if (this.breathing) {
            this.breathing.onFinish = () => {
                updateBreathingBtnUI(false);
                // Optionnel: On pourrait remettre le WebGL sur un mode aléatoire classique, 
                // mais le laisser ne pose pas de souci (le shuffle finira par le changer)
                if (this.visuals && this.visuals.currentModeIndex >= this.visuals.modes.length) {
                    this.visuals.currentModeIndex = 0; // Retour classique si on était sur lotus/orb
                }
            };
            
            this.breathing.onPhaseChange = (phaseIndex, label) => {
                if (!bubblesContainer) return;
                
                const bubble = document.createElement('div');
                bubble.className = 'breathing-bubble';
                bubble.innerText = label;
                
                // Positionne au centre horizontalement pour que ça n'explose pas l'écran
                // x entre 35% et 65% pour que le texte ait la place de chaque côté sur mobile
                const x = 35 + Math.random() * 30;
                // y entre 40 et 60 pour rester centré sur l'animation webGL
                const y = 40 + Math.random() * 20;
                
                bubble.style.left = `${x}%`;
                bubble.style.top = `${y}%`;

                // Rotation aléatoire légère stockée dans une variable CSS pour l'animation
                const rot = (Math.random() - 0.5) * 8;
                bubble.style.setProperty('--rot', `${rot}deg`);
                
                bubblesContainer.appendChild(bubble);

                // Nettoyage après l'animation (4 secondes)
                setTimeout(() => {
                    if (bubble.parentNode) {
                        bubble.parentNode.removeChild(bubble);
                    }
                }, 4000);
            };
        }
            
        if (reverbSlider) {
            // Pour forcer l'affichage du % au démarrage
            reverbSlider.dispatchEvent(new Event('input')); 
        }

        // Slider Doux / Clair (low/high-pass global)
        const toneSlider = document.getElementById('slider-tone');
        const toneVal = document.getElementById('val-tone');

        if (toneSlider && toneVal) {
            toneSlider.addEventListener('input', (e) => {
                const v = parseInt(e.target.value, 10);
                toneVal.innerText = v;
                const norm = v / 100; // 0–1
                if (app.audio && app.audio.setTone) {
                    app.audio.setTone(norm);
                }
            });

            // Valeur neutre au démarrage
            if (app.audio && app.audio.setTone) {
                app.audio.setTone(toneSlider.value / 100);
            }
        }
        // Écouteur pour le Sleep Timer
        const timerSlider = document.getElementById('slider-timer');
        if (timerSlider) {
            timerSlider.addEventListener('input', (e) => {
                const val = parseInt(e.target.value);
                const label = document.getElementById('val-timer');
                
                if (val === 0) {
                    label.innerText = window.i18n.t('app.disabled');
                    document.getElementById('timer-countdown').classList.add('hidden');
                } else {
                    label.innerText = val + ' min';
                }
            });

            // L'événement "change" se déclenche quand l'utilisateur lâche le curseur
            // C'est mieux que "input" pour ne pas relancer le timer 50 fois pendant qu'il glisse le doigt
            timerSlider.addEventListener('change', (e) => {
                const val = parseInt(e.target.value);
                this.startSleepTimer(val);
            });
        }

        const toggleBtn = document.getElementById('togglePanelBtn');
        const panel = document.getElementById('controlsPanel');
        toggleBtn.addEventListener('click', () => {
            this.isPanelOpen = !this.isPanelOpen;
            if(this.isPanelOpen) {
                panel.classList.remove('collapsed');
                toggleBtn.innerHTML = '<i data-lucide="chevron-down"></i>';
            } else {
                panel.classList.add('collapsed');
                toggleBtn.innerHTML = '<i data-lucide="chevron-up"></i>';
            }
            lucide.createIcons();
        });

        // Changement de visuel
        const visualModeBtn = document.getElementById('visualModeBtn');
        if (visualModeBtn) {
            visualModeBtn.addEventListener('click', () => {
                if (this.visuals) {
                    this.visuals.nextMode();
                }
            });
        }

        const themeBtn = document.getElementById('themeBtn');
        themeBtn.addEventListener('click', () => {
            this.isLightTheme = !this.isLightTheme;
            if (this.isLightTheme) {
                document.documentElement.setAttribute('data-theme', 'light');
                themeBtn.innerHTML = '<i data-lucide="moon"></i>';
            } else {
                document.documentElement.removeAttribute('data-theme');
                themeBtn.innerHTML = '<i data-lucide="sun"></i>';
            }
            lucide.createIcons();
        });

        const uiContainer = document.getElementById('uiContainer');
        const viewControls = document.getElementById('viewControls');
        const restoreArea = document.getElementById('restoreArea');
        
        document.getElementById('zenBtn').addEventListener('click', () => {
            uiContainer.classList.add('ui-hidden');
            viewControls.classList.add('opacity-0', 'pointer-events-none');
            restoreArea.style.display = 'block'; 
        });

        restoreArea.addEventListener('click', () => {
            uiContainer.classList.remove('ui-hidden');
            viewControls.classList.remove('opacity-0', 'pointer-events-none');
            restoreArea.style.display = 'none';
        });

        const fullscreenBtn = document.getElementById('fullscreenBtn');
        let isImmersiveFs = false;
        if (fullscreenBtn) {
            fullscreenBtn.addEventListener('click', () => {
                isImmersiveFs = !isImmersiveFs;
                if (window.AndroidBridge && typeof window.AndroidBridge.toggleFullScreen === 'function') {
                    window.AndroidBridge.toggleFullScreen();
                } else if (!document.fullscreenElement) {
                    if (document.documentElement.requestFullscreen) {
                        document.documentElement.requestFullscreen().catch(() => {});
                    }
                } else {
                    if (document.exitFullscreen) document.exitFullscreen().catch(() => {});
                }
                fullscreenBtn.innerHTML = isImmersiveFs
                    ? '<i data-lucide="minimize"></i>'
                    : '<i data-lucide="maximize"></i>';
                if (window.lucide) lucide.createIcons();
            });
        }
        
        // Charger presets custom depuis localStorage
        this.loadUserPresetsFromStorage();

        // Boutons presets pré-définis
        document.querySelectorAll('.preset-default-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-preset-id');
                this.applyPresetById(id);
                // Reset highlight sur tous les presets pré-définis
                document.querySelectorAll('.preset-default-btn').forEach(b => {
                b.classList.remove('preset-selected');
                });
                // Mettre en avant celui cliqué
                btn.classList.add('preset-selected');
                const status = document.getElementById('presetStatus');
                if (status) status.innerText = window.i18n.t('app.presetLoaded') + `"${this.defaultPresets[id]?.name || id}"`;
            });
        });

        // Boutons presets custom (C1..C4)
        document.querySelectorAll('.preset-user-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const slot = btn.getAttribute('data-preset-slot');
                app.selectedUserSlot = slot;  // on peut aussi expliciter app ici

                const status = document.getElementById('presetStatus');

                // Mode suppression
                if (app.isDeleteMode) {
                    delete app.userPresets[slot];
                    app.saveUserPresetsToStorage();
                    
                    app.isDeleteMode = false;
                    const clearBtn = document.getElementById('clearPresetBtn');
                    if (clearBtn) {
                        clearBtn.classList.remove('bg-red-900/50', 'text-white');
                    }
                    
                    if (status) status.innerText = window.i18n.t('app.slotCleared') + `C${slot}`;
                    
                    document.querySelectorAll('.preset-user-btn').forEach(b => {
                        b.classList.remove('preset-selected');
                    });
                    return;
                }

                // Mode sélection standard
                document.querySelectorAll('.preset-user-btn').forEach(b => {
                    b.classList.remove('preset-selected');
                });
                btn.classList.add('preset-selected');

                if (app.userPresets[slot]) {
                    if (status) status.innerText = window.i18n.t('app.slotSelected') + `C${slot}`;
                    app.applyUserPresetSlot(slot);
                } else {
                    if (status) status.innerText = window.i18n.t('app.slotEmpty') + `C${slot}`;
                }
            });
        });
        // Bouton Sauvegarde du preset custom
        const savePresetBtn = document.getElementById('savePresetBtn');
        if (savePresetBtn) {
            savePresetBtn.addEventListener('click', () => {
                app.saveCurrentToUserSlot();
            });
        }
        
        // --- NOUVEAU : Bouton Effacer ---
        const clearPresetBtn = document.getElementById('clearPresetBtn');
        if (clearPresetBtn) {
            app.isDeleteMode = false;
            clearPresetBtn.addEventListener('click', () => {
                app.isDeleteMode = !app.isDeleteMode;
                const status = document.getElementById('presetStatus');
                
                if (app.isDeleteMode) {
                    clearPresetBtn.classList.add('bg-red-900/50', 'text-white');
                    if (status) status.innerText = window.i18n.t('app.clickToClear');
                } else {
                    clearPresetBtn.classList.remove('bg-red-900/50', 'text-white');
                    if (status) status.innerText = window.i18n.t('app.clearCancel');
                }
            });
        }
        // --- NOUVEAU : Bouton Partager par URL ---
        const shareUrlBtn = document.getElementById('shareUrlBtn');
        if (shareUrlBtn) {
            shareUrlBtn.addEventListener('click', async () => {
                // 1. Récupérer le mix actuel avec ta fonction existante
                const preset = app.captureCurrentStateAsPreset();
                
                // On ne partage pas le timer de la personne, juste les sons et la reverb
                delete preset.timerMinutes;

                // 2. Transformer l'objet en chaîne de caractères sécurisée pour une URL (Base64)
                // Note: btoa() convertit une chaîne en Base64
                const base64Preset = btoa(JSON.stringify(preset));

                // 3. Créer l'URL finale
                let baseUrl = window.location.href;
                // Si l'application tourne en natif (Android/iOS), on force l'URL du site web public
                if (window.Capacitor && window.Capacitor.isNativePlatform()) {
                    baseUrl = 'https://relax-box.b-h.dev/';
                }
                
                const url = new URL(baseUrl);
                url.searchParams.set('p', base64Preset);

                // 4. Copier dans le presse-papier de l'utilisateur
                try {
                    await navigator.clipboard.writeText(url.toString());
                    const status = document.getElementById('presetStatus');
                    if (status) {
                        // Petit retour visuel bleu pour confirmer
                        status.style.color = '#60a5fa'; 
                        status.innerText = window.i18n.t('app.linkCopied');
                        // Reset de la couleur après 3 secondes
                        setTimeout(() => status.style.color = '', 3000); 
                    }
                } catch (err) {
                    console.error("Erreur lors de la copie du lien", err);
                }
            });
        }
    },


    renderSliders: function() {
        this.tracks.forEach(track => {
            const container = document.getElementById(`group-${track.group}`);
            if(!container) return;
            const div = document.createElement('div');
            div.className = 'fader-group';
            div.innerHTML = `
                <div class="flex justify-between mb-1">
                    <label for="slider-${track.id}" class="track-label" data-i18n="tracks.${track.id}">${window.i18n.t('tracks.' + track.id)}</label>
                    <span class="text-[10px] font-mono w-8 text-right opacity-70" id="val-${track.id}">0%</span>
                </div>
                <input type="range" id="slider-${track.id}" min="0" max="1" step="0.01" value="0">
            `;
            container.appendChild(div);
            div.querySelector('input').addEventListener('input', (e) => {
                const val = parseFloat(e.target.value);
                document.getElementById(`val-${track.id}`).innerText = Math.floor(val*100) + '%';
                this.audio.setVolume(track.id, val);
            });
        });
    },

    randomize: function() {
        this.tracks.forEach(track => {
            let chance = 0.7;
            if (track.group === 'melody') chance = 0.85; 
            const shouldPlay = Math.random() > chance; 
            const val = shouldPlay ? Math.random() * 0.25 : 0;
            this.updateSlider(track.id, val);
        });
    },

    reset: function() {
        console.log("RESET called - stopping Relax-Box audio engine");
        this.logSystemMetrics("Before reset");
        
        // Stop la respiration et met à jour le bouton
        if (this.breathing && this.breathing.isActive) {
            this.breathing.stop();
            const btn = document.getElementById('breathingNavBtn');
            if (btn) {
                btn.classList.add('text-purple-400', 'border-purple-500/50');
                btn.classList.remove('bg-purple-600', 'text-white', 'border-purple-400', 'shadow-[0_0_15px_rgba(168,85,247,0.5)]');
            }
            const bOverlay = document.getElementById('breathingOverlay');
            if (bOverlay) {
                bOverlay.style.opacity = '0';
                setTimeout(() => bOverlay.classList.add('hidden'), 500);
            }
        }

        // 1. Coupe le mode Auto-Shuffle s'il est allumé
        if (this.isAutoShuffle) {
            this.stopAutoShuffle();
        }
        
        // 2. Met toutes les pistes à 0 de façon brutale
        this.tracks.forEach(track => {
            // Optionnel : annuler les cibles de l'animation pour être sûr
            if (this.shuffleTargets) {
                delete this.shuffleTargets[track.id];
            }
            this.updateSlider(track.id, 0);
        });

        // 3. Arrêter le Web Worker scheduler
        if (this.audio && this.audio.stopScheduler) {
            this.audio.stopScheduler();
        }

        // 4. Suspendre le AudioContext pour arrêter complètement le son
        if (this.audio && this.audio.ctx && this.audio.ctx.state === 'running') {
            console.log("Suspending AudioContext to stop audio");
            this.audio.ctx.suspend();
        }

        // 5. Arrêter le monitoring des métriques
        if (this.metricsInterval) {
            clearInterval(this.metricsInterval);
            this.metricsInterval = null;
        }

        // 6. Libérer le Foreground Service (Permettre à Android de tuer l'app en arrière-plan)
        if (window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.MediaSession) {
            window.Capacitor.Plugins.MediaSession.setPlaybackState({ playbackState: 'paused' });
            this._mediaSessionPlaying = false;
        }
        
        this.logSystemMetrics("After reset");
        console.log("RESET completed - Relax-Box audio engine stopped");
    },

    logSystemMetrics: function(context) {
        const metrics = {
            context: context,
            timestamp: new Date().toISOString(),
            memory: null,
            audioContext: null,
            scheduler: null
        };

        // Mémoire (si disponible)
        if (performance && performance.memory) {
            metrics.memory = {
                usedJSHeapSize: Math.round(performance.memory.usedJSHeapSize / 1024 / 1024) + ' MB',
                totalJSHeapSize: Math.round(performance.memory.totalJSHeapSize / 1024 / 1024) + ' MB',
                jsHeapSizeLimit: Math.round(performance.memory.jsHeapSizeLimit / 1024 / 1024) + ' MB'
            };
        }

        // AudioContext state
        if (this.audio && this.audio.ctx) {
            metrics.audioContext = {
                state: this.audio.ctx.state,
                currentTime: this.audio.ctx.currentTime.toFixed(2) + 's'
            };
        }

        // Scheduler state
        if (this.audio) {
            metrics.scheduler = {
                workerActive: !!this.audio.worker,
                intervalActive: !!this.audio.schedulerTimer
            };
        }

        console.log("SYSTEM METRICS [" + context + "]:", JSON.stringify(metrics, null, 2));
    },


    updateSlider: function(id, val) {
        const el = document.getElementById(`slider-${id}`);
        if(el) {
            el.value = val;
            el.dispatchEvent(new Event('input'));
        }

        // Relancer intelligemment l'état "Playing" sans spammer le système Android
        if (val > 0 && !this._mediaSessionPlaying && window.Capacitor && window.Capacitor.Plugins && window.Capacitor.Plugins.MediaSession) {
            window.Capacitor.Plugins.MediaSession.setPlaybackState({ playbackState: 'playing' });
            this._mediaSessionPlaying = true;
        }
    },
    
    setPreset: function(settings) {
        setTimeout(() => {
            settings.forEach(s => this.updateSlider(s.id, s.val));
        }, 500);
    }
};

window.app = app;

// --- Highlight respirant pour Settings / Presets / Shuffle ---
function updatePanelButtonStates() {
    const settingsBtn = document.getElementById('globalSettingsBtn');
    const settingsPane = document.getElementById('globalSettingsPanel');
    const presetsBtn = document.getElementById('presetsBtn');
    const presetsPane = document.getElementById('presetsPanel');
    const shuffleBtn = document.getElementById('autoShuffleBtn');

    if (settingsBtn && settingsPane) {
        // On vérifie si c'est ouvert via la classe grid-rows-[1fr]
        const open = settingsPane.classList.contains('grid-rows-[1fr]');
        settingsBtn.classList.toggle('btn-breathing', open);
        settingsBtn.classList.toggle('bg-blue-900/50', open);
    }

    if (presetsBtn && presetsPane) {
        // On vérifie si c'est ouvert via la classe grid-rows-[1fr]
        const open = presetsPane.classList.contains('grid-rows-[1fr]');
        presetsBtn.classList.toggle('btn-breathing', open);
        presetsBtn.classList.toggle('bg-emerald-900/50', open);
    }

    if (shuffleBtn && window.app) {
        const active = app.isAutoShuffle;
        shuffleBtn.classList.toggle('btn-breathing', active);
        shuffleBtn.classList.toggle('bg-purple-900/50', active);
        shuffleBtn.classList.toggle('text-white', active);
    }
}

class WakeManager {
    constructor() {
        this.wakeLock = null;
        this.idleTimeout = null;
        this.idleTime = 180000; // 15 secondes d'inactivité avant écran noir
        
        // Créer l'overlay noir et l'ajouter au document
        this.overlay = document.createElement('div');
        this.overlay.id = 'sleep-overlay';
        document.body.appendChild(this.overlay);

        this.init();
    }

    async requestWakeLock() {
        if (this.wakeLock !== null) return;

        if ('wakeLock' in navigator) {
            try {
                this.wakeLock = await navigator.wakeLock.request('screen');
                console.log('Wake Lock activé');
                this.wakeLock.addEventListener('release', () => {
                    this.wakeLock = null;
                });
            } catch (err) {
                console.warn(`Wake Lock erreur: ${err.name}, ${err.message}`);
            }
        }
    }

    resetIdleTimer() {
        // Si l'écran est noir, on le rallume
        if (this.overlay.classList.contains('active')) {
            this.overlay.classList.remove('active');
        }
        
        // On relance le chrono
        clearTimeout(this.idleTimeout);
        this.idleTimeout = setTimeout(() => {
            // Le temps est écoulé -> écran noir complet
            this.overlay.classList.add('active');
        }, this.idleTime);
    }

    init() {
        // Demander le wake lock au premier clic sur la page
        const startLock = () => {
            this.requestWakeLock();
            document.removeEventListener('click', startLock);
            document.removeEventListener('touchstart', startLock);
        };
        document.addEventListener('click', startLock);
        document.addEventListener('touchstart', startLock);

        // Si l'utilisateur quitte l'onglet et revient
        document.addEventListener('visibilitychange', async () => {
            if (this.wakeLock !== null && document.visibilityState === 'visible') {
                this.requestWakeLock();
            }
        });

        // Le clic sur l'écran noir le réveille (sans déclencher de boutons dessous)
        this.overlay.addEventListener('click', (e) => {
            e.stopPropagation();
            this.resetIdleTimer();
        });
        this.overlay.addEventListener('touchstart', (e) => {
            e.stopPropagation();
            this.resetIdleTimer();
        }, {passive: true});

        // Détecter l'activité globale pour repousser la mise en veille
        const events = ['mousemove', 'touchstart', 'click', 'keydown'];
        events.forEach(e => document.addEventListener(e, () => this.resetIdleTimer(), {passive: true}));
        
        this.resetIdleTimer();
    }
    async releaseWakeLock() {
        if (this.wakeLock !== null) {
        try {
            await this.wakeLock.release();
            this.wakeLock = null;
            console.log('Wake Lock relâché (Fin du Timer)');
        } catch (err) {
            console.warn('Erreur lors du relâchement du Wake Lock:', err);
        }
        }
        // Si tu as un timer d'inactivité en cours, on le coupe aussi
        if (this.idleTimeout) {
        clearTimeout(this.idleTimeout);
        }
    }
}

// Lancer le système
const wakeManager = new WakeManager();

window.app = app; // Expose to window

// --- LUMIBOOK NATIVE BRIDGE ---
window.LumibookBridge = {
    _started: false,
    _currentPresetId: 'sleep-deep',
    setVisualsEnabled: function(enabled) {
        if (window.app && window.app.visuals) {
            window.app.visuals.start();
        }
    },
    startEngine: async function(presetId) {
        if (presetId) this._currentPresetId = presetId;
        const startOverlay = document.getElementById('startOverlay');
        if (startOverlay) {
            startOverlay.style.opacity = '0';
            startOverlay.style.display = 'none';
        }
        if (window.app && window.app.startApp) {
            await window.app.startApp(this._currentPresetId);
        } else {
            const bgAudio = document.getElementById('bg-audio');
            if (bgAudio) {
                try { await bgAudio.play(); } catch (e) {}
            }
        }
        this._started = true;
        this.notifyState();
    },
    playAutoMode: function(mode) {
        const m = (mode || this._currentPresetId || 'global').toLowerCase();
        this._currentPresetId = m;
        const startOverlay = document.getElementById('startOverlay');
        if (startOverlay) {
            startOverlay.style.opacity = '0';
            startOverlay.style.display = 'none';
        }
        if (!this._started) {
            this.startEngine(m);
            return;
        }
        this.play();
        if (window.app && window.app.startAutoShuffleWithMode) {
            window.app.startAutoShuffleWithMode(m);
        }
        this.notifyState();
    },
    play: async function() {
        console.log("PLAY called - resuming Relax-Box audio engine");
        this.logSystemMetrics("Before play");
        
        const startOverlay = document.getElementById('startOverlay');
        if (startOverlay) {
            startOverlay.style.opacity = '0';
            startOverlay.style.display = 'none';
        }
        if (!this._started) {
            await this.startEngine('global');
            return;
        }

        // 1. Reprendre le AudioContext s'il est suspendu (AVANT de relancer le scheduler)
        if (this.audio && this.audio.ctx && this.audio.ctx.state === 'suspended') {
            console.log("Resuming suspended AudioContext");
            try { await this.audio.ctx.resume(); } catch (e) {
                console.error("Error resuming AudioContext:", e);
            }
        }

        // 2. Redémarrer le scheduler audio (le resume() resynchronise les événements
        //    via resyncScheduledEvents() pour éviter tout burst audio)
        if (this.audio && this.audio.initSchedulerWorker) {
            console.log("Restarting audio scheduler");
            this.audio.initSchedulerWorker();
        }
        
        // Redémarrer le monitoring des métriques
        if (!this.metricsInterval) {
            this.metricsInterval = setInterval(() => {
                this.logSystemMetrics("Periodic check");
            }, 30000);
        }
        
        const bgAudio = document.getElementById('bg-audio');
        if (bgAudio && bgAudio.paused) {
            try { await bgAudio.play(); } catch (e) {}
        }
        
        if (!this.isAutoShuffle) {
            let hasVolume = this.tracks && this.tracks.some(t => {
                const el = document.getElementById(`slider-${t.id}`);
                return el && parseFloat(el.value) > 0;
            });
            if (!hasVolume && this.startAutoShuffleWithMode) {
                this.startAutoShuffleWithMode(this._currentPresetId || 'global');
            }
        }
        
        this.logSystemMetrics("After play");
        console.log("PLAY completed - Relax-Box audio engine resumed");
        this.notifyState();
    },
    pause: function() {
        if (window.app && window.app.reset) {
            window.app.reset();
        }
        const bgAudio = document.getElementById('bg-audio');
        if (bgAudio && !bgAudio.paused) {
            try { bgAudio.pause(); } catch (e) {}
        }
        if (window.app && window.app.audio && window.app.audio.ctx && window.app.audio.ctx.state === 'running') {
            try { window.app.audio.ctx.suspend(); } catch (e) {}
        }
        this.notifyState();
    },
    togglePlayPause: function() {
        if (this.isPlaying()) {
            this.pause();
        } else {
            this.playAutoMode('global');
        }
    },
    setVolume: function(volume) {
        const v = Math.max(0.0, Math.min(1.0, parseFloat(volume) || 0.0));
        if (window.app && window.app.audio && window.app.audio.masterGain && window.app.audio.ctx) {
            try {
                window.app.audio.masterGain.gain.cancelScheduledValues(window.app.audio.ctx.currentTime);
                window.app.audio.masterGain.gain.setValueAtTime(v, window.app.audio.ctx.currentTime);
            } catch (e) {
                window.app.audio.masterGain.gain.value = v;
            }
        }
        this.notifyState();
    },
    setAutoMode: function(mode) {
        const m = (mode || 'global').toLowerCase();
        this._currentPresetId = m;
        if (!this._started) {
            this.startEngine(m);
            return;
        }
        if (window.app) {
            if (window.app.audio && window.app.audio.ctx && window.app.audio.ctx.state === 'suspended') {
                try { window.app.audio.ctx.resume(); } catch (e) {}
            }
            if (window.app.startAutoShuffleWithMode) {
                window.app.startAutoShuffleWithMode(m);
            }
        }
        this.play();
        this.notifyState();
    },
    loadPreset: function(presetId) {
        const p = presetId || 'global';
        if (['global', 'sleep', 'focus', 'zen'].includes(p.toLowerCase())) {
            this.setAutoMode(p.toLowerCase());
            return;
        }
        this._currentPresetId = p;
        if (!this._started) {
            this.startEngine(p);
            return;
        }
        if (window.app && window.app.applyPresetById) {
            window.app.applyPresetById(p);
        }
        this.play();
        this.notifyState();
    },
    isPlaying: function() {
        const bgAudio = document.getElementById('bg-audio');
        const bgAudioPlaying = bgAudio && !bgAudio.paused;
        const ctxRunning = !!(window.app && window.app.audio && window.app.audio.ctx && window.app.audio.ctx.state === 'running');
        return bgAudioPlaying || ctxRunning;
    },
    getVolume: function() {
        if (window.app && window.app.audio && window.app.audio.masterGain) {
            return window.app.audio.masterGain.gain.value;
        }
        return 0.5;
    },
    notifyState: function() {
        if (window.AndroidBridge && window.AndroidBridge.onStateChanged) {
            try {
                window.AndroidBridge.onStateChanged(this.isPlaying(), this.getVolume(), this._currentPresetId || '');
            } catch (e) {}
        }
    }
};

const initApp = () => {
    app.init();
    const bg = document.getElementById('bg-audio');
    if (bg) {
        bg.addEventListener('play', () => {
            setTimeout(() => { if (window.LumibookBridge) window.LumibookBridge.notifyState(); }, 300);
        });
        bg.addEventListener('pause', () => {
            setTimeout(() => { if (window.LumibookBridge) window.LumibookBridge.notifyState(); }, 300);
        });
    }
};

if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', initApp);
} else {
    initApp();
}
