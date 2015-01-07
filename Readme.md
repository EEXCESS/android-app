# Contextopheles
Contextopheles is a mobile just-in-time retrieval application for the Android platform. Based on the current user context, it proactively retrieves and presents cultural heritage objects from Europeana. For example, if a text selection is copied to the clipboard, the content of the clipboard is analyzed and a corresponding query is sent to Europeana. 
![Copy text to clipboard](/demo/1.png "Copy text to clipboard")
If this query yields results, a little icon appears in the notification bar. Investigating this notification, a message with the query terms and the amount of received results is shown. 
![Notification, swipe to view query](/demo/2.png "Notification, swipe to view query")
Selecting the query displays the results in a list.
![Investigate results](/demo/3.png "Investigate results")



## Project Setup
This document should help to get the Contextopheles project up and running.

The project has been created using Android Studio and the cradle build system.
If either of those has been updated ever since, you most likely will have to adapt something in the project.

## Project Organisation

* BKS: needed to make a keystore (for Mingle.IO)
* Frameworks: the Frameworks used
* Plugins: The main context plugins for Contextopheles
* Prototypes: The prototype apps.
* Support: Support apps

## Important notes:

The AWARE Framework needs to be built both as an app and a framework.
1. make sure that the build.gradle in Frameworks/aware_framework_v2 reads

    apply plugin: 'android'
    //apply plugin: 'android-library'

clean, sync gradle and build it as an APK.

2. To build the other modules, change the build.gradle to

    apply plugin: 'android'
    //apply plugin: 'android-library'

and then sync gradle and build.

All Modules can be updated easily using the ALL build target in Android Studio.

Always install the AWARE app first to avoid running into permission problems!