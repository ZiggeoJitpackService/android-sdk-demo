package com.ziggeo.androidsdk.demo.ui.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentLogBinding
import com.ziggeo.androidsdk.demo.presentation.log.LogPresenter
import com.ziggeo.androidsdk.demo.presentation.log.LogView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment
import com.ziggeo.androidsdk.log.LogModel


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class LogFragment : BaseToolbarFragment<LogView, LogPresenter>(), LogView {
    override val layoutRes = R.layout.fragment_log

    @InjectPresenter
    lateinit var presenter: LogPresenter

    private var _binding: FragmentLogBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ProvidePresenter
    override fun providePresenter(): LogPresenter =
        scope.getInstance(LogPresenter::class.java)

    override fun getHeaderTextRes() = R.string.log_header

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.btnSendReport?.setOnClickListener {
            presenter.onBtnSendReportClicked()
        }
    }

    override fun showLogs(logModels: List<LogModel>) {
        _binding?.tvEmptyList?.visibility = View.INVISIBLE
        _binding?.rvLogs?.visibility = View.VISIBLE

        val adapter = LogAdapter(logModels.asReversed())
        _binding?.rvLogs?.layoutManager = LinearLayoutManager(context)
        _binding?.rvLogs?.adapter = adapter
        _binding?.rvLogs?.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun showNoLogsMessage() {
        _binding?.tvEmptyList?.visibility = View.VISIBLE
        _binding?.rvLogs?.visibility = View.INVISIBLE
    }
}