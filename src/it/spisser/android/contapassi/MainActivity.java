package it.spisser.android.contapassi;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	private SensorManager sensorManager;
	private Sensor sensor;
	private int todayStepsCount = 0;
	private int numberOfSteps = 0;

	SharedPreferences settings;

	private int todaySteps = -1;

	private int todayOffset = -1;
	private long previousStepTimeStamp = 0;

	private TextView todayStepsView;
	private TextView numberOfTotalStepsView;

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

		if (sensor != null) {
			sensorManager.registerListener(mySensorEventListener, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.i("MainActivity", "Registerered for stepcounter Sensor");

		} else {
			Log.e("MainActivity", "Registerered for Stepcounter Sensor");
			Toast.makeText(this, "stepcounter Sensor not found",
					Toast.LENGTH_LONG).show();
			finish();
		}

		settings = getSharedPreferences("myapp", MODE_PRIVATE);
		todayOffset = settings.getInt("todayOffset", -1);
		todaySteps = settings.getInt("TodaySteps", -1);
		previousStepTimeStamp = settings.getLong("previousStepTimeStamp", -1);

		Calendar current = Calendar.getInstance();
		long currentTimeMillis = current.getTimeInMillis();

		todayStepsView = (TextView) findViewById(R.id.numberOfSteps);
		numberOfTotalStepsView = (TextView) findViewById(R.id.numberTotalSteps);
		todayStepsView.setText(" " + numberOfSteps);

	}

	@Override
	protected void onStop() {
		super.onStop();

		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences("myapp", 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putInt("todayOffset", todayOffset);
		editor.putInt("todaySteps", todaySteps);
		editor.putInt("todayStepsCount", todayStepsCount);
		editor.putLong("previousStepTimeStamp", previousStepTimeStamp);

		// Commit the edits!
		editor.commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (sensor != null) {
			sensorManager.unregisterListener(mySensorEventListener);
		}
	}

	private SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			SimpleDateFormat format1 = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			Calendar actualTimeStamp = Calendar.getInstance();
			Log.d(TAG, "trigger evento");
			if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

				Calendar currentTimeStamp = Calendar.getInstance();
				currentTimeStamp.setTimeInMillis(event.timestamp / 1000000);

				int currentStepCount = (int) event.values[0];

				if (currentStepCount == 0) {
					/*
					 * abbiamo appena avuto un reboot oppure si parte da 0
					 */
					todayOffset = 0;
					previousStepTimeStamp = currentTimeStamp.getTimeInMillis();
				}

				Log.d(TAG, "new stepcount: " + currentStepCount);
				/* controlliamo se è cambiato giorno */
				Calendar current = Calendar.getInstance();
				current.setTime(actualTimeStamp.getTime());
				current.set(Calendar.HOUR_OF_DAY, 0);
				current.set(Calendar.MINUTE, 0);
				current.set(Calendar.SECOND, 0);
				current.set(Calendar.MILLISECOND, 0);

				Calendar previous = Calendar.getInstance();
				previous.setTimeInMillis(previousStepTimeStamp);
				previous.set(Calendar.HOUR_OF_DAY, 0);
				previous.set(Calendar.MINUTE, 0);
				previous.set(Calendar.SECOND, 0);
				previous.set(Calendar.MILLISECOND, 0);

				todayStepsCount++;
				if (current.after(previous)) {
					Log.d(TAG, "new day: " + format1.format(current.getTime())
							+ "\n" + format1.format(previous.getTime()));

					todayOffset = (int) event.values[0];
					todayStepsCount = 0;
					todaySteps = 0;

				}
				todaySteps = currentStepCount - todayOffset;

				previousStepTimeStamp = actualTimeStamp.getTimeInMillis();

				todayStepsView.setText(" " + todaySteps);
				numberOfTotalStepsView.setText(" " + currentStepCount);
				String formatted = format1.format(actualTimeStamp.getTime());
				// DateFormat dateFormat = new SimpleDateFormat(
				// "yyyy/MM/dd HH:mm:ss");
				// String a= dateFormat.format(current);
				Calendar prev = Calendar.getInstance();
				prev.setTimeInMillis(previousStepTimeStamp);
				/* fine controlliamo se è cambiato giorno */

			}

		}
	};

}
