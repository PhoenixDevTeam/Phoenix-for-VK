# Phoenix-for-VK
First open-sourced VK client for Android inspired by Material Design.

<b>Available at Google Play</b><br>
  <b>Full:</b> https://play.google.com/store/apps/details?id=biz.dealnote.phoenix <br>
  <b>Lite:</b> https://play.google.com/store/apps/details?id=biz.dealnote.messenger

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

  2) We also need to configure Google Services in order to get push-messages and crash reports.<br>
    a) Create file "app/google-services.json"
```
{
  "project_info": {
    "project_id": "",
    "project_number": "",
    "name": ""
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "",
        "client_id": "android:biz.dealnote.messenger",
        "client_type": 1,
        "android_client_info": {
          "package_name": "biz.dealnote.messenger"
        }
      },
      "oauth_client": [
        {
          "client_id": "",
          "client_type": 1,
          "android_info": {
            "package_name": "biz.dealnote.messenger",
            "certificate_hash": ""
          }
        }
      ],
      "api_key": [],
      "services": {
        "analytics_service": {
          "status": 1
        },
        "cloud_messaging_service": {
          "status": 2,
          "apns_config": []
        },
        "appinvite_service": {
          "status": 1,
          "other_platform_oauth_client": []
        },
        "google_signin_service": {
          "status": 1
        },
        "ads_service": {
          "status": 1
        }
      }
    }
  ],
  "client_info": [],
  "ARTIFACT_VERSION": "1"
}
```

  b) Create file "app/src/lite/google-services.json":
```
{
  "project_info": {
    "project_number": "",
    "firebase_url": "m",
    "project_id": "",
    "storage_bucket": ""
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "",
        "android_client_info": {
          "package_name": "biz.dealnote.messenger"
        }
      },
      "oauth_client": [
        {
          "client_id": "",
          "client_type": 3
        },
        {
          "client_id": "",
          "client_type": 1,
          "android_info": {
            "package_name": "biz.dealnote.messenger",
            "certificate_hash": ""
          }
        }
      ],
      "api_key": [
        {
          "current_key": ""
        }
      ],
      "services": {
        "analytics_service": {
          "status": 1
        },
        "appinvite_service": {
          "status": 2,
          "other_platform_oauth_client": [
            {
              "client_id": "",
              "client_type": 3
            }
          ]
        },
        "ads_service": {
          "status": 2
        }
      }
    }
  ],
  "configuration_version": "1"
}
```
  c) Create file "app/src/full/google-services.json"
  
```  
{
  "project_info": {
    "project_number": "",
    "firebase_url": "",
    "project_id": "",
    "storage_bucket": ""
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "",
        "android_client_info": {
          "package_name": "biz.dealnote.phoenix"
        }
      },
      "oauth_client": [
        {
          "client_id": "",
          "client_type": 3
        },
        {
          "client_id": "",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": ""
        }
      ],
      "services": {
        "analytics_service": {
          "status": 1
        },
        "appinvite_service": {
          "status": 1,
          "other_platform_oauth_client": []
        },
        "ads_service": {
          "status": 2
        }
      }
    }
  ],
  "configuration_version": "1"
}
```
All project variables are setup now.

  3) Build APK via Android Studio
