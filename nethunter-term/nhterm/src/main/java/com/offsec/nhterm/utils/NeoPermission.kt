package com.offsec.nhterm.utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.offsec.nhterm.R

/**
 * @author kiva
 */
object NeoPermission {
  const val REQUEST_APP_PERMISSION = 10086

  fun initAppPermission(context: AppCompatActivity, requestCode: Int) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.READ_EXTERNAL_STORAGE
        )
        != PackageManager.PERMISSION_GRANTED) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
          )) {
          MaterialAlertDialogBuilder(context, R.style.DialogStyle).setMessage("Please enable Storage permission")
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
              doRequestPermission(context, requestCode)
            }.show()

        } else {
          doRequestPermission(context, requestCode)
        }
      }
    }
  }

  private fun doRequestPermission(context: AppCompatActivity, requestCode: Int) {
    try {
        ActivityCompat.requestPermissions(
          context,
          arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
          requestCode)
    }  catch (ignore: ActivityNotFoundException) {
    // for MIUI, we ignore it.
  }
}
