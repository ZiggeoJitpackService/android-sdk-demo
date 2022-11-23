package com.ziggeo.androidsdk.demo.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.firebase.analytics.ktx.logEvent
import com.ziggeo.androidsdk.demo.R
import com.ziggeo.androidsdk.demo.Screens
import com.ziggeo.androidsdk.demo.databinding.FragmentDrawerFlowBinding
import com.ziggeo.androidsdk.demo.presentation.main.MainFlowPresenter
import com.ziggeo.androidsdk.demo.presentation.main.MainFlowView
import com.ziggeo.androidsdk.demo.presentation.main.MainFlowView.MenuItem.*
import com.ziggeo.androidsdk.demo.ui.global.BaseFlowFragment
import com.ziggeo.androidsdk.demo.ui.global.BaseFragment
import com.ziggeo.androidsdk.demo.ui.global.MessageDialogFragment


/**
 * Created by Alexander Bedulin on 25-Sep-19.
 * Ziggeo, Inc.
 * alexb@ziggeo.com
 */
class MainFlowFragment : BaseFlowFragment(), MainFlowView,
    MessageDialogFragment.OnClickListener {
    private var _binding: FragmentDrawerFlowBinding? = null
    private val binding get() = _binding!!

    override val layoutRes = R.layout.fragment_drawer_flow

    @InjectPresenter
    lateinit var presenter: MainFlowPresenter

    @ProvidePresenter
    fun providePresenter(): MainFlowPresenter =
        scope.getInstance(MainFlowPresenter::class.java)

    private val currentFragment
        get() = childFragmentManager.findFragmentById(R.id.main_container) as? BaseFragment

    private val itemClickListener = { view: View ->
        val menuItem = view.tag as MainFlowView.MenuItem
        presenter.onMenuItemClick(menuItem)
        selectMenuItem(menuItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawerFlowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getContainerId() = R.id.main_container

    override fun getLaunchScreen() = Screens.Recordings

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
        initDrawerMenu()
    }

    private fun openNavDrawer(open: Boolean) {
        analytics.logEvent("drawer_open") {
            param("open", open.toString())
        }
        if (open) binding.drawer.openDrawer(GravityCompat.START)
        else binding.drawer.closeDrawer(GravityCompat.START)
    }

    override fun showAccName(appToken: String?) {
        binding.drawerNav.tvAppToken.text = appToken
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            openNavDrawer(false)
        } else {
            currentFragment?.onBackPressed() ?: router.exit()
        }
    }

    override fun openDrawer() {
        openNavDrawer(true)
    }

    override fun closeDrawer() {
        openNavDrawer(false)
    }

    override fun openAuthScreen() {
        router.newRootFlow(Screens.AuthFlow)
    }

    override fun openRecordingsScreen() {
        router.newRootScreen(Screens.Recordings)
    }

    override fun openSettingsScreen() {
        router.newRootScreen(Screens.Settings)
    }

    override fun openLogScreen() {
        router.newRootScreen(Screens.Log)
    }

    override fun openSdksScreen() {
        router.newRootScreen(Screens.AvailableSdks)
    }

    override fun openClientsScreen() {
        router.newRootScreen(Screens.TopClients)
    }

    override fun openContactUsScreen() {
        router.newRootScreen(Screens.ContactUs)
    }

    override fun openAboutScreen() {
        router.newRootScreen(Screens.About)
    }

    override fun openVideoEditorScreen() {
        router.newRootScreen(Screens.VideoEditor)
    }

    override fun selectMenuItem(item: MainFlowView.MenuItem) {
        (0 until binding.drawerNav.drawerMenuContainer.childCount)
            .map { binding.drawerNav.drawerMenuContainer.getChildAt(it) }
            .forEach { menuItem -> menuItem.tag?.let { menuItem.isSelected = it == item } }
    }

    override fun dialogPositiveClicked(tag: String) {
        when (tag) {
            CONFIRM_LOGOUT_TAG -> presenter.onLogoutClick()
        }
    }

    private fun initToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener { openNavDrawer(true) }
        }
    }

    private fun initDrawerMenu() {
        binding.drawerNav.ivLogout.setOnClickListener {
            MessageDialogFragment.create(
                message = getString(R.string.logout_message),
                positive = getString(R.string.common_yes),
                negative = getString(R.string.common_no),
                tag = CONFIRM_LOGOUT_TAG
            ).show(childFragmentManager, CONFIRM_LOGOUT_TAG)
        }

        binding.drawerNav.miRecordings.tag = RECORDINGS
        binding.drawerNav.miSettings.tag = SETTINGS
        binding.drawerNav.miSdks.tag = SDKS
        binding.drawerNav.miClients.tag = CLIENTS
        binding.drawerNav.miContact.tag = CONTACT_US
        binding.drawerNav.miAbout.tag = ABOUT
        binding.drawerNav.miLog.tag = LOG

        binding.drawerNav.miRecordings.setOnClickListener(itemClickListener)
        binding.drawerNav.miSettings.setOnClickListener(itemClickListener)
        binding.drawerNav.miSdks.setOnClickListener(itemClickListener)
        binding.drawerNav.miClients.setOnClickListener(itemClickListener)
        binding.drawerNav.miContact.setOnClickListener(itemClickListener)
        binding.drawerNav.miAbout.setOnClickListener(itemClickListener)
        binding.drawerNav.miLog.setOnClickListener(itemClickListener)

        selectMenuItem(RECORDINGS)
    }

    private companion object {
        private const val CONFIRM_LOGOUT_TAG = "confirm_logout_tag"
    }
}