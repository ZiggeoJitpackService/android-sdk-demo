package com.ziggeo.androidsdk.demo.ui.topclients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.databinding.FragmentTopClientsBinding
import com.ziggeo.androidsdk.demo.model.data.feature.ClientModel
import com.ziggeo.androidsdk.demo.presentation.topclients.TopClientsPresenter
import com.ziggeo.androidsdk.demo.presentation.topclients.TopClientsView
import com.ziggeo.androidsdk.demo.ui.global.BaseToolbarFragment


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class TopClientsFragment : BaseToolbarFragment<TopClientsView, TopClientsPresenter>(),
    TopClientsView {
    private var _binding: FragmentTopClientsBinding? = null
    private val binding get() = _binding!!
    override val layoutRes = R.layout.fragment_top_clients

    @InjectPresenter
    lateinit var presenter: TopClientsPresenter

    @ProvidePresenter
    override fun providePresenter(): TopClientsPresenter =
        scope.getInstance(TopClientsPresenter::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopClientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getHeaderTextRes() = R.string.clients_header

    override fun showClients(clientsList: List<ClientModel>) {
        val adapter = TopClientsAdapter(clientsList)
        adapter.onItemClickListener = object : TopClientsAdapter.ItemClickListener {
            override fun onItemClick(model: ClientModel) {
                presenter.onClientItemClicked(model)
            }
        }
        binding.rvClients.layoutManager = GridLayoutManager(context, 2)
        binding.rvClients.adapter = adapter
    }

}