package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GAME TURBO PRO", appName)
  }

  @Test
  fun `test GameProfile JSON serialization and deserialization`() {
    val profile = GameProfile("com.activision.callofduty.shooter", "Call of Duty: Mobile")
    profile.dpiPreference = 440
    profile.brightnessPercent = 90
    profile.refreshRateHz = 120
    profile.isKeepScreenAwake = true
    profile.sensitivityNotes = "Camera: 85%"

    val json = profile.toJson()
    val restored = GameProfile.fromJson(json)

    assertEquals(profile.packageName, restored.packageName)
    assertEquals(profile.gameTitle, restored.gameTitle)
    assertEquals(440, restored.dpiPreference)
    assertEquals(90, restored.brightnessPercent)
    assertEquals(120, restored.refreshRateHz)
    assertEquals("Camera: 85%", restored.sensitivityNotes)
  }

  @Test
  fun `test DpiManager presets and adb command`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dpiManager = DpiManager(context)
    val presets = dpiManager.presets

    org.junit.Assert.assertFalse(presets.isEmpty())
    val cmd = dpiManager.getAdbCommand(440)
    assertEquals("adb shell wm density 440", cmd)
    val resetCmd = dpiManager.adbResetCommand
    assertEquals("adb shell wm density reset", resetCmd)
  }
}
