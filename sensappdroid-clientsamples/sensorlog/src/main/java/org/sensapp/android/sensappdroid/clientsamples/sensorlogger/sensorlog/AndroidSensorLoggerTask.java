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
package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.SensorActivity;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AndroidSensor;

/**
 * @author Jonathan Nain
 * This class presents a minimalist service which use the SensApp android API to log the sensors.
 * It is started by the alarm manager and self stopped as soon every sensor has inserted a new measure.
 */
public class AndroidSensorLoggerTask extends AbstractSensorLoggerTask implements SensorEventListener{

    public AndroidSensorLoggerTask(AndroidSensor as, Context c){
        super(as, c);
    }

    @Override
    public void run(){
        //Run by the task, this is the main of this task.

        if (SensAppHelper.isSensAppInstalled(context)) {
            if(sensors != null && sensor != null)
                registerAndListenSensor();
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
        }
    }

    protected void registerAndListenSensor(){
        super.registerAndListenSensor();
        if(sensor.isListened())
            sensorManager.registerListener(this, sensor.getSensor(), SensorManager.SENSOR_DELAY_UI);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
