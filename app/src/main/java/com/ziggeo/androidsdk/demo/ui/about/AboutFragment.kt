package com.ziggeo.androidsdk.demo.ui.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentAboutBinding
import com.ziggeo.androidsdk.demo.presentation.about.AboutPresenter
import com.ziggeo.androidsdk.demo.presentation.about.AboutView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment
import com.ziggeo.androidsdk.demo.util.fromHtml

/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class AboutFragment : BaseToolbarFragment<AboutView, AboutPresenter>(), AboutView {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    override val layoutRes = R.layout.fragment_about

    @InjectPresenter
    lateinit var presenter: AboutPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ProvidePresenter
    override fun providePresenter(): AboutPresenter =
        scope.getInstance(AboutPresenter::class.java)

    override fun getHeaderTextRes() = R.string.about_header

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvText.text = String.fromHtml(getString(R.string.about_text).replace("\n", "<br>"))
        binding.tvText.movementMethod = LinkMovementMethod.getInstance()
    }
}