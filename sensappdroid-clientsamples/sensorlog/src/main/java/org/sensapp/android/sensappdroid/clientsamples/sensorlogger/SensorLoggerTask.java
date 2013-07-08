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
    private int sensorIndex;
    static Context context;

    private class MyService extends Service{

        @Override
        public void onCreate(){
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }


    SensorLoggerTask(AbstractSensor as, int asIndex, Context context){
        sensor = as;
        sensorIndex = asIndex;
        this.context = context;
        Intent startService = new Intent(context, MyService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, startService, PendingIntent.FLAG_ONE_SHOT);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), sensor.getMeasureTime(), pendingIntent);
    }


    private void registerAndListenSensor(){
        sensor.registerInSensApp(context, R.drawable.ic_launcher);
        if(sensor.isListened() && sensor.getClass() == AndroidSensor.class)
            sensorManager.registerListener(this, sensor.getSensor(), SensorManager.SENSOR_DELAY_UI);
        else
            sensorManager.unregisterListener(this, sensor.getSensor());
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensor != null){
            if(sensor.isThreeDataSensor())
                sensor.setData(context, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            else
                sensor.setData(context, sensorEvent.values[0]);

            sensor.setMeasured();
            sensor.setFreshMeasure(true);
            sensor.insertMeasure(context);
            sensor.setLastMeasure();
            sensor.setFreshMeasure(false);
            sensorManager.unregisterListener(this, sensor.getSensor());
            this.cancel();
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

    @Override
    public void run() {
        if (SensAppHelper.isSensAppInstalled(context)) {
            if(sensors != null && sensor != null){
                if(sensor.getClass() != AndroidSensor.class){
                    sensor.setData(context);
                    sensor.setMeasured();            //at least one measure
                    sensor.setFreshMeasure(true);    //a new measure has been made
                    //insertMeasures();           //put it into local database
                    sensor.insertMeasure(context);
                    sensor.setLastMeasure(); //refresh time of the last measure
                    sensor.setFreshMeasure(false);
                    this.cancel();
                }
                else
                    registerAndListenSensor();
            }
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
        }
    }
}
