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
    override val layoutRes = R.layout.fragment_about
    private var _binding: FragmentAboutBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @InjectPresenter
    lateinit var presenter: AboutPresenter

    @ProvidePresenter
    override fun providePresenter(): AboutPresenter =
        scope.getInstance(AboutPresenter::class.java)

    override fun getHeaderTextRes() = R.string.about_header

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.tvText?.text = String.fromHtml(getString(R.string.about_text).replace("\n", "<br>"))
        _binding?.tvText?.movementMethod = LinkMovementMethod.getInstance()
    }
}