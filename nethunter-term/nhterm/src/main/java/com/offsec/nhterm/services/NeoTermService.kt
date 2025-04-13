package com.offsec.nhterm.services

import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.icu.util.TimeUnit
import android.net.wifi.WifiManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.offsec.nhterm.R
import com.offsec.nhterm.backend.EmulatorDebug
import com.offsec.nhterm.backend.TerminalSession
import com.offsec.nhterm.component.config.NeoTermPath
import com.offsec.nhterm.component.session.ShellParameter
import com.offsec.nhterm.component.session.XParameter
import com.offsec.nhterm.component.session.XSession
import com.offsec.nhterm.setup.SetupHelper
import com.offsec.nhterm.ui.other.AboutActivity
import com.offsec.nhterm.ui.term.NeoTermActivity
import com.offsec.nhterm.utils.NLog
import com.offsec.nhterm.utils.Terminals
import com.offsec.nhterm.utils.extractAssetsDir
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Process
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService


/**
 * @author kiva
 */

class NeoTermService : Service() {
  inner class NeoTermBinder : Binder() {
    var service = this@NeoTermService
  }

  private val serviceBinder = NeoTermBinder()
  private val mTerminalSessions = ArrayList<TerminalSession>()
  private val mXSessions = ArrayList<XSession>()
  private var mWakeLock: PowerManager.WakeLock? = null
  private var mWifiLock: WifiManager.WifiLock? = null

  override fun onCreate() {
    super.onCreate()

    // Check whather we need to populate initial boot scripts or not
    // By simply checking */usr folder of ours
    if (!checkPrefix()) {
      resetApp()
    }

    createNotificationChannel()
    startForeground(NOTIFICATION_ID, createNotification())
  }

  override fun onBind(intent: Intent): IBinder? {
    return serviceBinder
  }

  fun resetApp() {
    // Manual way of resetting required assets
    Runtime.getRuntime().exec("mkdir -p "+" "+"/data/data/com.offsec.nhterm/files/usr/").waitFor()
    Executer("/system/bin/rm -rf /data/data/com.offsec.nhterm/files/usr/bin")
    Thread.sleep(1200)
    extractAssetsDir("bin", "/data/data/com.offsec.nhterm/files/usr/bin/")
    Thread.sleep(800)
    Executer("/system/bin/chmod +x /data/data/com.offsec.nhterm/files/usr/bin/bash") // Static bash for arm ( works for *64 too )
    Executer("/system/bin/chmod +x /data/data/com.offsec.nhterm/files/usr/bin/kali") // Kali chroot scriptlet
    Executer("/system/bin/chmod +x /data/data/com.offsec.nhterm/files/usr/bin/android-su") // Android su scriptlet
  }

