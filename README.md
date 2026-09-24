# Lumibook — Application Officielle 📖✨

> *Livre interactif & immersif « Les Luminautes — La Voie de Lumière » — 100% Hors-ligne & Zéro Permission.*

[![Licence: MIT](https://img.shields.io/badge/Licence-MIT-blue.svg)](LICENSE)
[![Plateforme](https://img.shields.io/badge/Plateforme-Android%208.0%2B-green.svg)](https://android.com)
[![Permissions](https://img.shields.io/badge/Permissions-Z%C3%A9ro-brightgreen.svg)](#-confidentialit%C3%A9--permissions)
[![Hors-ligne](https://img.shields.io/badge/Hors--ligne-100%25-orange.svg)](#-fonctionnalit%C3%A9s)
[![F-Droid](https://img.shields.io/badge/F--Droid-Compatible-blue.svg)](fastlane/metadata/android)

---

## 📖 À propos de l'application

**Lumibook** est une application Android épurée et moderne développée en **Kotlin** et **Jetpack Compose**, spécialement conçue pour offrir une lecture sereine, contemplative et immersive du livre *« Les Luminautes — La Voie de Lumière »*.

L'application respecte scrupuleusement la vie privée de l'utilisateur : **aucune permission Android**, **aucun accès Internet**, **aucun compte requis**, **aucun traceur publicitaire ou télémétrique**.

---

## ✨ Fonctionnalités

- **Texte intégral** : Les 61 pages du livre officiel intégrées avec une typographie soignée et une navigation fluide par chapitres.
- **Ambiances sonores méditatives** : 4 compositions sonores relaxantes en boucle avec fondu enchaîné subtil (format audio Opus haute efficacité).
- **Narration audio & synchronisation** : Suivi mot par mot / phrase par phrase avec mise en valeur visuelle du texte lu.
- **Export & Consultation du PDF original** : Ouverture et partage sécurisé du livre PDF original *« Les Luminautes — La Voie de Lumière »* via un `FileProvider` Android standard.
- **Confort de lecture personnalisé** : Choix de palettes visuelles (Sombre, Nuit profonde, Sépia chaleureux, Papier clair), ajustement dynamique de la taille de la police et mémorisation automatique de votre progression.
- **100% Hors-ligne** : Tous les contenus (textes, pistes audio, couverture et PDF) sont directement intégrés dans l'application. Fonctionne parfaitement en mode avion.

---

## 🔒 Confidentialité & Permissions

L'application a été conçue selon un principe de minimalisme numérique absolu :

| Critère | État |
| :--- | :--- |
| **Permissions requises** | **0** (Aucune permission demandée dans `AndroidManifest.xml`) |
| **Accès Internet** | **Aucun** (Ne requiert ni n'utilise le réseau) |
| **Stockage externe** | **Aucun** (Utilise uniquement le bac à sable interne de l'application) |
| **Services d'arrière-plan** | **Aucun** (Aucune consommation de batterie hors utilisation) |
| **Traceurs / Publicités** | **0%** (Aucune bibliothèque tierce commerciale ou télémétrique) |

---

## 🛠️ Architecture & Technologies

- **Langage** : Kotlin 2.0+
- **Interface graphique** : Jetpack Compose & Material Design 3
- **Audio** : Moteur audio multi-flux natif Android `MediaPlayer` avec gestion de fondu sonore
- **Persistance locale** : `SharedPreferences` sécurisées pour enregistrer la page et vos préférences de confort
- **Export PDF** : `FileProvider` sécurisé avec partage par Intent Android standard

---

## 🚀 Compilation & Installation

### Prérequis
- JDK 17 ou 21
- Android SDK (API 34 / 35)

### Commandes Gradle

```bash
# Compiler le projet en mode Debug :
./gradlew assembleDebug

# Générer l'APK de Release optimisé :
./gradlew assembleRelease

# L'APK produit se trouve dans :
# app/build/outputs/apk/release/app-release.apk
```

Pour la publication sur F-Droid et Fastlane, les métadonnées officielles en français sont prêtes dans le dossier `fastlane/metadata/android/fr-FR/`.

---

## 📜 Licence & Droits

Ce projet est distribué sous licence libre **MIT**. Consultez le fichier [LICENSE](LICENSE) pour plus d'informations.  
Site officiel de l'œuvre : [https://luminautes.org](https://luminautes.org)
