package com.bidali.commerce

import android.content.res.Resources

import android.annotation.TargetApi
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.RelativeLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jetbrains.anko.browse
import org.json.JSONObject
import wendu.webviewjavascriptbridge.WVJBWebView
import java.math.BigDecimal

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.toDp(): Float = (this / Resources.getSystem().displayMetrics.density)
fun Float.toPx(): Float = (this * Resources.getSystem().displayMetrics.density)

class BidaliSDK(private val context: Context) {
    private val loadingHtmlString = "<html><head><style type='text/css'>html, body {height: 100%;background: #f8f8fc;left: 0;margin: 0;overflow: hidden;text-align: center;top: 0;width: 100%;}.spinner { height: 60px;max-height: 60vmin;max-width: 60vmin;left: 50%;position: absolute;top: 50%;transform: translateX(-50%) translateY(-50%);width: 60px;z-index: 10;} .spinner .loader { animation: rotation .7s infinite linear;border: 3px solid rgba(0, 0, 0, .15);border-top-color: #4B4DF1;border-radius: 100%;box-sizing: border-box;height: 100%;width: 100%;}@keyframes rotation {from { transform: rotate(0deg); }to { transform: rotate(359deg); }}</style></head><body><div class=\"spinner\"><div class=\"loader\"></div></div></html>"
    private val tag = "BidaliSDK"
    private var dialog: Dialog? = null
    private var closeButton: Button? = null
    private lateinit var webView: WVJBWebView
    private lateinit var loadingWebView: WebView
    private var parentViewGroup: ViewGroup? = null

    interface BidaliSDKListener {
        fun onPaymentRequest(paymentRequest: PaymentRequest)
    }

    private fun setupLayout(context: Context, viewGroup: ViewGroup?) {
        parentViewGroup = viewGroup
        webView = WVJBWebView(this.context)
        webView.visibility = View.GONE
        loadingWebView = WebView(this.context)
        loadingWebView.loadData(loadingHtmlString, "text/html", "UTF-8")

        val layout = RelativeLayout(context)
        val layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.addView(webView, layoutParams)
        layout.addView(loadingWebView, layoutParams)

        if (parentViewGroup === null) {
            closeButton = Button(context)
            val buttonLayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            buttonLayoutParams.width = 40.toPx()
            buttonLayoutParams.height = 40.toPx()
            closeButton?.layoutParams = buttonLayoutParams
            closeButton?.text = "✕"
            closeButton?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F)
            closeButton?.setOnClickListener {
                dialog?.dismiss()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                closeButton?.stateListAnimator = null
            }
            closeButton?.setBackgroundColor(Color.TRANSPARENT)
            closeButton?.setTextColor(Color.BLACK)
            layout.addView(closeButton)

            dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog?.setContentView(layout)

            dialog?.show()
        } else {
            parentViewGroup!!.addView(layout)
        }
    }

    private fun setupHandlers(context: Context, sdkOptions: BidaliSDKOptions) {

        doAsync {
            //TODO: We need to add something thats passed in props that hides the close button and stuff on the Commerce Web side
            val bridgeInitializationProps = buildProps(context, sdkOptions)

            uiThread {
                //Update the UI thread here
                val onLogHandler = WVJBWebView.WVJBHandler<Any, Any> { data, _ ->
                    Log.d(tag, "onLog called with: $data")
                }

                val onCloseHandler = WVJBWebView.WVJBHandler<JSONObject, Any> { _, _ ->
                    //TODO: What do we do here?
                    dialog?.dismiss()
                }

                val openUrlHandler = WVJBWebView.WVJBHandler<String, Any> { url, _ ->
                    context.browse(url)
                }

                val handleLoadingError = { urlString: String ->
                    val widgetUrl = urlForEnvironment(sdkOptions)
                    if (urlString == widgetUrl) {
                        if (dialog !== null) {
                            dialog?.dismiss()
                        }
                    }
                }

                val onPaymentRequestHandler = WVJBWebView.WVJBHandler<JSONObject, Any> { data, _ ->
                    val amount = BigDecimal(data.getString("amount"))
                    val currency = data.getString("currency")
                    val address = data.getString("address")
                    val chargeId = data.getString("chargeId")
                    val description = data.getString("description")

                    var extraId: String? = null
                    if (data.has("extraId")) {
                        extraId = data.getString("extraId")
                    }

                    var extraIdName: String? = null;
                    if (data.has("extraIdName")) {
                        extraIdName = data.getString("extraIdName")
                    }

                    sdkOptions.listener?.onPaymentRequest(PaymentRequest(amount, currency, address, chargeId, description, extraId, extraIdName))
                    //TODO: Don't auto-dismiss here!
                    if (dialog !== null) {
                        dialog?.dismiss()
                    }
                }

                val readyForSetupHandler = WVJBWebView.WVJBHandler<JSONObject, Any> { _, _ ->
                    webView.callHandler("setupBridge", bridgeInitializationProps, WVJBWebView.WVJBResponseCallback<Any> { Log.d(tag, "Bridge is setup!") })
                }

                webView.registerHandler("log", onLogHandler)
                webView.registerHandler("onPaymentRequest", onPaymentRequestHandler)
                webView.registerHandler("onClose", onCloseHandler)
                webView.registerHandler("openUrl", openUrlHandler)
                webView.registerHandler("readyForSetup", readyForSetupHandler)

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        webView.visibility = View.VISIBLE
                        loadingWebView.visibility = View.GONE
                        closeButton?.visibility = View.GONE
                        super.onPageFinished(view, url)
                    }


                    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                        Log.e(tag, "onReceivedError deprecated $failingUrl $errorCode $description")
                        handleLoadingError(failingUrl)
                    }

                    @TargetApi(Build.VERSION_CODES.M)
                    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                        Log.e(tag, "onReceivedError ${request.url} ${error.errorCode} ${error.description}")
                        handleLoadingError(request.url.toString())
                    }

                    @TargetApi(Build.VERSION_CODES.M)
                    override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                        Log.e(tag, "onReceivedHttpError ${request.url} ${errorResponse.statusCode} ${errorResponse.reasonPhrase}")
                        handleLoadingError(request.url.toString())
                    }
                }
            }
        }

    }

    fun show(context: Context, sdkOptions: BidaliSDKOptions) {
        this.setupLayout(context, null)
        this.setupHandlers(context, sdkOptions)

        val widgetUrl = urlForEnvironment(sdkOptions)
        webView.loadUrl(widgetUrl)
    }

    fun showIn(context: Context, viewGroup: ViewGroup, sdkOptions: BidaliSDKOptions) {
        this.setupLayout(context, viewGroup)
        this.setupHandlers(context, sdkOptions)

        val widgetUrl = urlForEnvironment(sdkOptions)
        webView.loadUrl(widgetUrl)
    }
}