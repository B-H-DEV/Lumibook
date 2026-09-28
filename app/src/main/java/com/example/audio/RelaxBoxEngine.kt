package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * RelaxBoxEngine manages the offline Relax-Box generative audio synthesizer
 * and ambient soundscape player running in background via Web Audio API.
 */
class RelaxBoxEngine(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    val volume = _volume.asStateFlow()

    private val _currentPreset = MutableStateFlow("global")
    val currentPreset = _currentPreset.asStateFlow()

    private val _isFullScreenOpen = MutableStateFlow(false)
    val isFullScreenOpen = _isFullScreenOpen.asStateFlow()

    private val _isImmersiveMode = MutableStateFlow(false)
    val isImmersiveMode = _isImmersiveMode.asStateFlow()

    private var webView: WebView? = null
    private val _webViewFlow = MutableStateFlow<WebView?>(null)
    val webViewFlow = _webViewFlow.asStateFlow()

    private var isEngineReady = false
    private var pendingPlayAfterReady = false
    private var pendingPresetAfterReady: String? = null

    init {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            initWebView()
        } else {
            mainHandler.post {
                initWebView()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        if (webView != null) return
        try {
            val wv = WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                setBackgroundColor(Color.TRANSPARENT)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                    allowContentAccess = true
                    allowFileAccessFromFileURLs = true
                    allowUniversalAccessFromFileURLs = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }

                addJavascriptInterface(AndroidJsBridge(), "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isEngineReady = true
                        Log.d("RelaxBoxEngine", "Relax-Box loaded successfully")
                        setVolume(_volume.value)

                        if (pendingPlayAfterReady) {
                            pendingPlayAfterReady = false
                            val preset = pendingPresetAfterReady ?: _currentPreset.value
                            loadPreset(preset)
                        }
                    }

                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                        Log.w("RelaxBoxEngine", "WebView render process gone (crashed=${detail?.didCrash()}). Recovering...")
                        try {
                            (webView?.parent as? ViewGroup)?.removeView(webView)
                            webView?.destroy()
                        } catch (e: Exception) {
                            Log.e("RelaxBoxEngine", "Error destroying webView after process gone", e)
                        }
                        webView = null
                        _webViewFlow.value = null
                        isEngineReady = false
                        mainHandler.postDelayed({
                            initWebView()
                        }, 400)
                        return true
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        _isImmersiveMode.value = true
                        callback?.onCustomViewHidden()
                    }

                    override fun onHideCustomView() {
                        _isImmersiveMode.value = false
                    }

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d(
                            "RelaxBoxEngine",
                            "JS [${consoleMessage?.messageLevel()}]: ${consoleMessage?.message()} -- line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}"
                        )
                        return true
                    }
                }
                loadUrl("file:///android_asset/relax-box/index.html")
            }
            webView = wv
            _webViewFlow.value = wv
        } catch (e: Exception) {
            Log.e("RelaxBoxEngine", "Failed to initialize WebView for Relax-Box", e)
        }
    }

    fun getWebView(): WebView? = webView

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (!isEngineReady) {
            pendingPlayAfterReady = true
            return
        }
        mainHandler.post {
            webView?.evaluateJavascript(
                "if (window.LumibookBridge) { window.LumibookBridge.playAutoMode('${_currentPreset.value}'); }",
                null
            )
        }
        _isPlaying.value = true
    }

    fun pause() {
        mainHandler.post {
            webView?.evaluateJavascript(
                "if (window.LumibookBridge) { window.LumibookBridge.pause(); }",
                null
            )
        }
        _isPlaying.value = false
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        mainHandler.post {
            webView?.evaluateJavascript(
                "if (window.LumibookBridge) { window.LumibookBridge.setVolume($clamped); }",
                null
            )
        }
    }

    /**
     * ANTI-POP : applique un volume cible SANS envoyer d'ordre au WebView.
     * Utilisé pour synchroniser l'état interne quand c'est Soundscape qui
     * pilote le volume, afin d'éviter que les deux moteurs se battent.
     */
    fun syncVolumeOnly(vol: Float) {
        _volume.value = vol.coerceIn(0f, 1f)
    }

    fun setAutoMode(mode: String) {
        val m = mode.lowercase()
        _currentPreset.value = m
        if (!isEngineReady) {
            pendingPlayAfterReady = true
            pendingPresetAfterReady = m
            return
        }
        mainHandler.post {
            webView?.evaluateJavascript(
                "if (window.LumibookBridge) { if (!window.LumibookBridge._started) { window.LumibookBridge.startEngine('$m'); } else { window.LumibookBridge.setAutoMode('$m'); } }",
                null
            )
        }
        _isPlaying.value = true
    }

    fun loadPreset(presetId: String) {
        val key = presetId.lowercase()
        if (key in listOf("global", "sleep", "focus", "zen")) {
            setAutoMode(key)
            return
        }
        _currentPreset.value = presetId
        if (!isEngineReady) {
            pendingPlayAfterReady = true
            pendingPresetAfterReady = presetId
            return
        }
        mainHandler.post {
            webView?.evaluateJavascript(
                "if (window.LumibookBridge) { if (!window.LumibookBridge._started) { window.LumibookBridge.startEngine('$presetId'); } else { window.LumibookBridge.loadPreset('$presetId'); } }",
                null
            )
        }
        _isPlaying.value = true
    }

    fun openFullScreen() {
        _isFullScreenOpen.value = true
        mainHandler.post {
            webView?.evaluateJavascript(
                "if (window.LumibookBridge) { if (!window.LumibookBridge._started) { window.LumibookBridge.startEngine('${_currentPreset.value}'); } else { window.LumibookBridge.setVisualsEnabled(true); } }",
                null
            )
        }
    }

    fun closeFullScreen() {
        _isFullScreenOpen.value = false
        _isImmersiveMode.value = false
    }

    fun toggleImmersiveMode() {
        _isImmersiveMode.value = !_isImmersiveMode.value
    }

    fun restoreState(initialVolume: Float, wasPlaying: Boolean, presetId: String) {
        _volume.value = initialVolume.coerceIn(0f, 1f)
        // On initialise le preset mais on NE RELANCE PAS la lecture (wasPlaying est ignoré)
        _currentPreset.value = "global" // On force le redémarrage à zéro comme demandé
        
        pendingPlayAfterReady = false
        pendingPresetAfterReady = null
        
        // Initialiser le moteur audio WebView sans lancer de mode
        mainHandler.post {
            webView?.evaluateJavascript(
                "if (window.LumibookBridge && !window.LumibookBridge._started) { window.LumibookBridge.startEngine(null); }",
                null
            )
        }
    }

    fun release() {
        mainHandler.post {
            try {
                webView?.evaluateJavascript(
                    "if (window.LumibookBridge) { window.LumibookBridge.pause(); }",
                    null
                )
                webView?.stopLoading()
                webView?.destroy()
                webView = null
                _webViewFlow.value = null
            } catch (e: Exception) {
                Log.w("RelaxBoxEngine", "Error releasing WebView: ${e.message}")
            }
        }
    }

    inner class AndroidJsBridge {
        @JavascriptInterface
        fun onStateChanged(playing: Boolean, vol: Float, preset: String) {
            mainHandler.post {
                _isPlaying.value = playing
                if (vol in 0f..1f) {
                    _volume.value = vol
                }
                if (preset.isNotBlank()) {
                    _currentPreset.value = preset
                }
            }
        }

        @JavascriptInterface
        fun toggleFullScreen() {
            mainHandler.post {
                _isImmersiveMode.value = !_isImmersiveMode.value
            }
        }

        @JavascriptInterface
        fun closeFullScreen() {
            mainHandler.post {
                _isFullScreenOpen.value = false
                _isImmersiveMode.value = false
            }
        }
    }
}
