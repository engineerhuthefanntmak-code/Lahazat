package com.floating.stopwatch

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class CrashReportActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fully independent plain Java/Kotlin Layout with no XML dependence
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(32, 32, 32, 32)
        }

        val titleView = TextView(this).apply {
            text = "APPLICATION CRASH DETECTED"
            setTextColor(Color.RED)
            textSize = 18f
            setPadding(0, 0, 0, 24)
        }
        rootLayout.addView(titleView)

        val stackTrace = intent.getStringExtra("error_stack_trace") ?: "No Stacktrace Available"

        val copyButton = Button(this).apply {
            text = "COPY STACK TRACE"
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Crash Stacktrace", stackTrace)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@CrashReportActivity, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
        rootLayout.addView(copyButton)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }

        val errorDetails = TextView(this).apply {
            text = stackTrace
            setTextColor(Color.WHITE)
            textSize = 13f
            setTextIsSelectable(true) // enables selecting and copying easily
        }

        scrollView.addView(errorDetails)
        rootLayout.addView(scrollView)

        setContentView(rootLayout)
    }
}
