package com.lua.dsbcafe.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter

class NfcManager(private val activity: Activity) {
    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    private val pendingIntent: PendingIntent = PendingIntent.getActivity(
        activity,
        0,
        Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_MUTABLE,
    )

    private val intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
    private val techLists = arrayOf(arrayOf("android.nfc.tech.IsoDep"))

    val isAvailable: Boolean get() = adapter != null

    fun enableForegroundDispatch() {
        adapter?.enableForegroundDispatch(activity, pendingIntent, intentFilters, techLists)
    }

    fun disableForegroundDispatch() {
        adapter?.disableForegroundDispatch(activity)
    }

    companion object {
        fun extractBadgeId(intent: Intent): String? {
            if (NfcAdapter.ACTION_TECH_DISCOVERED != intent.action) return null
            val tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID) ?: return null
            return tagId.joinToString("") { "%02x".format(it) }
        }
    }
}
