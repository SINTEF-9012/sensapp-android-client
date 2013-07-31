package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.SensorActivity;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AbstractSensor;

/**
 * @author Jonathan Nain
 * This class presents a minimalist service which use the SensApp android API to log the sensors.
 * It is started by the alarm manager and self stopped as soon every sensor has inserted a new measure.
 */
public class SensorLoggerTask extends AbstractSensorLoggerTask{

    public SensorLoggerTask(AbstractSensor as, Context c){
        super(as, c);
    }

    @Override
    public void run(){
        //Run by the task, this is the main of this task.

        if (SensAppHelper.isSensAppInstalled(context)) {
            if(sensors != null && sensor != null){
                registerAndListenSensor();
                sensor.setData(context);
                sensor.insertMeasure(context);
            }
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
        }
    }
}
