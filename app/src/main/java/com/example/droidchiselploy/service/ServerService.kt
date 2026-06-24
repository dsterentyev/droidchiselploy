package com.example.droidchiselploy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.system.Os
import android.system.OsConstants
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager.*
import com.example.droidchiselploy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.reflect.Field

class ServerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var chiselProcess: Process? = null
    private var notificationManager: NotificationManager? = null
    private val CHANNEL_ID = "server_service_channel"
    private val NOTIFICATION_ID = 1

    companion object {
        const val ACTION_SERVER_STATUS = "com.example.droidchiselploy.SERVER_STATUS"
        const val EXTRA_STATUS_MESSAGE = "status_message"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(NotificationManager::class.java)
        startForegroundService()
        val command = intent?.getStringExtra("COMMAND") ?: "client"

        if (command != null) {
            serviceScope.launch {
                try {
                    updateNotification("= Starting service...")
                    startChiselServer(command)
                    delay(2000) // Wait for start
                    updateNotification("✓ Service is live")
                    // Send broadcast with server URL
                    sendServerStatusBroadcast("Chisel started")
                    val retval = chiselProcess?.waitFor() // Blocks until process terminates
                    sendServerStatusBroadcast("Chisel exited with code $retval")
                    updateNotification("✗ Service is exited")
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateNotification("✗ Starting service failed: ${e.message}")
                    sendServerStatusBroadcast("Chisel process failed: ${e.message}")
                }
            }
        }

        return START_STICKY
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Server Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DroidChiselPloy Service")
            .setContentText("Initializing...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DroidChiselPloy Service")
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun startChiselServer(command: String) {
        try {
            val nativeDir = applicationInfo.nativeLibraryDir
            var libName = "libdroidchiselploy.so";
            val chiselExecutable = File(nativeDir, libName).absolutePath

            // Parse command to extract arguments
            val parts = command.trim().split("\\s+".toRegex())
            val args = if (parts[0].equals("chisel", ignoreCase = true)) {
                // Replace "chisel" with actual path
                listOf(chiselExecutable) + parts.drop(1)
            } else {
                // Direct execution
                listOf(chiselExecutable) + parts
            }
            //killing stale service process still running from previous app run. so crude, huh?
            killProcessByName(libName)
            val processBuilder = ProcessBuilder(args)
            processBuilder.redirectErrorStream(true)
            val env = processBuilder.environment()
            val process = processBuilder.start()
            chiselProcess = process

            // Log output
            /*
            serviceScope.launch {
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    println("chisel: $line")
                }
            }

             */
        } catch (e: Exception) {
            e.printStackTrace()
            updateNotification("✗ startChiselServer failed: ${e.message}")
        }
    }

    private fun sendServerStatusBroadcast(message: String) {
        val intent = Intent(ACTION_SERVER_STATUS)
        intent.putExtra(EXTRA_STATUS_MESSAGE, message)
        sendBroadcast(intent)
        getInstance(this).sendBroadcast(intent)
        android.util.Log.d("ServerService", "Broadcast sent: $message")
    }

    fun killProcessByName(binaryName: String) {
        try {
            // Use the native Android shell tool 'pkill' targeted to your binary name
            val pkillProcess = Runtime.getRuntime().exec(arrayOf("pkill", "-f", binaryName))
            pkillProcess.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNativeSubprocessPid(process: java.lang.Process): Int {
        return try {
            // In Android, the underlying implementation is usually a UNIXProcess or ProcessImpl
            val field: Field = process.javaClass.getDeclaredField("pid")
            field.isAccessible = true
            field.getInt(process)
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // Return -1 if reflection fails
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        val pid = getNativeSubprocessPid(chiselProcess?)
        chiselProcess?.destroy()
        updateNotification("Service stopped")
    }
}
