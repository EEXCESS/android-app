# Contextopheles - Installation and Setup Guide

# Installation

## Necessary Settings for Installation

### Allow Apps from unknown Origin
In the security settings screen, allow apps from unknown origin.

### Unlock Developer Mode (Optional to read log using logcat)
Unlocking Developer Mode differs from device to device. On recent Nexus Devices, you can unlock it by repeatedly (10 times) tapping the build-number in Settings -> About the phone.
Allow USB Debugging in your device's developer options and allow your computer, when asked.


## Installation of the AWARE Framework
### Install the modified AWARE Framework
[Download and install APK](APKs/aware_framework_v2-debug-unaligned.apk)

Make sure to install and at least once run this (AWARE Plugins or AWARE Settings App), before you install any of the plugins.

## Installation of the AWARE Plugins and necessary Settings 

### ClipboardCatcher
[Download and install APK](APKs/com.aware.plugin.clipboardcatcher.apk)

The ClipboardCatcher does not need any special settings.

### ImageReceiver
[Download and install APK](APKs/com.aware.plugin.imagereceiver.apk)

The ImageReceiver does not need any special settings.


### SMSReceiver
[Download and install APK](APKs/com.aware.plugin.smsreceiver.apk)

The SMSReceiver does not need any special settings.

### OSMPoiReceiver
[Download and install APK](APKs/com.aware.plugin.osmpoiresolver.apk)

The OSMPoiReceiver needs the Locations Module of AWARE. Activate it by checking GPS in AWARE Sensors -> Locations. You can decrease the time between updates for more frequent querying of POIs near your position.

### GeonameResolver
[Download and install APK](APKs/com.aware.plugin.geonameresolver.apk)

The GeonameResolver needs the Locations Module of AWARE. Activate it by checking GPS in AWARE Sensors -> Locations. You can decrease the time between updates for more frequent querying of places near your position.

### NotificationCatcher
[Download and install APK](APKs/com.aware.plugin.notificationcatcher.apk)

The NotificationCatcher needs the permission to access notifications. Activate it in Settings -> Accessibility Settings.

### UIContent
[Download and install APK](APKs/com.aware.plugin.uicontent.apk)

The UIContent Plugin needs the permission to access the content of the screen. Activate it  in Settings -> Accessibility Settings.

### TermCollector
[Download and install APK](APKs/com.aware.plugin.termcollector.apk)

The TermCollector does not need any special settings. To work correctly though, it needs at least one these Plugins working correctly:
ClipboardCatcher, SMSReceiver, OSMPoiReceiver, NotificationCatcher, UIContent

### AutomaticQuery
[Download and install APK](APKs/com.aware.plugin.automaticquery.apk)

The AutomaticQuery Plugin does not need any special settings. To work correctly though, it needs at least one these Plugins working correctly:
TermCollector


## Support Tools
### TermViewer
[Download and install APK](termviewer.apk)

The TermViewer tool allows access to the contents of the plugin-databases.

# Uninstall
To uninstall, remove the APKs via App Management.

# Troubleshooting
t.b.d.

# Changelog of this Document
* 2014-03-04, Initial Version
* 2014-03-09, Changed to use modified Repository in AWARE App