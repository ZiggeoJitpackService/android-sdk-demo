package com.ziggeo.androidsdk.demo.main

import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.screen.Screen
import com.ziggeo.androidsdk.R

open class RecordingDetailsScreen : Screen<RecordingDetailsScreen>()  {
    val ivPreview = KImageView { withId(R.id.iv_preview) }
    val etTokenOrKey = KEditText { withId(com.ziggeo.androidsdk.demo.R.id.et_token_or_key) }
    val etTitle = KEditText { withId(com.ziggeo.androidsdk.demo.R.id.et_title) }
    val etDescription = KEditText { withId(com.ziggeo.androidsdk.demo.R.id.et_description) }
}