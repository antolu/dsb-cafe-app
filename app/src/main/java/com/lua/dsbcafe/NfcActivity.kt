package com.lua.dsbcafe

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class NfcActivity : Activity(){
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null

    interface OnNfcTagReadListener {
        fun onNfcTagRead(badgeId: String)
    }

    private var listener: OnNfcTagReadListener? = null

    fun setOnNfcTagReadListener(listener: OnNfcTagReadListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        Log.d("NfcActivity", "NFC Adapter detected: $nfcAdapter")

        // Create a PendingIntent that will start this activity
        val intent = Intent(this, this::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val techIntentFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        intentFiltersArray = arrayOf(techIntentFilter)

    }

    override fun onResume() {
        super.onResume()
        Log.d("NfcActivity", "onResume")

        // Enable foreground dispatch to receive NFC intents while your activity is in the foreground
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null)
    }

    override fun onPause() {
        super.onPause()
        Log.d("NfcActivity", "onPause")

        // Disable foreground dispatch when your activity is not in the foreground
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("NfcActivity", "New intent received: $intent")

        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
            val tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
            val badgeId = tagId?.joinToString("") { "%02x".format(it) }

            listener?.onNfcTagRead(badgeId ?: "")
        }
    }
}