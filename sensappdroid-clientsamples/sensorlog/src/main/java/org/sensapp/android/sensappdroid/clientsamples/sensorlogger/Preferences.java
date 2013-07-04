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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.method.DigitsKeyListener;
import org.sensapp.android.sensappdroid.api.SensAppHelper;

import java.util.List;

/**
 * There is the UI permitting to edit preferences regarding sensors.
 */
public class Preferences extends PreferenceActivity {

	public static class PreferencesFragment extends PreferenceFragment {
        @Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
            List<AbstractSensor> sensors = SensorLoggerService.getSensors();
            final EditTextPreference compositeName = (EditTextPreference) findPreference(getString(R.string.pref_compositename_key));
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            compositeName.setDefaultValue(sp.getString(getString(R.string.pref_compositename_key), SensorActivity.compositeName));
            compositeName.setSummary(sp.getString(getString(R.string.pref_compositename_key), SensorActivity.compositeName));
            compositeName.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String lastName = sp.getString(getString(R.string.pref_compositename_key), SensorActivity.compositeName);

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                    String name = (String)newValue;
                    editor.putString(getString(R.string.pref_compositename_key), name).commit();
                    compositeName.setSummary(name);

                    if(name!=lastName){
                        SensorActivity.compositeName = name;
                        if(SensorLoggerService.sensors != null){
                            for(AbstractSensor s : SensorLoggerService.sensors)
                                s.setCompositeName(name);
                        }
                    }

                    return true;
                }
            });

            //Set the sensors into 'preferences'
            for(AbstractSensor s: sensors){
                EditTextPreference sNew = new EditTextPreference(getActivity());
                sNew.getEditText().setKeyListener(DigitsKeyListener.getInstance());
                sNew.setTitle(s.getName());
                sNew.setDialogTitle("Enter the refresh rate you wish for this sensor.");
                sNew.setDefaultValue(((Integer) s.getMeasureTime()).toString());
                sNew.setSummary(((Integer) s.getMeasureTime()).toString());

                getPreferenceScreen().addItemFromInflater(sNew);

                //set the function called when preferences changed for this preference/sensor
                sNew.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (((String) newValue).isEmpty()) {
                            return false;
                        }
                        AbstractSensor toChange = SensorLoggerService.getSensorByName((String) preference.getTitle());
                        toChange.setRefreshRate(Integer.parseInt((String) newValue));

                        AlarmHelper.cancelAlarm(getActivity().getApplicationContext());
                        AlarmHelper.setAlarm(getActivity().getApplicationContext(), toChange.getMeasureTime());

                        SharedPreferences.Editor editor;
                        editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                        editor.putInt(toChange.getName(), toChange.getMeasureTime());
                        editor.commit();
                        preference.setSummary(((Integer) toChange.getMeasureTime()).toString());
                        return true;
                    }
                });
            }
		}

        public void onDestroy(){
            super.onDestroy();
            getPreferenceScreen().removeAll();
        }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
	}
}
