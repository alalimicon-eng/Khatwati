package com.khatawati.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private SharedPreferences prefs;
    private TextView stepsText, progressText, rewardText, statusText;
    private int todaySteps = 0;
    private float lastSensorValue = -1;
    private static final int GOAL = 6000;
    private final NumberFormat fmt = NumberFormat.getInstance(new Locale("ar"));

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("khatawati", MODE_PRIVATE);
        todaySteps = prefs.getInt("steps", 0);
        buildScreen();
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null) {
            statusText.setText("مستشعر عدّاد الخطوات غير متوفر في هذا الهاتف");
        } else {
            statusText.setText("جاهز لاحتساب خطواتك");
        }
        updateUI();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 38, 28, 24);
        root.setBackgroundColor(Color.rgb(10, 20, 25));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("خطواتي");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        root.addView(title, match(70));

        TextView subtitle = new TextView(this);
        subtitle.setText("كل خطوة تقرّبك من هدفك");
        subtitle.setTextColor(Color.rgb(165, 190, 185));
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, match(45));

        stepsText = new TextView(this);
        stepsText.setTextColor(Color.rgb(100, 230, 190));
        stepsText.setTextSize(54);
        stepsText.setTypeface(null, 1);
        stepsText.setGravity(Gravity.CENTER);
        root.addView(stepsText, match(120));

        TextView label = new TextView(this);
        label.setText("خطوة اليوم");
        label.setTextColor(Color.WHITE);
        label.setTextSize(18);
        label.setGravity(Gravity.CENTER);
        root.addView(label, match(40));

        progressText = new TextView(this);
        progressText.setTextColor(Color.WHITE);
        progressText.setTextSize(18);
        progressText.setGravity(Gravity.CENTER);
        root.addView(progressText, match(55));

        View line = new View(this);
        line.setBackgroundColor(Color.rgb(45, 65, 68));
        LinearLayout.LayoutParams lp = match(2); lp.setMargins(0, 22, 0, 22);
        root.addView(line, lp);

        TextView challenge = new TextView(this);
        challenge.setText("🏆 تحدّي اليوم");
        challenge.setTextColor(Color.rgb(255, 210, 110));
        challenge.setTextSize(23);
        challenge.setTypeface(null, 1);
        challenge.setGravity(Gravity.CENTER);
        root.addView(challenge, match(50));

        rewardText = new TextView(this);
        rewardText.setTextColor(Color.WHITE);
        rewardText.setTextSize(17);
        rewardText.setGravity(Gravity.CENTER);
        root.addView(rewardText, match(75));

        statusText = new TextView(this);
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        root.addView(statusText, match(65));

        TextView note = new TextView(this);
        note.setText("ضع الهاتف معك أثناء المشي. قد يختلف العد حسب مستشعر الهاتف.");
        note.setTextColor(Color.rgb(135, 155, 155));
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        root.addView(note, match(65));

        setContentView(root);
    }

    private LinearLayout.LayoutParams match(int h) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h);
    }

    private void updateUI() {
        stepsText.setText(fmt.format(todaySteps));
        int percent = Math.min(100, todaySteps * 100 / GOAL);
        progressText.setText("الهدف: " + fmt.format(GOAL) + " خطوة  •  " + percent + "%");
        rewardText.setText(todaySteps >= GOAL
            ? "مبروك! أكملت التحدّي وربحت 50 نقطة ⭐"
            : "امشِ حتى " + fmt.format(GOAL) + " خطوة لتحصل على 50 نقطة ⭐");
    }

    @Override protected void onResume() {
        super.onResume();
        if (sensorManager != null && stepSensor != null)
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
    }
    @Override protected void onPause() {
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
        prefs.edit().putInt("steps", todaySteps).apply();
    }
    @Override public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (lastSensorValue < 0) lastSensorValue = e.values[0];
            int detected = Math.max(0, (int)(e.values[0] - lastSensorValue));
            if (detected > 0) {
                todaySteps += detected;
                lastSensorValue = e.values[0];
                prefs.edit().putInt("steps", todaySteps).apply();
                updateUI();
            }
        }
    }
    @Override public void onAccuracyChanged(Sensor s, int accuracy) {}
}
