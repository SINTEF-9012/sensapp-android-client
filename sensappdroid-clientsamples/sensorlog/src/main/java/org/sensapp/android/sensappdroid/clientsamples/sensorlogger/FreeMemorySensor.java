package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.content.Context;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import org.sensapp.android.sensappdroid.api.SensAppUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 13/06/13
 * Time: 13:59
 */
public class FreeMemorySensor extends AbstractSensor {

    final static int DEFAULT_RATE = 10000;

    FreeMemorySensor(String composite) {
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
        float level = (float)FreeMemory()/TotalMemory()*100;
        data[0] = level;
    }

    public boolean isThreeDataSensor(){
        return false;
    }

    public boolean isOneDataSensor(){
        return true;
    }

    public String getData(){
        StringBuilder out = new StringBuilder()
            .append(getSensor().getName()+"\r\n")
            .append(data[0])
            .append("\r\n\r\n");
        return out.toString();
    }

    public Sensor getSensor(){
        return null;
    }

    public String getType(){
        return "TYPE_MEMORY";
    }

    public String getName(){
        return "Memory";
    }

    public SensAppUnit getUnit(){
        return SensAppUnit.PERCENT;
    }

    public int TotalMemory()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        return (statFs.getBlockCount() * statFs.getBlockSize()) / 1048576;
    }

    public int FreeMemory()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        return (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
    }

    public int BusyMemory()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        int Total = (statFs.getBlockCount() * statFs.getBlockSize()) / 1048576;
        int Free  = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
        return Total - Free;
    }

    final public int getDefaultRate(){
        return DEFAULT_RATE;
    }
}
