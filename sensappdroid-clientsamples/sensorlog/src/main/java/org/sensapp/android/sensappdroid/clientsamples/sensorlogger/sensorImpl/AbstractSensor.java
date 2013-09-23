package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl;

import android.content.Context;
import android.hardware.Sensor;
import android.net.Uri;
import android.preference.PreferenceManager;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.api.SensAppUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 13/06/13
 * Time: 13:40
 */
public abstract class AbstractSensor {

    final static int DEFAULT_RATE = 2000;            //Default rate of the sensor measures in milliseconds
    protected float[] data;                           //Array to stock data
    protected Sensor mSensor;                         //The sensor
    protected String mComposite;                      //The group where the sensor is
    protected float entryLevel = 1;                   //Entry level of the sensor measures
    protected boolean measured = false;               //If the sensor has been measured once
    protected boolean freshMeasure = false;           //If the sensor has new data to post
    protected long lastMeasure=0;                     //The time of the last measure
    protected int refreshRate;                        //The current rate of the sensor measures in milliseconds
    protected boolean listened = false;               //If the sensor is listened or not


    /**
     * Register sensor and his group into SensApp API
     * If the group is not present, add it.
     * Then add the different sensors (x, y, z)
     * Then make the link between sensors and group
     */
    public Uri registerInSensApp(Context context, int drawable, String description){
        Uri u = null;
        refreshRate = PreferenceManager.getDefaultSharedPreferences(context).getInt(getName(), getDefaultRate());
        if(!SensAppHelper.isCompositeRegistered(context, mComposite))
            SensAppHelper.registerComposite(context, mComposite, description);
        if(isOneDataSensor()){
            u = SensAppHelper.registerNumericalSensor(context, getFullName(), getType(), getUnit(), drawable);
            if(!SensAppHelper.isComposeRegistered(context, mComposite, getFullName()))
                SensAppHelper.registerCompose(context, getName()+"1", mComposite, getFullName());
        }
        else if(isThreeDataSensor()){
            u = SensAppHelper.registerNumericalSensor(context, getFullName()+"X", getType(), getUnit(), drawable);
            SensAppHelper.registerNumericalSensor(context, getFullName()+"Y", getType(), getUnit(), drawable);
            SensAppHelper.registerNumericalSensor(context, getFullName()+"Z", getType(), getUnit(), drawable);
            if(!SensAppHelper.isComposeRegistered(context, mComposite, getFullName()+"X")){
                SensAppHelper.registerCompose(context, getFullName()+"1", mComposite, getFullName()+"X");
                SensAppHelper.registerCompose(context, getFullName()+"2", mComposite, getFullName()+"Y");
                SensAppHelper.registerCompose(context, getFullName()+"3", mComposite, getFullName()+"Z");
            }
        }
        return u;
    }

    /**
     * Insert the measures of the sensor into SensApp API by using SensAppHelper.
     * @param context : the context of the application
     */
    public void insertMeasure(Context context){
        try {
            if(isOneDataSensor()){
                SensAppHelper.insertMeasure(context, getFullName(), data[0]);
            }
            else if(isThreeDataSensor()){
                SensAppHelper.insertMeasure(context, getFullName()+"X", data[0]);
                SensAppHelper.insertMeasure(context, getFullName()+"Y", data[1]);
                SensAppHelper.insertMeasure(context, getFullName()+"Z", data[2]);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void setData(Context context){
    }

    public void setData(Context context, float x){
    }

    public void setData(Context context, float x, float y, float z){
    }

    abstract public boolean isThreeDataSensor();
    abstract public boolean isOneDataSensor();

    public void setListened(boolean set){
        listened = set;
    }

    public boolean isListened(){
        return listened;
    }

    public void setLastMeasure(){
        lastMeasure = System.currentTimeMillis();
    }

    public long getLastMeasure(){
        return lastMeasure;
    }

    public void setFreshMeasure(boolean setter){
        freshMeasure = setter;
    }

    public boolean isFreshMeasure(){
        return freshMeasure;
    }

    public void setMeasured(){
        measured = true;
    }

    public boolean isMeasured(){
        return measured;
    }

    abstract public String getData();
    abstract public Sensor getSensor();

    public int getMeasureTime(){
        return refreshRate;
    }

    public void setRefreshRate(int rate){
        refreshRate = rate;
    }

    abstract public String getType();
    abstract public String getName();
    abstract public SensAppUnit getUnit();
    abstract public int getDefaultRate();

    public String getFullName(){
        return mComposite+"_"+getName();
    }

    public void setCompositeName(String newName){
        mComposite = newName;
    }

    @Override
    public boolean equals(Object o){
        return (o instanceof AbstractSensor) && this.getName().equals(((AbstractSensor) o).getName());
    }

    @Override
    public int hashCode(){
       return this.getName().hashCode();
    }
}
