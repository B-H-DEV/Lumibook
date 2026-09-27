        // --- SHADER 1 : SYNTHWAVE (Danilo) ---
        export const fsSynthwave = `
            precision mediump float;
            uniform vec2 u_resolution;
            uniform float u_time;
            uniform float u_bass; 
            uniform float u_mid;  
            uniform float u_isLight; 

            void main() {
                vec3 c = vec3(0.0);
                float t = u_time * 0.5 + u_bass * 2.0; 
                float l;
                float z = t;
                
                for(int i = 0; i < 3; i++) {
                    vec2 uv;
                    vec2 p = gl_FragCoord.xy / u_resolution.xy;
                    uv = p;
                    p -= 0.5;
                    p.x *= u_resolution.x / u_resolution.y;
                    
                    z += 0.07;
                    l = length(p);
                    
                    float distortion = sin(l * 9.0 - z - z) * (1.0 + u_bass * 0.5);
                    uv += p / l * (sin(z) + 1.0) * abs(distortion);
                    
                    c[i] = (0.01 + u_mid * 0.015) / length(fract(uv) - 0.5);
                }
                
                vec3 finalColor = c / l;
                if (u_isLight > 0.5) finalColor = 1.0 - finalColor * 0.8; 
                gl_FragColor = vec4(finalColor, 1.0);
            }
        `;

        // --- SHADER 2 : FRACTAL ---
        export const fsFractal = `
            precision highp float;
            uniform vec2 u_resolution;
            uniform float u_time;
            uniform float u_bass; 
            uniform float u_mid;  
            uniform float u_high; 
            uniform float u_isLight; 
            uniform float u_pastel; 

            float nearestMult(float v, float of) {
                float m = mod(v, of);
                v -= m * sign(of / 2.0 - m);
                return v - mod(v, of);
            }

            vec4 pal(float t) {
                return 0.5 + 0.5 * cos(6.283 * (t + vec4(0.0, 1.0, 2.0, 0.0) / 3.0));
            }

            void main() {
                vec2 R = u_resolution.xy;
                vec2 center = vec2(0.0);
                vec2 p;
                float M = max(R.x, R.y);
                vec2 uv = (gl_FragCoord.xy - 0.5 * R) / M / 0.7;
                float l = length(uv);
                
                float st = u_bass * 1.5 + u_mid * 0.4 + u_high * 0.2; 
                float sCircRad = 0.23 * (0.045 / 0.23); 
                float ds = (2.0 + 1.4 * st) * (0.045 / 0.23); 
                float ang, dist;
                
                vec4 o = vec4(0.05); 
                
                for(int i = 0; i < 64; i++) {
                    p = uv - center;
                    ang = atan(p.y, p.x);		
                    ang = nearestMult(ang, 24.0 * 6.2831853 / 360.0);     
                    center += sCircRad / (0.045 / 0.23) * vec2(cos(ang), sin(ang));
                    dist = distance(center, uv);

                    if(dist <= sCircRad) {
                        o += 30.0 * dist * pal(fract(dist / sCircRad + st + l));
                    }
                    sCircRad *= ds;
                }
                
                if (u_pastel > 0.5) {
                    // Adoucit les couleurs mais conserve le fond sombre intact (lié à l'intensité lumineuse originale)
                    float l = length(o.rgb);
                    o.rgb = mix(o.rgb, vec3(0.6, 0.8, 1.0) * l, 0.6);
                }

                if (u_isLight > 0.5) o = 1.0 - o * 0.8;
                gl_FragColor = vec4(o.rgb, 1.0);
            }
        `;
        // --- SHADER 3 : SONIC FUSION ---
        export const fsSonic = `
        precision highp float;
        uniform vec2 u_resolution;
        uniform vec2 u_mouse;
        uniform float u_time;
        uniform float u_bass;
        uniform float u_mid;
        uniform float u_high;
        uniform float u_isLight;

        #define MAX_STEPS 50.
        #define SURF_DIST .001
        #define SIN(x) (sin(x)*.5+.5)
        #define COS(x) (cos(x)*.5+.5)
        #define S(x) smoothstep(0.,1.,x)
        #define PI 3.1415
        #define HPI 1.5708

        float bigRadius = 1.;
        float smallRadius = .8;

        vec3 R(vec2 uv, vec3 p, vec3 l, vec3 up, float z) {
            vec3 f = normalize(l-p),
                r = normalize(cross(up, f)),
                u = cross(f, r),
                c = p+f*z,
                i = c + uv.x*r + uv.y*u,
                d = normalize(i-p);
            return d;
        }

        mat2 Rot(float a) {
            float s = sin(a);
            float c = cos(a);
            return mat2(c, -s, s, c);
        }

        // Fonction de remplacement pour simuler un spectre FFT avec tes variables audio
        float getAudio(float x) {
            if(x < 0.33) return mix(u_bass, u_mid, x*3.0);
            if(x < 0.66) return mix(u_mid, u_high, (x-0.33)*3.0);
            return mix(u_high, u_bass, (x-0.66)*3.0);
        }

        float GlowDist(vec3 p) {
            float x = atan(p.x, p.z)+PI;
            float fft = getAudio(fract(x/6.2831+.5));
            
            p.y += (fft-.5)*.4;
            
            float s = 1.+sin(x*4.+u_time)*.05;
            float circle = length(vec2(length(p.xz*s)-bigRadius, p.y));
            float d = (circle+smallRadius*.001);
            return d;
        }

        float D(vec3 p) {
            float circle = length(vec2(length(p.xz)-bigRadius, p.y));
            float d = -(circle-smallRadius);
            return d;
        }

        vec3 N(vec3 p, float eps) {
            vec2 e = vec2(eps,0);
            return normalize(
                vec3(
                D(p+e.xyy)-D(p-e.xyy),
                D(p+e.yxy)-D(p-e.yxy),
                D(p+e.yyx)-D(p-e.yyx)
                )
            );
        }

        vec2 TV(vec3 p) {
            float y = atan(length(p.xz)-bigRadius, p.y);
            float x = atan(p.x, p.z);
            return vec2(x, y); 
        }
                            
        void main() {
            vec2 m = u_mouse.xy / u_resolution.xy;
            vec2 uv = (gl_FragCoord.xy - 0.5 * u_resolution.xy) / u_resolution.y;
            
            // Si la souris n'a pas bougé, on centre par défaut
            if (length(u_mouse) < 1.0) m = vec2(0.5);

            float t = u_time * 2.0 - (m.x - 0.5) * 10.0;
            float t4 = t/4.;
            float t8 = t/8.;
            float t16 = t/16.;
            float t32 = t/32.;
            
            float fft = u_bass; // Éclair global basé sur les basses
            
            smallRadius = mix(.125, .8, S(SIN(t8)));
            
            // Torsion de l'écran
            float d = 1.-dot(uv,uv);
            float twist = 1.0; 
            uv *= Rot(d*2.*twist*sin(t16)*(1.-COS(t32)));
            
            // Setup de la caméra dans le tore
            vec3 ro = vec3(0., sin(t8)*.25*smallRadius, -bigRadius-sin(t4)*smallRadius*.3);
            float lookAngle = ((SIN(-t8)));
            vec3 lookDir = vec3(0, 0, 1);
            lookDir.xz *= Rot(lookAngle*HPI);
            float fov =  mix(.5, .1+COS(t32), 0.5 + 0.5*sin(u_time * 0.1));      
            vec3 rd = R(uv*Rot(t*.1), ro, ro+lookDir, vec3(0,1,0), fov);
            
            // Lancer de rayon (Ray march)
            float dO = 0.;
            float minDistC = 5.;
            for(float i=0.; i<MAX_STEPS; i++) {
                vec3 p = ro + dO*rd;
                float dS = D(p);
                float dC = GlowDist(p);
                
                minDistC = min(minDistC, dC);
                
                dO += min(dS, dC);
                if(dS<SURF_DIST) break;
            }
            
            vec3 col = vec3(0);
            
            // Position, normale et UVs
            vec3 p = ro + dO*rd;
            vec3 n = N(p,.01);
            vec2 tv = TV(p)/6.2831+.5;
            
            // Texture
            tv.y += t*.05;
            d = fract((tv.y+tv.x)*10.);
            float outline = smoothstep(.3, .28, abs(d-.5));
            float swirl = smoothstep(.25, .2, abs(d-.5));
            float hatchUv = (tv.y-tv.x)*10.*3.1415;
            float hatch = sin(hatchUv*20.+t4)*SIN(t4);
            hatch += sin(hatchUv+t)*SIN(t8);
            col += (outline-swirl*hatch)*.5;
            
            // Vignette
            col *= 1.-dot(uv,uv);
            
            // Inversion de couleur
            float bw = 1.-clamp(sin(length(uv)*1.-t*.1)*3., 0., 1.);
            col = mix(col, 1.-col, bw);
            
            // Effet 3D
            float fresnel = -dot(n, rd);
            fresnel *= fresnel;
            col *= mix(1., fresnel*fresnel, SIN(t8*.5674));
            
            // Ajout des couleurs de façon permanente
            vec3 c = cross(rd, n);
            c.xy *= Rot(t);
            c.zy *= Rot(t4);
            c = c*.5+.5;
            c = pow(c, vec3(1.+SIN(t4)*3.));
            col = mix(col, col*c*3., 1.0);
            
            // Ajout des éclairs (réactifs au son)
            float lightning = fft*(.1/(minDistC));
            vec3 lightningCol = lightning*mix(vec3(1.,.1,.1), vec3(.1, .1, 1.), bw);
            col += lightningCol * 1.0;
            
            if (u_isLight > 0.5) col = 1.0 - col * 0.8;
            
            gl_FragColor = vec4(col,1.0);
        }
        `;


        // --- SHADER 4 : LOTUS FRACTAL ---
        export const fsLotus = `
            precision highp float;
            uniform vec2 u_resolution;
            uniform float u_time;
            uniform float u_bass; 
            uniform float u_mid;  
            uniform float u_isLight; 
            uniform float u_pastel;

            void main() {
                vec2 R = u_resolution.xy;
                vec2 uv = (gl_FragCoord.xy - 0.5 * R) / min(R.x, R.y);
                
                // Le souffle commande l'ouverture de la fleur
                float breath = u_bass * 1.5 + u_mid * 0.5;
                
                float r = length(uv);
                float a = atan(uv.y, uv.x);
                vec3 col = vec3(0.0);
                
                // On boucle pour créer une vraie profondeur fractale (pétales imbriqués)
                for(float i=1.0; i<=5.0; i++) {
                    // Les couches intérieures rétrécissent et respirent plus fort
                    float scale = i * (1.0 - breath * 0.15); 
                    float rad = r * scale * 2.0;
                    
                    // Décaler doucement l'angle de chaque coquille
                    float angle = a + i * 3.1415 / 8.0 + u_time * 0.05 * (mod(i,2.0)==0.?-1.:1.);
                    
                    float petals = sin(angle * 8.0) * 0.5 + 0.5;
                    float shape = (0.2 + breath * (0.2 / i)) + petals * (0.15 + breath * 0.15 / i);
                    
                    float outline = abs(rad - shape);
                    float layer = smoothstep(0.03, 0.005, outline);
                    float glow = 0.003 / max(outline, 0.001);
                    
                    vec3 baseColor = vec3(0.8, 0.2, 0.5); // Fushia profond
                    vec3 tipColor = vec3(0.3, 0.1, 0.8);  // Violet profond
                    vec3 layerCol = mix(baseColor, tipColor, rad / i);
                    
                    col += layerCol * (layer + glow * 1.2) * (1.5 / i);
                }
                
                // Cœur brillant qui pulse intensément
                col += vec3(1.0, 0.7, 0.3) * (0.01 / max(r, 0.001)) * (0.5 + breath * 0.8);

                // Équilibrage des couleurs (pour éviter de brûler les pixels)
                col *= 0.6; // Assombrit globalement pour garder des contrastes riches

                if (u_pastel > 0.5) {
                    float l = length(col);
                    // On mélange doucement vers un équilibre pastel sans toucher au fond (où l ~ 0)
                    col = mix(col, vec3(0.6, 0.4, 0.8) * l * 1.5, 0.6);
                }

                // Balance jour/nuit plus douce (pas d'inversion blanche pure)
                if (u_isLight > 0.5) {
                    // En mode clair, on inverse mais on assombrit pour que ça ne devienne pas blanc pur
                    col = (1.0 - col) * 0.85; 
                }
                
                gl_FragColor = vec4(col, 1.0);
            }
        `;

        // --- SHADER 5 : ORBE ETHERE ---
        export const fsOrb = `
            precision highp float;
            uniform vec2 u_resolution;
            uniform float u_time;
            uniform float u_bass; 
            uniform float u_mid;  
            uniform float u_isLight; 
            uniform float u_pastel;

            // Simplex noise simple
            vec3 permute(vec3 x) { return mod(((x*34.0)+1.0)*x, 289.0); }
            float snoise(vec2 v){
                const vec4 C = vec4(0.211324865, 0.366025403, -0.577350269, 0.0243902439);
                vec2 i  = floor(v + dot(v, C.yy) );
                vec2 x0 = v -   i + dot(i, C.xx);
                vec2 i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
                vec4 x12 = x0.xyxy + C.xxzz;
                x12.xy -= i1;
                i = mod(i, 289.0);
                vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 )) + i.x + vec3(0.0, i1.x, 1.0 ));
                vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
                m = m*m; m = m*m;
                vec3 x = 2.0 * fract(p * C.www) - 1.0;
                vec3 h = abs(x) - 0.5;
                vec3 ox = floor(x + 0.5);
                vec3 a0 = x - ox;
                m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );
                vec3 g;
                g.x  = a0.x  * x0.x  + h.x  * x0.y;
                g.yz = a0.yz * x12.xz + h.yz * x12.yw;
                return 130.0 * dot(m, g);
            }

            void main() {
                vec2 R = u_resolution.xy;
                vec2 uv = (gl_FragCoord.xy - 0.5 * R) / min(R.x, R.y);
                
                float breath = u_bass * 1.5 + u_mid * 0.5;
                
                // Mouvement fluide interne de l'orbe
                float n = snoise(uv * 3.0 - vec2(0.0, u_time * 0.2));
                
                float r = length(uv);
                
                // Le rayon gonfle avec la respiration
                float radius = 0.2 + breath * 0.15;
                
                // Déformation nébuleuse du bord
                float deform = radius + n * (0.05 + breath * 0.05);

                float d = smoothstep(deform + 0.1, deform - 0.1, r);
                
                // Halo externe lumineux
                float halo = 0.02 / max(abs(r - deform), 0.01);
                
                vec3 col = vec3(0.0);
                
                // Couleurs éthérées type énergie vitale, tempérées
                vec3 coreColor = vec3(0.4, 0.6, 0.9);
                vec3 edgeColor = vec3(0.1, 0.3, 0.8);
                
                col = mix(edgeColor, coreColor, d) * (d + halo * 0.4);

                // Pulse lumineux discret
                col += vec3(0.1, 0.2, 0.4) * halo * breath;

                if (u_pastel > 0.5) {
                    float l = length(col);
                    col = mix(col, vec3(0.4, 0.6, 0.8) * l * 1.2, 0.7);
                }

                if (u_isLight > 0.5) {
                    col = (1.0 - col) * 0.85;
                }
                
                gl_FragColor = vec4(col, 1.0);
            }
        `;