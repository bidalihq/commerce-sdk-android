package com.bidali.commerce

import java.util.ArrayList

data class BidaliSDKOptions(val apiKey: String) {
    @JvmField var env: String = "production"
    @JvmField var url: String? = null
    @JvmField var email: String? = null
    @JvmField var paymentType: PaymentType? = PaymentType.PREFILL
    @JvmField var paymentCurrencies: ArrayList<String>? = null
    @JvmField var defaultCountry: String? = null
    @JvmField var listener: BidaliSDK.BidaliSDKListener? = null
}