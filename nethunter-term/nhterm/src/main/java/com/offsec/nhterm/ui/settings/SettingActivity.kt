package com.offsec.nhterm.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import com.offsec.nhterm.R

/**
 * @author Lody
 */
class SettingActivity : BasePreferenceActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.title = getString(R.string.settings)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      addPreferencesFromResource(R.xml.settings_main)
    } else {
      addPreferencesFromResource(R.xml.older_settings_main)
    }
  }

  override fun onBuildHeaders(target: MutableList<Header>?) {
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item?.itemId) {
      android.R.id.home ->
        finish()
    }
    return item?.let { super.onOptionsItemSelected(it) }
  }
}
