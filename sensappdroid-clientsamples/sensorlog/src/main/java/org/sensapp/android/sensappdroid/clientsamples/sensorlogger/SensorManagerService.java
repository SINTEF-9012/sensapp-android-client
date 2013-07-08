package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 08/07/13
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */

public class SensorManagerService extends Service {

    private static final int ACTIVE_NOTIFICATION_ID = 79290;
    private static final String TAG = SensorManagerService.class.getSimpleName();
    private static Timer timer = null;
    private static List<SensorLoggerTask> taskList= new ArrayList<SensorLoggerTask>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        SensorLoggerTask.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String compositeName = sp.getString(getString(R.string.pref_compositename_key), SensorActivity.compositeName);

        if(SensorLoggerTask.sensors == null)
            SensorLoggerTask.initSensorArray();

        for(Sensor s: SensorLoggerTask.sensorManager.getSensorList(Sensor.TYPE_ALL)){
            AndroidSensor as = new AndroidSensor(s, compositeName);
            as.setRefreshRate(sp.getInt(as.getName(), as.getDefaultRate()));
            as.setListened(sp.getBoolean(as.getFullName(), false));
            if(!SensorLoggerTask.sensors.contains(as))
                SensorLoggerTask.addSensor(as);
            if(as.isListened())
                setLog(getApplicationContext(), as);
        }

        BatterySensor bs = new BatterySensor(compositeName);
        bs.setRefreshRate(sp.getInt(bs.getName(), bs.getDefaultRate()));
        bs.setListened(sp.getBoolean(bs.getFullName(), false));
        if(!SensorLoggerTask.sensors.contains(bs))
            SensorLoggerTask.addSensor(bs);
        if(bs.isListened())
            setLog(getApplicationContext(), bs);
        //Add the Free Memory percentage
        FreeMemorySensor fms = new FreeMemorySensor(compositeName);
        fms.setRefreshRate(sp.getInt(fms.getName(), fms.getDefaultRate()));
        fms.setListened(sp.getBoolean(fms.getFullName(), false));
        if(!SensorLoggerTask.sensors.contains(fms))
            SensorLoggerTask.addSensor(fms);
        if(fms.isListened())
            setLog(getApplicationContext(), fms);


        //Log.d("coucou", "sensors " + SensorLoggerTask.sensors.toString());
        Log.d("coucou", "test " + taskList.toString());

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected static void setLog(Context context, AbstractSensor as) {
        if(getTaskByAbstractSensor(as) != null)
            return;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(SensorActivity.SERVICE_RUNNING, true).commit();

        if(timer == null)
            timer = new Timer(false);

        SensorLoggerTask newTask = new SensorLoggerTask(as, context);
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
        SensorLoggerTask cancelledTask = getTaskByAbstractSensor(as);
        if(cancelledTask != null)
            cancelledTask.cancel();
        taskList.remove(cancelledTask);

        if(taskList.isEmpty()){
            timer.cancel();
            timer = null;
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(ACTIVE_NOTIFICATION_ID);
        }
    }

    private static SensorLoggerTask getTaskByAbstractSensor(AbstractSensor as){
        for(SensorLoggerTask t : taskList)
            if(t.getSensor().equals(as))
                return t;
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }

}
