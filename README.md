SensorLog
============================

The SensorLog android application is a client for SensApp android Application. It is an example of what clients can do to log data into SensApp android application.

This client can find every sensors (referenced like this by Android) contained in the android host system. These sensors can be logged, or not by a simple click on the row attached.

There is also some other "sensors" implemented which show that every kind of data can be logged using this client by implementing a simple class file.
We can see some systems sensors (battery, memory) and bluetooth sensor (BluetoothLight).

This last sensor (BluetoothLight) is a light sensor plugged to an Arduino. This Arduino is managed by the Android using a bluetooth connexion.

The sensors will log infinitely if you let them "on". To shut the application down easily, use the "Exit" button.

Download
---------------------
* [Latest release build](https://play.google.com/store/apps/details?id=org.sensapp.android.sensappdroid.clientsamples.sensorlogger): available on the Google Play.
* [Latest snapshot build](http://build.thingml.org/job/Build%20SensApp%20Android/lastSuccessfulBuild/org.sensapp.android$sensappdroid-main/): intall the *sensorlog-X.X.X-SNAPSHOT-aligned.apk* file.

Bugs and Feature requests
-------------------------
Please see and use the issue tracker at: https://github.com/SINTEF-9012/sensapp-android-client/issues

SensApp android application
-------------------------
As SensApp android application is needed to use this SensApp client you will be asked to install this application. If it is not the case you can find this appplication [here](https://play.google.com/store/apps/details?id=org.sensapp.android.sensappdroid).

Project documentation
------------------------
https://github.com/SINTEF-9012/sensapp-android-client

Screenshot
-------------------------
![SensorLog Start Screen](https://raw.github.com/SINTEF-9012/sensapp-android-client/master/extra/screenshot/startscreen.png) ![Sensors Logging](https://raw.github.com/SINTEF-9012/sensapp-android-client/master/extra/screenshot/startedsensors.png) ![Settings Screen](https://raw.github.com/SINTEF-9012/sensapp-android-client/master/extra/screenshot/settingsscreen.png) ![Setting Edit](https://raw.github.com/SINTEF-9012/sensapp-android-client/master/extra/screenshot/settingedit.png).


