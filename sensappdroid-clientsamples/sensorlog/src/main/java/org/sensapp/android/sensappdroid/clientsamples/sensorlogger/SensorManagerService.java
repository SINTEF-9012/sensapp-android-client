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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AbstractSensor;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AndroidSensor;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorlog.AbstractSensorLoggerTask;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorlog.AndroidSensorLoggerTask;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorlog.SensorLoggerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 08/07/13
 * Time: 10:31
 */
public class SensorManagerService extends Service {

    static private final int ACTIVE_NOTIFICATION_ID = 79290;
    static private Timer timer = null;
    static private List<AbstractSensorLoggerTask> taskList= new ArrayList<AbstractSensorLoggerTask>();
    static private Intent myIntent;
    private int REQUEST_ENABLE_BT = 2000; //What you want here.

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Make the service run infinitely
        myIntent = intent;
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        // we loose some time here, but this prevent code duplication as we use the code from
        // the activity
        while(SensorActivity.ME == null);
        SensorActivity.ME.connectBluetooth();
        AbstractSensorLoggerTask.setUpSensors(getApplicationContext(), (SensorManager) getSystemService(SENSOR_SERVICE),
                SensorActivity.ME.mBluetoothAdapter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static public void setLog(Context context, AbstractSensor as) {
        //if log already active for this sensor
        if(getTaskByAbstractSensor(as) != null)
            return;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(SensorActivity.SERVICE_RUNNING, true).commit();

        if(timer == null)
            timer = new Timer(false);

        AbstractSensorLoggerTask newTask;
        if(as instanceof AndroidSensor)
            newTask = new AndroidSensorLoggerTask((AndroidSensor)as, context);
        else
            newTask = new SensorLoggerTask(as, context);

        taskList.add(newTask);
        timer.scheduleAtFixedRate(newTask, 0, as.getMeasureTime());

        @SuppressWarnings("deprecation") // We don't want target only API 16...
                Notification notification = new Notification.Builder(context)
                .setContentTitle("SensorLog active")
                .setContentText("SensorLog active")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, SensorActivity.class), 0))
                .getNotification();
        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(ACTIVE_NOTIFICATION_ID, notification);
    }

    protected static void cancelLog(Context context, AbstractSensor as) {
        AbstractSensorLoggerTask cancelledTask = getTaskByAbstractSensor(as);
        if(cancelledTask == null)
            return;

        cancelledTask.cancel();
        taskList.remove(cancelledTask);

        if(taskList.isEmpty()){
            timer.cancel();
            timer = null;
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(ACTIVE_NOTIFICATION_ID);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            sp.edit().putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
        }
    }

    private static AbstractSensorLoggerTask getTaskByAbstractSensor(AbstractSensor as){
        for(AbstractSensorLoggerTask t : taskList){
            if(t.getSensor().equals(as))
                return t;
        }
        return null;
    }

    static public Intent getIntent(){
        return myIntent;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

}
