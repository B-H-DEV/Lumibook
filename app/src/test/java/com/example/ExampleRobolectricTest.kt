package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BookRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lumibook", appName)
  }

  @Test
  fun `verify book repository has 64 pages`() {
    assertEquals(64, BookRepository.pages.size)
  }

  @Test
  fun `verify sentence start rule skips preceding incomplete fragment`() {
    val testText = "la fin de la phrase précédente. Voici la première phrase complète. Et la suivante."
    val result = BookRepository.getFirstCompleteSentence(testText)
    assertEquals("Voici la première phrase complète. Et la suivante.", result)
  }

  @Test
  fun `verify sentence start rule preserves uppercase starting text`() {
    val testText = "Ressentez-vous parfois ce léger décalage ? C'est le début."
    val result = BookRepository.getFirstCompleteSentence(testText)
    assertEquals(testText, result)
  }

  @Test
  fun `verify sepia theme surface is warm yellowish and not pure white`() {
    val sepiaColors = com.example.ui.theme.SepiaReaderColors
    // Surface should not be pure white
    val isNotWhite = sepiaColors.surface.red < 0.98f || sepiaColors.surface.green < 0.98f || sepiaColors.surface.blue < 0.98f
    assertTrue(isNotWhite)
  }

  @Test
  fun `verify ai voices available`() {
    val voices = com.example.audio.AiVoice.values()
    assertTrue(voices.any { it.id == "Aoede" })
    assertTrue(voices.any { it.id == "Charon" })
  }

  @Test
  fun `verify default reading speed is 1_0f natural`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val player = com.example.audio.AudiobookPlayer(context)
    assertEquals(1.0f, player.speechRateMultiplier.value, 0.01f)
    player.release()
  }
}
