package com.ziggeo.androidsdk.demo.ui.recordings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentRecordingsBinding
import com.ziggeo.androidsdk.demo.presentation.recordings.RecordingsPresenter
import com.ziggeo.androidsdk.demo.presentation.recordings.RecordingsView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment
import com.ziggeo.androidsdk.net.models.ContentModel


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class RecordingsFragment : BaseToolbarFragment<RecordingsView, RecordingsPresenter>(),
    RecordingsView {
    private var _binding: FragmentRecordingsBinding? = null
    private val binding get() = _binding!!
    override val layoutRes = R.layout.fragment_recordings

    @InjectPresenter
    lateinit var presenter: RecordingsPresenter

    private lateinit var rotateForward: Animation
    private lateinit var rotateBackward: Animation

    @ProvidePresenter
    override fun providePresenter(): RecordingsPresenter =
        scope.getInstance(RecordingsPresenter::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getHeaderTextRes() = R.string.recordings_header

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFab()
        binding.pullToRefresh.setOnRefreshListener {
            analytics.logEvent("refresh_recordings", null)
            presenter.onPullToRefresh()
        }
    }

    override fun showRecordingsList(list: List<ContentModel>) {
        binding.tvEmptyList.visibility = View.INVISIBLE
        binding.rvRecordings.visibility = View.VISIBLE

        val adapter = RecordingsAdapter(list)
        adapter.onItemClickListener = object : RecordingsAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                presenter.onItemClicked(list[position])
            }
        }

        binding.rvRecordings.layoutManager = LinearLayoutManager(context)
        binding.rvRecordings.adapter = adapter
        binding.rvRecordings.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.fabSelector.isShown) {
                    presenter.onScrollDown()
                } else if (dy < 0 && !binding.fabSelector.isShown) {
                    presenter.onScrollUp()
                }
            }
        })
    }

    override fun showActionFabs() {
        analytics.logEvent("fab_show_actions", null)
        binding.fabCamera.show()
        binding.fabScreen.show()
        binding.fabAudio.show()
        binding.fabImage.show()
        binding.fabFile.show()
    }

    override fun hideActionFabs() {
        analytics.logEvent("fab_hide_actions", null)
        binding.fabCamera.hide()
        binding.fabScreen.hide()
        binding.fabAudio.hide()
        binding.fabImage.hide()
        binding.fabFile.hide()
    }

    override fun hideSelectorFab() {
        binding.fabSelector.hide()
    }

    override fun showSelectorFab() {
        binding.fabSelector.show()
    }

    override fun showNoRecordingsMessage() {
        binding.tvEmptyList.visibility = View.VISIBLE
        binding.rvRecordings.visibility = View.INVISIBLE
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            binding.tvEmptyList.visibility = View.INVISIBLE
            binding.pullToRefresh.isRefreshing = true
        } else {
            binding.pullToRefresh.isRefreshing = false
        }
    }

    override fun startCameraRecorder() {
        analytics.logEvent("start_camera_recorder", null)
        ziggeo.startCameraRecorder()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun startScreenRecorder() {
        analytics.logEvent("start_screen_recorder", null)
        ziggeo.startScreenRecorder(null)
    }

    override fun startAudioRecorder() {
        analytics.logEvent("start_audio_recorder", null)
        ziggeo.startAudioRecorder()
    }

    override fun startImageCapture() {
        analytics.logEvent("start_image_capture", null)
        ziggeo.startImageRecorder()
    }

    override fun startFileSelector() {
        analytics.logEvent("start_file_selector", null)
        ziggeo.startFileSelector()
    }

    override fun startShowAnimationMainFab() {
        binding.fabSelector.startAnimation(rotateForward)
    }

    override fun startHideAnimationMainFab() {
        binding.fabSelector.startAnimation(rotateBackward)
    }

    private fun initFab() {
        rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)

        binding.fabSelector.setOnClickListener {
            presenter.onFabActionsClicked()
        }
        binding.fabCamera.setOnClickListener {
            presenter.onFabCameraClicked()
        }
        binding.fabScreen.setOnClickListener {
            presenter.onFabScreenClicked()
        }
        binding.fabAudio.setOnClickListener {
            presenter.onFabAudioClicked()
        }
        binding.fabImage.setOnClickListener {
            presenter.onFabImageClicked()
        }
        binding.fabFile.setOnClickListener {
            presenter.onFabFileClicked()
        }
    }
}