package com.ziggeo.androidsdk.demo.ui.recordings

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentRecordingDetailsBinding
import com.ziggeo.androidsdk.demo.presentation.recordings.RecordingDetailsPresenter
import com.ziggeo.androidsdk.demo.presentation.recordings.RecordingDetailsView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment
import com.ziggeo.androidsdk.demo.ui.global.MessageDialogFragment
import com.ziggeo.androidsdk.net.models.ContentModel


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
private const val CONFIRM_TAG = "CONFIRM_TAG"

class RecordingDetailsFragment : BaseToolbarFragment<RecordingDetailsView,
        RecordingDetailsPresenter>(),
    RecordingDetailsView,
    MessageDialogFragment.OnClickListener {

    override fun getHeaderTextRes(): Int = R.string.details_header

    override val layoutRes = R.layout.fragment_recording_details

    @InjectPresenter
    lateinit var presenter: RecordingDetailsPresenter

    private var _binding: FragmentRecordingDetailsBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordingDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ProvidePresenter
    override fun providePresenter(): RecordingDetailsPresenter =
        scope.getInstance(RecordingDetailsPresenter::class.java)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.toolbar?.setNavigationOnClickListener {
            presenter.onBackPressed()
        }
        _binding?.toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item_edit -> presenter.onEditClicked()
                R.id.item_delete -> presenter.onDeleteClicked()
                R.id.item_save -> presenter.onSaveClicked(
                    _binding?.etTokenOrKey?.text.toString(),
                    _binding?.etTitle?.text.toString(),
                    _binding?.etDescription?.text.toString()
                )
            }
            true
        }
        _binding?.fabPlay?.setOnClickListener {
            presenter.onPlayClicked()
        }
    }

    override fun showRecordingData(contentModel: ContentModel) {
        _binding?.etTokenOrKey?.setText(contentModel.key ?: contentModel.token)
        _binding?.etTitle?.setText(contentModel.title)
        _binding?.etDescription?.setText(contentModel.description)
    }

    override fun showPreview(url: String, isVideo: Boolean) {
        if (url.isEmpty()) {
            _binding?.ivPreview?.setImageResource(R.drawable.ic_microphone)
            _binding?.ivPreview?.setOnClickListener {
                presenter.onPlayClicked()
            }
        } else {

            val circularProgressDrawable = CircularProgressDrawable(requireActivity().baseContext)
            circularProgressDrawable
                .setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorAccent), PorterDuff.Mode.SRC_IN )
            circularProgressDrawable.strokeWidth = 10f
            circularProgressDrawable.centerRadius = 35f
            circularProgressDrawable.start()

            Glide.with(_binding?.ivPreview!!)
                .load(url)
                .placeholder(circularProgressDrawable)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        circularProgressDrawable.stop()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (isVideo) {
                            _binding?.fabPlay?.show()
                        } else {
                            _binding?.ivPreview?.setOnClickListener {
                                presenter.onPlayClicked()
                            }
                        }
                        return false
                    }

                })
                .into(_binding?.ivPreview!!)
        }
    }

    override fun showViewsInEditState() {
        _binding?.toolbar?.menu?.clear()
        _binding?.toolbar?.inflateMenu(R.menu.details_menu_edit_mode)

        _binding?.etTokenOrKey?.isEnabled = true
        _binding?.etTitle?.isEnabled = true
        _binding?.etDescription?.isEnabled = true

        context?.let {
            _binding?.toolbar?.navigationIcon = ContextCompat.getDrawable(it, R.drawable.ic_close_white_24dp)
            _binding?.toolbar?.setNavigationOnClickListener {
                presenter.onCloseClicked()
            }
        }
    }

    override fun showViewsInViewState() {
        _binding?.toolbar?.menu?.clear()
        _binding?.toolbar?.inflateMenu(R.menu.details_menu_view_mode)

        _binding?.etTokenOrKey?.isEnabled = false
        _binding?.etTitle?.isEnabled = false
        _binding?.etDescription?.isEnabled = false

        context?.let {
            _binding?.toolbar?.navigationIcon =
                ContextCompat.getDrawable(it, R.drawable.ic_arrow_back_white_24dp)
            _binding?.toolbar?.setNavigationOnClickListener {
                presenter.onBackPressed()
            }
        }
    }

    override fun dialogPositiveClicked(tag: String) {
        super.dialogPositiveClicked(tag)
        presenter.onConfirmYesClicked()
    }

    override fun dialogNegativeClicked(tag: String) {
        super.dialogNegativeClicked(tag)
        presenter.onConfirmNoClicked()
    }

    override fun showConfirmDeleteDialog() {
        childFragmentManager.let {
            val dialog = MessageDialogFragment.create(
                null,
                getString(R.string.common_confirm_message),
                getString(R.string.common_yes),
                getString(R.string.common_no)
            )
            dialog.show(it, CONFIRM_TAG)
        }
    }

    override fun hideConfirmDeleteDialog() {
        childFragmentManager.let {
            (it.findFragmentByTag(CONFIRM_TAG) as MessageDialogFragment).dismiss()
        }
    }

}