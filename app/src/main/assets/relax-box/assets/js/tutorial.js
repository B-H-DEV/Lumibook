export class TutorialManager {
    constructor() {
        this.steps = [
            { target: '#tracksContainer' },
            { target: '#autoShuffleBtn'  },
            { target: '#presetsBtn' },
            { target: '#globalSettingsBtn' },
            { target: '#breathingNavBtn' },
            { target: 'button[onclick="app.reset()"]' },
            { target: '#viewControls' }
        ];
        this.currentStep = 0;
        this.isActive = false;
        
        // DOM Elements
        this.welcomeOverlay = document.getElementById('tutorialWelcomeOverlay');
        this.spotlightOverlay = document.getElementById('tutorialSpotlightOverlay');
        this.tooltip = document.getElementById('tutorialTooltip');
        this.tooltipText = document.getElementById('tutorialTooltipText');
        this.nextBtn = document.getElementById('tutorialNextBtn');
        this.skipBtn = document.getElementById('tutorialSkipBtn');
        
        // Welcome View Buttons
        this.startBtn = document.getElementById('tutorialStartTourBtn');
        this.ignoreBtn = document.getElementById('tutorialIgnoreTourBtn');

        // Event Listeners
        if (this.nextBtn) this.nextBtn.addEventListener('click', () => this.nextStep());
        if (this.skipBtn) this.skipBtn.addEventListener('click', () => this.endTutorial());
        
        if (this.startBtn) this.startBtn.addEventListener('click', () => this.startTutorial());
        if (this.ignoreBtn) this.ignoreBtn.addEventListener('click', () => this.endTutorial());

        // Resize listener to recalculate spotlight
        window.addEventListener('resize', () => {
            if (this.isActive) {
                this.positionSpotlight();
            }
        });
    }

    checkAndStart() {
        if (!localStorage.getItem('relaxbox_tutorial_seen')) {
            if (this.welcomeOverlay) {
                this.welcomeOverlay.classList.remove('hidden');
                // Small timeout to allow css transition
                setTimeout(() => {
                    this.welcomeOverlay.style.opacity = '1';
                }, 50);
            }
        }
    }

    startTutorial() {
        if (this.welcomeOverlay) {
            this.welcomeOverlay.style.opacity = '0';
            setTimeout(() => {
                this.welcomeOverlay.classList.add('hidden');
            }, 500);
        }
        
        this.isActive = true;
        
        if (this.spotlightOverlay) {
            this.spotlightOverlay.classList.remove('hidden');
            // Give time for display:block to apply before animating opacity
            setTimeout(() => {
                this.spotlightOverlay.style.opacity = '1';
                this.showStep(this.currentStep);
            }, 50);
        }
    }

    endTutorial() {
        localStorage.setItem('relaxbox_tutorial_seen', 'true');
        this.isActive = false;
        
        if (this.welcomeOverlay) {
            this.welcomeOverlay.style.opacity = '0';
            setTimeout(() => {
                this.welcomeOverlay.classList.add('hidden');
            }, 500);
        }

        if (this.spotlightOverlay) {
            this.spotlightOverlay.style.opacity = '0';
            setTimeout(() => {
                this.spotlightOverlay.classList.add('hidden');
                this.spotlightOverlay.style.clipPath = '';
            }, 500);
        }
    }

    showStep(index) {
        if (index >= this.steps.length) {
            this.endTutorial();
            return;
        }

        const step = this.steps[index];
        if (this.tooltipText) {
            this.tooltipText.innerHTML = window.i18n.t('tutorial.steps')[index];
        }
        
        this.positionSpotlight();
        
        // Change button text on last step
        if (this.nextBtn) {
            if (index === this.steps.length - 1) {
                this.nextBtn.innerHTML = window.i18n.t('tutorial.finish') + ' <i data-lucide="check" class="w-4 h-4 ml-2"></i>';
            } else {
                this.nextBtn.innerHTML = window.i18n.t('tutorial.next') + ' <i data-lucide="chevron-right" class="w-4 h-4 ml-2"></i>';
            }
            if (window.lucide) {
                window.lucide.createIcons();
            }
        }
    }

    positionSpotlight() {
        const step = this.steps[this.currentStep];
        const targetEl = document.querySelector(step.target);
        
        if (!targetEl) {
            console.warn(`Tutorial target not found: ${step.target}`);
            return;
        }

        // Scroll to the element if it's inside tracksContainer or out of view
        targetEl.scrollIntoView({ behavior: 'smooth', block: 'center' });

        // Wait a tiny bit for the scroll event to settle, then compute bounds
        setTimeout(() => {
            const rect = targetEl.getBoundingClientRect();
            // A bit of padding
            const paddingX = 15;
            const paddingY = 15;

            const top = rect.top - paddingY;
            const left = rect.left - paddingX;
            const right = rect.right + paddingX;
            const bottom = rect.bottom + paddingY;

            // Clip-path with polygon to draw a "hole"
            // The outer box is drawn counter-clockwise: (0,0) -> (0,H) -> (W,H) -> (W,0) -> (0,0)
            // The inner hole is drawn clockwise
            if (this.spotlightOverlay) {
                this.spotlightOverlay.style.clipPath = `polygon(
                    0% 0%, 0% 100%, 100% 100%, 100% 0%, 0% 0%,
                    ${left}px ${top}px, ${right}px ${top}px, ${right}px ${bottom}px, ${left}px ${bottom}px, ${left}px ${top}px
                )`;
            }

            // Position Tooltip
            if (this.tooltip) {
                // Ensure tooltip fits on screen
                let tooltipTop = bottom + 20;
                let tooltipLeft = left + (rect.width / 2) - 160; // Assume 320px tooltip width
                
                if (tooltipTop + 200 > window.innerHeight) {
                    tooltipTop = top - 180; // above
                }
                
                if (tooltipLeft < 10) tooltipLeft = 10;
                if (tooltipLeft + 320 > window.innerWidth) tooltipLeft = window.innerWidth - 330;

                this.tooltip.style.top = `${tooltipTop}px`;
                this.tooltip.style.left = `${tooltipLeft}px`;
                this.tooltip.style.opacity = '1';
                this.tooltip.style.transform = 'translateY(0)';
            }
        }, 300); // 300ms is enough for smooth scroll
    }

    nextStep() {
        // Fade out tooltip slightly for transition effect
        if (this.tooltip) {
            this.tooltip.style.opacity = '0';
            this.tooltip.style.transform = 'translateY(10px)';
        }
        
        setTimeout(() => {
            this.currentStep++;
            this.showStep(this.currentStep);
        }, 150); // fast transition
    }
}
