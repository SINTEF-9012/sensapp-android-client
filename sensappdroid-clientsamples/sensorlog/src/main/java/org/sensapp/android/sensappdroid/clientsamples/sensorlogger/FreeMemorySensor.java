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
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import org.sensapp.android.sensappdroid.api.SensAppUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 13/06/13
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
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
        return "TYPE_MEMORY";
    }

    public String getName(){
        return mComposite+"_"+"Memory";
    }

    public SensAppUnit getUnit(){
        return SensAppUnit.PERCENT;
    }

    public int TotalMemory()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        int Total = (statFs.getBlockCount() * statFs.getBlockSize()) / 1048576;
        return Total;
    }

    public int FreeMemory()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        int Free  = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
        return Free;
    }

    public int BusyMemory()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        int Total = (statFs.getBlockCount() * statFs.getBlockSize()) / 1048576;
        int Free  = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
        int Busy  = Total - Free;
        return Busy;
    }

    final public int getDefaultRate(){
        return DEFAULT_RATE;
    }
}
