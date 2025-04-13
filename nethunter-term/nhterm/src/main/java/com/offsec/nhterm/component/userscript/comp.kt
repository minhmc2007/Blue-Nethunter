package com.offsec.nhterm.component.userscript

import android.content.Context
import android.system.Os
import android.util.Log
import com.offsec.nhterm.App
import com.offsec.nhterm.component.NeoComponent
import com.offsec.nhterm.component.config.NeoTermPath
import com.offsec.nhterm.utils.NLog
import com.offsec.nhterm.utils.extractAssetsDir
import com.topjohnwu.superuser.Shell
import java.io.File

class UserScript(val scriptFile: File)

class UserScriptComponent : NeoComponent {
  var userScripts = listOf<UserScript>()
  var binFiles = listOf<UserScript>()
  val scriptDir = File(NeoTermPath.USER_SCRIPT_PATH)
  val binDir = File(NeoTermPath.BIN_PATH)

  override fun onServiceInit() = checkForFiles()

  override fun onServiceDestroy() {
  }

  override fun onServiceObtained() = checkForFiles()

  fun extractDefaultScript(context: Context) = kotlin.runCatching {
    Shell.cmd("mkdir -p /data/data/com.offsec.nhterm/files/usr/").exec()
    Shell.cmd("rm -rf /data/data/com.offsec.nhterm/files/usr/bin/*")

    // Usual user script extraction
    context.extractAssetsDir("scripts", NeoTermPath.USER_SCRIPT_PATH)

    scriptDir.listFiles()?.forEach {
      Os.chmod(it.absolutePath, 448 /*Dec of 0700*/)
    }

    // Lets also extract the usual binaries too here
    context.extractAssetsDir("bin", NeoTermPath.BIN_PATH)
    binDir.listFiles()?.forEach {
      Os.chmod(it.absolutePath, 448 /*Dec of 0700*/)
    }

  }.onFailure {
    NLog.e("UserScript", "Failed to extract default user scripts: ${it.localizedMessage}")
  }

  private fun checkForFiles() {
    extractDefaultScript(App.get())
    reloadScripts()
  }

  private fun reloadScripts() {
    userScripts = scriptDir.listFiles()
      .takeWhile { it.canExecute() }
      .map { UserScript(it) }
      .toList()

    binFiles = binDir.listFiles()
      .takeWhile { it.canExecute() }
      .map { UserScript(it) }
      .toList()
  }
}
