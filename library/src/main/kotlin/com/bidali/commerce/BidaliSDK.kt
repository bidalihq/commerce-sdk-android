package com.bidali.commerce

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jetbrains.anko.browse
import org.json.JSONObject
import wendu.webviewjavascriptbridge.WVJBWebView
import java.math.BigDecimal
import java.util.*

class BidaliSDK(private val context: Context) {
    private val loadingHtmlString = "<html><head><style type='text/css'>html, body {height: 100%;background: #f8f8fc;left: 0;margin: 0;overflow: hidden;text-align: center;top: 0;width: 100%;}.spinner { height: 60px;max-height: 60vmin;max-width: 60vmin;left: 50%;position: absolute;top: 50%;transform: translateX(-50%) translateY(-50%);width: 60px;z-index: 10;} .spinner .loader { animation: rotation .7s infinite linear;border: 3px solid rgba(0, 0, 0, .15);border-top-color: #4B4DF1;border-radius: 100%;box-sizing: border-box;height: 100%;width: 100%;}@keyframes rotation {from { transform: rotate(0deg); }to { transform: rotate(359deg); }}</style></head><body><div class=\"spinner\"><div class=\"loader\"></div></div></html>"
    private val tag = "BidaliSDK"
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

    private fun setupHandlers(context: Context, sdkOptions: BidaliSDKOptions) {

        doAsync {

            val bridgeInitializationProps = buildProps(context, sdkOptions)

            uiThread {
                //Update the UI thread here
                val onLogHandler = WVJBWebView.WVJBHandler<Object, Any> { data, _ ->
                    Log.d(tag, "onLog called with: $data")
                }

                val onCloseHandler = WVJBWebView.WVJBHandler<JSONObject, Any> { _, _ ->
                    Log.d(tag, "onCloseHandler called")
                    dialog.dismiss()
                }

                val openUrlHandler = WVJBWebView.WVJBHandler<String, Any> { url, _ ->
                    Log.d(tag, "openUrlHandler called$url")
                    context.browse(url)
                }

                val onPaymentRequestHandler = WVJBWebView.WVJBHandler<JSONObject, Any> { data, _ ->
                    Log.d(tag, "onPaymentRequest called:" + data.javaClass + ":" + data)
                    val amount = BigDecimal(data.getString("amount"))
                    val currency = data.getString("currency")
                    val address = data.getString("address")
                    val chargeId = data.getString("chargeId")
                    val description = data.getString("description")
                    sdkOptions.listener?.onPaymentRequest(PaymentRequest(amount, currency, address, chargeId, description))
                    dialog.dismiss()
                }

                val readyForSetupHandler = WVJBWebView.WVJBHandler<JSONObject, Any> { _, _ ->
                    Log.d(tag, "readyForSetupHandler called")
                    webView.callHandler("setupBridge", bridgeInitializationProps, WVJBWebView.WVJBResponseCallback<Any> { Log.d(tag, "Bridge is setup!") })
                }

                webView.registerHandler("log", onLogHandler)
                webView.registerHandler("onPaymentRequest", onPaymentRequestHandler)
                webView.registerHandler("onClose", onCloseHandler)
                webView.registerHandler("openUrl", openUrlHandler)
                webView.registerHandler("readyForSetup", readyForSetupHandler)

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d(tag, "onPageFinished:$url")
                        webView.visibility = View.VISIBLE
                        loadingWebView.visibility = View.GONE
                        super.onPageFinished(view, url)
                    }

                    //TODO: Handle loading errors appropriately
                }
            }
        }

    }

    fun show(context: Context, sdkOptions: BidaliSDKOptions) {
        this.setupLayout(context)
        this.setupHandlers(context, sdkOptions)

        val widgetUrl = urlForEnvironment(sdkOptions)
        Log.d(tag, "loading $widgetUrl")
        webView.loadUrl(widgetUrl)
    }
}