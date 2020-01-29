package com.example.demo

import android.content.DialogInterface
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
import kotlinx.android.synthetic.main.activity_second.*

class SecondActivity : AppCompatActivity() {

    private val logTag = "SecondActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_second)

        if (Build.VERSION.SDK_INT >= 21) {
            Objects.requireNonNull<ActionBar>(supportActionBar).elevation = 0.5f
        }
        val bidaliSDK = BidaliSDK(applicationContext)


        val options = BidaliSDKOptions("12345")
        options.email = "csmith@bidali.com"
        options.env = "local"
        options.url = "http://192.168.0.12:3009/embed"
        options.listener = object : BidaliSDK.BidaliSDKListener {
            override fun onPaymentRequest(paymentRequest: PaymentRequest) {
                Log.d(logTag, "onPaymentRequest called with obj:$paymentRequest")
                Log.d(logTag, "onPaymentRequest called with amount:" + paymentRequest.amount)
                Log.d(logTag, "onPaymentRequest called with amount toString:" + paymentRequest.amount)
                val dialogBuilder = AlertDialog.Builder(this@SecondActivity)
                dialogBuilder.setTitle("Buy ${paymentRequest.chargeDescription}")
                dialogBuilder.setMessage("Authorize this transaction for ${paymentRequest.amount} ${paymentRequest.currency}?\n${paymentRequest.chargeId}")
                dialogBuilder.setPositiveButton("Yes, Authorize") { _: DialogInterface, _ ->

                }
                dialogBuilder.setNegativeButton("No") { _: DialogInterface, _ ->

                }
                dialogBuilder.show()
            }
        }

        bidaliSDK.showIn(this, mainLayout, options)
    }
}
