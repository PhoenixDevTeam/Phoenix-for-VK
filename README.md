# Phoenix (R) for VK
First open-sourced VK client for Android inspired by Material Design.

<b>Screenshots:</b>
<img src="Screenshots.jpg"/>

<b>Available at Google Play:</b> https://play.google.com/store/apps/details?id=dev.ezorrio.phoenix <br>

<b>Build guide:</b>
Requirements:
  1) Android Studio 3.5 or higher
  2) Android SDK r28
  
<b>Setting up enviroment:</b>
In order to build this project you need to add missing file with several VK keys.

  1) Create "build-config-fields.properties" file in the root of project with data in following format

```
vk_app_id=6209567
fcm_sender_id=""
vk_service_token=""
vk_client_secret=""
youtube_dev_key=""
```

You can use one value for Lite and Full version if you wish.

  2) We also need to configure Google Services in order to get push-messages and crash reports. Following files you can find in your Google Play console.<br>
  <b>Create file:</b><br>
    - app/google-services.json

  All project variables are setup now.

  3) Build APK :)
