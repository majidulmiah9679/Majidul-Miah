# JARVIS OS - Tactical Autonomous Assistant

JARVIS is an AI-powered voice & system automation assistant for Android with real-time Gemini AI integration, Accessibility automation, speech recognition, and system diagnostics.

---

## 🚀 How to Build APK on GitHub (GitHub Actions)

This repository includes a pre-configured GitHub Actions workflow in `.github/workflows/build-apk.yml` to automatically build your APK!

### Step 1: Push Repository to GitHub
1. Create a new GitHub repository (Public or Private).
2. Push your project code to the `main` or `master` branch:
   ```bash
   git init
   git add .
   git commit -m "Initial JARVIS commit"
   git branch -M main
   git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPO>.git
   git push -u origin main
   ```

### Step 2: Automatic APK Generation
- Once pushed, click the **Actions** tab in your GitHub repository.
- You will see the **Build Android APK** workflow running.
- When finished (takes ~2 minutes), scroll down to **Artifacts** and click **jarvis-assistant-debug-apk** to download your installable APK!

---

## 🛠 Local Build (Command Line)
To build the debug APK locally:
```bash
./gradlew assembleDebug
```
The resulting APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Real Features Configured

1. **Gemini AI Integration (`GeminiManager`)**:
   - Supports Gemini 2.5 Flash.
   - Dynamic real-time API Key validation with visual Green/Red indicators.
   - Natural Bengali / Banglish dialogue system addressing you as "Boss".

2. **Full System Accessibility Service (`JarvisAccessibilityService`)**:
   - Tap coordinates (`performClick`)
   - Swipes & gestures (`performSwipe`)
   - Text node search & click (`clickNodeByText`)
   - Automated text input typing (`typeText`)
   - System Back, Home (`goToHomeScreen`), and Lock Screen (`lockScreen`)

3. **Notification Listener Service (`JarvisNotificationService`)**:
   - Live notification interception for sender, title, and text content (e.g., WhatsApp, SMS).

4. **Interactive UI & Voice**:
   - Real-time speech-to-text recognition with Bengali (`bn-IN`) support.
   - Real-time Text-to-Speech (TTS) audio playback.
   - Real-time live Battery & RAM telemetry monitor (`JarvisDiagnostics`).
   - One-tap direct shortcuts to Accessibility Settings, Notification Access, Overlay Permission, and Battery Optimization.
