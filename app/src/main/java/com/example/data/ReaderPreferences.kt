package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists and restores all playback state, viewing position, and audio highlight
 * across app restarts, lifecycle changes, and pauses.
 */
class ReaderPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lumibook_reader_state", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_VIEWED_PAGE = "key_viewed_page"
        private const val KEY_AUDIOBOOK_PAGE = "key_audiobook_page"
        private const val KEY_AUDIOBOOK_POS_MS = "key_audiobook_pos_ms"
        private const val KEY_AUDIOBOOK_WAS_PLAYING = "key_audiobook_was_playing"
        private const val KEY_MUSIC_TRACK_INDEX = "key_music_track_index"
        private const val KEY_MUSIC_POS_MS = "key_music_pos_ms"
        private const val KEY_MUSIC_WAS_PLAYING = "key_music_was_playing"
        private const val KEY_MUSIC_VOLUME = "key_music_volume"
    }

    var viewedPage: Int
        get() = prefs.getInt(KEY_VIEWED_PAGE, 1)
        set(value) = prefs.edit().putInt(KEY_VIEWED_PAGE, value).apply()

    var audiobookPage: Int
        get() = prefs.getInt(KEY_AUDIOBOOK_PAGE, 1)
        set(value) = prefs.edit().putInt(KEY_AUDIOBOOK_PAGE, value).apply()

    var audiobookPositionMs: Int
        get() = prefs.getInt(KEY_AUDIOBOOK_POS_MS, 0)
        set(value) = prefs.edit().putInt(KEY_AUDIOBOOK_POS_MS, value).apply()

    var audiobookWasPlaying: Boolean
        get() = prefs.getBoolean(KEY_AUDIOBOOK_WAS_PLAYING, false)
        set(value) = prefs.edit().putBoolean(KEY_AUDIOBOOK_WAS_PLAYING, value).apply()

    var musicTrackIndex: Int
        get() = prefs.getInt(KEY_MUSIC_TRACK_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_MUSIC_TRACK_INDEX, value).apply()

    var musicPositionMs: Int
        get() = prefs.getInt(KEY_MUSIC_POS_MS, 0)
        set(value) = prefs.edit().putInt(KEY_MUSIC_POS_MS, value).apply()

    var musicWasPlaying: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_WAS_PLAYING, false)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC_WAS_PLAYING, value).apply()

    var musicVolume: Float
        get() = prefs.getFloat(KEY_MUSIC_VOLUME, 0.55f)
        set(value) = prefs.edit().putFloat(KEY_MUSIC_VOLUME, value).apply()

    fun saveFullState(
        currentReadingPage: Int,
        audioPage: Int,
        audioPosMs: Int,
        audioPlaying: Boolean,
        trackIdx: Int,
        trackPosMs: Int,
        musicPlaying: Boolean,
        volume: Float
    ) {
        prefs.edit()
            .putInt(KEY_VIEWED_PAGE, currentReadingPage)
            .putInt(KEY_AUDIOBOOK_PAGE, audioPage)
            .putInt(KEY_AUDIOBOOK_POS_MS, audioPosMs)
            .putBoolean(KEY_AUDIOBOOK_WAS_PLAYING, audioPlaying)
            .putInt(KEY_MUSIC_TRACK_INDEX, trackIdx)
            .putInt(KEY_MUSIC_POS_MS, trackPosMs)
            .putBoolean(KEY_MUSIC_WAS_PLAYING, musicPlaying)
            .putFloat(KEY_MUSIC_VOLUME, volume)
            .apply()
    }
}
