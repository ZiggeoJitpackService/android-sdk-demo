package com.ziggeo.androidsdk.demo.ui.contactus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentContactUsBinding
import com.ziggeo.androidsdk.demo.presentation.contactus.ContactUsPresenter
import com.ziggeo.androidsdk.demo.presentation.contactus.ContactUsView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment
import com.ziggeo.androidsdk.demo.util.openUrl


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class ContactUsFragment : BaseToolbarFragment<ContactUsView, ContactUsPresenter>(), ContactUsView {
    private var _binding: FragmentContactUsBinding? = null
    private val binding get() = _binding!!
    override val layoutRes = R.layout.fragment_contact_us

    @InjectPresenter
    lateinit var presenter: ContactUsPresenter

    @ProvidePresenter
    override fun providePresenter(): ContactUsPresenter =
        scope.getInstance(ContactUsPresenter::class.java)

    override fun getHeaderTextRes() = R.string.contact_header

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnVisitSupport.setOnClickListener {
            presenter.onVisitSupportClicked()
        }
        binding.btnContactUs.setOnClickListener {
            presenter.onContactUsClicked()
        }
    }

    override fun openSupportPage() {
        val supportUrl = "https://support.ziggeo.com"
        context?.let {
            openUrl(it, supportUrl)
        }
    }
}