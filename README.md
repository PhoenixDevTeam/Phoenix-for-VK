# Phoenix-for-VK
First open-sourced VK client for Android inspired by Material Design.

<b>Build guide:</b>
Requirements:
  1) Android Studio 3.0 Beta 7 or higher
  2) Android SDK r26, Build-Tools v.26.0.2
  
<b>Setting up enviroment:</b>
In order to build this project you need to add missing file with several VK keys.

  1) Create "build-config-fields.properties" file in the root of project with the following data

```
full_vk_app_id=
full_gcm_sender_id=""
full_vk_service_token=""
full_vk_client_secret=""
lite_vk_app_id=
lite_gcm_sender_id=""
lite_vk_service_token=""
lite_vk_client_secret=""
```

You can use one value for Lite and Full version if you wish.

  2) Build APK via Android Studio
