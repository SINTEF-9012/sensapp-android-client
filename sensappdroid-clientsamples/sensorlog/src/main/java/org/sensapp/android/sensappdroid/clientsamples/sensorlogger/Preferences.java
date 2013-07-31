package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.method.DigitsKeyListener;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorlog.AbstractSensorLoggerTask;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AbstractSensor;
import org.sensapp.android.sensappdroid.clientsamples.sensorlogger.sensorimpl.AndroidSensor;

import java.util.List;

/**
 * There is the UI permitting to edit preferences regarding sensors.
 */
public class Preferences extends PreferenceActivity {

    static public class PreferencesFragment extends PreferenceFragment {
        @Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
            List<AbstractSensor> sensors = AbstractSensorLoggerTask.getSensors();

            final String compositeNameKey = getString(R.string.pref_compositename_key);
            final String descriptionKey = getString(R.string.pref_description_key);
            final String batteryKey = getString(R.string.pref_battery_key);
            final EditTextPreference compositeName = (EditTextPreference) findPreference(compositeNameKey);
            final EditTextPreference description = (EditTextPreference) findPreference(descriptionKey);
            final EditTextPreference battery = (EditTextPreference) findPreference(batteryKey);
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            final SharedPreferences.Editor prefEditor = sp.edit();

            //Add Composite Name EditText to the list of preferences
            compositeName.setDefaultValue(sp.getString(compositeNameKey, SensorActivity.compositeName));
            compositeName.setSummary(sp.getString(compositeNameKey, SensorActivity.compositeName));
            compositeName.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String lastName = sp.getString(compositeNameKey, SensorActivity.compositeName);

                    String name = (String)newValue;
                    prefEditor.putString(compositeNameKey, name).commit();
                    compositeName.setSummary(name);

                    if(!name.equals(lastName)){
                        SensorActivity.compositeName = name;
                        if(AbstractSensorLoggerTask.sensors != null){
                            for(AbstractSensor s : AbstractSensorLoggerTask.sensors)
                                s.setCompositeName(name);
                        }
                    }

                    return true;
                }
            });

            //Add Description EditText to the list of preferences
            description.setSummary(sp.getString(descriptionKey, "No description"));
            description.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String desc = (String)newValue;
                    prefEditor.putString(descriptionKey, desc).commit();
                    description.setSummary(desc);
                    return true;
                }
            });

            //Add Battery EditText to the list of preferences
            battery.setSummary(sp.getString(batteryKey, "0"));
            battery.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int bat = Integer.parseInt((String)newValue);
                    prefEditor.putString(batteryKey, String.valueOf(bat)).commit();
                    battery.setSummary(String.valueOf(bat));

                    for(AbstractSensor as: AbstractSensorLoggerTask.sensors)
                        SensorActivity.refreshConsumption(as, getActivity().getApplicationContext());

                    return true;
                }
            });

            //Add the sensors refresh rate into 'preferences'
            for(AbstractSensor s: sensors){
                final EditTextPreference sNew = new EditTextPreference(getActivity());
                sNew.getEditText().setKeyListener(DigitsKeyListener.getInstance());
                sNew.setTitle(s.getName());
                sNew.setDialogTitle("Enter the refresh rate you wish for this sensor.");
                sNew.setDefaultValue(((Integer) s.getMeasureTime()).toString());
                sNew.setSummary(((Integer) s.getMeasureTime()).toString());

                getPreferenceScreen().addItemFromInflater(sNew);

                //set the function called when preferences changed for this preference/sensor
                sNew.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (((String) newValue).isEmpty())
                            return false;
                        AbstractSensor toChange = AbstractSensorLoggerTask.getSensorByName((String) preference.getTitle());
                        toChange.setRefreshRate(Integer.parseInt((String) newValue));
                        prefEditor.putInt(toChange.getName(), Integer.parseInt((String) newValue)).commit();
                        sNew.setSummary(((Integer) Integer.parseInt((String) newValue)).toString());

                        if(toChange.isListened()){
                            SensorManagerService.cancelLog(getActivity().getApplicationContext(), toChange);
                            SensorManagerService.setLog(getActivity().getApplicationContext(), toChange);
                        }
                        if(toChange instanceof AndroidSensor)
                            SensorActivity.refreshConsumption(toChange, getActivity().getApplicationContext());

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
