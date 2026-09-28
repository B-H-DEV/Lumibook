import { fsSynthwave, fsFractal, fsSonic, fsLotus } from './shaders.js';
export class VisualEngine {
    constructor(canvasId, audioEngine) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        this.audio = audioEngine;
        this.particles = [];
        this.width = 0;
        this.height = 0;
        this.mandalaRotation = 0;

        // Modes 2D légers + 3D (WebGl)
        this.modes = ['particles', 'aurora', 'plexus', 'mandala', 'fractal', 'lotus'];
        this.currentModeIndex = 0;

        this.isRunning = true;

        this.smoothedAvg = 0;
        this.smoothedBass = 0;
        this.smoothedMid = 0;
        this.smoothedHigh = 0;

        // Préparation du Canvas 2D pour le fondu croisé
        this.canvas.style.transition = 'opacity 0.8s ease';

        this.glCanvas = null;
        this.gl = null;
        this.glInitialized = false;

        // Création du canvas de transition (Fondu)
        this.fadeCanvas = document.createElement('canvas');
        this.fadeCanvas.className = 'visualizer-canvas';
        this.fadeCanvas.style.opacity = '0';
        this.fadeCanvas.style.pointerEvents = 'none';
        this.fadeCanvas.style.position = 'absolute';
        this.fadeCanvas.style.top = '0';
        this.fadeCanvas.style.left = '0';
        this.fadeCanvas.style.zIndex = '2';
        this.fadeCtx = this.fadeCanvas.getContext('2d');
        this.canvas.parentNode.insertBefore(this.fadeCanvas, this.canvas);

        this.resize = this.resize.bind(this);
        window.addEventListener('resize', this.resize);
        
        // APPEL OBLIGATOIRE ICI pour forcer la bonne taille dès le départ
        this.resize(); 
        
        this.createParticles(200);

        // Captation de la position et mouvement du touch ou souris
        this.mouseX = 0;
        this.mouseY = 0;

        window.addEventListener('mousemove', (e) => {
            this.mouseX = e.clientX;
            this.mouseY = e.clientY;
        });

