package com.ziggeo.androidsdk.demo.ui.custom

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentCustomVideoPlayerBinding
import com.ziggeo.androidsdk.demo.di.DI
import com.ziggeo.androidsdk.demo.presentation.custom.CustomModeVideoPresenter
import com.ziggeo.androidsdk.demo.ui.global.BaseScreenFragment

class CustomModeVideoFragment :
    BaseScreenFragment<CustomModeVideoView,
            CustomModeVideoPresenter>(), CustomModeVideoView {

    override val parentScopeName = DI.APP_SCOPE

    override val layoutRes: Int
        get() = R.layout.fragment_custom_video_player

    @InjectPresenter
    lateinit var presenter: CustomModeVideoPresenter
    private var _binding: FragmentCustomVideoPlayerBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomVideoPlayerBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ProvidePresenter
    override fun providePresenter(): CustomModeVideoPresenter =
        scope.getInstance(CustomModeVideoPresenter::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.zVideoView?.loadConfigs()
        _binding?.zVideoView?.initViews()

        if (_binding?.zVideoView?.callback != null) {
            _binding?.zVideoView?.callback!!.loaded()
        }
    }

    override fun onPause() {
        super.onPause()
        _binding?.zVideoView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        _binding?.zVideoView?.onResume()
    }

    override fun startVideo(token: String) {
        _binding?.zVideoView?.videoTokens = listOf(token)
        _binding?.zVideoView?.prepareQueueAndStartPlaying()
    }

    override fun startVideoFile(path: Uri) {
        _binding?.zVideoView?.videoUris = listOf(path)
        _binding?.zVideoView?.prepareQueueAndStartPlaying()
    }

}