  fun Executer(command: String?): String? {
    val output = StringBuilder()
    val p: Process
    try {
      p = Runtime.getRuntime().exec(command)
      p.waitFor()
      val reader = BufferedReader(InputStreamReader(p.inputStream))
      var line: String?
      while (reader.readLine().also { line = it } != null) {
        output.append(line).append('\n')
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return output.toString()
  }
  fun checkPrefix(): Boolean {
    val PREFIX_FILE = File(NeoTermPath.USR_PATH)
    return !PREFIX_FILE.isDirectory
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val action = intent.action
    when (action) {
      ACTION_SERVICE_STOP -> {
        for (i in mTerminalSessions.indices)
          mTerminalSessions[i].finishIfRunning()
        stopSelf()
      }

      ACTION_ACQUIRE_LOCK -> acquireLock()

      ACTION_RELEASE_LOCK -> releaseLock()
    }

    return Service.START_NOT_STICKY
  }

  override fun onDestroy() {
    stopForeground(true)

    for (i in mTerminalSessions.indices)
      mTerminalSessions[i].finishIfRunning()
    mTerminalSessions.clear()
  }

  val sessions: List<TerminalSession>
    get() = mTerminalSessions

  val xSessions: List<XSession>
    get() = mXSessions

  fun createTermSession(parameter: ShellParameter): TerminalSession {
    val session = createOrFindSession(parameter)
    updateNotification()
    return session
  }

  fun removeTermSession(sessionToRemove: TerminalSession): Int {
    val indexOfRemoved = mTerminalSessions.indexOf(sessionToRemove)
    if (indexOfRemoved >= 0) {
      mTerminalSessions.removeAt(indexOfRemoved)
      updateNotification()
    }
    return indexOfRemoved
  }

  fun createXSession(activity: AppCompatActivity, parameter: XParameter): XSession {
    val session = Terminals.createSession(activity, parameter)
    mXSessions.add(session)
    updateNotification()
    return session
  }

  fun removeXSession(sessionToRemove: XSession): Int {
    val indexOfRemoved = mXSessions.indexOf(sessionToRemove)
    if (indexOfRemoved >= 0) {
      mXSessions.removeAt(indexOfRemoved)
      updateNotification()
    }
    return indexOfRemoved
  }

  private fun createOrFindSession(parameter: ShellParameter): TerminalSession {
    if (parameter.willCreateNewSession()) {
      NLog.d("createOrFindSession: creating new session")
      val session = Terminals.createSession(this, parameter)
      mTerminalSessions.add(session)
      return session
    }

    val sessionId = parameter.sessionId!!
    NLog.d("createOrFindSession: find session by id $sessionId")

    val session = mTerminalSessions.find { it.mHandle == sessionId.sessionId }
      ?: throw IllegalArgumentException("cannot find session by given id")

    if (parameter.initialCommand?.isNotEmpty() == true) {
      session.write(parameter.initialCommand + "\n")
    }
    return session
  }

  private fun updateNotification() {
    val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    service.notify(NOTIFICATION_ID, createNotification())
  }

  private fun createNotification(): Notification {
    val notifyIntent = Intent(this, NeoTermActivity::class.java)
    notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, FLAG_IMMUTABLE)

    val sessionCount = mTerminalSessions.size
    val xSessionCount = mXSessions.size
    var contentText = getString(R.string.service_status_text, sessionCount)

    val lockAcquired = mWakeLock != null
    if (lockAcquired) contentText += getString(R.string.service_lock_acquired)

    val builder = NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
    builder.setContentTitle(getText(R.string.app_name))
    builder.setContentText(contentText)
    builder.setSmallIcon(R.drawable.ic_notification_icon)
    builder.setContentIntent(pendingIntent)
    builder.setOngoing(true)
    builder.setShowWhen(false)
    builder.color = 0xFF000000.toInt()

    builder.priority = if (lockAcquired) Notification.PRIORITY_HIGH else Notification.PRIORITY_LOW

    val exitIntent = Intent(this, NeoTermService::class.java).setAction(ACTION_SERVICE_STOP)
    builder.addAction(
      android.R.drawable.ic_delete,
      getString(R.string.exit),
      PendingIntent.getService(this, 0, exitIntent, FLAG_IMMUTABLE)
    )

    val newWakeAction = if (lockAcquired) ACTION_RELEASE_LOCK else ACTION_ACQUIRE_LOCK
    val toggleWakeLockIntent = Intent(this, NeoTermService::class.java).setAction(newWakeAction)
    val actionTitle = getString(
      if (lockAcquired)
        R.string.service_release_lock
      else
        R.string.service_acquire_lock
    )
    val actionIcon = if (lockAcquired) android.R.drawable.ic_lock_idle_lock else android.R.drawable.ic_lock_lock
    builder.addAction(actionIcon, actionTitle, PendingIntent.getService(this, 0, toggleWakeLockIntent, FLAG_IMMUTABLE))

    return builder.build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val channel = NotificationChannel(DEFAULT_CHANNEL_ID, "NetHunter", NotificationManager.IMPORTANCE_LOW)
    channel.description = "NetHunter notifications"
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
  }

  @SuppressLint("WakelockTimeout")
  private fun acquireLock() {
    if (mWakeLock == null) {
      val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
      mWakeLock = pm.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK,
        EmulatorDebug.LOG_TAG + ":"
      )
      mWakeLock!!.acquire()

      val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
      mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, EmulatorDebug.LOG_TAG)
      mWifiLock!!.acquire()

      updateNotification()
    }
  }

  private fun releaseLock() {
    if (mWakeLock != null) {
      mWakeLock!!.release()
      mWakeLock = null

      mWifiLock!!.release()
      mWifiLock = null

      updateNotification()
    }
  }

  companion object {
    val ACTION_SERVICE_STOP = "neoterm.action.service.stop"
    val ACTION_ACQUIRE_LOCK = "neoterm.action.service.lock.acquire"
    val ACTION_RELEASE_LOCK = "neoterm.action.service.lock.release"
    private val NOTIFICATION_ID = 52019

    val DEFAULT_CHANNEL_ID = "neoterm_notification_channel"
  }
}
