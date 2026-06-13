package com.sample.mosquito.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.sample.mosquito.R;
import com.sample.mosquito.services.MainService;
import com.sample.mosquito.services.ServiceController;
import com.sample.mosquito.utils.Annotator;
import com.sample.mosquito.utils.AppPreferences;
import com.sample.mosquito.utils.Notify;
import com.sample.mosquito.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ConnectActivity extends Activity {

    private Button buttonConnect, buttonTopic;
    private EditText editTextBroker, editTextPort;
    private MainService mainService;
    private boolean anotherActivityStarted;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        checkBox = findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppPreferences.setBoolean("reconnect", isChecked);
                if (mainService != null) mainService.setReconnect(isChecked);
            }
        });
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonTopic = findViewById(R.id.buttonTopic);
        editTextBroker = findViewById(R.id.editTextBroker);
        editTextPort = findViewById(R.id.editTextPort);
        editTextBroker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mainService == null) return;
                String s = editTextBroker.getText().toString();
                buttonConnect.setEnabled(editTextPort.getText().length() > 0 && s.length() > 0 && s.contains("."));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editTextPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mainService == null) return;
                String s = editTextBroker.getText().toString();
                buttonConnect.setEnabled(editTextPort.getText().length() > 0 && s.length() > 0 && s.contains("."));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.buttonConnect) {
                    if (mainService.isConnected()) {
                        mainService.disconnect();
                        editTextBroker.setEnabled(true);
                        editTextPort.setEnabled(true);
                        buttonTopic.setEnabled(false);
                        buttonConnect.setText(R.string.connect);
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("broker", editTextBroker.getText().toString());
                            jsonObject.put("port", Integer.parseInt(editTextPort.getText().toString()));
                            new Annotator("Enter.json").setContent(jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (!Utils.hasInternetConnection()) {
                            Notify.showToast(R.string.error_no_internet_connection);
                            return;
                        }
                        editTextBroker.setEnabled(false);
                        editTextPort.setEnabled(false);
                        buttonConnect.setText(R.string.connecting);
                        buttonConnect.setEnabled(false);

                        // tcp://broker.mqtt-dashboard.com:1883
                        mainService.connect(editTextBroker.getText().toString() + ":" + editTextPort.getText().toString());
                    }
                } else if (view.getId() == R.id.buttonTopic) {
                    anotherActivityStarted = true;
                    startActivity(new Intent(ConnectActivity.this, TopicsActivity.class));
                    finish();
                }
            }
        };
        buttonConnect.setOnClickListener(onClick);
        buttonTopic.setOnClickListener(onClick);
        Annotator annotator = new Annotator("Enter.json");
        if (annotator.exists()) {
            try {
                JSONObject jsonObject = new JSONObject(annotator.getContent());
                editTextBroker.setText(jsonObject.getString("broker"));
                editTextPort.setText(String.valueOf(jsonObject.getInt("port")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ServiceController.getInstance(this, new ServiceController.OnServiceAvailableListener() {
            @Override
            public void onAvailable(MainService mainService) {
                ConnectActivity.this.mainService = mainService;
                mainService.setCallback(callback);
                mainService.stopForeground();

                mainService.setReconnect(checkBox.isChecked());
                if (mainService.isConnecting()) {
                    editTextBroker.setEnabled(false);
                    editTextPort.setEnabled(false);
                    buttonConnect.setText(Utils.hasInternetConnection() ? R.string.connecting : R.string.wait_for_internet_connection);
                    buttonConnect.setEnabled(false);
                } else {
                    String s = editTextBroker.getText().toString();
                    buttonConnect.setEnabled(editTextPort.getText().length() > 0 && s.length() > 0);
                    buttonConnect.setText(mainService.isConnected() ? R.string.disconnect : R.string.connect);
                    buttonTopic.setEnabled(mainService.isConnected());
                    editTextBroker.setEnabled(!mainService.isConnected());
                    editTextPort.setEnabled(!mainService.isConnected());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        anotherActivityStarted = false;
        checkBox.setChecked(AppPreferences.getBoolean("reconnect"));

        if (!isIgnoringBatteryOptimizations()) skipBatteryOptimizations();
    }

    public boolean isIgnoringBatteryOptimizations() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(getPackageName());
    }

    public void skipBatteryOptimizations() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mainService != null) {
            if (!anotherActivityStarted && mainService.isConnected()) {
                mainService.setCallback(null);
                mainService.startForeground();
            }
        }
    }

    private final MainService.Callback callback = new MainService.Callback() {

        @Override
        public void onBrokerConnectionChanged(boolean isConnected) {
            Utils.vibrate();
            buttonConnect.setEnabled(true);
            buttonConnect.setText(isConnected ? R.string.disconnect : R.string.connect);
            buttonTopic.setEnabled(isConnected);
            if (!isConnected) {
                Notify.showToast(R.string.mqtt_disconnected);
                editTextBroker.setEnabled(true);
                editTextPort.setEnabled(true);
            } else {
                anotherActivityStarted = true;
                startActivity(new Intent(ConnectActivity.this, TopicsActivity.class));
                finish();
            }
        }

        @Override
        public void onSubscribeChanged(boolean subscribed) {

        }

        @Override
        public void onPublicationArrived() {

        }

        @Override
        public void onReceive(String topic, String publication) {
            Utils.vibrate();
            Notify.showToast(publication);
        }

        @Override
        public void onTimeChanged(long milliseconds) {

        }

        @Override
        public void onGate() {

        }
    };
}