package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.benchmark;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.R;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.SensorActivity;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AbstractSensor;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AndroidSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * @author Jonathan Nain
 * This class presents a minimalist service which use the SensApp android API to log the sensors.
 * It is started by the alarm manager and self stopped as soon every sensor has inserted a new measure.
 */
public class BenchmarkTask extends TimerTask implements SensorEventListener{

    static private SensorManager sensorManager = null;
    static List<AbstractSensor> sensors;
    private AbstractSensor sensor = null;
    static private Context context;
    private long registerTime;
    static final int NB_MEASURES = 10;

    BenchmarkTask(AbstractSensor as, Context c){
        sensor = as;
        context = c;
        if(sensorManager == null){
            initSensorManager(c);
        }
    }

    @Override
    public void run() {
        //Run by the task, this is the main of this task.

        if (SensAppHelper.isSensAppInstalled(context)) {
            if(sensors != null && sensor != null){
                registerAndListenSensor();
            }
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
        }
    }

    private void registerAndListenSensor(){
        if(sensor.isListened() && sensor.getClass() == AndroidSensor.class){
            registerTime = System.currentTimeMillis();
            sensorManager.registerListener(this, sensor.getSensor(), SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Run when an AndroidSensor is listened and has made a nex measure.

        if(sensor != null && (System.currentTimeMillis() - sensor.getLastMeasure()) > sensor.getMeasureTime()){
            if(sensor.isThreeDataSensor())
                sensor.setData(context, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            else
                sensor.setData(context, sensorEvent.values[0]);

            sensor.setLastMeasure();
            sensorManager.unregisterListener(this, sensor.getSensor());

            ((AndroidSensor)sensor).setBenchmarkTime(((AndroidSensor)sensor).getBenchmarkTime()+(System.currentTimeMillis() - registerTime));
            ((AndroidSensor)sensor).increaseNbMeasure();
            if(((AndroidSensor)sensor).getNbMeasures() == NB_MEASURES)
                BenchmarkService.cancelLog(context, sensor);
        }
    }

    static public void addSensor(AbstractSensor as){
        if(!sensors.contains(as))
            sensors.add(as);
    }

    static public void initSensorArray(){
        if(sensors == null)
            sensors = new ArrayList<AbstractSensor>();
    }

    static public List<AbstractSensor> getSensors(){
        return sensors;
    }

    public AbstractSensor getSensor(){
        return this.sensor;
    }

    static public AbstractSensor getSensorByName(String name){
        for(AbstractSensor s: sensors){
            if(s.getName().equals(name))
                return s;
        }
        return null;
    }

    static public void initSensorManager(Context c){
        Intent startService = new Intent(c, BenchmarkService.class);
        PendingIntent pendingIntent = PendingIntent.getService(c, 0, startService, PendingIntent.FLAG_ONE_SHOT);
        ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
    }

    static public void setUpSensors(Context c, SensorManager manager){
        sensorManager = manager;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String compositeName = sp.getString(c.getString(R.string.pref_compositename_key), SensorActivity.compositeName);

        if(sensors == null)
            initSensorArray();

        for(Sensor s: sensorManager.getSensorList(Sensor.TYPE_ALL)){
            AndroidSensor as = new AndroidSensor(s, compositeName);
            setUpSensor(as, c);
        }
    }

    static private void setUpSensor(AbstractSensor as, Context c){
        as.setRefreshRate(100);
        as.setListened(true);
        addSensor(as);
        BenchmarkService.setLog(c, as);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
