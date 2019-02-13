# `Bidali Commerce Android SDK`
[![](https://jitpack.io/v/bidalihq/commerce-sdk-android.svg)](https://jitpack.io/#bidalihq/commerce-sdk-android)

### Installing with gradle

1. Add the JitPack repository to your build file

   ```java
   allprojects {
     repositories {
       ...
       maven { url 'https://jitpack.io' }
     }
   }
   ```

2. Add the dependency

   ```java
   dependencies {
     compile 'com.github.bidalihq:commerce-sdk-android:0.0.1'
   }
   ```

### Usage


```java
import com.bidali.commerce.BidaliSDK;
import com.bidali.commerce.BidaliSDKOptions;
import com.bidali.commerce.PaymentRequest;
...
String apiKey = "12345";
BidaliSDKOptions options = new BidaliSDKOptions(apiKey);
options.listener = new BidaliSDK.BidaliSDKListener() {
    @Override
    public void onPaymentRequest(PaymentRequest paymentRequest) {

    }
};
bidaliSDK.show(context, options);
```

```kotlin
import com.bidali.commerce.BidaliSDK;
import com.bidali.commerce.BidaliSDKOptions;
import com.bidali.commerce.PaymentRequest;
...
val apiKey = "12345"
val options = BidaliSDKOptions(apiKey)
options.listener = object : BidaliSDK.BidaliSDKListener {
  override fun onPaymentRequest(paymentRequest: PaymentRequest) {

  }
}
bidaliSDK.show(this, options)
```