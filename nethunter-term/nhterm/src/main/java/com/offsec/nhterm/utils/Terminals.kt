package com.offsec.nhterm.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.offsec.nhterm.backend.TerminalSession
import com.offsec.nhterm.component.ComponentManager
import com.offsec.nhterm.component.config.NeoPreference
import com.offsec.nhterm.component.font.FontComponent
import com.offsec.nhterm.component.session.SessionComponent
import com.offsec.nhterm.component.session.ShellParameter
import com.offsec.nhterm.component.session.XParameter
import com.offsec.nhterm.component.session.XSession
import com.offsec.nhterm.frontend.session.view.TerminalView
import com.offsec.nhterm.frontend.session.view.TerminalViewClient
import com.offsec.nhterm.frontend.session.view.extrakey.ExtraKeysView

/**
 * @author kiva
 */
object Terminals {
  fun setupTerminalView(terminalView: TerminalView?, terminalViewClient: com.offsec.nhterm.frontend.session.view.TerminalViewClient? = null) {
    terminalView?.textSize = NeoPreference.getFontSize();

    val fontComponent = ComponentManager.getComponent<FontComponent>()
    fontComponent.applyFont(terminalView, null, fontComponent.getCurrentFont())

    if (terminalViewClient != null) {
      terminalView?.setTerminalViewClient(terminalViewClient)
    }
  }

  fun setupExtraKeysView(extraKeysView: ExtraKeysView?) {
    val fontComponent = ComponentManager.getComponent<FontComponent>()
    val font = fontComponent.getCurrentFont()
    fontComponent.applyFont(null, extraKeysView, font)
  }

  fun createSession(context: Context, parameter: ShellParameter): TerminalSession {
    val sessionComponent = ComponentManager.getComponent<SessionComponent>()
    return sessionComponent.createSession(context, parameter)
  }

  fun createSession(activity: AppCompatActivity, parameter: XParameter): XSession {
    val sessionComponent = ComponentManager.getComponent<SessionComponent>()
    return sessionComponent.createSession(activity, parameter)
  }

  fun escapeString(s: String?): String {
    if (s == null) {
      return ""
    }

    val builder = StringBuilder()
    val specialChars = "\"\\$`!"
    builder.append('"')
    val length = s.length
    for (i in 0 until length) {
      val c = s[i]
      if (specialChars.indexOf(c) >= 0) {
        builder.append('\\')
      }
      builder.append(c)
    }
    builder.append('"')
    return builder.toString()
  }
}
