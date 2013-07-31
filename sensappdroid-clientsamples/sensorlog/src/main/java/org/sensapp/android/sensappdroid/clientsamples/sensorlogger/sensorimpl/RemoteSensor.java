package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl;

import android.content.Context;
import android.hardware.Sensor;
import org.sensapp.android.sensappdroid.api.SensAppUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 05/06/13
 * Time: 14:25
 *
 * This is an interface used to manage sensors provided by the Android system.
 * A generalisation is possible by passing arguments through the constructor like type, unit...
 * At the moment there are only sensors with 1 or 3 data, so 2 data sensor or not implemented.
 */

public class RemoteSensor extends AbstractSensor {
    private String sensorName;

    public RemoteSensor(String sensorName) {
        setEntryLevel();
        initData();
        this.sensorName = sensorName;
    }

    final public boolean isThreeDataSensor(){
        return false;
    }

    final public boolean isOneDataSensor(){
        return true;
    }

    private void initData(){
        data = new float[1];
    }

    public void setData(Context context, float x){
        if(!isOneDataSensor())
            return;
        data[0]=x*entryLevel;
    }

    private void setEntryLevel(){
        entryLevel = 1;
    }

    public String getData(){
        StringBuilder out = new StringBuilder();
        out .append(getSensor().getName()+"\r\n")
            .append(data[0]);
        if(isThreeDataSensor()){
            out .append(", " + data[1])
                .append(", " + data[2]);
        }
        out.append("\r\n\r\n");
        return out.toString();
    }

    public Sensor getSensor(){
        return mSensor;
    }

    public String getType() {
        return "TYPE_UNKNOWN";
    }

    public String getName() {
        return sensorName;
    }

    public SensAppUnit getUnit() {
        return SensAppUnit.MOL;
    }

    @Override
    public int getDefaultRate() {
        return 0;
    }
}
