package com.offsec.nhterm.frontend.floating

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.offsec.nhterm.R
import com.offsec.nhterm.backend.TerminalSession
import com.offsec.nhterm.component.config.DefaultValues.initialCommand
import com.offsec.nhterm.component.session.ShellParameter
import com.offsec.nhterm.component.session.ShellTermSession
import com.offsec.nhterm.frontend.session.terminal.BasicSessionCallback
import com.offsec.nhterm.frontend.session.terminal.BasicViewClient
import com.offsec.nhterm.frontend.session.view.TerminalView
import com.offsec.nhterm.frontend.session.view.TerminalViewClient
import com.offsec.nhterm.utils.Terminals

typealias DialogSessionFinished = (TerminalDialog, TerminalSession?) -> Unit

class TerminalDialog(val context: Context) {
  private val termWindowView = WindowTermView(context)
  private val terminalSessionCallback: BasicSessionCallback
  private var dialog: AlertDialog? = null
  private var terminalSession: TerminalSession? = null
  private var sessionFinishedCallback: DialogSessionFinished? = null
  private var cancelListener: DialogInterface.OnCancelListener? = null

  init {
    termWindowView.setTerminalViewClient(BasicViewClient(termWindowView.terminalView))
    terminalSessionCallback = object : BasicSessionCallback(termWindowView.terminalView) {
      override fun onSessionFinished(finishedSession: TerminalSession?) {
        sessionFinishedCallback?.let { it(this@TerminalDialog, finishedSession) }
        super.onSessionFinished(finishedSession)
      }
    }
  }

  fun execute(executablePath: String, arguments: String, extraarg: String): TerminalDialog {
    if (terminalSession != null) {
      terminalSession?.finishIfRunning()
    }

    dialog = MaterialAlertDialogBuilder(context, R.style.DialogStyle)
      .setView(termWindowView.rootView)
      .setOnCancelListener {
        terminalSession?.finishIfRunning()
        cancelListener?.onCancel(it)
      }
      .create()

    val cmd = listOf(arguments + " " + extraarg + " && exit 0")

    val parameter = ShellParameter()
      .executablePath(executablePath)
      .initialCommand(cmd.joinToString())
      .callback(terminalSessionCallback)
      .systemShell(false)

    terminalSession = Terminals.createSession(context, parameter)
    if (terminalSession is ShellTermSession) {
      (terminalSession as ShellTermSession).exitPrompt = context.getString(R.string.process_exit_prompt_press_back)
    }

    termWindowView.attachSession(terminalSession)
    return this
  }

  fun onDismiss(cancelListener: DialogInterface.OnCancelListener?): TerminalDialog {
    this.cancelListener = cancelListener
    return this
  }

  fun setTitle(title: String?): TerminalDialog {
    dialog?.setTitle(title)
    return this
  }

  fun onFinish(finishedCallback: DialogSessionFinished): TerminalDialog {
    this.sessionFinishedCallback = finishedCallback
    return this
  }

  fun show(title: String?) {
    dialog?.setTitle(title)
    dialog?.setCanceledOnTouchOutside(false)
    dialog?.show()
  }

  fun dismiss(): TerminalDialog {
    dialog?.dismiss()
    return this
  }

  fun imeEnabled(enabled: Boolean): TerminalDialog {
    if (enabled) {
      termWindowView.setInputMethodEnabled(true)
    }
    return this
  }
}

class WindowTermView(val context: Context) {
  @SuppressLint("InflateParams")
  var rootView: View = LayoutInflater.from(context).inflate(R.layout.ui_term_dialog, null, false)
    private set
  var terminalView: TerminalView = rootView.findViewById<TerminalView>(R.id.terminal_view_dialog)
    private set

  init {
    Terminals.setupTerminalView(terminalView)
  }

  fun setTerminalViewClient(terminalViewClient: TerminalViewClient?) {
    terminalView.setTerminalViewClient(terminalViewClient)
  }

  fun attachSession(terminalSession: TerminalSession?) {
    terminalView.attachSession(terminalSession)
  }

  fun setInputMethodEnabled(enabled: Boolean) {
    terminalView.isFocusable = enabled
    terminalView.isFocusableInTouchMode = enabled
  }
}
