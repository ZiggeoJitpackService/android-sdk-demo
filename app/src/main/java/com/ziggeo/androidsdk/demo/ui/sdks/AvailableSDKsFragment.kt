package com.ziggeo.androidsdk.demo.ui.sdks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentAvailableSdksBinding
import com.ziggeo.androidsdk.demo.model.data.feature.FeatureModel
import com.ziggeo.androidsdk.demo.model.data.feature.SdkModel
import com.ziggeo.androidsdk.demo.presentation.availablesdks.AvailableSDKsPresenter
import com.ziggeo.androidsdk.demo.presentation.availablesdks.AvailableSDKsView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class AvailableSDKsFragment : BaseToolbarFragment<AvailableSDKsView, AvailableSDKsPresenter>(),
    AvailableSDKsView {

    override val layoutRes = R.layout.fragment_available_sdks

    @InjectPresenter
    lateinit var presenter: AvailableSDKsPresenter

    private var _binding: FragmentAvailableSdksBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvailableSdksBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @ProvidePresenter
    override fun providePresenter(): AvailableSDKsPresenter =
        scope.getInstance(AvailableSDKsPresenter::class.java)

    override fun getHeaderTextRes() = R.string.sdks_header

    override fun showSdks(clientsList: List<FeatureModel>) {
        val adapter = AvailableSDKsAdapter(clientsList)
        adapter.onItemClickListener = object : AvailableSDKsAdapter.ItemClickListener {
            override fun onItemClick(model: SdkModel) {
                presenter.onClientItemClicked(model)
            }
        }
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    TYPE_CATEGORY -> 2
                    TYPE_ITEM -> 1
                    else -> 1
                }
            }
        }
        _binding?.rvSdks?.layoutManager = layoutManager
        _binding?.rvSdks?.adapter = adapter
    }

}