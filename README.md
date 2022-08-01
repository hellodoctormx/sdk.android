
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

**Telehealth video calls**: The SDK handles almost everything regarding an active video call with minimal configuration within your own application

**Request consultations**: Use the ConsultationService provided by the SDK to retrieve doctor availability and request a telehealth video consultation

**UI Kit (in development)**: Native screens and elements to request, view and manage a user's consultations and other HelloDoctor data.

<img src="https://user-images.githubusercontent.com/54091648/182131158-99a3fe47-0828-48c3-bf57-132c853d95c3.jpg" width="156"/>

## Integration Lifecycle
### User Creation/Authentication
![HelloDoctor Integration Guide - User Authentication](https://user-images.githubusercontent.com/54091648/182129252-7c9109aa-a3ec-4c48-958c-d202a925107a.svg)

 1. **Request linked HelloDoctor user ID and server authentication token**
 2. **Check Your Database for existing linked HelloDoctor user ID**
 3. **Depending on whether the linked user already exists:**
		 a) If linked HelloDoctor user **exists**, request authentication token for linked user from HelloDoctor Server
		 b) If linked user **does not exist**, send request to HelloDoctor Server to create new user
 4. 1.  *(Only if new linked user was just created)* **Save new HelloDoctor user ID to linked user, and then request auth token** (3a above)
 5. **Return HelloDoctor user ID and authentication token to Your Device**
 6. **Request HelloDoctor access token to authenticate current session**


### Video calls
![HelloDoctor Integration Guide - Video Calls](https://user-images.githubusercontent.com/54091648/182129272-df4d6674-03b1-4921-a8ef-96bf2a8a05a0.svg)

 1. **HelloDoctor doctor user launches video call**
 2. **HelloDoctor Server notifies Your Server of incoming video call**
 3. **Your Server retrieves device tokens for Your user**
 4. **Your Server notifies Your Device of incoming call**
 5. **When Your user accepts the incoming video call, HelloDoctor SDK automatically requests the access token for the call from HelloDoctor Server**
 6. **The active video call is peer-to-peer between the devices except for connection state data - which is managed by Twilio - and ending the call**
	 - If the **app user ends the call**, the HelloDoctor SDK automatically handles notifying HelloDoctor Server/User that the call has ended 
	 - If the **HelloDoctor user ends the call**, Your Server will receive a notification of type `videoCallEnded`, and it should push that notification to Your Device to then be handled by the SDK


## Installation

### Requirements

* Android 8.0 (API level 26) and above
* [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) 7.2.2
* [Gradle](https://gradle.org/releases/) 7.3.3+
* [AndroidX](https://developer.android.com/jetpack/androidx/)

### Configuration
Add GitHub Packages repository (*Will eventually be moved to Maven Central*)
```
def props = new Properties()  
file("gpr.properties").withInputStream { props.load(it) }

...

repositories {
	...
	
	maven {  
	  url = uri("https://maven.pkg.github.com/hellodoctormx/sdk.android")  
	    credentials {  
			username = props.getProperty("gpr.user")  
			password = props.getProperty("gpr.key")  
	    }  
	}
}
```

Add `gpr.properties` file, which contains the keys for the GitHub repository
```
gpr.user=<from setup config>
gpr.key=<from setup config>
```

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
- The [FoodPass example project](https://github.com/hellodoctormx/sdk.examples) demonstrates how to integrate and use our prebuilt ui, as well as the user authentication lifecycle
