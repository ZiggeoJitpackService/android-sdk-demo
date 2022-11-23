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
    private var _binding: FragmentCustomVideoPlayerBinding? = null
    private val binding get() = _binding!!

    override val parentScopeName = DI.APP_SCOPE

    override val layoutRes: Int
        get() = R.layout.fragment_custom_video_player

    @InjectPresenter
    lateinit var presenter: CustomModeVideoPresenter

    @ProvidePresenter
    override fun providePresenter(): CustomModeVideoPresenter =
        scope.getInstance(CustomModeVideoPresenter::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.zVideoView.loadConfigs()
        binding.zVideoView.initViews()

        if (binding.zVideoView.callback != null) {
            binding.zVideoView.callback.loaded()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.zVideoView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.zVideoView.onResume()
    }

    override fun startVideo(token: String) {
        binding.zVideoView.videoTokens = listOf(token)
        binding.zVideoView.prepareQueueAndStartPlaying()
    }

    override fun startVideoFile(path: Uri) {
        binding.zVideoView.videoUris = listOf(path)
        binding.zVideoView.prepareQueueAndStartPlaying()
    }

}