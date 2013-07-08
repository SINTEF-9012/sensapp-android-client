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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import org.sensapp.android.sensappdroid.api.SensAppHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * @author Fabien Fleurey , Jonathan Nain
 * This class presents a minimalist service which use the SensApp android API to log the sensors.
 * It is started by the alarm manager and self stopped as soon every sensor has inserted a new measure.
 */
public class SensorLoggerTask extends TimerTask implements SensorEventListener{

	private static final String TAG = SensorLoggerTask.class.getSimpleName();

    static SensorManager sensorManager = null;
    static List<AbstractSensor> sensors;
    private AbstractSensor sensor = null;
    static Context context;

    SensorLoggerTask(AbstractSensor as,Context context){
        sensor = as;
        this.context = context;
        if(sensorManager == null){
            initSensorManager();
        }
    }

    @Override
    public void run() {
        //Run by the task, this is the main of this task.

        if (SensAppHelper.isSensAppInstalled(context)) {
            if(sensors != null && sensor != null){
                registerAndListenSensor();
                if(sensor.getClass() != AndroidSensor.class && (System.currentTimeMillis() - sensor.getLastMeasure()) > sensor.getMeasureTime()){
                    sensor.setData(context);
                    sensor.insertMeasure(context);
                    sensor.setLastMeasure(); //refresh time of the last measure
                }
            }
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
        }
    }

    private void registerAndListenSensor(){
        if(sensorManager == null){
            initSensorManager();
        }
        else{
            sensor.registerInSensApp(context, R.drawable.ic_launcher);
            if(sensor.isListened() && sensor.getClass() == AndroidSensor.class)
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

            sensor.insertMeasure(context);
            sensor.setLastMeasure();
            sensorManager.unregisterListener(this, sensor.getSensor());
        }
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

    public AbstractSensor getSensor(){
        return this.sensor;
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

    static public AbstractSensor getSensorByName(String name){
        for(AbstractSensor s: sensors){
            if(s.getName().equals(name))
                return s;
        }
        return null;
    }

    void initSensorManager(){
        Intent startService = new Intent(context, SensorManagerService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, startService, PendingIntent.FLAG_ONE_SHOT);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
