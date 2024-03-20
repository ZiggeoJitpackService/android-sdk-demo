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
import com.ziggeo.androidsdk.Ziggeo
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentRecordingsBinding
import com.ziggeo.androidsdk.demo.presentation.recordings.RecordingsPresenter
import com.ziggeo.androidsdk.demo.presentation.recordings.RecordingsView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment
import com.ziggeo.androidsdk.net.models.ContentModel
import com.ziggeo.androidsdk.recorder.RecorderConfig
import com.ziggeo.androidsdk.widgets.cameraview.CameraView


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class RecordingsFragment : BaseToolbarFragment<RecordingsView, RecordingsPresenter>(),
    RecordingsView {
    override val layoutRes = R.layout.fragment_recordings

    @InjectPresenter
    lateinit var presenter: RecordingsPresenter

    private lateinit var rotateForward: Animation
    private lateinit var rotateBackward: Animation

    private var _binding: FragmentRecordingsBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordingsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ProvidePresenter
    override fun providePresenter(): RecordingsPresenter =
        scope.getInstance(RecordingsPresenter::class.java)

    override fun getHeaderTextRes() = R.string.recordings_header

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFab()
        _binding?.pullToRefresh?.setOnRefreshListener {
            analytics.logEvent("refresh_recordings", null)
            presenter.onPullToRefresh()
        }
    }

    override fun showRecordingsList(list: List<ContentModel>) {
        _binding?.tvEmptyList?.visibility = View.INVISIBLE
        _binding?.rvRecordings?.visibility = View.VISIBLE

        val adapter = RecordingsAdapter(list)
        adapter.onItemClickListener = object : RecordingsAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                presenter.onItemClicked(list[position])
            }
        }

        _binding?.rvRecordings?.layoutManager = LinearLayoutManager(context)
        _binding?.rvRecordings?.adapter = adapter
        _binding?.rvRecordings?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && (_binding != null && _binding!!.fabSelector.isShown)) {
                    presenter.onScrollDown()
                } else if (dy < 0 && !(_binding != null && _binding!!.fabSelector.isShown)) {
                    presenter.onScrollUp()
                }
            }
        })
    }

    override fun showActionFabs() {
        analytics.logEvent("fab_show_actions", null)
        _binding?.fabCamera?.show()
        _binding?.fabScreen?.show()
        _binding?.fabAudio?.show()
        _binding?.fabImage?.show()
        _binding?.fabFile?.show()
    }

    override fun hideActionFabs() {
        analytics.logEvent("fab_hide_actions", null)
        _binding?.fabCamera?.hide()
        _binding?.fabScreen?.hide()
        _binding?.fabAudio?.hide()
        _binding?.fabImage?.hide()
        _binding?.fabFile?.hide()
    }

    override fun hideSelectorFab() {
        _binding?.fabSelector?.hide()
    }

    override fun showSelectorFab() {
        _binding?.fabSelector?.show()
    }

    override fun showNoRecordingsMessage() {
        _binding?.tvEmptyList?.visibility = View.VISIBLE
        _binding?.rvRecordings?.visibility = View.INVISIBLE
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            _binding?.tvEmptyList?.visibility = View.INVISIBLE
            _binding?.pullToRefresh?.isRefreshing = true
        } else {
            _binding?.pullToRefresh?.isRefreshing = false
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
//        ziggeo.startFileSelector()

        initAndStart()
    }

    private fun initAndStart() {
        val activity = getActivity()
        var mZiggeo = Ziggeo( ziggeo.getAppToken(), activity!!)

        mZiggeo?.recorderConfig = RecorderConfig.Builder(activity)
            .maxDuration(10000)
            .shouldSendImmediately(false)
            .shouldEnableCoverShot(false)
            .shouldConfirmStopRecording(false)
            .facing(CameraView.FACING_FRONT)
            .shouldShowFaceOutline(false)
            .quality(CameraView.QUALITY_HIGH)
            .build()
        mZiggeo?.startCameraRecorder()
    }


    override fun startShowAnimationMainFab() {
        _binding?.fabSelector?.startAnimation(rotateForward)
    }

    override fun startHideAnimationMainFab() {
        _binding?.fabSelector?.startAnimation(rotateBackward)
    }

    private fun initFab() {
        rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)

        _binding?.fabSelector?.setOnClickListener {
            presenter.onFabActionsClicked()
        }
        _binding?.fabCamera?.setOnClickListener {
            presenter.onFabCameraClicked()
        }
        _binding?.fabScreen?.setOnClickListener {
            presenter.onFabScreenClicked()
        }
        _binding?.fabAudio?.setOnClickListener {
            presenter.onFabAudioClicked()
        }
        _binding?.fabImage?.setOnClickListener {
            presenter.onFabImageClicked()
        }
        _binding?.fabFile?.setOnClickListener {
            presenter.onFabFileClicked()
        }
    }
}