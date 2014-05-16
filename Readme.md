# Contextopheles README (2014-05-16)

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