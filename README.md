<h1 align="center"><img width=20% height=50% alt="WomenSafety&TrackingApp" src="https://github.com/user-attachments/assets/5571b51d-4966-4fc8-8a2a-8918242acf6f"></h1>

<h1 align="center">Women Safety & Tracking App</h1>

<!--<p align="center">-->
<!-- <img src="https://github.com//blob/master/README-images/banner.png" width="90%" height="400" alt="Collage Photo of app and features" />-->
<!--</p>-->

<h2 align="center" id="development">this application is currently under development.</h2>
<p><b>Due to under-development currently the SOS sms to govt. emergency helpline is disabled. SOS sms only sent to contacts. </b> </p>


<h2 align="start" id="About">About</h2>
Thinking about it..

<h2 align="start" id="Inspiration">Inspiration</h2>
Given the crucial importance of women's safety and security everywhere, I wanted to contribute by creating a solution that is simple, safe, and feature-rich. This app is designed not only to enhance safety but also to empowering users to feel both secure and confident.
<br><br>
<b>As there are many women safety app but majority of them lacks simplity and ease to use Interface and filled with a lots of clutter and b& (can't use another synonym but you may know) and is hard to navigate. There was no distinction between the essential services and additonal services.</b> 
<br><br>
<b>So, Keeping this in mind decided to develop an app which is ease to use with simplicity in UI but keeping features and seprating the essentials and additional features and services and trying to make it robust.</b>


<h2 align="start" id="Demo">Demo
<br>
 <video width="320" height="240" src="https://github.com/user-attachments/assets/455e85b2-6ec1-439c-a7f5-6cc1f41d982c" type="video/mp4"> Your browser does not support the video tag. </video>
//Here is the video demo

<h2 align="start" id="core_features"> 🤺 Core Features</h2>

* <b>Emergency Service:</b> Continues to work in the foreground, even when the app is not running.
* <b>Location Service:</b> Active in the foreground, even when the app is not running.
* <b>Shake to SOS:</b> Triggers an emergency alert while the Emergency Service is running in the foreground.
* <b>Location Sharing:</b> Sends real-time location updates via SMS, both during an emergency and in normal tracking mode (in emergency it also sends sos sms to police helpline).
* <b>Normal Tracking:</b> Tracks the user’s location under normal conditions.
* <b>Location History:</b> Access to past location data for tracking and safety purposes.
* <b>Contact:</b> Provides quick access to contacts for calling or sharing location during both emergency situations and normal tracking.
* <b>Current Location on Map:</b> Displays the user's current location with a satellite view.
* <b>Nearby Police Stations:</b> Identifies the closest police stations based on the user's current location.

<br>


<h2 align="start" id="TechStack">Tech Stack
<br>

```diff
Native Android App(Kotlin + XML)
```
</h2>

<h2 align="start" id="Libraries">Libraries Used

```diff
Fused Location API
Preferences Datastore API
SMS Manager API
Sensor Manager API
Foreground Service
Coroutines
Jetpack Navigation Library
Splash Screen API
Room API
Squareup-Seismic (tried to implement it into Kotlin from Java)
TomTom (Map and Search)
Google Dagger Hilt

```
</h2>

<br>

## 🔗 Links for project:  
 
Download APK :<a href="https://drive.google.com/file/d/17v9bL8hH4ayOOF0EBo2eXltDw0t6lmTV/view?usp=sharing">download[here]</a>


## 🚩 Features and Interfaces:
Feature | Images
------------ | -------------
 **Home Fragment(initial fragment)**  
 Simple, easy to use, and incredibly convenient with a minimalistic UI for essentials services.  | <img src="https://github.com/user-attachments/assets/93bedb62-87b0-43e6-8722-4757a6fab413" width="450" height="350">
 **More Fragment(second fragment)**
This More fragmnet contains more features which it easier to access and uses and differentiate between the essentials and other servies. | <img src="https://github.com/user-attachments/assets/5a682bba-92a0-4c59-a073-cdbb6cd4e19c" width="450" height="350">
 **Emergency Area(HOME)**
Easy to indentify and recognisable to start SOS request.| <img src="https://github.com/user-attachments/assets/7f0fdfb6-0f0e-457c-832e-b3d45e503b50" width="450" height="350">
**Normal Tracking Area(HOME)**
Easy to indentify and recognisable to start normal tracking. | <img src="https://github.com/user-attachments/assets/8d26ce94-61c7-478f-913b-e870d972e3e0" width="450" height="350">
**Emergency Alerts & Location Sharing through SMS**
Sends alerts periodically with the important ones for both Emergency and Normal Tracking. |<img src="https://github.com/user-attachments/assets/7c6dae51-aacd-41e6-ab86-87f5b72ed1c2" width="450" height="350">
**Contacts**
Users are provided with multiple ways to access contacts to ease add/remove/modify contacts process using smaller window or full fragment on their fingertips. | <img src="https://github.com/user-attachments/assets/9c94fa4a-80e4-48ae-899f-24577d400ba3" width="450" height="350">
**Location**
Users are provided with location history and multiple ways to to ease add/remove location history using smaller window or full fragment on their fingertips. |<img src="https://github.com/user-attachments/assets/7667976c-4640-46c8-8ae5-7343b288111b" width="450" height="350">
**Map** 
Users can see their current location on the map in satalite view and also find see nearby police stations.|<img src="https://github.com/user-attachments/assets/25f52745-9a37-4c2c-89fc-e7b273f33c66" width="450" height="350">
**Noraml Tracking Option Dialog**
Contains options like interval at which location is to be shared and is sms needed to to sent or not in normal tracking. |<img src="https://github.com/user-attachments/assets/ff52394e-4761-4a17-aa0b-4acc4d5f7dfb" width="450" height="350">
**Widget**
Widget feature is also added so that user can easily, swiftly and instantly SOS. |<img src="https://github.com/user-attachments/assets/2962a641-22d7-43dc-8f94-6c1c30cac7d6" width="450" height="350">
**Shake Detector**
Shake frequency detector to send SOS even when app isn't active (it only works when the emergency service is running in foreground even when the app isn't active) | <img src="https://github.com/user-attachments/assets/303978c7-0ef1-46ed-a073-a3cf9caeb2d4" width="450" height="350">

<br>

<h2 align="start" id="Permission">Permssions Required</h2>

* Manifest.permission.ACCESS_COARSE_LOCATION,
* Manifest.permission.ACCESS_FINE_LOCATION,
* Manifest.permission.POST_NOTIFICATIONS,
* Manifest.permission.READ_PHONE_STATE,
* Manifest.permission.SEND_SMS,
* Manifest.permission.READ_CONTACTS,
* Manifest.permission.ACCESS_BACKGROUND_LOCATION,
* CALL_PHONE, FOREGROUND_SERVICE, FOREGROUND_SERVICE_LOCATION, ACCESS_NETWORK_STATE, INTERNET
<br>
<br>
<b>These permissions are required which are essential for the core functionality of the app.</b>

<h2 align="start" id="Inspiration">Installation False Warnings</h2>
As this application is under development due to which it is not currently on PlayStore and the app requires the above permissions for the core functionality, so the google might show a dialog to not to install the app as it is not avaiable on it and not signed by them so please ignore it and proceed to installation.<br>
Similary some app inbuilt security applications <b>might falsely flag it as virus</b> as it is not installed from the playstore or thier repective app store and requires the above permission, so please ignore it.<br>
<b>This application is completely safe and no information is being collected by anyone not even by the developer, so be assured the application is completely isolated to the user's device only<b>
<br>

