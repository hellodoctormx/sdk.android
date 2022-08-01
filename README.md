
# HelloDoctor Android SDK

[![GitHub release](https://img.shields.io/github/release/hellodoctormx/sdk.android.svg?maxAge=60)](https://github.com/hellodoctormx/sdk.android/releases)
[![License](https://img.shields.io/github/license/hellodoctormx/sdk.android)](https://github.com/hellodoctormx/sdk.android/blob/master/LICENSE)

The HelloDoctor Android SDK makes it easy to integrate HelloDoctor's telemedicine service directly within your own app. The SDK provides an Android Activity for incoming video calls, as well as a number of services to request and manage consultations and other HelloDoctor user data.

Get started with our  [example project](https://github.com/hellodoctormx/sdk.examples), or [📘 browse the SDK reference](https://docs.hellodoctor.mx).


Table of contents
=================

<!--ts-->
   * [Features](#features)
   * [Integration Lifecycle](#integration-lifecycle)
   * [Installation](#installation)
      * [Requirements](#requirements)
      * [Configuration](#configuration)
   * [Getting Started](#getting-started)
   * [Examples](#examples)
<!--te-->

## Features

**Request consultations**: Use the SDK to collect credit card numbers and remain [PCI compliant](https://stripe.com/docs/security#pci-dss-guidelines). This means sensitive data is sent directly to Stripe instead of passing through your server. For more information, see our [Integration Security Guide](https://stripe.com/docs/security).

**Telehealth video calls**: The SDK automatically performs native [3D Secure authentication](https://stripe.com/docs/payments/3d-secure) to comply with [Strong Customer Authentication](https://stripe.com/docs/strong-customer-authentication) regulation in Europe.

**UI Kit (in development)**: Native screens and elements to request, view and manage a user's consultations and other HelloDoctor data.

<img src="https://user-images.githubusercontent.com/54091648/182131158-99a3fe47-0828-48c3-bf57-132c853d95c3.jpg" width="156"/>

## Integration Lifecycle
### User Creation/Authentication
![HelloDoctor Integration Guide - User Authentication](https://user-images.githubusercontent.com/54091648/182129252-7c9109aa-a3ec-4c48-958c-d202a925107a.svg)

 1. **Request mapped HelloDoctor user ID and server authentication token**
 2. **Check DeliLife database for existing mapped HelloDoctor user ID**
 3. **Depending on whether the mapped user already exists:**
		 a) If mapped HelloDoctor user **exists**, request authentication token for mapped user from HelloDoctor Server
		 b) If mapped user **does not exist**, send request to HelloDoctor Server to create new user
 4. 1.  *(Only if new mapped user was just created)* **Save new HelloDoctor user ID to mapped DeliLife user, and then request auth token** (3a above)
 5. **Return HelloDoctor user ID and authentication token to DeliLife device**
 6. **Request HelloDoctor access token to authenticate current session**


### Video calls
![HelloDoctor Integration Guide - Video Calls](https://user-images.githubusercontent.com/54091648/182129272-df4d6674-03b1-4921-a8ef-96bf2a8a05a0.svg)

 1. **HelloDoctor doctor user launches video call**
 2. **HelloDoctor Server notifies DeliLife Server of incoming video call**
 3. **DeliLife Server retrieves device tokens for DeliLife user**
 4. **DeliLife Server notifies DeliLife Device of incoming call**
 5. **When DeliLife user accepts the incoming video call, HelloDoctor SDK automatically requests the access token for the call from HelloDoctor Server**
 6. **The active video call is peer-to-peer between the devices except for connection state data - which is managed by Twilio - and ending the call**
	 - If the **app user ends the call**, the HelloDoctor SDK automatically handles notifying HelloDoctor Server/User that the call has ended 
	 - If the **HelloDoctor user ends the call**, the DeliLife Device should handle the FCM notification (for both Android and iOS) of type videoCallEnded


## Installation

### Requirements

* Android 8.0 (API level 26) and above
* [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) 3.5.1
* [Gradle](https://gradle.org/releases/) 5.4.1+
* [AndroidX](https://developer.android.com/jetpack/androidx/)

### Configuration

Add `hellodoctor-native-sdk` to your `build.gradle` dependencies.

```
dependencies {
    implementation "com.hellodoctormx.sdk:hellodoctor-native-sdk:0.4.1"
}
```

## Getting Started

### Integration
Get started with our [example project](#examples), or [📘 browse the SDK reference](https://docs.hellodoctor.mx).

### Examples
- The [FoodPass example project](https://github.com/stripe/stripe-android/tree/master/paymentsheet-example) demonstrates how to integrate and use our prebuilt ui, as well as the user authentication lifecycle
