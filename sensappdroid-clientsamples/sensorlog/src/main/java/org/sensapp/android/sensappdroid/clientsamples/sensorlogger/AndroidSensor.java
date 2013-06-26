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
 * Date: 05/06/13
 * Time: 14:25
 *
 * This is an interface used to manage sensors provided by the Android system.
 * A generalisation is possible by passing arguments through the constructor like type, unit...
 * At the moment there are only sensors with 1 or 3 data, so 2 data sensor or not implemented.
 */

public class AndroidSensor extends AbstractSensor{

    private static final String TAG = AndroidSensor.class.getSimpleName();
    final static int DEFAULT_RATE = 500;

    /*final static int DEFAULT_RATE = 500;            //Default rate of the sensor measures in milliseconds
    private float[] data;                           //Array to stock data
    private Sensor mSensor;                         //The sensor
    private String mComposite;                      //The group where the sensor is
    private float entryLevel = 1;                   //Entry level of the sensor measures
    private boolean measured = false;               //If the sensor has been measured once
    private boolean freshMeasure = false;           //If the sensor has new data to post
    private long lastMeasure=0;                     //The time of the last measure
    private int refreshRate;                        //The current rate of the sensor measures in milliseconds
    private boolean listened = false;               //If the sensor is listened or not   */

    AndroidSensor(Sensor s, String composite) {
        mSensor = s;
        setEntryLevel();
        initData();
        mComposite = composite;
    }

    public boolean isThreeDataSensor(){
        if(mSensor.getType() == Sensor.TYPE_ACCELEROMETER
                || mSensor.getType() == Sensor.TYPE_GYROSCOPE
                || mSensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                || mSensor.getType() == Sensor.TYPE_ORIENTATION
                || mSensor.getType() == Sensor.TYPE_GRAVITY
                || mSensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION
                || mSensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
            return true;
        return false;
    }

    public boolean isOneDataSensor(){
        if(mSensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE
                || mSensor.getType() == Sensor.TYPE_LIGHT
                || mSensor.getType() == Sensor.TYPE_PRESSURE
                || mSensor.getType() == Sensor.TYPE_PROXIMITY
                || mSensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY
                || mSensor.getType() == Sensor.TYPE_TEMPERATURE)
            return true;
        return false;
    }

    private void initData(){
        if(isOneDataSensor())
            data = new float[1];
        if(isThreeDataSensor())
            data = new float[3];
    }

    public void setData(Context context, float x){
        if(!isOneDataSensor())
            return;
        data[0]=x*entryLevel;
    }

    public void setData(Context context, float x, float y, float z){
        if(!isThreeDataSensor())
            return;
        data[0]=x*entryLevel;
        data[1]=y*entryLevel;
        data[2]=z*entryLevel;
    }
    /*
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
    }         */

    /**
     * Set up the entry level for each sensor.
     * Here are just some examples based on our experiment tablet.
     */
    private void setEntryLevel(){
        switch(mSensor.getType()){
            case Sensor.TYPE_MAGNETIC_FIELD:entryLevel = (float)0.000001;break;          //uTesla to Tesla
            case Sensor.TYPE_ORIENTATION:entryLevel = (float)(Math.PI/180.0);break;//Deg to Rad
            case Sensor.TYPE_PRESSURE:entryLevel = (float)100;      break;               //HPascal to Pascal
            case Sensor.TYPE_PROXIMITY:    entryLevel = (float)0.01;    break;           //cm to Meter
            default:    entryLevel = 1;break;                                            //no change
        }
    }

    public String getData(){
        StringBuffer out = new StringBuffer();
        out.append(getSensor().getName()+"\r\n");
        out.append(data[0]);
        if(isThreeDataSensor()){
            out.append(", " + data[1]);
            out.append(", " + data[2]);
        }
        out.append("\r\n\r\n");
        return out.toString();
    }

    public Sensor getSensor(){
        return mSensor;
    }

    final public int getDefaultRate(){
        return DEFAULT_RATE;
    }
       /*
    public int getMeasureTime(){
        return refreshRate;
    }

    public void setRefreshRate(int rate){
        refreshRate = rate;
    }     */

    public String getType() {
        String strType;
        switch (mSensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: strType = "TYPE_ACCELEROMETER";break;
            case Sensor.TYPE_GRAVITY:strType = "TYPE_GRAVITY";break;
            case Sensor.TYPE_GYROSCOPE:    strType = "TYPE_GYROSCOPE";    break;
            case Sensor.TYPE_LIGHT:strType = "TYPE_LIGHT";break;
            case Sensor.TYPE_LINEAR_ACCELERATION:strType = "TYPE_LINEAR_ACCELERATION";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:strType = "TYPE_MAGNETIC_FIELD";break;
            case Sensor.TYPE_ORIENTATION:strType = "TYPE_ORIENTATION";break;
            case Sensor.TYPE_PRESSURE:strType = "TYPE_PRESSURE";break;
            case Sensor.TYPE_PROXIMITY:    strType = "TYPE_PROXIMITY";    break;
            case Sensor.TYPE_ROTATION_VECTOR:    strType = "TYPE_ROTATION_VECTOR";break;
            case Sensor.TYPE_TEMPERATURE:strType = "TYPE_TEMPERATURE";break;
            default:    strType = "TYPE_UNKNOW";break;
        }
        return strType;
    }

    public String getName() {
        String strType;
        switch (mSensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: strType = "Accelerometer";break;
            case Sensor.TYPE_GRAVITY:strType = "Gravity";break;
            case Sensor.TYPE_GYROSCOPE:    strType = "Gyroscope";    break;
            case Sensor.TYPE_LIGHT:strType = "Light";break;
            case Sensor.TYPE_LINEAR_ACCELERATION:strType = "Linear_Acceleration";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:strType = "Magnetic_Field";break;
            case Sensor.TYPE_ORIENTATION:strType = "Orientation";break;
            case Sensor.TYPE_PRESSURE:strType = "Pressure";break;
            case Sensor.TYPE_PROXIMITY:    strType = "Proximity";    break;
            case Sensor.TYPE_ROTATION_VECTOR:    strType = "Rotation_Vector";break;
            case Sensor.TYPE_TEMPERATURE:strType = "Temperature";break;
            default:    strType = "Unknown";break;
        }
        return strType;
    }

    public SensAppUnit getUnit() {
        SensAppUnit strType;
        switch (mSensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: strType = SensAppUnit.ACCELERATION;break;
            case Sensor.TYPE_GRAVITY:strType = SensAppUnit.ACCELERATION;break;
            case Sensor.TYPE_GYROSCOPE:    strType = SensAppUnit.GYROSCOPE;    break;
            case Sensor.TYPE_LIGHT:strType = SensAppUnit.LUX;break;
            case Sensor.TYPE_LINEAR_ACCELERATION:strType = SensAppUnit.ACCELERATION;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:strType = SensAppUnit.TESLA;break;
            case Sensor.TYPE_ORIENTATION:strType = SensAppUnit.RADIAN;break;
            case Sensor.TYPE_PRESSURE:strType = SensAppUnit.PASCAL;break;
            case Sensor.TYPE_PROXIMITY:    strType = SensAppUnit.METER;    break;
            case Sensor.TYPE_ROTATION_VECTOR:    strType = SensAppUnit.MOL;break;
            case Sensor.TYPE_TEMPERATURE:strType = SensAppUnit.DEGREES_CELSIUS;break;
            default:    strType = SensAppUnit.MOL;break;
        }
        return strType;
    }
}
