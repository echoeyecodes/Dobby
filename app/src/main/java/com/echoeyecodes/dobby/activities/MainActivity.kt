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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.dobby.R
import com.echoeyecodes.dobby.adapters.EmptyAdapter
import com.echoeyecodes.dobby.adapters.FileItemAdapter
import com.echoeyecodes.dobby.callbacks.adaptercallbacks.EmptyAdapterCallback
import com.echoeyecodes.dobby.callbacks.adaptercallbacks.FileItemAdapterCallback
import com.echoeyecodes.dobby.callbacks.dialogfragmentcallbacks.AddUrlDialogFragmentCallback
import com.echoeyecodes.dobby.callbacks.downloadmanagercallbacks.DownloadManagerCallback
import com.echoeyecodes.dobby.databinding.ActivityMainBinding
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.echoeyecodes.dobby.fragments.bottomsheets.DownloadActionBottomSheetFragment
import com.echoeyecodes.dobby.fragments.dialogfragments.AddUrlDialogFragment
import com.echoeyecodes.dobby.fragments.dialogfragments.ProgressDialogFragment
import com.echoeyecodes.dobby.models.EmptyModel
import com.echoeyecodes.dobby.utils.*
import com.echoeyecodes.dobby.viewmodel.MainActivityViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), EmptyAdapterCallback, FileItemAdapterCallback,
    AddUrlDialogFragmentCallback, DownloadManagerCallback {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val downloadManager by lazy { DownloadManager.getInstance(this) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var addBtn: View
    private lateinit var progressDialogFragment: ProgressDialogFragment
    private val viewModel by lazy { ViewModelProvider(this)[MainActivityViewModel::class.java] }
    private lateinit var addUrlDialogFragment: AddUrlDialogFragment
    private lateinit var fileAdapter: FileItemAdapter

    private val requiredPermissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE
        )
    }else{
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        if (checkSelfPermission(requiredPermissions)) {
            initActivity()
        } else {
            requestPermissions()
        }
    }

    private fun initActivity() {
        setContentView(binding.root)

        fab = binding.addUrlBtn
        recyclerView = binding.recyclerView
        addBtn = binding.toolbar.btn

        downloadManager.addDownloadManagerCallback(this)
        progressDialogFragment =
            supportFragmentManager.findFragmentByTag(ProgressDialogFragment.TAG) as ProgressDialogFragment?
                ?: ProgressDialogFragment.newInstance()

        addUrlDialogFragment =
            supportFragmentManager.findFragmentByTag(AddUrlDialogFragment.TAG) as AddUrlDialogFragment?
                ?: AddUrlDialogFragment.newInstance()

        val itemDecoration = CustomItemDecoration(0, 0)
        val layoutManager = LinearLayoutManager(this)
        fileAdapter = FileItemAdapter(this)
        val emptyAdapter = EmptyAdapter(this)

        val emptyModel = EmptyModel(
            R.drawable.ic_empty,
            "Dedicated space for active and pending downloads",
            "Add File"
        )
        val adapter = ConcatAdapter(emptyAdapter, fileAdapter)

        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(itemDecoration)
        recyclerView.adapter = adapter

        addBtn.setOnClickListener { openInputUrlDialog() }
        fab.setOnClickListener { openInputUrlDialog() }

        viewModel.getFilesLiveData().observe(this) {
            if (it.isEmpty()) {
                emptyAdapter.submitList(listOf(emptyModel))
            } else {
                emptyAdapter.submitList(ArrayList())
            }
            fileAdapter.submitList(it)
        }
        startBackgroundService()

        handleShareToDownloadIntent(intent)

        viewModel.getNetworkState().observe(this) {
            if (it == NetworkState.LOADING) {
                AndroidUtilities.showFragment(supportFragmentManager, progressDialogFragment)
            } else {
                AndroidUtilities.dismissFragment(progressDialogFragment)
            }

            if (it == NetworkState.COMPLETE) {
                scrollToTop()
            }
            if (it == NetworkState.ERROR) {
                AndroidUtilities.showSnackBar(recyclerView, "Failed to fetch data")
            }
        }

    }

    private fun scrollToTop() {
        lifecycleScope.launchWhenResumed {
            withContext(Dispatchers.IO) {
                delay(100)
                withContext(Dispatchers.Main) {
                    recyclerView.smoothScrollToPosition(0)
                }
            }
        }
    }

    private fun openInputUrlDialog() {
        AndroidUtilities.showFragment(supportFragmentManager, addUrlDialogFragment)
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

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions,
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun startBackgroundService() {
        if (checkSelfPermission(requiredPermissions)) {
            startService()
        } else {
            requestPermissions()
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
            initActivity()
        } else {
            AndroidUtilities.showToastMessage(
                this,
                "You need to accept permissions to download the files"
            )
            finish()
        }
    }

    override fun onMorePressed(id: String) {
        val actionBottomSheetFragment = DownloadActionBottomSheetFragment.newInstance(id)
        if (!actionBottomSheetFragment.isVisible) {
            actionBottomSheetFragment.show(
                supportFragmentManager,
                DownloadActionBottomSheetFragment.TAG
            )
        }
    }

    override fun onItemPressed(model: FileDBModel) {
        if (model.status != DownloadStatus.COMPLETE) {
            AndroidUtilities.showSnackBar(recyclerView, "Download not complete")
        } else {
            model.path?.let {
                AndroidUtilities.openFile(this, it)
            }
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

    override fun onButtonPressed() {
        openInputUrlDialog()
    }

    private fun updateDownloadProgress(id: String, bytesRead: Long, size: Long) {
        val currentList = fileAdapter.currentList
        val position = currentList.indexOfFirst { it.id == id }
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        viewHolder?.let {
            if (it is FileItemAdapter.FileItemAdapterViewHolder) {
                it.updateProgress(bytesRead, size)
            }
        }
    }

    override fun onDownloadStarted(id: String) {

    }

    override fun onPathDetermined(id: String, path: String) {

    }

    override fun onDownloadProgress(id: String, bytesRead: Long, total: Long) {
        updateDownloadProgress(id, bytesRead, total)
    }

    override fun onDownloadComplete(id: String) {

    }

    override fun onDownloadCancelled(id: String, exception: Exception) {

    }

    override fun onDownloadsFinished() {

    }
}