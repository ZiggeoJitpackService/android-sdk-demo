package com.ziggeo.androidsdk.demo.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.material.snackbar.Snackbar
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentSettingsBinding
import com.ziggeo.androidsdk.demo.presentation.settings.SettingsPresenter
import com.ziggeo.androidsdk.demo.presentation.settings.SettingsView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class SettingsFragment : BaseToolbarFragment<SettingsView, SettingsPresenter>(), SettingsView {
    override val layoutRes = R.layout.fragment_settings

    @InjectPresenter
    lateinit var presenter: SettingsPresenter

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ProvidePresenter
    override fun providePresenter(): SettingsPresenter =
        scope.getInstance(SettingsPresenter::class.java)

    override fun getHeaderTextRes() = R.string.settings_header

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.etStartDelay?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var startDelayValue = 0
                if (!s.isNullOrEmpty()) {
                    startDelayValue = s.toString().toInt()
                }
                presenter.onStartDelayChanged(startDelayValue)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        _binding?.scCustomVideo?.isChecked = presenter.getCustomVideoMode()
        _binding?.scCustomCamera?.isChecked = presenter.getCustomCameraMode()
        _binding?.scBlurMode?.isChecked = presenter.getBlurMode()

        _binding?.scCustomVideo?.setOnCheckedChangeListener { _, isChecked ->
            presenter.onCustomVideoChanged(
                isChecked
            )
        }

        _binding?.scCustomCamera?.setOnCheckedChangeListener { _, isChecked ->
            presenter.onCustomCameraChanged(
                isChecked
            )
        }

        _binding?.scBlurMode?.setOnCheckedChangeListener { _, isChecked ->
            presenter.onBlurModeChanged(
                isChecked
            )
        }

        _binding?.btnSave?.setOnClickListener {
            presenter.onSaveClicked()
        }
    }

    override fun showSavedNotification() {
        Snackbar.make(root, R.string.successfully_saved_message, Snackbar.LENGTH_SHORT).show()
    }
}