        window.addEventListener('touchmove', (e) => {
            if (e.touches.length > 0) {
                this.mouseX = e.touches[0].clientX;
                this.mouseY = e.touches[0].clientY;
            }
        }, { passive: true });
    }

    ensureWebGL() {
        if (!this.glCanvas) {
            try {
                this.glCanvas = document.createElement('canvas');
                this.glCanvas.className = 'visualizer-canvas';
                this.glCanvas.style.opacity = '0';
                this.glCanvas.style.pointerEvents = 'none';
                this.glCanvas.style.position = 'absolute';
                this.glCanvas.style.top = '0';
                this.glCanvas.style.left = '0';
                this.glCanvas.style.zIndex = '1';
                this.canvas.parentNode.insertBefore(this.glCanvas, this.canvas);

                // Force redimensionnement immédiat
                this.glCanvas.width = this.width;
                this.glCanvas.height = this.height;

                this.initWebGL();
            } catch (e) {
                console.warn("WebGL canvas creation failed:", e);
                this.glCanvas = null;
                this.gl = null;
            }
        }
        return !!this.gl;
    }

    updateCanvasVisibility() {
        const currentMode = this.modes[this.currentModeIndex];
        const isWebGL = (currentMode === 'fractal' || currentMode === 'lotus' || currentMode === 'tunnel' || currentMode === 'sonic');

        if (isWebGL) {
            if (this.glCanvas) this.glCanvas.style.opacity = '1';
            if (this.canvas) this.canvas.style.opacity = '0';
        } else {
            if (this.glCanvas) this.glCanvas.style.opacity = '0';
            if (this.canvas) this.canvas.style.opacity = '1';
        }
    }

    initWebGL() {
        if (!this.glCanvas) return;
        
        try {
            this.gl = this.glCanvas.getContext('webgl', { alpha: true, preserveDrawingBuffer: false, powerPreference: 'low-power' }) ||
                      this.glCanvas.getContext('experimental-webgl', { alpha: true, preserveDrawingBuffer: false, powerPreference: 'low-power' });
        } catch (e) {
            console.warn("WebGL not supported or disabled:", e);
            this.gl = null;
            return;
        }

        if (!this.gl) {
            console.warn("WebGL unavailable - 2D procedural rendering active");
            return;
        }

        try {
            this.glCanvas.addEventListener("webglcontextlost", (event) => {
                event.preventDefault();
                console.warn("WebGL context lost - switching to safe 2D fallback");
                this.gl = null;
                this.updateCanvasVisibility();
            }, false);

            this.glCanvas.addEventListener("webglcontextrestored", () => {
                console.log("WebGL context restored");
                try { this.initWebGL(); } catch (e) {
                    console.warn("WebGL reinitialization failed:", e);
                }
            }, false);

            // --- VERTEX SHADER (Commun aux deux) ---
            const vsSource = `
                attribute vec2 a_position;
                void main() {
                    gl_Position = vec4(a_position, 0.0, 1.0);
                }
            `;

            // Fonction utilitaire de compilation avec gestion d'erreur sécurisée
            const compileProgram = (fsCode) => {
                try {
                    const vs = this.gl.createShader(this.gl.VERTEX_SHADER);
                    this.gl.shaderSource(vs, vsSource);
                    this.gl.compileShader(vs);
                    if (!this.gl.getShaderParameter(vs, this.gl.COMPILE_STATUS)) {
                        console.warn("VS error:", this.gl.getShaderInfoLog(vs));
                        return null;
                    }

                    const fs = this.gl.createShader(this.gl.FRAGMENT_SHADER);
                    this.gl.shaderSource(fs, fsCode);
                    this.gl.compileShader(fs);
                    if (!this.gl.getShaderParameter(fs, this.gl.COMPILE_STATUS)) {
                        console.warn("FS error:", this.gl.getShaderInfoLog(fs));
                        return null;
                    }

                    const program = this.gl.createProgram();
                    this.gl.attachShader(program, vs);
                    this.gl.attachShader(program, fs);
                    this.gl.linkProgram(program);
                    if (!this.gl.getProgramParameter(program, this.gl.LINK_STATUS)) {
                        console.warn("Program link error:", this.gl.getProgramInfoLog(program));
                        return null;
                    }
                    return program;
                } catch (err) {
                    console.warn("compileProgram failed:", err);
                    return null;
                }
            };

            // On compile et on stocke les programmes
            this.programSynth = compileProgram(fsSynthwave);
            this.programFractal = compileProgram(fsFractal);
            this.programSonic = compileProgram(fsSonic);
            this.programLotus = compileProgram(fsLotus);

            // Rectangle qui prend tout l'écran
            const vertices = new Float32Array([-1,-1, 1,-1, -1,1, -1,1, 1,-1, 1,1]);
            this.buffer = this.gl.createBuffer();
            this.gl.bindBuffer(this.gl.ARRAY_BUFFER, this.buffer);
            this.gl.bufferData(this.gl.ARRAY_BUFFER, vertices, this.gl.STATIC_DRAW);

            // Fonction pour récupérer les Uniforms dynamiquement selon le programme
            this.getUniforms = (prog) => {
                if (!this.gl || !prog) return {};
                return {
                    res: this.gl.getUniformLocation(prog, "u_resolution"),
                    mouse: this.gl.getUniformLocation(prog, "u_mouse"),
                    time: this.gl.getUniformLocation(prog, "u_time"),
                    bass: this.gl.getUniformLocation(prog, "u_bass"),
                    mid: this.gl.getUniformLocation(prog, "u_mid"),
                    high: this.gl.getUniformLocation(prog, "u_high"),
                    isLight: this.gl.getUniformLocation(prog, "u_isLight"),
                    pastel: this.gl.getUniformLocation(prog, "u_pastel")
                };
            };
        } catch (setupError) {
            console.warn("WebGL initialization failed, falling back to 2D:", setupError);
            this.gl = null;
        }
    }

    drawWebGL(prog, bNorm, mNorm, hNorm, isLight, isBreathing = false) {
        if (!this.gl || !prog) return;
        try {
            this.gl.viewport(0, 0, this.width, this.height);
            this.gl.clearColor(0, 0, 0, 0);
            this.gl.clear(this.gl.COLOR_BUFFER_BIT);
            
            // Sélectionne le bon Shader
            this.gl.useProgram(prog);

            // Reconnecte le Buffer de position (obligatoire en changeant de programme)
            this.gl.bindBuffer(this.gl.ARRAY_BUFFER, this.buffer);
            const posAttr = this.gl.getAttribLocation(prog, "a_position");
            this.gl.enableVertexAttribArray(posAttr);
            this.gl.vertexAttribPointer(posAttr, 2, this.gl.FLOAT, false, 0, 0);

            // Récupère les bons pointeurs de variables
            const uni = this.getUniforms(prog);

            // Envoi des données
            this.gl.uniform2f(uni.res, this.width, this.height);
            if (uni.mouse) {this.gl.uniform2f(uni.mouse, this.mouseX, this.mouseY);}
            this.gl.uniform1f(uni.time, performance.now() * 0.001);
            // bNorm, mNorm, hNorm sont déjà normalisés 0–1
            this.gl.uniform1f(uni.bass, bNorm * 2);
            this.gl.uniform1f(uni.mid,  mNorm * 2);
            if (uni.high) this.gl.uniform1f(uni.high, hNorm * 2);
            this.gl.uniform1f(uni.isLight, isLight ? 1.0 : 0.0);
            if (uni.pastel) this.gl.uniform1f(uni.pastel, isBreathing ? 1.0 : 0.0);

            this.gl.drawArrays(this.gl.TRIANGLES, 0, 6);
        } catch (err) {
            console.warn("drawWebGL error:", err);
        }
    }

    nextMode() {
        // 1. CAPTURE DE L'IMAGE ACTUELLE
        this.fadeCtx.clearRect(0, 0, this.width, this.height);
        const oldMode = this.modes[this.currentModeIndex];
        const isOldWebGL = (oldMode === 'tunnel' || oldMode === 'fractal' || oldMode === 'sonic');
        
        // On dessine le rendu actuel comme une simple image morte
        if (isOldWebGL && this.glCanvas) {
            this.fadeCtx.drawImage(this.glCanvas, 0, 0);
        } else if (this.canvas) {
            this.fadeCtx.drawImage(this.canvas, 0, 0);
        }

        // 2. RÉINITIALISATION DU FONDU
        this.fadeCanvas.style.transition = 'none';
        this.fadeCanvas.style.opacity = '1';
        
        // Force le navigateur à appliquer le style (reflow) avant la frame suivante
        void this.fadeCanvas.offsetWidth;

        // 3. LANCEMENT DU FONDU CSS (0 calcul CPU supplémentaire)
        this.fadeCanvas.style.transition = 'opacity 1.5s ease-in-out';
        this.fadeCanvas.style.opacity = '0';

        // Libération de la mémoire GPU après le fondu pour éviter de saturer la RAM (surtout en plein écran)
        clearTimeout(this.fadeTimeout);
        this.fadeTimeout = setTimeout(() => {
            if (this.fadeCtx) this.fadeCtx.clearRect(0, 0, this.width, this.height);
        }, 1500);

        // 4. CHANGEMENT DE VISUEL
        const numStandardModes = this.modes.length;
        
        if (this.currentModeIndex >= numStandardModes) {
            this.currentModeIndex = 0;
        } else {
            this.currentModeIndex = (this.currentModeIndex + 1) % numStandardModes;
        }

        if (this.modes[this.currentModeIndex] === 'particles') {
        this.createParticles(200);
        }

        this.updateCanvasVisibility();
    }
    resize() {
        if (this.width === window.innerWidth && this.height === window.innerHeight) return;
        this.width = window.innerWidth;
        this.height = window.innerHeight;
        
        // Redimensionner le Canvas 2D
        this.canvas.width = this.width;
        this.canvas.height = this.height;
        
        // Redimensionner le Canvas WebGL (si créé)
        if (this.glCanvas) {
            this.glCanvas.width = this.width;
            this.glCanvas.height = this.height;
            
            // TRES IMPORTANT : dire à WebGL que la taille de l'écran a changé
            if (this.gl) {
                this.gl.viewport(0, 0, this.width, this.height);
            }
        }
        if (this.fadeCanvas) {
            this.fadeCanvas.width = this.width;
            this.fadeCanvas.height = this.height;
        }
    }

    createParticles(count) {
        this.particles = [];
        for(let i=0; i<count; i++) {
            this.particles.push({
                angle: Math.random() * Math.PI * 2,
                orbit: Math.random() * 300 + 50,
                speed: (Math.random() * 0.002) + 0.0005,
                baseR: Math.random() * 2 + 1,
                offset: Math.random() * 100
            });
        }
    }

    draw() {
        // NOUVEAU : Si on a arrêté le moteur, on ne demande plus la frame suivante
        if (!this.isRunning) return; 

        // Si l'application est minimisée (1dp) ou cachée, ne pas exécuter de shaders GPU lourds
        if (document.hidden || this.width < 50 || this.height < 50) {
            setTimeout(() => {
                if (this.isRunning) requestAnimationFrame(() => this.draw());
            }, 300);
            return;
        }

        if (!this.audio || !this.audio.analyser) {
            requestAnimationFrame(() => this.draw());
            return;
        }

        const bufferLength = this.audio.analyser.frequencyBinCount;
        if (!this.dataArray || this.dataArray.length !== bufferLength) {
            this.dataArray = new Uint8Array(bufferLength);
        }
        this.audio.analyser.getByteFrequencyData(this.dataArray);
        const dataArray = this.dataArray;

        let sum = 0, bassSum = 0, midSum = 0, highSum = 0;
        for(let i = 0; i < bufferLength; i++) {
            const val = dataArray[i];
            sum += val;
            if(i < bufferLength * 0.1) bassSum += val;
            else if(i < bufferLength * 0.5) midSum += val;
            else highSum += val;
        }

        // 1. Valeurs BRUTES (nerveuses)
        const rawAvg = (sum / bufferLength) / 255;
        const rawBass = (bassSum / (bufferLength * 0.1)) / 255;
        const rawMid = (midSum / (bufferLength * 0.4)) / 255;
        const rawHigh = (highSum / (bufferLength * 0.5)) / 255;

        // 2. Valeurs LISSÉES (fluides) pour l'Aurore
        const easing = 0.05; 
        this.smoothedAvg += (rawAvg - this.smoothedAvg) * easing;
        this.smoothedBass += (rawBass - this.smoothedBass) * easing;
        this.smoothedMid += (rawMid - this.smoothedMid) * easing;
        this.smoothedHigh += (rawHigh - this.smoothedHigh) * easing;

        // MODE RESPIRATION : On écrase les valeurs si actif
        let isBreathing = false;
        if (window.app && window.app.breathing && window.app.breathing.isActive) {
            isBreathing = true;
            let pi = window.app.breathing.phaseIndex;
            let cp = window.app.breathing.currentProgress;
            let scale = 0;
            if (pi === 0) scale = cp;                  // Inhale
            else if (pi === 1) scale = 1;              // Hold
            else if (pi === 2) scale = 1 - cp;         // Exhale
            else if (pi === 3) scale = 0;              // Hold out
            
            // Smoothing de la respiration nativement (par une courbe sinusoidale douce)
            let smoothScale = (1 - Math.cos(scale * Math.PI)) / 2;

            this.smoothedBass = smoothScale * 0.8;
            this.smoothedMid  = smoothScale * 0.4;
            this.smoothedHigh = 0;
        }

        // s'assurer que la bonne surface est visible
        this.updateCanvasVisibility();
        const isLight = document.documentElement.hasAttribute('data-theme');
        const currentMode = this.modes[this.currentModeIndex];
        const isWebGL = (currentMode === 'fractal' || currentMode === 'lotus' || currentMode === 'tunnel' || currentMode === 'sonic');

        // S'assurer que WebGL est initialisé si nécessaire (non-bloquant)
        if (isWebGL && !this.glInitialized) {
            // Initialisation différée pour ne pas bloquer le démarrage
            setTimeout(() => {
                try {
                    this.ensureWebGL();
                    this.glInitialized = !!this.gl;
                } catch (e) {
                    console.warn("WebGL deferred initialization failed:", e);
                    this.glInitialized = false;
                }
            }, 100);
        }

        // --- MODES 3D ---
        if (isWebGL && this.gl) {
            try {
                this.ctx.clearRect(0, 0, this.width, this.height);

                let activeProgram;
                if (currentMode === 'tunnel') activeProgram = this.programSynth;
                else if (currentMode === 'fractal') activeProgram = this.programFractal;
                else if (currentMode === 'sonic') activeProgram = this.programSonic;
                else if (currentMode === 'lotus') activeProgram = this.programLotus;

                if (activeProgram) {
                    this.drawWebGL(activeProgram, this.smoothedBass, this.smoothedMid, this.smoothedHigh, isLight, isBreathing);
                } else {
                    this.drawMandala(this.smoothedAvg, this.smoothedBass, isLight);
                }
            } catch (e) {
                console.warn("WebGL rendering failed, falling back to 2D:", e);
                this.gl = null;
                this.updateCanvasVisibility();
            }
        }
        // --- MODES 2D ---
        else {
            if (this.glCanvas) this.glCanvas.style.opacity = '0';

            // Rendu 2D normal
            this.ctx.clearRect(0, 0, this.width, this.height);
            this.ctx.globalCompositeOperation = 'source-over';

            if (currentMode === 'particles') {
                this.drawParticles(rawAvg, rawBass, rawMid, rawHigh, isLight);
            } else if (currentMode === 'aurora') {
                this.drawAurora(this.smoothedAvg, this.smoothedBass, this.smoothedMid, this.smoothedHigh, isLight);
            } else if (currentMode === 'plexus') {
                this.drawPlexus(rawAvg, this.smoothedBass, rawHigh, isLight);
            } else {
                this.drawMandala(this.smoothedAvg, this.smoothedBass, isLight);
            }
        }

        requestAnimationFrame(() => this.draw());
    } // Fin de la méthode draw()

    // L'ANIMATION DE BASE EXACTEMENT COMME AVANT
    drawParticles(avgNorm, bNorm, mNorm, hNorm, isLight) {
        const cx = this.width / 2;
        const cy = this.height / 2;
        
        // RESTAURATION DU TEMPS ET DU "BREATH" D'ORIGINE
        const time = Date.now() / 1000;
        const cycle = time % 19;
        let breathScale = 1;
        if (cycle < 4) breathScale = 1 + (cycle/4) * 0.3;
        else if (cycle < 11) breathScale = 1.3;
        else breathScale = 1.3 - ((cycle-11)/8) * 0.3;
        
        const reactiveScale = breathScale + (bNorm * 0.5);

        // Récupération des couleurs CSS d'origine
        const style = getComputedStyle(document.body);
        const accent = style.getPropertyValue('--accent-primary').trim();
        const accent2 = style.getPropertyValue('--accent-secondary').trim();

        this.ctx.save();
        this.ctx.translate(cx, cy);
        this.ctx.rotate(time * 0.05 + (avgNorm * 0.1));

        // RESTAURATION DU CŒUR GLOWING
        const coreAlpha = 0.1 + (bNorm * 0.3);
        const gradient = this.ctx.createRadialGradient(0,0, 10, 0,0, 300 * reactiveScale);
        gradient.addColorStop(0, accent === '#00f3ff' ? `rgba(0, 243, 255, ${coreAlpha})` : `rgba(41, 128, 185, ${coreAlpha})`);
        gradient.addColorStop(1, 'rgba(0,0,0,0)');
        this.ctx.fillStyle = gradient;
        this.ctx.beginPath();
        this.ctx.arc(0,0, 300 * reactiveScale, 0, Math.PI*2);
        this.ctx.fill();

        // RESTAURATION DES 6 ANNEAUX CONCENTRIQUES
        const rings = 6;
        for(let i=0; i<rings; i++) {
            this.ctx.beginPath();
            const ringAlpha = 0.05 + (mNorm * 0.3) + (i/rings)*0.1;
            this.ctx.strokeStyle = accent === '#00f3ff' ? `rgba(0, 243, 255, ${ringAlpha})` : `rgba(41, 128, 185, ${ringAlpha})`;
            this.ctx.lineWidth = 1 + (bNorm * 5) + (hNorm * 3);
            
            let r = 40 + (i * 40) + (bNorm * 100) * breathScale;
            const spikes = 5 + Math.floor(mNorm * 10);
            const deformAmount = 15 * avgNorm;
            
            for(let a=0; a<Math.PI*2; a+=0.05) {
                let deform = Math.sin(a * spikes + time + i + bNorm*10) * deformAmount;
                if(hNorm > 0.1) deform += Math.random() * hNorm * 5;
                
                let x = Math.cos(a) * (r + deform);
                let y = Math.sin(a) * (r + deform);
                if(a===0) this.ctx.moveTo(x,y); else this.ctx.lineTo(x,y);
            }
            this.ctx.closePath();
            this.ctx.stroke();
        }
        this.ctx.restore();

        // RESTAURATION DES PARTICULES
        this.particles.forEach((p, idx) => {
            p.angle += p.speed + (bNorm * 0.02);
            let pRadius = p.orbit * reactiveScale + (mNorm * 50);
            let wobble = Math.sin(time * 5 + idx) * (hNorm * 20);
            
            let x = cx + Math.cos(p.angle) * (pRadius + wobble);
            let y = cy + Math.sin(p.angle + p.offset) * ((pRadius + wobble) * 0.8);
            
            const pAlpha = 0.4 + avgNorm;
            
            if(idx % 2 === 0) {
                this.ctx.fillStyle = accent2 === '#bc13fe' ? `rgba(188, 19, 254, ${pAlpha})` : `rgba(39, 174, 96, ${pAlpha})`;
            } else {
                this.ctx.fillStyle = accent === '#00f3ff' ? `rgba(0, 243, 255, ${pAlpha})` : `rgba(41, 128, 185, ${pAlpha})`;
            }
            
            const size = p.baseR + (hNorm * 3);
            this.ctx.beginPath();
            this.ctx.arc(x, y, size, 0, Math.PI*2);
            this.ctx.fill();
        });
    }


    // L'AURORE LISSÉE
    drawAurora(avgNorm, bNorm, mNorm, hNorm, isLight) {
        const time = performance.now() * 0.0005; // Mouvement de base plus lent
        const alphaMultiplier = isLight ? 0.3 : 0.6;
        
        const color1 = isLight ? '41, 128, 185' : '0, 243, 255'; 
        const color2 = isLight ? '39, 174, 96' : '188, 19, 254'; 
        const color3 = isLight ? '142, 68, 173' : '0, 255, 128';

        for (let wave = 0; wave < 3; wave++) {
            this.ctx.beginPath();
            
            // L'amplitude utilise les données lissées (bNorm)
            const amplitude = 50 + (bNorm * 150) + (wave * 30);
            const speed = time + (wave * 10) + (hNorm * 0.5);
            
            for (let x = 0; x <= this.width; x += 20) {
                const y = (this.height / 2) 
                        + Math.sin(x * 0.003 + speed) * amplitude 
                        + Math.cos(x * 0.002 - speed * 0.8) * (amplitude * 0.6);
                
                if (x === 0) this.ctx.moveTo(x, y);
                else this.ctx.lineTo(x, y);
            }
            
            this.ctx.lineTo(this.width, this.height);
            this.ctx.lineTo(0, this.height);
            this.ctx.closePath();

            const gradient = this.ctx.createLinearGradient(0, this.height / 4, 0, this.height);
            
            let waveColor;
            if (wave === 0) waveColor = color1;
            else if (wave === 1) waveColor = color2;
            else waveColor = color3;

            const currentAlpha = (0.3 + (avgNorm * 0.5)) * alphaMultiplier;
            
            gradient.addColorStop(0, `rgba(${waveColor}, 0)`);
            gradient.addColorStop(0.5, `rgba(${waveColor}, ${currentAlpha})`);
            gradient.addColorStop(1, `rgba(${waveColor}, 0)`);

            this.ctx.fillStyle = gradient;
            this.ctx.globalCompositeOperation = isLight ? 'multiply' : 'lighter';
            this.ctx.fill();
        }
        
        this.ctx.globalCompositeOperation = 'source-over';
    }
    
    drawPlexus(avgNorm, bNorm, hNorm, isLight) {
        const cx = this.width / 2;
        const cy = this.height / 2;
        
        const color1 = isLight ? '41, 128, 185' : '0, 243, 255'; 
        const color2 = isLight ? '142, 68, 173' : '188, 19, 254';  
        
        const connectionDistance = 100 + (hNorm * 100);
        this.ctx.lineWidth = 0.5 + (avgNorm * 2);

        // OPTIMISATION 1 : On ne relie que les 70 premières particules (au lieu de 200)
        // Ça divise les calculs par 10 (de 40 000 à 4 900) !
        const maxConnectedParticles = Math.min(this.particles.length, 70);

        // Dessiner les connexions d'abord
        for (let i = 0; i < maxConnectedParticles; i++) {
            const p1 = this.particles[i];
            
            p1.angle += (p1.speed * 0.5) + (bNorm * 0.01);
            let r1 = p1.orbit * (1 + bNorm * 0.2);
            let x1 = cx + Math.cos(p1.angle) * r1;
            let y1 = cy + Math.sin(p1.angle + p1.offset) * r1;

            for (let j = i + 1; j < maxConnectedParticles; j++) {
                const p2 = this.particles[j];
                
                let r2 = p2.orbit * (1 + bNorm * 0.2);
                let x2 = cx + Math.cos(p2.angle) * r2;
                let y2 = cy + Math.sin(p2.angle + p2.offset) * r2;

                // Optimisation mathématique (on évite le Math.sqrt tant qu'on n'est pas sûr)
                const dx = x1 - x2;
                const dy = y1 - y2;
                // Si la distance au carré est plus grande que la connexion max au carré, on ignore direct
                if (dx*dx + dy*dy > connectionDistance*connectionDistance) continue;

                const distance = Math.sqrt(dx * dx + dy * dy);
                const opacity = 1 - (distance / connectionDistance);
                
                // OPTIMISATION 2 : Plus de gradient linéaire coûteux. On utilise juste la couleur du point 1.
                const c1 = i % 2 === 0 ? color1 : color2;
                
                this.ctx.beginPath();
                this.ctx.strokeStyle = `rgba(${c1}, ${opacity * (0.3 + avgNorm)})`;
                this.ctx.moveTo(x1, y1);
                this.ctx.lineTo(x2, y2);
                this.ctx.stroke();
            }
        }

        // On dessine quand même TOUS les points (les 200), c'est très léger
        for (let i = 0; i < this.particles.length; i++) {
            const p1 = this.particles[i];
            
            // On ne met à jour l'angle que pour les particules > 70 (les 70 premières ont déjà été mises à jour)
            if (i >= maxConnectedParticles) {
                p1.angle += (p1.speed * 0.5) + (bNorm * 0.01);
            }
            
            let r1 = p1.orbit * (1 + bNorm * 0.2);
            let x1 = cx + Math.cos(p1.angle) * r1;
            let y1 = cy + Math.sin(p1.angle + p1.offset) * r1;

            const dotColor = i % 2 === 0 ? color1 : color2;
            this.ctx.beginPath();
            this.ctx.fillStyle = `rgba(${dotColor}, ${0.5 + hNorm})`;
            this.ctx.arc(x1, y1, p1.baseR * (1 + hNorm * 3), 0, Math.PI * 2);
            this.ctx.fill();
        }
    }


    drawMandala(avgNorm, bNorm, isLight) {
        const cx = this.width / 2;
        const cy = this.height / 2;
        
        // NOUVEAU : La vitesse de base est de 0.002. 
        // Les moyennes et hautes fréquences l'accélèrent jusqu'à 0.02.
        const rotationSpeed = 0.002 + (avgNorm * 0.01);
        this.mandalaRotation += rotationSpeed;

        const color1 = isLight ? '41, 128, 185' : '188, 19, 254'; 
        const color2 = isLight ? '39, 174, 96' : '0, 243, 255';   

        // --- Les particules en arrière-plan ---
        this.ctx.save();
        this.particles.forEach((p, idx) => {
            p.angle += (p.speed * 0.3); 
            let pRadius = 150 + p.orbit + (bNorm * 20); 
            let x = cx + Math.cos(p.angle) * pRadius;
            let y = cy + Math.sin(p.angle + p.offset) * pRadius;
            
            const alpha = 0.2 + Math.sin(this.mandalaRotation * 5 + idx) * 0.1 + (avgNorm * 0.2);
            const pColor = idx % 2 === 0 ? color1 : color2;

            this.ctx.beginPath();
            this.ctx.fillStyle = `rgba(${pColor}, ${Math.max(0, alpha)})`;
            this.ctx.arc(x, y, p.baseR * 0.8, 0, Math.PI * 2);
            this.ctx.fill();
        });
        this.ctx.restore();

        // --- LE MANDALA ---
        this.ctx.save();
        this.ctx.translate(cx, cy);
        
        // NOUVEAU : On applique la rotation globale dynamique
        this.ctx.rotate(this.mandalaRotation); 

        const numPetals = 12; 
        const radius = 100 + (bNorm * 150) + (avgNorm * 50);

        for (let layer = 0; layer < 3; layer++) {
            const currentRadius = radius * (1 - layer * 0.25);
            const layerColor = layer % 2 === 0 ? color1 : color2;
            
            // Rotation spécifique à chaque couche pour l'effet engrenage
            this.ctx.rotate(this.mandalaRotation * (layer % 2 === 0 ? 1 : -1) * 0.5);

            for (let i = 0; i < numPetals; i++) {
                this.ctx.rotate((Math.PI * 2) / numPetals);
                
                this.ctx.beginPath();
                this.ctx.moveTo(0, 0);
                
                const controlX = currentRadius * 0.5 * (1 + bNorm);
                
                this.ctx.quadraticCurveTo(controlX, currentRadius * 0.5, 0, currentRadius);
                this.ctx.quadraticCurveTo(-controlX, currentRadius * 0.5, 0, 0);
                
                this.ctx.strokeStyle = `rgba(${layerColor}, ${0.3 + avgNorm * 0.5})`;
                this.ctx.lineWidth = 2;
                this.ctx.stroke();

                this.ctx.fillStyle = `rgba(${layerColor}, ${0.05 + bNorm * 0.1})`;
                this.ctx.fill();
            }
        }
        
        // Le cœur de la fleur
        this.ctx.beginPath();
        this.ctx.arc(0, 0, 20 + bNorm * 30, 0, Math.PI * 2);
        this.ctx.fillStyle = `rgba(${color1}, ${0.5 + bNorm})`;
        this.ctx.fill();

        this.ctx.restore();
    }
    stop() {
        this.isRunning = false;
        // On efface tout pour être propre
        if (this.ctx) this.ctx.clearRect(0, 0, this.width, this.height);
        if (this.gl) {
            this.gl.clearColor(0, 0, 0, 1); // Noir complet
            this.gl.clear(this.gl.COLOR_BUFFER_BIT);
        }
    }

    start() {
        if (!this.isRunning) {
            this.isRunning = true;
            this.draw();
        }
    }
}