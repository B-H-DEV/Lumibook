export class BreathingEngine {
    constructor(appRef) {
        this.app = appRef; 
        this.isActive = false;
        
        // Patterns [Inhale, Hold In, Exhale, Hold Out] (en millisecondes)
        this.patterns = {
            'coherence': { name: "Cohérence Cardiaque", times: [5000, 0, 5000, 0] },
            'box':       { name: "Box Breathing", times: [4000, 4000, 4000, 4000] },
            'sleep':     { name: "Relaxation 4-7-8", times: [4000, 7000, 8000, 0] }
        };
        
        this.currentPattern = 'coherence';
        this.useAudioGuide = true;
        
        // Variables d'état en temps réel
        this.phaseIndex = 0; // 0=Inhale, 1=HoldIn, 2=Exhale, 3=HoldOut
        this.phaseStartTime = 0;
        this.animationId = null;
        this.currentProgress = 0;

        // Callbacks optionnels (UI attachée)
        this.onPhaseChange = null; 
        this.onTick = null; 
        this.onFinish = null; 
    }

    start(patternId = 'coherence', useAudio = true, durationSec = 0) {
        this.currentPattern = patternId;
        this.useAudioGuide = useAudio;
        this.durationSec = durationSec;
        this.isActive = true;
        this.phaseIndex = -1; // Pour forcer la transition initiale
        
        const now = performance.now();
        this.overallStartTime = now;
        
        this.nextPhase(now);
        this.tick();
    }

    stop() {
        this.isActive = false;
        if (this.animationId) cancelAnimationFrame(this.animationId);

        // Réinitialiser les volumes si l'audio était activé
        if (this.app.audio && this.app.audio.resetBreathingGuide) {
            this.app.audio.resetBreathingGuide();
        }
    }

    nextPhase(now) {
        this.phaseIndex = (this.phaseIndex + 1) % 4;
        this.phaseStartTime = now;
        
        const phaseTimes = this.patterns[this.currentPattern].times;
        
        // Si la phase dure 0s (ex: pas de rétention), on passe directement à la suivante
        if (phaseTimes[this.phaseIndex] === 0) {
            this.nextPhase(now);
            return;
        }

        // --- ICI on déclenche les événements de phase ---
        this.triggerPhaseEvents(this.phaseIndex);
    }

    tick() {
        if (!this.isActive) return;
        
        const now = performance.now();

        // 1. Check overview duration
        if (this.durationSec > 0) {
            const totalElapsed = (now - this.overallStartTime) / 1000;
            if (totalElapsed >= this.durationSec) {
                this.stop();
                if (this.onFinish) this.onFinish();
                return;
            }
        }

        const phaseDuration = this.patterns[this.currentPattern].times[this.phaseIndex];
        const elapsed = now - this.phaseStartTime;
        
        if (elapsed >= phaseDuration) {
            this.nextPhase(now);
        } else {
            // progress va de 0.0 à 1.0 au cours de la phase actuelle
            this.currentProgress = elapsed / phaseDuration;
            
            // --- ICI on transmettra le 'progress' au WebGL et à l'Audio ---
            this.updateVisualsAndAudio(this.phaseIndex, this.currentProgress);
        }
        
        this.animationId = requestAnimationFrame(() => this.tick());
    }
    
    triggerPhaseEvents(phaseIndex) {
        const labels = ["Inspirer", "Retenir", "Expirer", "Retenir"];
        
        // Logique audio de clochette (seulement si le guide audio est activé)
        if (this.useAudioGuide && this.app.audio && this.app.audio.triggerBell) {
            this.app.audio.triggerBell();
        }

        // Fait remonter l'évènement pour l'interface utilisateur
        if (this.onPhaseChange) {
            this.onPhaseChange(phaseIndex, labels[phaseIndex]);
        }
    }
    
    updateVisualsAndAudio(phaseIndex, progress) {
        // Envoi des infos à l'AudioEngine pour la "vague" de volume/pitch
        if (this.useAudioGuide && this.app.audio && this.app.audio.updateBreathingGuide) {
            this.app.audio.updateBreathingGuide(phaseIndex, progress);
        }

        // Fait remonter le 'tick' pour les visuels WebGL (visuals.js n'a besoin de connaître que phaseIndex & progress)
        if (this.onTick) {
            this.onTick(phaseIndex, progress);
        }
    }
}
