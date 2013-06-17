package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.api.SensAppUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 13/06/13
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public class BatterySensor extends AbstractSensor {

    BatterySensor(String composite) {
        mSensor = null;
        setEntryLevel();
        initData();
        mComposite = composite;
    }

    void setEntryLevel(){
        entryLevel = 1;
    }

    void initData(){
        data = new float[1];
    }

    public Uri registerInSensApp(Context context, int drawable){
        return super.registerInSensApp(context, drawable);
    }

    public void setData(Context context){
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        data[0] = level;
    }

    public boolean isThreeDataSensor(){
        return false;
    }

    public boolean isOneDataSensor(){
        return true;
    }

    public String getData(){
        StringBuffer out = new StringBuffer();
        out.append(getSensor().getName()+"\r\n");
        out.append(data[0]);
        out.append("\r\n\r\n");
        return out.toString();
    }

    public Sensor getSensor(){
        return null;
    }

    public String getType(){
        return "TYPE_BATTERY";
    }

    public String getName(){
        return mComposite+"_"+"Battery";
    }

    public SensAppUnit getUnit(){
        return SensAppUnit.PERCENT;
    }
}
