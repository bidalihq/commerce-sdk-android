package com.bidali.commerce

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import org.json.JSONObject
import wendu.webviewjavascriptbridge.WVJBWebView
import java.math.BigDecimal
import java.util.*

class BidaliSDK(private val context: Context) {
    private val loadingHtmlString = "<html><head><style type='text/css'>html, body {height: 100%;background: #f8f8fc;left: 0;margin: 0;overflow: hidden;text-align: center;top: 0;width: 100%;}.spinner { height: 60px;max-height: 60vmin;max-width: 60vmin;left: 50%;position: absolute;top: 50%;transform: translateX(-50%) translateY(-50%);width: 60px;z-index: 10;} .spinner .loader { animation: rotation .7s infinite linear;border: 3px solid rgba(0, 0, 0, .15);border-top-color: #4B4DF1;border-radius: 100%;box-sizing: border-box;height: 100%;width: 100%;}@keyframes rotation {from { transform: rotate(0deg); }to { transform: rotate(359deg); }}</style></head><body><div class=\"spinner\"><div class=\"loader\"></div></div></html>"
    private val tag = "BidaliSDK"
    private val defaultEnv = "production"
    private val urls = object : HashMap<String, String>() {
        init {
            put("local", "http://10.0.3.2:3009/embed")
            put("staging", "https://commerce.staging.bidali.com/embed")
            put("production", "https://commerce.bidali.com/embed")
        }
    }
    private lateinit var dialog: Dialog
    private lateinit var webView: WVJBWebView
    private lateinit var loadingWebView: WebView

    interface BidaliSDKListener {
        fun onPaymentRequest(paymentRequest: PaymentRequest)
    }

    private fun setupLayout(context: Context) {
        webView = WVJBWebView(this.context)
        webView.visibility = View.GONE
        loadingWebView = WebView(this.context)
        loadingWebView.loadData(loadingHtmlString, "text/html", "UTF-8")

        val layout = RelativeLayout(context)
        val layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.addView(webView, layoutParams)
        layout.addView(loadingWebView, layoutParams)

        dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(layout)

        dialog.show()
    }

    private fun setupBridge(sdkOptions: BidaliSDKOptions) {
        val props = HashMap<String, Any?>()

        props["apiKey"] = sdkOptions.apiKey

        if (sdkOptions.email != null) {
            props["email"] = sdkOptions.email
        }

        if (sdkOptions.paymentType != null) {
            props["paymentType"] = sdkOptions.paymentType
        }

        if (sdkOptions.paymentCurrencies != null) {
            props["paymentCurrencies"] = sdkOptions.paymentCurrencies
        }

        val data = JSONObject(props)

        val onCloseHandler = WVJBWebView.WVJBHandler<JSONObject, Any> { _, _ ->
            Log.d(tag, "onCloseHandler called")
            dialog.dismiss()
        }

        val onPaymentRequestHandler = WVJBWebView.WVJBHandler<JSONObject, Any> { data, _ ->
            Log.d(tag, "onPaymentRequest called:" + data.javaClass + ":" + data)
            val amount = BigDecimal(data.getString("amount"))
            val currency = data.getString("currency")
            val address = data.getString("address")
            sdkOptions.listener?.onPaymentRequest(PaymentRequest(amount, currency, address))
            dialog.dismiss()
        }

        webView.registerHandler("onPaymentRequest", onPaymentRequestHandler)
        webView.registerHandler("onClose", onCloseHandler)

        var widgetUrl = urls[defaultEnv]
        if (sdkOptions.url != null) {
            widgetUrl = sdkOptions.url
        } else if (urls[sdkOptions.env] != null) {
            widgetUrl = urls[sdkOptions.env]
        }

        Log.d(tag, widgetUrl)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d(tag, "onPageFinished:$url")
                webView.callHandler("setupBridge", data, WVJBWebView.WVJBResponseCallback<Any> { Log.d(tag, "Bridge is setup!") })
                webView.visibility = View.VISIBLE
                loadingWebView.visibility = View.GONE
                super.onPageFinished(view, url)
            }

            //TODO: Handle loading errors appropriately
        }

        webView.loadUrl(widgetUrl)
    }

    fun show(context: Context, sdkOptions: BidaliSDKOptions) {
        this.setupLayout(context)
        this.setupBridge(sdkOptions)
    }
}