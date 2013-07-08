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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public final class AlarmHelper {

	protected static final int REFRESH_RATE = 500; // Polling interval in milliseconds to start the service.
	
	private static final int ACTIVE_NOTIFICATION_ID = 79290;
    private static final String TAG = AlarmHelper.class.getSimpleName();
	private static final Timer timer = new Timer();
    private static List<SensorLoggerTask> taskList= new ArrayList<SensorLoggerTask>();
	private AlarmHelper() {}
	
	protected static void setAlarm(Context context, int refresh_rate, AbstractSensor as) {
		/*Intent startService = new Intent(context, SensorManagerService.class);
        //startService.putExtra("sensorIndex", sensorIndex);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, startService, PendingIntent.FLAG_ONE_SHOT);
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        context.startService(startService);*/

        SensorLoggerTask newTask = new SensorLoggerTask(as, context);
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
	
	protected static void cancelAlarm(Context context, AbstractSensor as) {
        SensorLoggerTask cancelledTask = getTaskByAbstractSensor(as);
        if(cancelledTask != null)
            cancelledTask.cancel();
        taskList.remove(cancelledTask);

        if(taskList.isEmpty())
		    ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(ACTIVE_NOTIFICATION_ID);
	}

    private static SensorLoggerTask getTaskByAbstractSensor(AbstractSensor as){
        for(SensorLoggerTask t : taskList)
            if(t.getSensor() == as)
                return t;
        return null;
    }
}
