package com.example.demo

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log

import com.bidali.commerce.BidaliSDK
import com.bidali.commerce.BidaliSDKOptions
import com.bidali.commerce.PaymentRequest
import com.bidali.commerce.PaymentType

import java.util.ArrayList
import java.util.Objects

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val logTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= 21) {
            Objects.requireNonNull<ActionBar>(supportActionBar).elevation = 0.5f
        }
        val bidaliSDK = BidaliSDK(applicationContext)


        with_defaults.setOnClickListener {
            val options = BidaliSDKOptions("YOUR API KEY")
            options.email = "csmith@bidali.com"
            options.listener = object : BidaliSDK.BidaliSDKListener {
                override fun onPaymentRequest(paymentRequest: PaymentRequest) {
                    Log.d(logTag, "onPaymentRequest called with obj:$paymentRequest")
                    Log.d(logTag, "onPaymentRequest called with amount:" + paymentRequest.amount)
                    Log.d(logTag, "onPaymentRequest called with amount toString:" + paymentRequest.amount)
                    val dialogBuilder = AlertDialog.Builder(this@MainActivity)
                    dialogBuilder.setTitle("Buy ${paymentRequest.chargeDescription}")
                    dialogBuilder.setMessage("Authorize this transaction for ${paymentRequest.amount} ${paymentRequest.currency}?\n${paymentRequest.chargeId}")
                    dialogBuilder.setPositiveButton("Yes, Authorize") { _: DialogInterface, _ ->

                    }
                    dialogBuilder.setNegativeButton("No") { _: DialogInterface, _ ->

                    }
                    dialogBuilder.show()
                }
            }
            bidaliSDK.show(this, options)
        }

        with_bitcoin.setOnClickListener {
            val options = BidaliSDKOptions("YOUR API KEY")
            options.paymentCurrencies = object : ArrayList<String>() {
                init {
                    add("BTC")
                }
            }
            options.listener = object : BidaliSDK.BidaliSDKListener {
                override fun onPaymentRequest(paymentRequest: PaymentRequest) {
                    Log.d(logTag, "onPaymentRequest called with$paymentRequest")
                }
            }
            bidaliSDK.show(this, options)
        }

        with_viewgroup.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }
}
