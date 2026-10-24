package com.rusroute.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.BufferedReader
import java.io.StringReader

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        Thread {
            try {
                val prefs = context.getSharedPreferences("rusroute", Context.MODE_PRIVATE)
                val rawConfig = prefs.getString("wireguard_config", null) ?: return@Thread
                // Android requires the user to approve VPN access in the app first.
                if (VpnService.prepare(context) != null) return@Thread

                val config = Config.parse(BufferedReader(StringReader(rawConfig)))
                val tunnel = object : Tunnel {
                    override fun getName(): String = "RusRoute"
                    override fun onStateChange(newState: Tunnel.State) {
                        Log.i("RusRouteBoot", "Tunnel state: $newState")
                    }
                }
                GoBackend(context).setState(tunnel, Tunnel.State.UP, config)
            } catch (e: Exception) {
                Log.e("RusRouteBoot", "Could not auto-connect after boot", e)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }
}
