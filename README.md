# Lumibook — Liseuse & Livre Audio Immersif 📖✨

> *L'application officielle pour le livre « Les Luminautes — La Voie de Lumière ».*  
> **100% Hors-ligne • Zéro Permission • Zéro Traqueur • Conforme F-Droid & Logiciel Libre.**

[![Licence: MIT](https://img.shields.io/badge/Licence-MIT-blue.svg)](LICENSE)
[![Plateforme](https://img.shields.io/badge/Plateforme-Android%207.0%2B%20(API%2024%2B)-green.svg)](https://android.com)
[![Permissions](https://img.shields.io/badge/Permissions-0%20(Aucune)-brightgreen.svg)](#-vie-privée--zéro-permission)
[![Hors-ligne](https://img.shields.io/badge/Hors--ligne-100%25-orange.svg)](#-fonctionnalités-clés)
[![F-Droid](https://img.shields.io/badge/F--Droid-Ready-blue.svg)](fastlane/metadata/android)

---

## 📖 Présentation

**Lumibook** est une application Android moderne conçue en **Kotlin** et **Jetpack Compose (Material 3)**. Elle propose une expérience de lecture et d'écoute contemplative, douce et poétique du livre *« Les Luminautes — La Voie de Lumière »*.

L'application est pensée dans un esprit de sobriété numérique : elle n'exige **aucune permission** (même pas l'accès à Internet), n'intègre **aucun SDK publicitaire ou de télémétrie**, et fonctionne de façon totalement autonome hors-ligne, que ce soit au quotidien ou en mode avion.

---

## ✨ Fonctionnalités Clés

### 📚 Lecture & Typographie
- **64 pages intégrales** du livre officiel des Luminautes avec structure par chapitres et table des matières interactive.
- **Thèmes de lecture confortables** :
  - **Sombre Cosmique** (bleu nuit profond `#0F172A`)
  - **Clair Épuré** (fond ivoire doux `#F8FAFC`)
  - **Sépia Chaleureux** (ton parchemin reposant `#FBF0D9`)
- **Taille de police ajustable** avec mise à l'échelle typographique fluide (sans déformation).
- **Mémorisation automatique** de la dernière page lue, du volume et du préréglage musical.

### 🎙️ Narration Vocale & Synchronisation Mot à Mot
- **Double moteur audio** : Voix studio naturelles embarquées ou moteur Text-To-Speech (TTS) natif Android.
- **Mise en valeur visuelle synchrone** : Le mot et le paragraphe en cours de lecture sont surlignés en temps réel avec défilement automatique doux et centré.
- **Vitesse de lecture paramétrable** (0.75x, 1.0x, 1.25x, 1.5x) sans distorsion de pitch.

### 🧘 Relax-Box Intégrée (Générateur Sonore & Visuel Méditatif)
- **Synthèse sonore procédurale 100% locale** : Générée via la Web Audio API (oscillateurs, générateurs de bruit blanc/rose/brun, filtres biquad, battements binauraux, carillons et bols tibétains).
- **Zéro streaming, zéro fichier audio externe lourd** : L'environnement sonore est synthétisé en temps réel sans nécessiter de connexion.
- **Visualisations génératives animées** : Mandalas sacrés, aurores polaires, particules flottantes et plexus interactif en Canvas HTML5.
- **Mode Plein Écran & Immersion** : Affichage épuré pour les séances de relaxation, respiration guidée et méditation.
- **Contrôle continu en arrière-plan** : Le paysage sonore accompagne la lecture du livre sans interruption.

### 📄 Export & Partage du PDF Officiel
- Fichier PDF haute définition inclus directement dans l'application (`assets/pdf/`).
- Partage et ouverture sécurisés vers les visionneuses externes via un `FileProvider` Android standard.

---

## 🔒 Vie Privée & Zéro Permission

Lumibook respecte scrupuleusement la souveraineté numérique de ses utilisateurs et satisfait les critères stricts d'inclusion **F-Droid** :

| Critère | Statut |
| :--- | :--- |
| **Permissions Android (`AndroidManifest.xml`)** | **0** (aucune permission requise, y compris `INTERNET`) |
| **Connexion Réseau** | **Désactivée** (100% des contenus et sons sont embarqués localement) |
| **Traceurs & Publicités** | **0%** (aucun traqueur, aucun SDK tiers propriétaire) |
| **Données Personnelles** | **Aucune collecte** (aucune création de compte, aucun identifiant unique) |
| **Anti-Features F-Droid** | **Aucune** (pas de publicité, pas de dépendance non-libre, pas de tracking) |

---

## 🛠️ Architecture & Pile Technique

- **Langage** : Kotlin 2.0+
- **UI Toolkit** : Jetpack Compose avec Material Design 3
- **Architecture** : Clean MVVM / Unidirectional Data Flow avec Kotlin `StateFlow` & Coroutines
- **Moteur Relax-Box** : Sandbox WebView Android locale (`file:///android_asset/relax-box/index.html`) avec pont bidirectionnel sécurisé `@JavascriptInterface`
- **Audiobook Player** : Android `MediaPlayer` pour l'audio local combiné à `android.speech.tts.TextToSpeech`
- **Stockage Local** : `SharedPreferences` sécurisées pour la sauvegarde de progression
- **Métadonnées Fastlane** : Disponibles dans `fastlane/metadata/android/` (fr-FR et en-US)

---

## 🚀 Compilation & Build F-Droid

### Prérequis
- Java Development Kit (JDK) 17 ou 21
- Android SDK (API 34 / 36)

### Commandes Gradle

```bash
# Vérifier et lancer les tests unitaires locaux (Robolectric)
./gradlew testDebugUnitTest

# Compiler l'application en mode Debug
./gradlew assembleDebug

# Produire l'APK de Release non signé ou signé
./gradlew assembleRelease
```

Le fichier APK produit est généré dans :
`app/build/outputs/apk/release/app-release.apk`

---

## 📦 Soumission F-Droid

Le dépôt est configuré pour une intégration directe dans le dépôt F-Droid :
1. Code source 100% ouvert sous licence libre MIT.
2. Structure standard `fastlane/metadata/android/` prête avec descriptions et titres en français et en anglais.
3. Aucune dépendance propriétaire ni binaire opaque.
4. Aucun service Google Play Services obligatoire.

---

## 📜 Licence

Ce projet est distribué sous licence libre **MIT**. Consultez le fichier [LICENSE](LICENSE) pour plus de détails.

- **Livre & Textes** : © Les Luminautes — [https://luminautes.org](https://luminautes.org)
- **Application** : Open source sous licence MIT
