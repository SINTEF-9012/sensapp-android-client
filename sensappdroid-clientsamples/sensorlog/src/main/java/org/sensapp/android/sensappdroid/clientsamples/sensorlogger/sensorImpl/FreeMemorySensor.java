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
import android.hardware.Sensor;
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

    static final int DEFAULT_RATE = 10000;

    public FreeMemorySensor(String composite) {
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
        float level = (float)freeMemory()/totalMemory()*100;
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

    public int totalMemory()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        return (statFs.getBlockCount() * statFs.getBlockSize()) / 1048576;
    }

    public int freeMemory()
    {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        return (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
    }

    final public int getDefaultRate(){
        return DEFAULT_RATE;
    }
}
