package com.example.glumotion;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * 🚶 Counts the steps automatically with the phone's step sensor (TYPE_STEP_COUNTER).
 * The sensor gives "all steps since the phone was turned on", so we save the value at the
 * start of the day (SharedPreferences) and today's steps = now − start of day.
 *
 * ===================== كيف تفعّله =====================
 * بملف HealthActivity.java شيل الـ // من الأسطر اللي مكتوب فوقها "عدّاد الخطوات"
 * (الإذن ACTIVITY_RECOGNITION موجود بالـ Manifest، والكود هون بيطلبه من المستخدم لحاله)
 * ملاحظة: الإيميوليتر ما فيه حساس خطوات → جرّبه على موبايل حقيقي.
 * ======================================================
 */
public class StepCounterHelper implements SensorEventListener {

    // The screen gets the new number of steps through this interface
    public interface StepListener {
        void onSteps(int stepsToday);
    }

    private final AppCompatActivity activity;
    private final StepListener listener;
    private final SensorManager sensorManager;
    private final Sensor stepSensor;
    private final ActivityResultLauncher<String> permissionLauncher;

    // Must be created in onCreate (because of registerForActivityResult)
    public StepCounterHelper(AppCompatActivity activity, StepListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            this.stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        } else {
            this.stepSensor = null;
        }
        // Runtime permission (Android 10+): "Physical activity"
        this.permissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean granted) {
                        if (granted) {
                            register();
                        } else {
                            Toast.makeText(StepCounterHelper.this.activity, "Steps permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Starts counting (asks for the permission if needed)
    public void start() {
        if (stepSensor == null) {
            Toast.makeText(activity, "This phone has no step sensor", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 29 && ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
            return;
        }
        register();
    }

    // Stops counting (call in onDestroy to save battery)
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // Connects this class to the sensor
    private void register() {
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
    }

    // Called by Android every time the sensor has a new value
    @Override
    public void onSensorChanged(SensorEvent event) {
        float total = event.values[0];
        String today = DateUtil.todayKey();

        // New day OR the phone restarted (counter went back) → today starts from here
        if (!today.equals(AppPrefs.getStepsBaseDay(activity)) || total < AppPrefs.getStepsBase(activity)) {
            AppPrefs.setStepsBase(activity, today, total);
        }
        int steps = (int) (total - AppPrefs.getStepsBase(activity));
        listener.onSteps(steps);
    }

    // Not needed, but SensorEventListener requires it
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
