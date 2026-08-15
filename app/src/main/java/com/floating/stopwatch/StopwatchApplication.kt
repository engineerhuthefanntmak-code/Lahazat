package com.floating.stopwatch

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class StopwatchApplication : Application() {

    override fun onCreate() {
        // Register UncaughtExceptionHandler at the very first line of Application startup before everything else
        setupVisualCrashLogging(applicationContext)
        super.onCreate()
    }

    private fun setupVisualCrashLogging(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val stringWriter = StringWriter()
                val printWriter = PrintWriter(stringWriter)
                throwable.printStackTrace(printWriter)
                val fullStackTraceStr = stringWriter.toString()

                // Limit trace lines to simplify diagnostics
                val lines = fullStackTraceStr.split("\n")
                val limitedTrace = lines.take(25).joinToString("\n")

                Log.e("StopwatchApp", "CRASH REGISTERED: $limitedTrace")

                // Start CrashReportActivity safely
                val intent = Intent(context, CrashReportActivity::class.java).apply {
                    putExtra("error_stack_trace", "Device: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n\n$limitedTrace")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)

            } catch (e: Exception) {
                Log.e("StopwatchApp", "Failed to start CrashReportActivity: ${e.message}")
            }

            // Pass uncaught exception to system default handler to safely transition to CrashReportActivity
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
