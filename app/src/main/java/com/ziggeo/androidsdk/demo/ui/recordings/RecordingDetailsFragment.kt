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
    private var _binding: FragmentRecordingDetailsBinding? = null
    private val binding get() = _binding!!

    override fun getHeaderTextRes(): Int = R.string.details_header

    override val layoutRes = R.layout.fragment_recording_details

    @InjectPresenter
    lateinit var presenter: RecordingDetailsPresenter

    @ProvidePresenter
    override fun providePresenter(): RecordingDetailsPresenter =
        scope.getInstance(RecordingDetailsPresenter::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            presenter.onBackPressed()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item_edit -> presenter.onEditClicked()
                R.id.item_delete -> presenter.onDeleteClicked()
                R.id.item_save -> presenter.onSaveClicked(
                    binding.etTokenOrKey.text.toString(),
                    binding.etTitle.text.toString(),
                    binding.etDescription.text.toString()
                )
            }
            true
        }
        binding.fabPlay.setOnClickListener {
            presenter.onPlayClicked()
        }
    }

    override fun showRecordingData(contentModel: ContentModel) {
        binding.etTokenOrKey.setText(contentModel.key ?: contentModel.token)
        binding.etTitle.setText(contentModel.title)
        binding.etDescription.setText(contentModel.description)
    }

    override fun showPreview(url: String, isVideo: Boolean) {
        if (url.isEmpty()) {
            binding.ivPreview.setImageResource(R.drawable.ic_microphone)
            binding.ivPreview.setOnClickListener {
                presenter.onPlayClicked()
            }
        } else {

            val circularProgressDrawable = CircularProgressDrawable(requireActivity().baseContext)
            circularProgressDrawable
                .setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorAccent), PorterDuff.Mode.SRC_IN )
            circularProgressDrawable.strokeWidth = 10f
            circularProgressDrawable.centerRadius = 35f
            circularProgressDrawable.start()

            Glide.with(binding.ivPreview)
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
                            binding.fabPlay.show()
                        } else {
                            binding.ivPreview.setOnClickListener {
                                presenter.onPlayClicked()
                            }
                        }
                        return false
                    }

                })
                .into(binding.ivPreview)
        }
    }

    override fun showViewsInEditState() {
        binding.toolbar.menu.clear()
        binding.toolbar.inflateMenu(R.menu.details_menu_edit_mode)

        binding.etTokenOrKey.isEnabled = true
        binding.etTitle.isEnabled = true
        binding.etDescription.isEnabled = true

        context?.let {
            binding.toolbar.navigationIcon = ContextCompat.getDrawable(it, R.drawable.ic_close_white_24dp)
            binding.toolbar.setNavigationOnClickListener {
                presenter.onCloseClicked()
            }
        }
    }

    override fun showViewsInViewState() {
        binding.toolbar.menu.clear()
        binding.toolbar.inflateMenu(R.menu.details_menu_view_mode)

        binding.etTokenOrKey.isEnabled = false
        binding.etTitle.isEnabled = false
        binding.etDescription.isEnabled = false

        context?.let {
            binding.toolbar.navigationIcon =
                ContextCompat.getDrawable(it, R.drawable.ic_arrow_back_white_24dp)
            binding.toolbar.setNavigationOnClickListener {
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