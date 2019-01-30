package com.bidali.commerce.demo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bidali.commerce.BidaliCommerceSDK;

import com.bidali.commerce.SDKOptions;
import com.bidali.commerce.demo.R;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            getSupportActionBar().setElevation(0.5f);
        }
        final BidaliCommerceSDK bidaliCommerceSDK = new BidaliCommerceSDK(getApplicationContext());

        Button withDefaultsButton = (Button) findViewById(R.id.with_defaults);
        withDefaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKOptions options = new SDKOptions();
                options.listener = new BidaliCommerceSDK.BidaliCommerceSDKListener() {
                    @Override
                    public void onOrderCreated(JSONObject data) {
                        Log.d("MainActivity", "onOrderCreated called");
                    }
                };
                bidaliCommerceSDK.show(MainActivity.this, options);
            }
        });

        Button withRegionButton = (Button) findViewById(R.id.with_region);
        withRegionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKOptions options = new SDKOptions();
                options.currency = "AUD";
                options.listener = new BidaliCommerceSDK.BidaliCommerceSDKListener() {
                    @Override
                    public void onOrderCreated(JSONObject data) {
                        Log.d("MainActivity", "onOrderCreated called");
                    }
                };
                bidaliCommerceSDK.show(MainActivity.this, options);
            }
        });

        Button withBrandButton = (Button) findViewById(R.id.with_brand);
        withBrandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKOptions options = new SDKOptions();
                options.brandCode = "amazonus";
                options.listener = new BidaliCommerceSDK.BidaliCommerceSDKListener() {
                    @Override
                    public void onOrderCreated(JSONObject data) {
                        Log.d("MainActivity", "onOrderCreated called");
                    }
                };
                bidaliCommerceSDK.show(MainActivity.this, options);
            }
        });

        Button withAPIPaymentButton = (Button) findViewById(R.id.with_api_payment);
        withAPIPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKOptions options = new SDKOptions();
                options.paymentType = "API";
                options.listener = new BidaliCommerceSDK.BidaliCommerceSDKListener() {
                    @Override
                    public void onOrderCreated(JSONObject data) {
                        Log.d("MainActivity", "onOrderCreated called");
                    }
                };
                bidaliCommerceSDK.show(MainActivity.this, options);
            }
        });

        Button withBitcoinButton = (Button) findViewById(R.id.with_bitcoin);
        withBitcoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SDKOptions options = new SDKOptions();
                options.cryptoPaymentMethods = new ArrayList<String>() {{
                    add("BTC");
                }};
                options.listener = new BidaliCommerceSDK.BidaliCommerceSDKListener() {
                    @Override
                    public void onOrderCreated(JSONObject data) {
                        Log.d("MainActivity", "onOrderCreated called");
                    }
                };
                bidaliCommerceSDK.show(MainActivity.this, options);
            }
        });
    }
}
