package com.bidali.commerce

import java.math.BigDecimal

data class PaymentRequest(val amount: BigDecimal, val currency: String, val address: String)