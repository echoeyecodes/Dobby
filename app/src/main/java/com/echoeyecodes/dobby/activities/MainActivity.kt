package com.echoeyecodes.dobby.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.callbacks.dialogfragmentcallbacks.AddUrlDialogFragmentCallback
import com.echoeyecodes.dobby.databinding.ActivityMainBinding
import com.echoeyecodes.dobby.fragments.dialogfragments.AddUrlDialogFragment
import com.echoeyecodes.dobby.fragments.dialogfragments.ProgressDialogFragment
import com.echoeyecodes.dobby.fragments.tabfragments.DownloadListFragment
import com.echoeyecodes.dobby.utils.*
import com.echoeyecodes.dobby.viewmodel.MainActivityViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(),
    AddUrlDialogFragmentCallback {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var fab: FloatingActionButton
    private lateinit var addBtn: View
    private lateinit var progressDialogFragment: ProgressDialogFragment
    private val viewModel by lazy { ViewModelProvider(this)[MainActivityViewModel::class.java] }
    private lateinit var addUrlDialogFragment: AddUrlDialogFragment
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    companion object {
        const val ACTIVITY_START_PERMISSIONS_REQUEST_CODE = 1
        const val EVENT_CLICK_PERMISSIONS_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fab = binding.addUrlBtn
        addBtn = binding.toolbar.btn
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        viewPager.offscreenPageLimit = 2

        progressDialogFragment =
            supportFragmentManager.findFragmentByTag(ProgressDialogFragment.TAG) as ProgressDialogFragment?
                ?: ProgressDialogFragment.newInstance()

        addUrlDialogFragment =
            supportFragmentManager.findFragmentByTag(AddUrlDialogFragment.TAG) as AddUrlDialogFragment?
                ?: AddUrlDialogFragment.newInstance()

        addBtn.setOnClickListener { openInputUrlDialog() }
        fab.setOnClickListener { openInputUrlDialog() }

        val adapter = DownloadListPagerFragment(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            if (position == 0) {
                tab.text = "ACTIVE"
            } else {
                tab.text = "COMPLETE"
            }
        }.attach()

        createNotificationChannel()

        if (!checkSelfPermission(requiredPermissions)) {
            initActivity()
        } else {
            requestPermissions(ACTIVITY_START_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun initActivity() {
        startBackgroundService()

        handleShareToDownloadIntent(intent)

        viewModel.getNetworkState().observe(this) {
            if (it == NetworkState.LOADING) {
                AndroidUtilities.showFragment(supportFragmentManager, progressDialogFragment)
            } else {
                AndroidUtilities.dismissFragment(progressDialogFragment)
            }

            if (it == NetworkState.COMPLETE) {
                viewPager.currentItem = 0
                AndroidUtilities.showToastMessage(this, "Added successfully to downloads")
            }
            if (it == NetworkState.ERROR) {
                AndroidUtilities.showSnackBar(fab, "Failed to fetch data")
            }
        }

    }

    fun openInputUrlDialog() {
        if (checkSelfPermission(requiredPermissions)) {
            AndroidUtilities.showFragment(supportFragmentManager, addUrlDialogFragment)
        } else {
            requestPermissions(EVENT_CLICK_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleShareToDownloadIntent(intent)
    }

    private fun checkSelfPermission(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startService() {
        ServiceUtil.startDownloadService(this)
    }

    private fun requestPermissions(requestCode: Int) {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            requestCode
        )
    }

    private fun startBackgroundService() {
        if (checkSelfPermission(requiredPermissions)) {
            startService()
        } else {
            requestPermissions(ACTIVITY_START_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun handleShareToDownloadIntent(sharedIntent: Intent?) {
        if (sharedIntent != null) {
            if (sharedIntent.action == Intent.ACTION_SEND && sharedIntent.type == "text/plain") {
                val url = sharedIntent.getStringExtra(Intent.EXTRA_TEXT)
                if (url != null) {
                    onUrlAdded(url)
                }
            } else if (sharedIntent.action == Intent.ACTION_VIEW) {
                val url = sharedIntent.data
                if (url != null) {
                    onUrlAdded(url.toString())
                }
            }
        }
    }

    override fun onUrlAdded(url: String) {
        AndroidUtilities.dismissFragment(addUrlDialogFragment)
        viewModel.fetchData(url)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (granted) {
            if (requestCode == EVENT_CLICK_PERMISSIONS_REQUEST_CODE) {
                openInputUrlDialog()
            } else {
                initActivity()
            }
        } else {
            AndroidUtilities.showToastMessage(
                this,
                "You need to accept permissions to download the files"
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_title)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                getString(R.string.notification_channel_id),
                name,
                importance
            ).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    inner class DownloadListPagerFragment(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            val type = if (position == 0) {
                DownloadListPageType.ACTIVE
            } else {
                DownloadListPageType.COMPLETED
            }
            return DownloadListFragment.getInstance(type)
        }

    }

    override fun onBackPressed() {
        if (viewPager.currentItem != 0) {
            viewPager.currentItem = 0
            return
        }
        super.onBackPressed()
    }
}