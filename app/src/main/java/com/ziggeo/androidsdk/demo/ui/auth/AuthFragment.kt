package com.ziggeo.androidsdk.demo.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentAuthBinding
import com.ziggeo.androidsdk.demo.di.DI
import com.ziggeo.androidsdk.demo.presentation.auth.AuthPresenter
import com.ziggeo.androidsdk.demo.presentation.auth.AuthView
import com.ziggeo.androidsdk.demo.ui.global.BaseScreenFragment


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class AuthFragment : BaseScreenFragment<AuthView, AuthPresenter>(), AuthView {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    override val layoutRes = R.layout.fragment_auth

    override val parentScopeName = DI.APP_SCOPE

    @InjectPresenter
    lateinit var presenter: AuthPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnScanQr.setOnClickListener {
            analytics.logEvent("scan_qr_clicked", null)
            presenter.onScanQrClicked()
        }
        binding.tvEnterManually.setOnClickListener {
            presenter.onEnterQrManuallyClicked()
        }
        binding.tvUseScanner.setOnClickListener {
            presenter.onUseScannerClicked()
        }
        binding.etQr.setOnEditorActionListener { _, _, _ ->
            binding.btnUseEnteredQr.performClick()
            true
        }
        binding.btnUseEnteredQr.setOnClickListener {
            analytics.logEvent("use_entered_qr_clicked", null)
            presenter.onUseEnteredQrClicked(binding.etQr.text.toString())
        }
    }

    override fun showQrCannotBeEmptyError() {
        binding.etQr.error = getString(R.string.err_not_empty)
    }

    @ProvidePresenter
    override fun providePresenter(): AuthPresenter = scope.getInstance(AuthPresenter::class.java)

    override fun showScannerButton() {
        binding.btnScanQr.visibility = View.VISIBLE
        binding.tvEnterManually.visibility = View.VISIBLE

        binding.tilQr.visibility = View.INVISIBLE
        binding.btnUseEnteredQr.visibility = View.GONE
        binding.tvUseScanner.visibility = View.GONE
    }

    override fun showEnterQrField() {
        binding.btnScanQr.visibility = View.GONE
        binding.tvEnterManually.visibility = View.GONE

        binding.tilQr.visibility = View.VISIBLE
        binding.btnUseEnteredQr.visibility = View.VISIBLE
        binding.tvUseScanner.visibility = View.VISIBLE
    }
}