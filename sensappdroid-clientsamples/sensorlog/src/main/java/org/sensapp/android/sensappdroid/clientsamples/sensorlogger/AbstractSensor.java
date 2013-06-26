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

import android.content.Context;
import android.hardware.Sensor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.api.SensAppUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 13/06/13
 * Time: 13:40
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSensor {
    private static final String TAG = AndroidSensor.class.getSimpleName();

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
    public Uri registerInSensApp(Context context, int drawable){
        Uri u = null;
        refreshRate = PreferenceManager.getDefaultSharedPreferences(context).getInt(getName(), getDefaultRate());
        if(!SensAppHelper.isCompositeRegistered(context, mComposite))
            SensAppHelper.registerComposite(context, mComposite, "List some sensors of the tablet");
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
            if(isFreshMeasure()){
                Uri measureUri;
                if(isOneDataSensor()){
                    measureUri = SensAppHelper.insertMeasure(context, getName(), data[0]);
                    Log.i(TAG, "New measure (" + data[0] + ") available at " + measureUri);
                }
                else if(isThreeDataSensor()){
                    measureUri = SensAppHelper.insertMeasure(context, getName()+"X", data[0]);
                    Log.i(TAG, "New measure (" + data[0] + ") available at " + measureUri);
                    measureUri = SensAppHelper.insertMeasure(context, getName()+"Y", data[1]);
                    Log.i(TAG, "New measure (" + data[1] + ") available at " + measureUri);
                    measureUri = SensAppHelper.insertMeasure(context, getName()+"Z", data[2]);
                    Log.i(TAG, "New measure (" + data[2] + ") available at " + measureUri);
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void setData(Context context){
        return;
    }

    public void setData(Context context, float x){
        return;
    }

    public void setData(Context context, float x, float y, float z){
        return;
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
}
