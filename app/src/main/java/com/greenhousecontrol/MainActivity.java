package com.greenhousecontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase RTDB;

    public MainActivity() {
        counter = new float[0];
    }

    private void initializeFirebase() {
        RTDB = FirebaseDatabase.getInstance();
    }

    // global
    ScrollView scrollView;
    TabLayout tabLayout;
    
    // hydration
    ConstraintLayout clHydration;
    ConstraintLayout clHydrationSensors, clHydrationControls;
    MaterialButton btnToggleHydrationSensors, btnToggleHydrationControls;
    // hydration sensors
    ArcGauge gaugeSoilMoisture;
    TextView tvMoisture;
    // hydration controls
    ChipGroup cgHydrationControlMode;
    Chip chipWaterPumpManual, chipWaterPumpAutomatic;
    MaterialButton btnPumpWater;
    ConstraintLayout clHydrationControlsManual, clHydrationControlsAutomatic;

    // climate
    ConstraintLayout clClimate;
    ConstraintLayout clClimateSensors, clClimateControls;
    MaterialButton btnToggleClimateSensors, btnToggleClimateControls;
    // climate sensors
    LineChart chartTemperature;
    LineChart chartHumidity;
    LineChart chartHeatIndex;
    // climate controls
    ConstraintLayout clClimateControlsManual, clClimateControlsAutomatic;
    ChipGroup cgClimateControlMode;
    Chip chipServoManual, chipServoAutomatic;
    MaterialButton btnToggleWindow, btnClimateSaveChanges;
    TextInputEditText etMinHeatIndex, etMaxHeatIndex;
    TextView tvWindowStatus;

    // charts
    ArrayList<Entry> arrTemperature;
    ArrayList<Entry> arrHumidity;
    ArrayList<Entry> arrHeatIndex;
    float[] counter;

    //
    final double[] min = new double[1];
    final double[] max = new double[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFirebase();
        initializeViews();
        handleNavigation();
        handleHydrationControlModeToggle();
        handleClimateControlModeToggle();
        stylizeCharts();

        renderSoilMoistureGauge();
        handleWaterPumpButton();
        listenToDhtSensorData();
        handleServoButton();

        btnClimateSaveChanges.setOnClickListener(view -> {
            DatabaseReference refServo = RTDB.getReference("servo");
            refServo.child("minHeatIndex").setValue(Double.parseDouble(etMinHeatIndex.getText().toString()));
            refServo.child("maxHeatIndex").setValue(Double.parseDouble(etMaxHeatIndex.getText().toString()));

            btnClimateSaveChanges.setEnabled(false);
            hideKeyboard();

            // update min max values
            handleClimateControlModeToggle();
        });
    }

    private void initializeViews() {
        scrollView = findViewById(R.id.scrollView);
        tabLayout = findViewById(R.id.tabLayout);

        // hydration
        clHydration = findViewById(R.id.clHydration);
        clHydrationSensors = findViewById(R.id.clHydrationSensors);
        clHydrationControls = findViewById(R.id.clHydrationControls);
        btnToggleHydrationSensors = findViewById(R.id.btnToggleHydrationSensors);
        btnToggleHydrationControls = findViewById(R.id.btnToggleHydrationControls);
        // hydration sensors
        gaugeSoilMoisture = findViewById(R.id.gaugeSoilMoisture);
        tvMoisture = findViewById(R.id.tvMoisture);
        // hydration controls
        btnPumpWater = findViewById(R.id.btnPumpWater);
        cgHydrationControlMode = findViewById(R.id.cgHydrationControlMode);
        chipWaterPumpAutomatic = findViewById(R.id.chipWaterPumpAutomatic);
        chipWaterPumpManual = findViewById(R.id.chipWaterPumpManual);
        clHydrationControlsManual = findViewById(R.id.clHydrationControlsManual);
        clHydrationControlsAutomatic = findViewById(R.id.clHydrationControlsAutomatic);

        // climate
        clClimate = findViewById(R.id.clClimate);
        clClimateSensors = findViewById(R.id.clClimateSensors);
        clClimateControls = findViewById(R.id.clClimateControls);
        btnToggleClimateSensors = findViewById(R.id.btnToggleClimateSensors);
        btnToggleClimateControls = findViewById(R.id.btnToggleClimateControls);
        // climate sensors
        chartTemperature = findViewById(R.id.chartTemperature);
        chartHumidity = findViewById(R.id.chartHumidity);
        chartHeatIndex = findViewById(R.id.chartHeatIndex);
        clClimateControlsManual = findViewById(R.id.clClimateControlsManual);
        clClimateControlsAutomatic = findViewById(R.id.clClimateControlsAutomatic);
        // climate controls
        btnToggleWindow = findViewById(R.id.btnToggleWindow);
        btnClimateSaveChanges = findViewById(R.id.btnClimateSaveChanges);
        etMinHeatIndex = findViewById(R.id.etMinHeatIndex);
        etMaxHeatIndex = findViewById(R.id.etMaxHeatIndex);
        tvWindowStatus = findViewById(R.id.tvWindowStatus);
        cgClimateControlMode = findViewById(R.id.cgClimateControlMode);
        chipServoManual = findViewById(R.id.chipServoManual);
        chipServoAutomatic = findViewById(R.id.chipServoAutomatic);
    }

    private void handleNavigation() {
        btnToggleHydrationSensors.setOnClickListener(view -> {
            if (clHydrationSensors.getVisibility() == View.VISIBLE) {
                clHydrationSensors.setVisibility(View.GONE);
                btnToggleHydrationSensors.setIcon(getResources().getDrawable(R.drawable.arrow_down_24));
            }
            else {
                clHydrationSensors.setVisibility(View.VISIBLE);
                btnToggleHydrationSensors.setIcon(getResources().getDrawable(R.drawable.arrow_up_24));
            }
        });

        btnToggleHydrationControls.setOnClickListener(view -> {
            if (clHydrationControls.getVisibility() == View.VISIBLE) {
                clHydrationControls.setVisibility(View.GONE);
                btnToggleHydrationControls.setIcon(getResources().getDrawable(R.drawable.arrow_down_24));
            }
            else {
                clHydrationControls.setVisibility(View.VISIBLE);
                btnToggleHydrationControls.setIcon(getResources().getDrawable(R.drawable.arrow_up_24));
            }
        });

        btnToggleClimateControls.setOnClickListener(view -> {
            if (clClimateControls.getVisibility() == View.VISIBLE) {
                clClimateControls.setVisibility(View.GONE);
                btnToggleClimateControls.setIcon(getResources().getDrawable(R.drawable.arrow_down_24));
            }
            else {
                clClimateControls.setVisibility(View.VISIBLE);
                btnToggleClimateControls.setIcon(getResources().getDrawable(R.drawable.arrow_up_24));
            }
        });

        btnToggleClimateSensors.setOnClickListener(view -> {
            if (clClimateSensors.getVisibility() == View.VISIBLE) {
                clClimateSensors.setVisibility(View.GONE);
                btnToggleClimateSensors.setIcon(getResources().getDrawable(R.drawable.arrow_down_24));
            }
            else {
                clClimateSensors.setVisibility(View.VISIBLE);
                btnToggleClimateSensors.setIcon(getResources().getDrawable(R.drawable.arrow_up_24));
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                hideKeyboard();
                if (tabLayout.getSelectedTabPosition() == 0) {
                    clHydration.setVisibility(View.VISIBLE);
                    clClimate.setVisibility(View.GONE);
                }
                else {
                    clHydration.setVisibility(View.GONE);
                    clClimate.setVisibility(View.VISIBLE);

                    etMinHeatIndex.setText(String.valueOf(min[0]));
                    etMaxHeatIndex.setText(String.valueOf(max[0]));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void renderSoilMoistureGauge() {
        Range rangeWet = new Range();
        rangeWet.setColor(getResources().getColor(R.color.wet));
        rangeWet.setFrom(0.0);
        rangeWet.setTo(370.0);

        Range rangeMoist = new Range();
        rangeMoist.setColor(getResources().getColor(R.color.moist));
        rangeMoist.setFrom(371.0);
        rangeMoist.setTo(600.0);

        Range rangeDry = new Range();
        rangeDry.setColor(getResources().getColor(R.color.dry));
        rangeDry.setFrom(601.0);
        rangeDry.setTo(800.0);

        Range rangeVeryDry = new Range();
        rangeVeryDry.setColor(getResources().getColor(R.color.veryDry));
        rangeVeryDry.setFrom(801.0);
        rangeVeryDry.setTo(1500.0);

        gaugeSoilMoisture.addRange(rangeWet);
        gaugeSoilMoisture.addRange(rangeMoist);
        gaugeSoilMoisture.addRange(rangeDry);
        gaugeSoilMoisture.addRange(rangeVeryDry);

        gaugeSoilMoisture.setMinValue(0.0);
        gaugeSoilMoisture.setMaxValue(1000.0);
        gaugeSoilMoisture.setValue(683.0);

        listenToSoilMoistureData();
    }

    private void listenToSoilMoistureData() {
        DatabaseReference refSoilMoisture = RTDB.getReference("soilMoisture");
        refSoilMoisture.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float soilMoisture = Float.parseFloat(Objects.requireNonNull(snapshot.getValue()).toString());
                gaugeSoilMoisture.setValue(Math.round(soilMoisture * 100.0) / 100.0);

                if (soilMoisture <= 370.0) {
                    tvMoisture.setText("WET");
                    tvMoisture.setTextColor(getResources().getColor(R.color.wet));
                }
                else if (soilMoisture <= 600.0) {
                    tvMoisture.setText("MOIST");
                    tvMoisture.setTextColor(getResources().getColor(R.color.moist));
                }
                else if (soilMoisture <= 800.0) {
                    tvMoisture.setText("DRY");
                    tvMoisture.setTextColor(getResources().getColor(R.color.dry));
                }
                else if (soilMoisture > 800.0) {
                    tvMoisture.setText("VERY DRY");
                    tvMoisture.setTextColor(getResources().getColor(R.color.veryDry));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void handleHydrationControlModeToggle() {
        // sync to last selected mode
        DatabaseReference refWaterPump = RTDB.getReference("waterPump");
        refWaterPump.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int mode = Integer.parseInt(Objects.requireNonNull(snapshot.child("mode").getValue()).toString());

                if (mode == 0) {
                    chipWaterPumpManual.setChecked(true);
                    clHydrationControlsManual.setVisibility(View.VISIBLE);
                    clHydrationControlsAutomatic.setVisibility(View.GONE);
                }
                else {
                    chipWaterPumpAutomatic.setChecked(true);
                    clHydrationControlsManual.setVisibility(View.GONE);
                    clHydrationControlsAutomatic.setVisibility(View.VISIBLE);
                    scrollToBottom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cgHydrationControlMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.get(0) == R.id.chipWaterPumpManual) {
                clHydrationControlsManual.setVisibility(View.VISIBLE);
                clHydrationControlsAutomatic.setVisibility(View.GONE);

                refWaterPump.child("mode").setValue(0);
            }
            else if (checkedIds.get(0) == R.id.chipWaterPumpAutomatic) {
                clHydrationControlsManual.setVisibility(View.GONE);
                clHydrationControlsAutomatic.setVisibility(View.VISIBLE);
                scrollToBottom();

                refWaterPump.child("mode").setValue(1);
            }
        });
    }

    private void handleWaterPumpButton() {
        btnPumpWater.setOnClickListener(view -> {
            btnPumpWater.setEnabled(false);
            btnPumpWater.setText("Pumping Water...");

            DatabaseReference refWaterPumpEvent = RTDB.getReference("waterPump/state");
            refWaterPumpEvent.setValue(1);

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                btnPumpWater.setEnabled(true);
                btnPumpWater.setText("Pump Water");
                refWaterPumpEvent.setValue(0);
            }, 2000);
        });
    }

    private void listenToDhtSensorData() {
        arrTemperature = new ArrayList<>();
        arrHumidity = new ArrayList<>();
        arrHeatIndex = new ArrayList<>();
        counter = new float[]{1};

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                renderTemperatureChart();
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private void renderTemperatureChart() {
        DatabaseReference refDht = RTDB.getReference("dht");
        refDht.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get data from firebase
                float heatIndex = Float.parseFloat(Objects.requireNonNull(snapshot.child("heatIndex").getValue()).toString());
                float humidity = Float.parseFloat(Objects.requireNonNull(snapshot.child("humidity").getValue()).toString());
                float temperature = Float.parseFloat(Objects.requireNonNull(snapshot.child("temperature").getValue()).toString());

                // render charts
                counter[0]++;
                arrTemperature.add(new Entry(counter[0], temperature));
                arrHumidity.add(new Entry(counter[0], humidity));
                arrHeatIndex.add(new Entry(counter[0], heatIndex));

                if (arrTemperature.size() > 10) {
                    arrTemperature.remove(0);
                }
                if (arrHumidity.size() > 10) {
                    arrHumidity.remove(0);
                }
                if (arrHeatIndex.size() > 10) {
                    arrHeatIndex.remove(0);
                }

                LineDataSet dataSetTemperature = new LineDataSet(arrTemperature, "Temperature");
                dataSetTemperature.setValueFormatter(new CelsiusValueFormatter());
                dataSetTemperature.setDrawFilled(true);
                if (Utils.getSDKInt() >= 18) {
                    Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.gradient_temperature);
                    dataSetTemperature.setFillDrawable(drawable);
                }
                else {
                    dataSetTemperature.setFillColor(Color.BLACK);
                }
                dataSetTemperature.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSetTemperature.setColors(getResources().getColor(R.color.temperature));
                dataSetTemperature.setCircleColor(getResources().getColor(R.color.temperature));

                LineDataSet dataSetHumidity = new LineDataSet(arrHumidity, "Humidity");
                dataSetHumidity.setValueFormatter(new PercentageValueFormatter());
                dataSetHumidity.setDrawFilled(true);
                if (Utils.getSDKInt() >= 18) {
                    Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.gradient_humidity);
                    dataSetHumidity.setFillDrawable(drawable);
                }
                else {
                    dataSetHumidity.setFillColor(Color.BLACK);
                }
                dataSetHumidity.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSetHumidity.setColors(getResources().getColor(R.color.humidity));
                dataSetHumidity.setCircleColor(getResources().getColor(R.color.humidity));

                LineDataSet dataSetHeatIndex = new LineDataSet(arrHeatIndex, "Heat Index");
                dataSetHeatIndex.setValueFormatter(new CelsiusValueFormatter());
                dataSetHeatIndex.setDrawFilled(true);
                if (Utils.getSDKInt() >= 18) {
                    Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.gradient_heat_index);
                    dataSetHeatIndex.setFillDrawable(drawable);
                }
                else {
                    dataSetHeatIndex.setFillColor(Color.BLACK);
                }
                dataSetHeatIndex.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSetHeatIndex.setColors(getResources().getColor(R.color.heatIndex));
                dataSetHeatIndex.setCircleColor(getResources().getColor(R.color.heatIndex));


                chartTemperature.setData(new LineData(dataSetTemperature));
                chartTemperature.invalidate();
                chartHumidity.setData(new LineData(dataSetHumidity));
                chartHumidity.invalidate();
                chartHeatIndex.setData(new LineData(dataSetHeatIndex));
                chartHeatIndex.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void stylizeCharts() {
        // temperature chart
        chartTemperature.setDescription(null);
        chartTemperature.getAxisLeft().setDrawLabels(false);
        chartTemperature.getAxisRight().setDrawLabels(false);
        chartTemperature.getXAxis().setDrawLabels(false);
        chartTemperature.getXAxis().setDrawGridLines(false);
        chartTemperature.getAxisLeft().setDrawGridLines(false);
        chartTemperature.getAxisRight().setDrawGridLines(false);
        chartTemperature.getAxisLeft().setDrawAxisLine(false);
        chartTemperature.getXAxis().setDrawAxisLine(false);
        chartTemperature.getAxisRight().setDrawAxisLine(false);
        Description descTemperature = new Description();
        descTemperature.setText("Temperature");

        // humidity chart
        chartHumidity.setDescription(null);
        chartHumidity.getAxisLeft().setDrawLabels(false);
        chartHumidity.getAxisRight().setDrawLabels(false);
        chartHumidity.getXAxis().setDrawLabels(false);
        chartHumidity.getXAxis().setDrawGridLines(false);
        chartHumidity.getAxisLeft().setDrawGridLines(false);
        chartHumidity.getAxisRight().setDrawGridLines(false);
        chartHumidity.getAxisLeft().setDrawAxisLine(false);
        chartHumidity.getXAxis().setDrawAxisLine(false);
        chartHumidity.getAxisRight().setDrawAxisLine(false);
        Description descHumidity = new Description();
        descHumidity.setText("Humidity");

        // heat index chart
        chartHeatIndex.setDescription(null);
        chartHeatIndex.getAxisLeft().setDrawLabels(false);
        chartHeatIndex.getAxisRight().setDrawLabels(false);
        chartHeatIndex.getXAxis().setDrawLabels(false);
        chartHeatIndex.getXAxis().setDrawGridLines(false);
        chartHeatIndex.getAxisLeft().setDrawGridLines(false);
        chartHeatIndex.getAxisRight().setDrawGridLines(false);
        chartHeatIndex.getAxisLeft().setDrawAxisLine(false);
        chartHeatIndex.getXAxis().setDrawAxisLine(false);
        chartHeatIndex.getAxisRight().setDrawAxisLine(false);
        Description descHeatIndex = new Description();
        descHeatIndex.setText("Heat Index");
    }

    private void handleServoButton() {
        DatabaseReference refServoState = RTDB.getReference("servo/state");
        refServoState.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int servoState = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());

                // update UI
                if (servoState == 0) {
                    tvWindowStatus.setText("CLOSED");
                    tvWindowStatus.setTextColor(getResources().getColor(R.color.veryDry));
                    btnToggleWindow.setText("Open Window");
                }
                else {
                    tvWindowStatus.setText("OPEN");
                    tvWindowStatus.setTextColor(getResources().getColor(R.color.moist));
                    btnToggleWindow.setText("Close Window");
                }

                // listen for clicks
                btnToggleWindow.setOnClickListener(view -> {
                    if (servoState == 0) {
                        refServoState.setValue(1);
                    }
                    else {
                        refServoState.setValue(0);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void handleClimateControlModeToggle() {
        // sync to last selected mode
        DatabaseReference refServo = RTDB.getReference("servo");
        refServo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int mode = Integer.parseInt(Objects.requireNonNull(snapshot.child("mode").getValue()).toString());
                float minHeatIndex = Float.parseFloat(Objects.requireNonNull(snapshot.child("minHeatIndex").getValue()).toString());
                float maxHeatIndex = Float.parseFloat(Objects.requireNonNull(snapshot.child("maxHeatIndex").getValue()).toString());

                if (mode == 0) {
                    chipServoManual.setChecked(true);
                    clClimateControlsManual.setVisibility(View.VISIBLE);
                    clClimateControlsAutomatic.setVisibility(View.GONE);
                }
                else {
                    chipServoAutomatic.setChecked(true);
                    clClimateControlsManual.setVisibility(View.GONE);
                    clClimateControlsAutomatic.setVisibility(View.VISIBLE);

                    etMinHeatIndex.setText(String.valueOf(minHeatIndex));
                    etMinHeatIndex.setEnabled(true);
                    min[0] = minHeatIndex;
                    etMaxHeatIndex.setText(String.valueOf(maxHeatIndex));
                    etMaxHeatIndex.setEnabled(true);
                    max[0] = maxHeatIndex;
                    btnClimateSaveChanges.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // listen to mode changes
        cgClimateControlMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            hideKeyboard();

            if (checkedIds.get(0) == R.id.chipServoManual) {
                clClimateControlsManual.setVisibility(View.VISIBLE);
                clClimateControlsAutomatic.setVisibility(View.GONE);

                refServo.child("mode").setValue(0);
            }
            else if (checkedIds.get(0) == R.id.chipServoAutomatic) {
                // reset et values
                etMinHeatIndex.setText(String.valueOf(min[0]));
                etMaxHeatIndex.setText(String.valueOf(max[0]));

                clClimateControlsManual.setVisibility(View.GONE);
                clClimateControlsAutomatic.setVisibility(View.VISIBLE);

                refServo.child("mode").setValue(1);


                etMinHeatIndex.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        validateHeatIndexValues(min[0], max[0]);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });

                etMaxHeatIndex.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        validateHeatIndexValues(min[0], max[0]);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });
            }
        });
    }

    private void validateHeatIndexValues(double minLastValue, double maxLastValue) {
        String minStr = etMinHeatIndex.getText().toString();
        String maxStr = etMaxHeatIndex.getText().toString();

        if (minStr.isEmpty() || maxStr.isEmpty()) {
            btnClimateSaveChanges.setEnabled(false);
            return;
        }

        double min = Double.parseDouble(minStr);
        double max = Double.parseDouble(etMaxHeatIndex.getText().toString());

        if (min != minLastValue || max != maxLastValue) {
            btnClimateSaveChanges.setEnabled(true);
            scrollToBottom();
        }
        else {
            btnClimateSaveChanges.setEnabled(false);
        }
    }

    private void scrollToBottom()  {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
            int bottom = lastChild.getBottom() + scrollView.getPaddingBottom();
            int sy = scrollView.getScrollY();
            int sh = scrollView.getHeight();
            int delta = bottom - (sy + sh);

            scrollView.smoothScrollBy(0, delta);
        }, 100);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private static class CelsiusValueFormatter extends ValueFormatter {
        @Override
        public String getPointLabel(Entry entry) {
            String formatted = String.format("%.2f", entry.getY());
            return super.getPointLabel(new Entry(entry.getX(), Float.parseFloat(formatted))) + "\u00B0C";
        }
    }

    private static class PercentageValueFormatter extends ValueFormatter {
        @Override
        public String getPointLabel(Entry entry) {
            return super.getPointLabel(entry) + "%";
        }
    }
}