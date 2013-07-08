package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 08/07/13
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */

public class SensorManagerService extends Service {

    @Override
    public void onCreate(){
        super.onCreate();
        SensorLoggerTask.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
