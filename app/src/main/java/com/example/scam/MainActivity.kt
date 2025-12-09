package com.example.scam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID = "web_notifications_channel"

    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        webView = findViewById(R.id.webView)
        
        // Настройка канала уведомлений и WebView
        createNotificationChannel()
        checkNotificationPermission()
        setupWebView()
        
        // Обработка Intent для открытия URL
        handleIntent(intent)
        
        // Настройка обработки нажатия кнопки "назад"
        setupOnBackPressed()
    }
    
    private fun setupWebView() {
        // Настройка WebView для эмуляции десктопного режима
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        
        // Отключение определения устройства как мобильного
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        
        // Эмуляция десктопного User-Agent
        webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        
        // Настройка WebViewClient для обработки навигации и инъекции JS
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString()

                if (url != null) {
                    // Обработка кастомной схемы max://
                    if (url.startsWith("max://")) {
                        val maxUrl = url.replace("max://", "https://web.max.ru/")
                        webView.loadUrl(maxUrl)
                        return true
                    } else if (url.startsWith("http://") || url.startsWith("https://")) {
                        // Проверяем, является ли URL частью домена web.max.ru
                        if (url.contains("web.max.ru")) {
                            // Загружаем внутри WebView
                            return false
                        } else {
                            // Открываем внешние ссылки в браузере по умолчанию
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                            return true
                        }
                    }
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                try {
                    if (url != null && url.contains("web.max.ru")) {
                        // Регистрируем JS-интерфейс только для доверенного домена
                        try {
                            view?.addJavascriptInterface(WebAppInterface(this@MainActivity, CHANNEL_ID), "Android")
                        } catch (_: Exception) {
                        }
                        injectNotificationHook(view)
                    }
                } catch (_: Exception) {
                }
            }
        }

        // JS-интерфейс регистрируется динамически в onPageFinished для web.max.ru
        
        // Загрузка начальной страницы
        webView.loadUrl("https://web.max.ru")
    }

    private fun injectNotificationHook(view: WebView?) {
        val js = "(function(){if(window.__androidNotificationHookInstalled) return;window.__androidNotificationHookInstalled=true;var OriginalNotification=window.Notification;function AndroidNotification(title,options){try{var body=(options&&options.body)?options.body:'';var icon=(options&&options.icon)?options.icon:'';if(window.Android&&window.Android.postNotification){window.Android.postNotification(title||'',body,icon);} }catch(e){} return new OriginalNotification(title,options);} AndroidNotification.permission=OriginalNotification.permission;AndroidNotification.requestPermission=function(cb){if(cb){OriginalNotification.requestPermission(function(p){cb(p);});}return OriginalNotification.requestPermission();};AndroidNotification.prototype=OriginalNotification.prototype;window.Notification=AndroidNotification;})();"
        try {
            view?.evaluateJavascript(js, null)
        } catch (_: Exception) {
            try {
                view?.loadUrl("javascript:$js")
            } catch (_: Exception) {
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Web Notifications"
            val descriptionText = "Уведомления от встроенного веба"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(perm), 101)
            }
        }
    }
    
    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val data = intent.dataString
        
        if (Intent.ACTION_VIEW == action && data != null) {
            if (data.startsWith("max://")) {
                // Преобразуем max:// в https://web.max.ru/
                val maxUrl = data.replace("max://", "https://web.max.ru/")
                webView.loadUrl(maxUrl)
            } else {
                webView.loadUrl(data)
            }
        }
    }
    
    private fun setupOnBackPressed() {
        // Обработка нажатия кнопки "назад" с использованием OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish() // Закрываем приложение, если нельзя вернуться назад
                }
            }
        })
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на уведомления предоставлено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }
}