package com.ziggeo.androidsdk.demo.ui.custom

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import com.ziggeo.androidsdk.IZiggeo
import com.ziggeo.androidsdk.StopRecordingConfirmationDialogConfig
import com.ziggeo.androidsdk.callbacks.IAnalyticsManager
import com.ziggeo.androidsdk.callbacks.IRecorderCallback
import com.ziggeo.androidsdk.callbacks.RecorderCallback
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentAboutBinding
import com.ziggeo.androidsdk.demo.databinding.FragmentCustomCameraRecorderBinding
import com.ziggeo.androidsdk.demo.di.DI
import com.ziggeo.androidsdk.demo.presentation.custom.CustomModeCameraPresenter
import com.ziggeo.androidsdk.demo.ui.global.BaseScreenFragment
import com.ziggeo.androidsdk.log.ZLog
import com.ziggeo.androidsdk.recorder.CountDownHandler
import com.ziggeo.androidsdk.recorder.FixedUpdateHandler
import com.ziggeo.androidsdk.recorder.RecorderConfig
import com.ziggeo.androidsdk.ui.dialogs.CountDownDialog
import com.ziggeo.androidsdk.utils.DateTimeUtils
import com.ziggeo.androidsdk.utils.FileUtils
import com.ziggeo.androidsdk.widgets.ToggleImageView
import com.ziggeo.androidsdk.widgets.cameraview.CameraView
import com.ziggeo.androidsdk.widgets.cameraview.BaseCameraView.CameraCallback
import com.ziggeo.androidsdk.widgets.cameraview.ZiggeoCameraUtils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*

