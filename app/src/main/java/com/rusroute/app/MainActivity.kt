package com.rusroute.app

import android.app.Activity
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.net.VpnService
import android.os.Bundle
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader

class MainActivity : Activity() {
    private lateinit var backend: GoBackend
    private lateinit var status: TextView
    private var tunnelConfig: Config? = null
    private val prefs by lazy { getSharedPreferences("rusroute", MODE_PRIVATE) }
    private val tunnel = object : Tunnel {
        override fun getName(): String = "RusRoute"
        override fun onStateChange(newState: Tunnel.State) {
            runOnUiThread {
                status.text = if (newState == Tunnel.State.UP) "Durum: VPN bağlı" else "Durum: VPN bağlı değil"
            }
        }
    }

    companion object {
        private const val PICK_CONFIG = 100
        private const val VPN_PERMISSION = 101
        private const val CONFIG_KEY = "wireguard_config"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backend = GoBackend(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 40, 28, 28)
            setBackgroundColor(Color.rgb(247, 248, 250))
        }
        val title = TextView(this).apply {
            text = "RusRoute"
            textSize = 30f
            setTextColor(Color.rgb(30, 50, 85))
            gravity = Gravity.CENTER
        }
        val intro = TextView(this).apply {
            text = "Rusya VPN bağlantısı"
            textSize = 18f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 24)
        }
        status = TextView(this).apply {
            text = "Durum: VPN bağlı değil"
            textSize = 16f
            setTextColor(Color.rgb(70, 90, 70))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 24)
        }
        val import = Button(this).apply {
            text = "Rusya VPN yapılandırması seç (.conf)"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(intent, PICK_CONFIG)
            }
        }
        val vlessList = Button(this).apply {
            text = "Ücretsiz sunucu listesini kopyala (VLESS)"
            setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(
                    "RusRoute free VLESS subscription",
                    "https://raw.githubusercontent.com/aviamastersgh/vpn-free-russia/main/verified_configs.txt"
                ))
                Toast.makeText(this@MainActivity, "Liste bağlantısı kopyalandı. Bu liste için VLESS destekli motor gerekir; mevcut WireGuard bağlantısı bunu çalıştıramaz.", Toast.LENGTH_LONG).show()
            }
        }
        val connect = Button(this).apply {
            text = "Bağlan"
            setOnClickListener { requestVpnPermissionAndConnect() }
        }
        val disconnect = Button(this).apply {
            text = "Bağlantıyı kes"
            setOnClickListener { disconnectTunnel() }
        }
        val note = TextView(this).apply {
            text = "Otomatik bağlanma: Kaydedilmiş WireGuard yapılandırması varsa uygulama açıldığında bağlantı başlatılır. İlk kullanımda geçerli .conf dosyası ve Android VPN izni gerekir. Rusya IP'si için sunucunun Rusya konumunda olması gerekir."
            textSize = 14f
            setTextColor(Color.rgb(100, 100, 100))
            setPadding(0, 28, 0, 0)
        }
        root.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(intro)
        root.addView(status)
        root.addView(import, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(vlessList, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(connect, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(disconnect, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(note)
        setContentView(root)

        loadSavedConfig()
        if (tunnelConfig != null) {
            status.post { requestVpnPermissionAndConnect() }
        }
    }

    private fun loadSavedConfig() {
        val saved = prefs.getString(CONFIG_KEY, null) ?: return
        try {
            tunnelConfig = Config.parse(BufferedReader(StringReader(saved)))
        } catch (_: Exception) {
            tunnelConfig = null
            status.text = "Durum: Kaydedilmiş yapılandırma okunamadı"
        }
    }

    private fun requestVpnPermissionAndConnect() {
        if (tunnelConfig == null) {
            Toast.makeText(this, "Önce geçerli bir WireGuard .conf dosyası seç.", Toast.LENGTH_LONG).show()
            return
        }
        val permissionIntent = VpnService.prepare(this)
        if (permissionIntent != null) startActivityForResult(permissionIntent, VPN_PERMISSION)
        else connectTunnel()
    }

    @Deprecated("Deprecated in Android, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONFIG && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try {
                val rawConfig = contentResolver.openInputStream(uri).use { stream ->
                    if (stream == null) throw IllegalArgumentException("Dosya açılamadı")
                    BufferedReader(InputStreamReader(stream)).use { it.readText() }
                }
                tunnelConfig = Config.parse(BufferedReader(StringReader(rawConfig)))
                prefs.edit().putString(CONFIG_KEY, rawConfig).apply()
                Toast.makeText(this, "Yapılandırma kaydedildi. Bundan sonraki açılışlarda otomatik bağlanacak.", Toast.LENGTH_LONG).show()
                requestVpnPermissionAndConnect()
            } catch (_: Exception) {
                tunnelConfig = null
                prefs.edit().remove(CONFIG_KEY).apply()
                Toast.makeText(this, "Yapılandırma okunamadı. Geçerli bir WireGuard .conf dosyası seç.", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == VPN_PERMISSION && resultCode == RESULT_OK) {
            connectTunnel()
        } else if (requestCode == VPN_PERMISSION) {
            status.text = "Durum: VPN izni verilmedi"
        }
    }

    private fun connectTunnel() {
        val config = tunnelConfig ?: return
        try {
            backend.setState(tunnel, Tunnel.State.UP, config)
            status.text = "Durum: VPN bağlantısı başlatıldı"
        } catch (_: Exception) {
            status.text = "Durum: Bağlantı kurulamadı"
            Toast.makeText(this, "Bağlantı başarısız. Sunucu ve yapılandırma bilgilerini kontrol et.", Toast.LENGTH_LONG).show()
        }
    }

    private fun disconnectTunnel() {
        try {
            backend.setState(tunnel, Tunnel.State.DOWN, null)
            status.text = "Durum: VPN bağlı değil"
        } catch (_: Exception) {
            Toast.makeText(this, "VPN bağlantısı kapatılamadı.", Toast.LENGTH_SHORT).show()
        }
    }
}
