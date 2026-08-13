package com.floating.stopwatch

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StopwatchApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCrashLogging(applicationContext)
    }

    private fun setupCrashLogging(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val stringWriter = StringWriter()
                val printWriter = PrintWriter(stringWriter)
                throwable.printStackTrace(printWriter)
                val stackTraceStr = stringWriter.toString()

                val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                val filename = "crash_log_$timestamp.txt"

                // Safely write to external files directory for easy user access without storage permissions
                val dir = context.getExternalFilesDir(null)
                if (dir != null) {
                    if (!dir.exists()) dir.mkdirs()
                    val file = File(dir, filename)
                    val writer = FileWriter(file)
                    writer.write("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
                    writer.write("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
                    writer.write("Thread: ${thread.name}\n")
                    writer.write("Timestamp: $timestamp\n")
                    writer.write("----------------------------------------\n\n")
                    writer.write(stackTraceStr)
                    writer.flush()
                    writer.close()
                    Log.e("StopwatchApp", "CRASH DETECTED! Saved to: ${file.absolutePath}")
                }
            } catch (e: Exception) {
                Log.e("StopwatchApp", "Failed to write crash log: ${e.message}")
            }

            // Fallback to Android system default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