open class CustomModeCameraFragment : BaseScreenFragment<CustomModeCameraView,
        CustomModeCameraPresenter>(), CustomModeCameraView {

    override val parentScopeName = DI.APP_SCOPE

    @InjectPresenter
    lateinit var presenter: CustomModeCameraPresenter

    @ProvidePresenter
    override fun providePresenter(): CustomModeCameraPresenter =
        scope.getInstance(CustomModeCameraPresenter::class.java)

    private lateinit var config: RecorderConfig
    private lateinit var analyticsManager: IAnalyticsManager

    private var stopRecordingConfirmationDialog: AlertDialog? = null
    private var isPaused = false
    private var justSend = false
    private var startDelay = 0
    private var disableCameraSwitching = false
    private var quality = 0
    private var recordedFile: File? = null
    private var shouldAutoStart = false
    private var maxDuration: Long = 0
    private var actualDuration: Long = 0
    private var sendImmediately = false
    private var extraArgs: HashMap<String, String>? = null
    private var defaultPath: File? = null

    @ColorInt
    protected var colorForStoppedCameraOverlay = 0

    @DrawableRes
    protected var drawableForStoppedCameraOverlay = 0

    @ColorInt
    protected var colorForStoppedCameraOverlayDefault = 0

    private val countDownHandler = CountDownHandler()
    private val fixedUpdateHandler = FixedUpdateHandler()

    private val onClickListener = View.OnClickListener {
        val id: Int = it.id
        if (id == com.ziggeo.androidsdk.R.id.v_start_stop) {
            if (isRecording()) {
                confirmStopIfNeeded {
                    if (isRecording()) {
                        onStopClicked()
                    }
                }
            } else {
                startRecording(startDelay)
            }
        } else if (id == com.ziggeo.androidsdk.R.id.v_play) {
            play()
        }
    }

    override val layoutRes: Int
        get() = R.layout.fragment_custom_camera_recorder

    private var _binding: FragmentCustomCameraRecorderBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomCameraRecorderBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        if (!isPermissionsGranted()) {
            requestActionPermissions()
        } else {
            onPermissionsGranted()
        }
    }

    override fun onPause() {
        super.onPause()
        if (_binding?.vStartStop != null && isRecording()) {
            removeBtnStartListener()
            _binding?.vStartStop?.performClick()
            setBtnStartListener()
        }

        isPaused = true
        _binding?.cvCamera?.stop()
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
    }

    private fun setUi() {
        setBtnStartListener()
        _binding?.vPlay?.setOnClickListener(onClickListener)

        if (disableCameraSwitching) {
            _binding?.vSwitchCamera?.visibility = View.INVISIBLE
        } else {
            _binding?.vSwitchCamera?.setOnCheckedChangeListener { _: ToggleImageView?, _: Boolean -> switchCamera() }
        }

        _binding?.cvCamera?.start()
    }

    override fun loadConfigs(ziggeo: IZiggeo) {
        this.config = ziggeo.recorderConfig
        if (getCallback() != null) {
            getCallback()!!.loaded()
        }
        this.analyticsManager = ziggeo.analyticsManager()
        analyticsManager.addEmbeddingRecorderLoadedEvent()

        this.startDelay = config.startDelay
        this.maxDuration = config.maxDuration
        this.sendImmediately = config.shouldSendImmediately
        this.shouldAutoStart = config.shouldAutoStartRecording
        this.disableCameraSwitching = config.shouldDisableCameraSwitch
        this.quality = config.videoQuality
        this.extraArgs = config.extraArgs
        this.defaultPath = config.cacheConfig.cacheRoot

        countDownHandler.addListener { secondsLeft: Int ->
            if (getCallback() != null) {
                getCallback()!!.countdown(secondsLeft)
            }
            if (secondsLeft == 0) {
                startRecording()
            }
        }

        fixedUpdateHandler.addListener { millis: Long ->
            if (getCallback() != null) {
                getCallback()!!.recordingProgress(millis)
            }
            fixedUpdate(millis, maxDuration)
        }

        colorForStoppedCameraOverlayDefault =
            ContextCompat.getColor(activity!!, R.color.colorStoppedCameraOverlay)
        if (config.drawableForStoppedCameraOverlay != 0) {
            drawableForStoppedCameraOverlay = config.drawableForStoppedCameraOverlay
        }
        if (config.colorForStoppedCameraOverlay != 0) {
            colorForStoppedCameraOverlay = config.colorForStoppedCameraOverlay
        }
        updateTimeStatusText(0, maxDuration)

    }

    private fun loadCameraConfigs() {

        _binding?.cvCamera?.setResolution(this.config.resolution)
        _binding?.cvCamera?.setVideoBitrate(this.config.videoBitrate)
        _binding?.cvCamera?.setAudioBitrate(this.config.audioBitrate)
        _binding?.cvCamera?.setAudioSampleRate(this.config.audioSampleRate)
        _binding?.cvCamera?.quality = quality
        _binding?.cvCamera?.facing = this.config.facing

        _binding?.cvCamera?.setCameraCallback(object : CameraCallback() {
            override fun cameraOpened() {
                super.cameraOpened()
                readyToRecord()
            }

            override fun cameraClosed() {
                super.cameraClosed()
            }
        })

        _binding?.cvCamera?.setRecorderCallback(object : RecorderCallback() {
            override fun recordingStarted() {
                super.recordingStarted()
                this@CustomModeCameraFragment.recordingStarted()
            }

            override fun error(throwable: Throwable) {
                super.error(throwable)
                recordingError(throwable)
            }

            override fun recordingStopped(path: String) {
                super.recordingStopped(path)
                recordingStopped()
            }

            override fun streamingStarted() {
                super.streamingStarted()
                started()
            }

            override fun streamingStopped() {
                super.streamingStopped()
                reset()
            }
        })

        setUi()
    }

    override fun onBackPressed() {
        var justClose = true
        justClose = justClose and !(isRecording())
        if (justClose) {
            if (getCallback() != null) {
                getCallback()!!.canceledByUser()
            }
        }
        justClose = justClose or (isRecording() && !confirmStopIfNeeded {
            if (getCallback() != null) {
                getCallback()!!.canceledByUser()
            }
            val activity: Activity? = activity
            activity?.finish()
        })
        if (justClose) {
            super.onBackPressed()
        }
    }

    private fun recordingStarted() {
        if (getCallback() != null) {
            getCallback()!!.recordingStarted()
        }
        started()
    }

    private fun onStopClicked() {
        temporaryDisableStartStopBtn()
        stopRecording()
    }

    protected open fun stopRecording() {
        reset()
        if (config.blurMode) {
            _binding?.cvCamera?.stopLiveRecording()
        } else {
            _binding?.cvCamera?.stopRecording()
        }
    }

    private fun readyToRecord() {
        if (getCallback() != null) {
            getCallback()!!.readyToRecord()
        }
        if (shouldAutoStart) {
            startRecording(startDelay)
        }
    }

    private fun recordingError(throwable: Throwable) {
        if (getCallback() != null) {
            getCallback()!!.error(throwable)
        }
        reset()
    }

    private fun play() {
        if (recordedFile != null)
            presenter.onPlayClicked(recordedFile!!.path)
    }

    protected open fun reset() {
        resetFixedUpdate()
        countDownHandler.stop()
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        if (stopRecordingConfirmationDialog != null) {
            stopRecordingConfirmationDialog!!.hide()
        }
        _binding?.vPlay?.visibility = View.INVISIBLE
        if (!disableCameraSwitching) {
            _binding?.vSwitchCamera?.visibility = View.VISIBLE
        }
        removeBtnStartListener()
        switchRecordStartIcon(false)
        setBtnStartListener()
        updateTimeStatusText(0, maxDuration)
    }

    fun started() {
        fixedUpdateHandler.start()
        switchRecordStartIcon(true)
        _binding?.vPlay?.visibility = View.INVISIBLE
        _binding?.vSwitchCamera?.visibility = View.INVISIBLE
    }

    private fun confirmStopIfNeeded(positiveAction: Action): Boolean {
        if (config.shouldConfirmStopRecording) {
            val stopConfig: StopRecordingConfirmationDialogConfig =
                config.stopRecordingConfirmationDialogConfig
            val dialogBuilder = AlertDialog.Builder(
                activity
            )
            if (stopConfig.mesResId != 0) {
                dialogBuilder.setMessage(stopConfig.mesResId)
            }
            if (stopConfig.titleResId != 0) {
                dialogBuilder.setTitle(stopConfig.titleResId)
            }
            if (stopConfig.posBtnResId != 0) {
                dialogBuilder.setPositiveButton(
                    stopConfig.posBtnResId
                ) { _: DialogInterface?, _: Int ->
                    try {
                        positiveAction.run()
                    } catch (e: Exception) {
                        ZLog.e(e.toString())
                    }
                }
            }
            if (stopConfig.negBtnResId != 0) {
                dialogBuilder.setNegativeButton(
                    stopConfig.negBtnResId
                ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            }
            dialogBuilder.setCancelable(false)
            stopRecordingConfirmationDialog = dialogBuilder.create()
            (stopRecordingConfirmationDialog)?.show()
            return true
        } else {
            try {
                positiveAction.run()
            } catch (e: Exception) {
                ZLog.e(e.toString())
            }
        }
        return false
    }

    protected open fun startRecording(delay: Int) {
        if (recordedFile != null) {
            recordedFile!!.delete()
            if (getCallback() != null) {
                getCallback()!!.rerecord()
            }
        }
        val downDialog = CountDownDialog()
        val args = Bundle()
        args.putInt(CountDownDialog.ARG_INITIAL_TIME, delay)
        downDialog.arguments = args
        downDialog.show(requireFragmentManager(), null)
        countDownHandler.start(delay)
        requireActivity().requestedOrientation = ZiggeoCameraUtils.getCurrentOrientationToRequest(
            requireContext()
        )
    }

    private fun checkPermissions(): Boolean {
        if (!isPermissionsGranted()) {
            requestActionPermissions()
            return false
        }
        return true
    }

    private fun checkFileDirection(): Boolean {
        if (checkPermissions()) {
            if (defaultPath != null && !(defaultPath!!.exists())) {
                defaultPath?.mkdirs()
            }
            justSend = false
            return true
        }
        return false
    }

    protected open fun startRecording(): Boolean {
        if (!isPermissionsGranted()) {
            requestActionPermissions()
            return false
        } else {
            if (defaultPath != null && !defaultPath!!.exists()) {
                defaultPath!!.mkdirs()
            }
            justSend = false
            if (checkFileDirection()) {
                startCameraPreview()
                Completable.fromAction {
                    FileUtils.clearCacheIfNeeded(
                        config.cacheConfig
                    )
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { t: Throwable? ->
                        ZLog.e(
                            t
                        )
                    }
                    .doFinally {
                        recordedFile = File(
                            defaultPath,
                            FileUtils.getVideoFileName()
                        )
                        if (config.blurMode) {
                            _binding?.cvCamera?.startLiveRecording(recordedFile!!.path, maxDuration.toInt())
                        } else {
                            _binding?.cvCamera?.startRecording(recordedFile!!.path, maxDuration.toInt())
                        }
                    }
                    .subscribe()
                return true
            }
        }
        return false
    }

    private fun recordingStopped() {
        val path: String = recordedFile?.absolutePath ?: ""
        actualDuration = FileUtils.getDurationMillis(
            Uri.fromFile(recordedFile),
            context!!
        )

        if (getCallback() != null) {
            getCallback()!!.recordingStopped(path)
        }
        resetFixedUpdate()
        _binding?.vPlay?.visibility = View.VISIBLE
    }

    private fun resetFixedUpdate() {
        fixedUpdateHandler.stop()
        fixedUpdate(0, maxDuration)
    }


    private fun fixedUpdate(elapsed: Long, maxDuration: Long) {
        updateTimeStatusText(elapsed, maxDuration)
    }

    open fun updateTimeStatusText(elapsedTime: Long, maxDuration: Long) {
        when {
            config.timeFormatFunction != null -> {
                try {
                    _binding?.tvTimeStatus?.text = config.timeFormatFunction.apply(elapsedTime, maxDuration)
                } catch (e: java.lang.Exception) {
                    ZLog.e(e.toString())
                    _binding?.tvTimeStatus?.text = null
                }
            }
            maxDuration > 0 -> {
                _binding?.tvTimeStatus?.text = DateTimeUtils.getTimeStatus(elapsedTime, maxDuration)
            }
            else -> {
                if (isVisible)
                    _binding?.tvTimeStatus?.text = DateTimeUtils.formatDuration(elapsedTime)
            }
        }
    }

    private fun startCameraPreview() {
        if (activity == null) return
        ZLog.d("startCameraPreview()")
        if (_binding?.cvCamera != null) {
            _binding?.cvCamera?.visibility = View.VISIBLE
        }
        if (_binding?.vgRecorder != null) {
            _binding?.vgRecorder?.setBackgroundColor(colorForStoppedCameraOverlayDefault)
        }
    }

    private fun setBtnStartListener() {
        if (_binding?.vStartStop != null) {
            _binding?.vStartStop?.setOnClickListener(onClickListener)
            _binding?.vStartStop?.isEnabled = true
        }
    }

    private fun removeBtnStartListener() {
        if (_binding?.vStartStop != null) {
            _binding?.vStartStop!!.setOnClickListener(null)
            _binding?.vStartStop!!.isEnabled = false
        }
    }

    open fun switchCamera() {
        val isFacingBack = _binding?.cvCamera?.facing == CameraView.FACING_BACK
        _binding?.cvCamera?.facing = if (isFacingBack) CameraView.FACING_FRONT else CameraView.FACING_BACK
    }

    protected open fun isRecording(): Boolean {
        return _binding?.cvCamera?.isRecording?:false;
    }

    private fun switchRecordStartIcon(isStarted: Boolean) {
        _binding?.vStartStop?.setImageResource(
            if (isStarted) com.ziggeo.androidsdk.R.drawable.ic_stop
            else com.ziggeo.androidsdk.R.drawable.ic_start
        )
    }

    private fun temporaryDisableStartStopBtn() {
        removeBtnStartListener()
        Handler(Looper.getMainLooper()).postDelayed({ setBtnStartListener() }, 1500)
    }

    private fun getCallback(): IRecorderCallback? {
        return this.config.callback
    }

    private fun isPermissionsGranted(): Boolean {
        var granted = true
        for (permission in getNeededPermissionsList()) {
            granted = granted and isPermissionGranted(permission)
        }
        return granted
    }

    private fun isPermissionGranted(permission: String): Boolean {
        val activity: Activity? = activity
        return if (activity != null) {
            ActivityCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
    }

    private fun getNeededPermissionsList(): Array<String> {
        val permissions: ArrayList<String> = object : ArrayList<String>() {
            init {
                add(Manifest.permission.CAMERA)
                if (Build.VERSION.SDK_INT <= 29) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
        return permissions.toArray(arrayOf())
    }

    private fun requestActionPermissions() {
        Dexter.withContext(activity)
            .withPermissions(*getNeededPermissionsList())
            .withListener(CompositeMultiplePermissionsListener(
                getRationalePermissionListener(getRationaleMessageId()),
                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (isPermissionsGranted()) {
                            onPermissionsGranted()
                        } else if (getCallback() != null) {
                            // looking for permanently denied permissions
                            val permsList: MutableList<String> = ArrayList()
                            for (response in report.deniedPermissionResponses) {
                                permsList.add(response.permissionName)
                            }
                            if (permsList.isNotEmpty()) {
                                getCallback()!!.accessForbidden(permsList)
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                    }
                }
            )).onSameThread()
            .check()
    }

    private fun getRationalePermissionListener(@StringRes stringRes: Int): MultiplePermissionsListener? {
        return SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
            .with(root, stringRes)
            .withOpenSettingsButton(R.string.settings)
            .build()
    }

    private fun getRationaleMessageId() = R.string.camera_permission_rationale


    private fun onPermissionsGranted() {
        presenter.loadConfig()

        if (getCallback() != null) {
            getCallback()!!.accessGranted()
        }
        if (hasMicrophone()) {
            if (getCallback() != null) {
                getCallback()!!.hasMicrophone()
            }
        } else {
            if (getCallback() != null) {
                getCallback()!!.noMicrophone()
            }
        }

        if (hasCamera()) {
            if (getCallback() != null) {
                getCallback()!!.hasCamera()
            }
            loadCameraConfigs()
            _binding?.cvCamera?.start()
        } else {
            if (getCallback() != null) {
                getCallback()!!.noCamera()
            }
        }
    }

    private fun hasCamera(): Boolean {
        val activity: Activity? = activity
        return if (activity != null) {
            ((activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                    activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
                    && _binding?.cvCamera!!.numberOfCameras > 0)
        } else {
            false
        }
    }

    private fun hasMicrophone(): Boolean {
        val activity: Activity? = activity
        return activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
            ?: false
    }
}