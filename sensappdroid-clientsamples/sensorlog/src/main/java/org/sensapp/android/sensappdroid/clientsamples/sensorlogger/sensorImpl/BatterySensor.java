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
package org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.BatteryManager;
import org.sensapp.android.sensappdroid.api.SensAppUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 13/06/13
 * Time: 13:59
 */
public class BatterySensor extends AbstractSensor {

    static final int DEFAULT_RATE = 5000;

    public BatterySensor(String composite) {
        mSensor = null;
        setEntryLevel();
        initData();
        mComposite = composite;
    }

    final void setEntryLevel(){
        entryLevel = 1;
    }

    final void initData(){
        data = new float[1];
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
        return "TYPE_BATTERY";
    }

    public String getName(){
        return "Battery";
    }

    public SensAppUnit getUnit(){
        return SensAppUnit.PERCENT;
    }

    final public int getDefaultRate(){
        return DEFAULT_RATE;
    }
}
