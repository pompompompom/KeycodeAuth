package com.mitayun.keycodeauth;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {

    private static final String TAG = "KEYCODE_AUTH";

    private static final String POSTFIX_ACC = "_acc";
    private static final String POSTFIX_GYRO = "_gyro";
    private static final String POSTFIX_POS = "_pos";

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyro;

    private boolean started = false;

    private List<String> accValues = new ArrayList<>();
    private List<String> gyroValues = new ArrayList<>();
    private List<String> positionValues = new ArrayList<>();

    private EditText fileNameEditText;
    private View targetView;
    private View boundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileNameEditText = (EditText) findViewById(R.id.filename);
        targetView = findViewById(R.id.targetView);
        boundView = findViewById(R.id.boundView);
        boundView.setOnTouchListener(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void startRecording(View view) {
        if (fileNameEditText.getText() != null && fileNameEditText.getText().length() > 0) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);

            started = true;
        }
    }

    public void stopRecording(View view) {
        // unregister
        sensorManager.unregisterListener(this);
        started = false;

        // write arraylist to file
        String fileName = fileNameEditText.getText().toString();
        writeToFile(fileName + POSTFIX_ACC, accValues);
        writeToFile(fileName + POSTFIX_GYRO, gyroValues);
        writeToFile(fileName + POSTFIX_POS, positionValues);

        // clear arraylist
        accValues.clear();
        gyroValues.clear();
        positionValues.clear();

        // clear filename edittext
        fileNameEditText.setText("");
    }

    private void writeToFile(String fileName, List<String> listToWrite) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            for (String s : listToWrite) {
                fileOutputStream.write(s.getBytes());
            }
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected Point getRelativePosition(View v, MotionEvent event) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        float screenX = event.getRawX();
        float screenY = event.getRawY();
        float viewX = screenX - location[0];
        float viewY = screenY - location[1];
        return new Point((int) viewX, (int) viewY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        List<String> targetList = null;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                targetList = accValues;
                break;
            case Sensor.TYPE_GYROSCOPE:
                targetList = gyroValues;
                break;
        }
        if (targetList != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            String result = x + "," + y + "," + z + "\n";
            Log.d(TAG, "Sensor " + event.sensor.getType() + ": " + result);
            targetList.add(result);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && started) {
            Point p = getRelativePosition(targetView, event);
            String result = p.x + "," + p.y + "\n";
            Log.d(TAG, "onTouch: " + result);
            positionValues.add(result);
        }
        return false;
    }
}
