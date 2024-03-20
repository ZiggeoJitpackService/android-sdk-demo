package com.ziggeo.androidsdk.demo.main

import android.Manifest
import android.os.Build
import android.os.SystemClock
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.ziggeo.androidsdk.demo.di.DI
import com.ziggeo.androidsdk.demo.model.data.storage.Prefs
import com.ziggeo.androidsdk.demo.ui.AppActivity
import org.junit.*
import org.junit.runner.RunWith
import toothpick.Toothpick


@RunWith(AndroidJUnit4ClassRunner::class)
class CameraRecorderTest : BaseTest() {

    @Rule
    @JvmField
    val rule = ActivityTestRule(AppActivity::class.java)

    @Rule
    @JvmField
    var grantPermissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= 33) {
            grant(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (Build.VERSION.SDK_INT <= 29) {
            grant(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            grant(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        }

    @Before
    fun before() {
        // make sure prefs has store token before launch
        // this will allow to navigate to the main screen
        val scope = Toothpick.openScope(DI.APP_SCOPE)
        prefs = scope.getInstance(Prefs::class.java)
        prefs.appToken = AuthScreenTest.TOKEN
//        prefs.appToken = BuildConfig.APP_TOKEN
        prefs.startDelay = 0
    }

    @Test
    fun dumbTest() {

    }

    @Test
    fun testRecorder() {
        onScreen<RecordingsScreen> {
            btnShowActions.click()
            btnCameraRecorder.click()
        }
        SystemClock.sleep(1000)
        onScreen<CameraRecorderScreen> {
            btnStartStop.isVisible()
            btnStartStop.click()
            SystemClock.sleep(5000)
            btnStartStop.click()
            SystemClock.sleep(500)
            btnPlay.click()
            SystemClock.sleep(3000)
            pressBack()
            SystemClock.sleep(500)
            btnSendAndClose.click()
        }
        onScreen<RecordingsScreen> {
            btnShowActions.isVisible()
            SystemClock.sleep(2000)
            btnShowActions.isVisible()
        }

    }
    @Test
    fun testRecorderCheck() {
        onScreen<RecordingsScreen> {
            btnShowActions.click()
            btnCameraRecorder.click()
        }
        SystemClock.sleep(1000)
        onScreen<CameraRecorderScreen> {
            btnStartStop.isVisible()
            btnStartStop.click()
            SystemClock.sleep(5000)
            btnSendAndClose.click()
            SystemClock.sleep(2000)
        }
        onScreen<RecordingsScreen> {
            btnShowActions.isVisible()
            SystemClock.sleep(2000)
            btnShowActions.isVisible()
        }

    }

}
