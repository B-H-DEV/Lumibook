package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundscapeOpusPlayer
import com.example.audio.soundscapeTracks
import com.example.data.AmbianceMode
import com.example.data.ReaderPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SoundscapePlayerTest {

    @Test
    fun `verify 3 opus soundscape tracks are defined`() {
        assertEquals(3, soundscapeTracks.size)
        assertEquals("track_1.opus", soundscapeTracks[0].fileName)
        assertEquals("track_2.opus", soundscapeTracks[1].fileName)
        assertEquals("track_3.opus", soundscapeTracks[2].fileName)
    }

    @Test
    fun `verify soundscape opus player initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val player = SoundscapeOpusPlayer(context)
        assertNotNull(player)
        assertFalse(player.isPlaying.value)
        assertEquals(0, player.currentTrackIndex.value)
        player.release()
    }

    @Test
    fun `verify ambiance mode preferences persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ReaderPreferences(context)

        prefs.ambianceMode = AmbianceMode.SOUNDSCAPE
        assertEquals(AmbianceMode.SOUNDSCAPE, prefs.ambianceMode)

        prefs.ambianceMode = AmbianceMode.NONE
        assertEquals(AmbianceMode.NONE, prefs.ambianceMode)

        prefs.ambianceMode = AmbianceMode.RELAX_BOX
        assertEquals(AmbianceMode.RELAX_BOX, prefs.ambianceMode)
    }
}
