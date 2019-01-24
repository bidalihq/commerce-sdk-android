package com.bidali.giftcards;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.HashMap;

import org.json.JSONObject;

import wendu.webviewjavascriptbridge.WVJBWebView;


public class BidaliGiftcardsSDK {

    public interface BidaliGiftcardsSDKListener {
        /**
         * <p> Callback method to update when order is created</p>
         */
        void onOrderCreated(JSONObject data);
    }


    private Context context;

    public BidaliGiftcardsSDK(Context context) {
        this.context = context;

    }

    public void show(Activity activity, SDKOptions sdkOptions) {

        WVJBWebView webView = new WVJBWebView(this.context);

        RelativeLayout layout = new RelativeLayout(activity);
//        layout.setVisibility(View.GONE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(webView, layoutParams);
        layout.setBackgroundColor(Color.CYAN);

        final Dialog bidaliViewDialog_ = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        bidaliViewDialog_.setContentView(layout);

        layout.setVisibility(View.VISIBLE);
        webView.setVisibility(View.VISIBLE);
        bidaliViewDialog_.show();

        webView.loadUrl("http://192.168.0.11:3000/embed");
        webView.registerHandler("onOrderCreated", new WVJBWebView.WVJBHandler() {
            @Override
            public void handler(Object data, WVJBWebView.WVJBResponseCallback callback) {
                callback.onResult(null);
            }
        });

        webView.registerHandler("onClose", new WVJBWebView.WVJBHandler() {
            @Override
            public void handler(Object data, WVJBWebView.WVJBResponseCallback callback) {
                //TODO: Close the webview
                Log.d("BidaliGiftcardsSDK", "Close called");
                bidaliViewDialog_.dismiss();
//                callback.onResult(null);
            }
        });

        HashMap<String, Object> props = new HashMap<String, Object>();

        if(sdkOptions.brandCode != null) {
            props.put("brandCode", sdkOptions.brandCode);
        }

        if(sdkOptions.currency != null) {
            props.put("currency", sdkOptions.currency);
        }

        if(sdkOptions.email != null) {
            props.put("email", sdkOptions.email);
        }

        if(sdkOptions.referralCode != null) {
            props.put("referralCode", sdkOptions.referralCode);
        }

        if(sdkOptions.paymentType != null) {
            props.put("paymentType", sdkOptions.paymentType);
        }

        if(sdkOptions.cryptoPaymentMethods != null) {
            props.put("cryptoPaymentMethods", sdkOptions.cryptoPaymentMethods);
        }

        JSONObject data = new JSONObject(props);

        webView.callHandler("setupBridge", data, new WVJBWebView.WVJBResponseCallback<Object>() {
            @Override
            public void onResult(Object o) {
                Log.d("BidaliGiftcardsSDK", "Bridge is setup!");
            }
        });
    }
}