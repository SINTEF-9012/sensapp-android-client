package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.benchmark;

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
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.R;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.SensorActivity;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AbstractSensor;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AndroidSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 08/07/13
 * Time: 10:31
 */
public class BenchmarkService extends Service {

    static private final int ACTIVE_NOTIFICATION_ID = 79290;
    static private Timer timer = null;
    static private List<BenchmarkTask> taskList= new ArrayList<BenchmarkTask>();
    static private Intent myIntent;
    static private SharedPreferences sp;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myIntent = intent;
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        BenchmarkTask.setUpSensors(getApplicationContext(), (SensorManager) getSystemService(SENSOR_SERVICE));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected static void setLog(Context context, AbstractSensor as) {
        //if log already active for this sensor
        if(getTaskByAbstractSensor(as) != null)
            return;
        sp = PreferenceManager.getDefaultSharedPreferences(context);

        sp.edit().putBoolean(SensorActivity.SERVICE_RUNNING, true).commit();

        if(timer == null)
            timer = new Timer(false);

        BenchmarkTask newTask = new BenchmarkTask(as, context);
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
        BenchmarkTask cancelledTask = getTaskByAbstractSensor(as);
        if(cancelledTask == null)
            return;

        cancelledTask.cancel();
        taskList.remove(cancelledTask);

        if(taskList.isEmpty()){
            registerBenchmarkResults(context);
            timer.cancel();
            timer = null;
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(ACTIVE_NOTIFICATION_ID);
            sp.edit().putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
        }
    }

    private static void registerBenchmarkResults(Context c){
        for(AbstractSensor abs: BenchmarkTask.sensors){
            if(abs instanceof AndroidSensor){
                AndroidSensor s = (AndroidSensor)abs;
                sp.edit().putFloat(s.getName()+"_avg", (float)s.getBenchmarkTime() / s.getNbMeasures()).commit();
                s.setBenchmarkAvg((double)s.getBenchmarkTime() / s.getNbMeasures());

                SensorActivity.refreshConsumption(abs, c);
            }
        }
    }

    private static BenchmarkTask getTaskByAbstractSensor(AbstractSensor as){
        for(BenchmarkTask t : taskList){
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
