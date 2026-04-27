package com.example.smartvendor.vendor

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.smartvendor.databinding.ActivityAnalyticsBinding

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCanvasGraph()
        setupWebView()
    }

    private fun setupCanvasGraph() {
        // Unit II: Canvas - Setting dummy data for the custom sales graph
        val mockSalesData = listOf(10f, 40f, 25f, 80f, 50f, 90f, 100f)
        binding.salesGraph.setData(mockSalesData)
    }

    private fun setupWebView() {
        // Unit VI: WebView - Loading a mock analytics dashboard
        binding.webViewAnalytics.settings.javaScriptEnabled = true
        binding.webViewAnalytics.webViewClient = WebViewClient()
        binding.webViewAnalytics.loadUrl("https://www.google.com/search?q=vending+machine+analytics+dashboard")
    }
}
