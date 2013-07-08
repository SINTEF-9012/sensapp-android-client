/**
 * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import org.sensapp.android.sensappdroid.api.SensAppHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Fleurey , Jonathan Nain
 * This class presents a minimalist service which use the SensApp android API to log the sensors.
 * It is started by the alarm manager and self stopped as soon every sensor has inserted a new measure.
 */
public class SensorLoggerService extends Service implements SensorEventListener{

	private static final String TAG = SensorLoggerService.class.getSimpleName();

    /*static */SensorManager sensorManager = null;
    static List<AbstractSensor> sensors;
    private AbstractSensor sensor = null;
    private int sensorIndex;
    private Runnable alarmLauncher;
    private Handler alarmHandler = new Handler();
    private Boolean notSent = true;

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){
        sensorIndex = intent.getExtras().getInt("sensorIndex");
        if(sensors != null)
            sensor = sensors.get(sensorIndex);

        if(sensor==null || !sensor.isListened())
            stopSelf();
        else{
            sensor.setMeasured(false);
            alarmLauncher = new Runnable() {
                @Override
                public void run() {
                    AlarmHelper.setAlarm(getApplicationContext(), sensor.getMeasureTime(), sensorIndex);
                }
            };

            if (SensAppHelper.isSensAppInstalled(getApplicationContext())) {
                // Get all the sensors of the Android.
                if(sensorManager == null)
                    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                if(sensor != null && sensor.isListened()){
                    registerAndListenSensor();
                    if(sensor.getClass() != AndroidSensor.class){
                        sensor.setData(this);
                        sensor.setMeasured(true);
                        sensor.insertMeasure(getApplicationContext());

                        if(sensor.isListened() && notSent){
                            notSent = false;
                            alarmHandler.postDelayed(alarmLauncher, sensor.getMeasureTime());
                        }
                        stopSelf();
                    }
                }
            } else {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
                AlarmHelper.cancelAlarm(getApplicationContext());
                //Log.e(TAG, "SensApp has been uninstalled");
            }
        }
        return 0;
    }

    private void registerAndListenSensor(){
        sensor.registerInSensApp(getApplicationContext(), R.drawable.ic_launcher);
        if(sensor.isListened() && sensor.getClass() == AndroidSensor.class)
            sensorManager.registerListener(this, sensor.getSensor(), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final AbstractSensor as = getSensorBySensor(sensorEvent.sensor);

        if(sensorEvent.sensor == sensor.getSensor() && (System.currentTimeMillis() - sensor.getLastMeasure()) > sensor.getMeasureTime()){
            Log.d("coucou", " " + sensor.getName());

            if(sensor != null && sensor.isListened()){
                if(sensor.isThreeDataSensor())
                    sensor.setData(this, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                else
                    sensor.setData(this, sensorEvent.values[0]);

                sensor.setMeasured(true);
                sensor.insertMeasure(getApplicationContext());
                sensor.setLastMeasure();
            }

            if(sensor.isListened() && notSent){
                notSent = false;
                alarmHandler.postDelayed(alarmLauncher, sensor.getMeasureTime());
                sensorManager.unregisterListener(this, sensor.getSensor());
            }
            stopSelf();
        }

        if((System.currentTimeMillis() - as.getLastMeasure()) > as.getMeasureTime()){
            Log.d("coucou", "2 " + as.getName());

            if(as != null && as.isListened()){
                if(as.isThreeDataSensor())
                    as.setData(this, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                else
                    as.setData(this, sensorEvent.values[0]);

                as.setMeasured(true);
                as.insertMeasure(getApplicationContext());
                as.setLastMeasure();
            }

            if(as.isListened()){
                alarmLauncher = new Runnable() {
                    @Override
                    public void run() {
                        AlarmHelper.setAlarm(getApplicationContext(), as.getMeasureTime(), sensors.indexOf(as));
                    }
                };
                alarmHandler.postDelayed(alarmLauncher, as.getMeasureTime());
                sensorManager.unregisterListener(this, as.getSensor());
            }
            stopSelf();
        }
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

    static public AbstractSensor getSensorByName(String name){
        for(AbstractSensor s: sensors){
            if(s.getName().equals(name))
                return s;
        }
        return null;
    }

    static public AbstractSensor getSensorBySensor(Sensor sensor){
        for(AbstractSensor s: sensors){
            if(s.getSensor() == sensor)
                return s;
        }
        return null;
    }

    static public void addSensor(AbstractSensor as){
        sensors.add(as);
    }

    static public void initSensorArray(){
        sensors = new ArrayList<AbstractSensor>();
    }

    static public List<AbstractSensor> getSensors(){
        return sensors;
    }

    static public boolean noSensorListened(){
        if(sensors != null){
            for(AbstractSensor s : sensors){
                if(s.isListened())
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
