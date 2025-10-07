package com.example.scam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        webView = findViewById(R.id.webView)
        
        // Настройка WebView
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
        
        // Настройка WebViewClient для обработки навигации
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
        }
        
        // Загрузка начальной страницы
        webView.loadUrl("https://web.max.ru")
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
}