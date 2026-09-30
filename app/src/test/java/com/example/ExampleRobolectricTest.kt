package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.tvremote.domain.model.PushMediaType
import com.example.tvremote.domain.model.PushToTvPayload
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.RemoteMacro
import com.example.tvremote.domain.model.TvDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nova Remote", appName)
  }

  @Test
  fun `verify standard android TV keycodes`() {
    assertEquals(19, RemoteCommand.DpadUp.androidKeyCode)
    assertEquals(20, RemoteCommand.DpadDown.androidKeyCode)
    assertEquals(21, RemoteCommand.DpadLeft.androidKeyCode)
    assertEquals(22, RemoteCommand.DpadRight.androidKeyCode)
    assertEquals(23, RemoteCommand.DpadCenter.androidKeyCode)
    assertEquals(3, RemoteCommand.Home.androidKeyCode)
    assertEquals(4, RemoteCommand.Back.androidKeyCode)
    assertEquals(24, RemoteCommand.VolumeUp.androidKeyCode)
    assertEquals(25, RemoteCommand.VolumeDown.androidKeyCode)
    assertEquals(26, RemoteCommand.PowerToggle.androidKeyCode)
    assertEquals(85, RemoteCommand.PlayPause.androidKeyCode)
    assertEquals(166, RemoteCommand.ChannelUp.androidKeyCode)
    assertEquals(167, RemoteCommand.ChannelDown.androidKeyCode)
  }

  @Test
  fun `verify universal push to tv payload parsing`() {
    val ytPayload = PushToTvPayload.parse("Check this video: https://youtu.be/dQw4w9WgXcQ")
    assertEquals(PushMediaType.YOUTUBE, ytPayload.mediaType)
    assertEquals("https://youtu.be/dQw4w9WgXcQ", ytPayload.targetUri)

    val webPayload = PushToTvPayload.parse("https://en.wikipedia.org/wiki/Television")
    assertEquals(PushMediaType.WEB_BROWSER, webPayload.mediaType)

    val textPayload = PushToTvPayload.parse("SecretPassword123!")
    assertEquals(PushMediaType.TEXT_INPUT, textPayload.mediaType)
    assertEquals("SecretPassword123!", textPayload.targetUri)
  }

  @Test
  fun `verify preset macros available including youtube ad skipper`() {
    val presets = RemoteMacro.defaultPresets()
    assertTrue(presets.isNotEmpty())
    assertNotNull(presets.find { it.id == "preset_cinema_mode" })
    assertNotNull(presets.find { it.id == "preset_mi_patchwall" })
    assertNotNull(presets.find { it.id == "preset_skip_youtube_ad" })
  }

  @Test
  fun `verify Xiaomi Mi Stick device classification`() {
    val miDevice = TvDevice(
        id = "mi_1",
        name = "Mi TV Stick",
        ipAddress = "192.168.1.50",
        isMiStick = true
    )
    assertTrue(miDevice.displaySubtitle.contains("Xiaomi Mi TV Stick"))
  }
}
