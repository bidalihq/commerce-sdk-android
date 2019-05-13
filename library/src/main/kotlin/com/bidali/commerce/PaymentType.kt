package com.bidali.commerce

enum class PaymentType(val description: String) {
    MANUAL("manual"),
    PREFILL("prefill"),
    API("api")
}