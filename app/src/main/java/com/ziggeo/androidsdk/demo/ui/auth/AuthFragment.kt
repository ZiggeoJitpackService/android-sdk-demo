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
    override val layoutRes = R.layout.fragment_auth

    override val parentScopeName = DI.APP_SCOPE

    @InjectPresenter
    lateinit var presenter: AuthPresenter

    private var _binding: FragmentAuthBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.btnScanQr?.setOnClickListener {
            analytics.logEvent("scan_qr_clicked", null)
            presenter.onScanQrClicked()
        }
        _binding?.tvEnterManually?.setOnClickListener {
            presenter.onEnterQrManuallyClicked()
        }
        _binding?.tvUseScanner?.setOnClickListener {
            presenter.onUseScannerClicked()
        }
        _binding?.etQr?.setOnEditorActionListener { _, _, _ ->
            _binding?.btnUseEnteredQr?.performClick()
            true
        }
        _binding?.btnUseEnteredQr?.setOnClickListener {
            analytics.logEvent("use_entered_qr_clicked", null)
            presenter.onUseEnteredQrClicked(_binding?.etQr?.text.toString())
        }
    }

    override fun showQrCannotBeEmptyError() {
        _binding?.etQr?.error = getString(R.string.err_not_empty)
    }

    @ProvidePresenter
    override fun providePresenter(): AuthPresenter = scope.getInstance(AuthPresenter::class.java)

    override fun showScannerButton() {
        _binding?.btnScanQr?.visibility = View.VISIBLE
        _binding?.tvEnterManually?.visibility = View.VISIBLE

        _binding?.tilQr?.visibility = View.INVISIBLE
        _binding?.btnUseEnteredQr?.visibility = View.GONE
        _binding?.tvUseScanner?.visibility = View.GONE
    }

    override fun showEnterQrField() {
        _binding?.btnScanQr?.visibility = View.GONE
        _binding?.tvEnterManually?.visibility = View.GONE

        _binding?.tilQr?.visibility = View.VISIBLE
        _binding?.btnUseEnteredQr?.visibility = View.VISIBLE
        _binding?.tvUseScanner?.visibility = View.VISIBLE
    }
}