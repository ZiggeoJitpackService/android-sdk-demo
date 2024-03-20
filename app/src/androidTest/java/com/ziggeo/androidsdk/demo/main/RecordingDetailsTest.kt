package com.ziggeo.androidsdk.demo.main

import android.os.SystemClock
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.di.DI
import com.ziggeo.androidsdk.demo.model.data.storage.Prefs
import com.ziggeo.androidsdk.demo.ui.AppActivity
import com.ziggeo.androidsdk.demo.util.nthChildOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import toothpick.Toothpick

@RunWith(AndroidJUnit4ClassRunner::class)
class RecordingDetailsTest : BaseTest() {

    @Rule
    @JvmField
    val rule = ActivityTestRule(AppActivity::class.java)

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


//    @Test
//    fun testDrawerOpen() {
//        // closed by default
//        Espresso.onView(ViewMatchers.withId(R.id.drawer))
//            .check(ViewAssertions.matches(DrawerMatchers.isClosed(Gravity.LEFT)))
//
//        // click on toolbar icon
//        Espresso.onView(
//            nthChildOf(
//                ViewMatchers.withId(R.id.toolbar),
//                0
//            )
//        ).perform(ViewActions.click())
//
//        // make sure drawer is opened after the click
//        Espresso.onView(ViewMatchers.withId(R.id.drawer))
//            .check(ViewAssertions.matches(DrawerMatchers.isOpen(Gravity.LEFT)))
//    }

    @Test
    fun testContent() {
        SystemClock.sleep(4000)
        onScreen<RecordingsScreen> {
            rvRecordings {
                this.firstChild<RecordingsScreen.NestedItem> {
                    this.ivIcon.click()
                    SystemClock.sleep(3000)
                }
            }
        }

        onScreen<RecordingDetailsScreen> {
            ivPreview.isDisplayed()
            etTokenOrKey.isDisplayed()
            etTitle.isDisplayed()
            etDescription.isDisplayed()
        }
    }

    @Test
    fun testEditMode() {
        SystemClock.sleep(4000)
        onScreen<RecordingsScreen> {
            rvRecordings {
                this.firstChild<RecordingsScreen.NestedItem> {
                    this.ivIcon.click()
                    SystemClock.sleep(3000)
                }
            }
        }

        onScreen<RecordingDetailsScreen> {
            ivPreview.isDisplayed()
            etTokenOrKey.isDisplayed()
            etTitle.isDisplayed()
            etDescription.isDisplayed()

            etTokenOrKey.isDisabled()
            etTitle.isDisabled()
            etDescription.isDisabled()

            Espresso.onView(
                nthChildOf(
                    ViewMatchers.withId(R.id.toolbar),
                    1
                )
            ).perform(ViewActions.click())

            etTokenOrKey.isEnabled()
            etTitle.isEnabled()
            etDescription.isEnabled()
        }
    }

    @Test
    fun testSavingEditedData() {
        SystemClock.sleep(4000)
        onScreen<RecordingsScreen> {
            rvRecordings {
                this.firstChild<RecordingsScreen.NestedItem> {
                    this.ivIcon.click()
                    SystemClock.sleep(3000)
                }
            }
        }

        onScreen<RecordingDetailsScreen> {
            ivPreview.isDisplayed()
            etTokenOrKey.isDisplayed()
            etTitle.isDisplayed()
            etDescription.isDisplayed()

            Espresso.onView(
                nthChildOf(
                    ViewMatchers.withId(R.id.toolbar),
                    1
                )
            ).perform(ViewActions.click())

            Espresso.onView(ViewMatchers.withId(R.id.et_title))
                .perform(ViewActions.typeText("some nice title"))

            Espresso.onView(
                nthChildOf(
                    ViewMatchers.withId(R.id.toolbar),
                    1
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(1000)

            Espresso.onView(
                nthChildOf(
                    ViewMatchers.withId(R.id.toolbar),
                    0
                )
            ).perform(ViewActions.click())
        }

        SystemClock.sleep(4000)
        onScreen<RecordingsScreen> {
            rvRecordings {
                this.firstChild<RecordingsScreen.NestedItem> {
                    this.ivIcon.click()
                    SystemClock.sleep(3000)
                }
            }
        }

        onScreen<RecordingDetailsScreen> {
            ivPreview.isDisplayed()
            etTokenOrKey.isDisplayed()
            etTitle.isDisplayed()
            etDescription.isDisplayed()

            //todo
//            etTitle.hasAnyText()
        }

    }

    @Test
    fun testDeleting() {
        SystemClock.sleep(4000)
        onScreen<RecordingsScreen> {
            rvRecordings {
                this.firstChild<RecordingsScreen.NestedItem> {
                    this.ivIcon.click()
                    SystemClock.sleep(1000)
                }
            }
        }

        onScreen<RecordingDetailsScreen> {
            onView(withId(R.id.item_delete))
                .perform(click())
            onView(withText(R.string.common_confirm_message)).check(matches(isDisplayed()))
            onView(withId(android.R.id.button1)).perform(click());
            SystemClock.sleep(1000)
        }

        onScreen<RecordingsScreen> {
            isDisplayed()
        }
    }

}