package com.akarbowy.demodigitalframe

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_digital_frame.*


class DigitalFrameActivity : AppCompatActivity() {

    private val viewModel: DigitalFrameViewModel by viewModels()

    private val frameController: DigitalFrameController by lazy {
        DigitalFrameController(lifecycle, video, image)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_frame)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.files.observe(this, Observer<List<MediaFile>> { files ->
            onFilesReceived(files)
        })

        if (isStoragePermissionGranted()) {
            viewModel.loadFiles()
        } else {
            requestStoragePermission()
        }
    }

    private fun onFilesReceived(data: List<MediaFile>) {
        frameController.setMedia(data)
        frameController.play()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PERMISSION_GRANTED) {
                    onStoragePermissionGranted()
                } else {
                    onStoragePermissionDenied()
                }
                return
            }
        }
    }

    private fun onStoragePermissionGranted() {
        viewModel.loadFiles()
    }

    private fun onStoragePermissionDenied() {
        Toast.makeText(
            this,
            "Grant storage permission in the settings or enjoy the darkness.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun isStoragePermissionGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED

    private fun requestStoragePermission() {
        if (!isStoragePermissionGranted()) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_READ_EXTERNAL_STORAGE)
        }
    }

    companion object {

        private const val REQUEST_READ_EXTERNAL_STORAGE = 1234

    }

}


