package com.rusroute.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            text = "DNS ve VPN bağlantı profillerini yönetmek için başlangıç uygulaması."
            textSize = 17f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 24)
        }
        val status = TextView(this).apply {
            text = "Durum: VPN bağlı değil"
            textSize = 16f
            setTextColor(Color.rgb(90, 90, 90))
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 24)
        }
        val dns = Button(this).apply {
            text = "Özel DNS ayarları"
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Android Ayarlar > Ağ ve İnternet > Özel DNS bölümünden DNS sağlayıcısı ayarlayabilirsin.", Toast.LENGTH_LONG).show()
            }
        }
        val vpn = Button(this).apply {
            text = "VPN kurulumu hakkında"
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Rusya IP adresi için Rusya çıkış sunucusu olan bir VPN sağlayıcısı ve geçerli yapılandırma gerekir. Bu sürüm VPN tüneli oluşturmaz.", Toast.LENGTH_LONG).show()
            }
        }
        val note = TextView(this).apply {
            text = "Not: DNS tek başına IP adresini Rusya'ya taşımaz. Bu ilk sürüm VPN bağlantısı kurmaz."
            textSize = 14f
            setTextColor(Color.rgb(100, 100, 100))
            setPadding(0, 28, 0, 0)
        }
        root.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(intro)
        root.addView(status)
        root.addView(dns, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(vpn, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(note)
        setContentView(root)
    }
}
