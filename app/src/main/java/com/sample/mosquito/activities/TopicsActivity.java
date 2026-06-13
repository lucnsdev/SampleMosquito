package com.sample.mosquito.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import com.sample.mosquito.R;
import com.sample.mosquito.mqtt.Publication;
import com.sample.mosquito.services.BaseService;
import com.sample.mosquito.services.MainService;
import com.sample.mosquito.services.ServiceController;
import com.sample.mosquito.utils.Annotator;
import com.sample.mosquito.utils.AppPreferences;
import com.sample.mosquito.utils.Notify;
import com.sample.mosquito.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class TopicsActivity extends Activity {

    private MainService mainService;
    private boolean anotherActivityStarted;
    private Button buttonSubscribe, buttonSend, buttonSendRetained, buttonDeleteRetained;
    private EditText editTextMessage, editTextTopicReceive, editTextTopicSend, editTextPeriodicity;
    private Switch aSwitch;
    private TextView textTimeCounter;
    private ListView listView;
    private MyListAdapter listAdapter;
    private long timerCounterValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        long value = AppPreferences.getLong("periodicity");
        timerCounterValue = value == 0 ? 10 : value;
        textTimeCounter = findViewById(R.id.textTimeCounter);

        /*
        try {
            JSONObject jsonObject = new JSONObject(new Annotator("Enter.json").getContent());
            ((TextView) findViewById(R.id.textTitle)).setText(getString(R.string.topics) + " (" + jsonObject.getString("broker") + ")");
        } catch (JSONException e) {
            e.printStackTrace();
            ((TextView) findViewById(R.id.textTitle)).setText(R.string.topics);
        }
         */
        buttonSubscribe = findViewById(R.id.buttonSubscribe);
        buttonSend = findViewById(R.id.buttonSend);
        buttonSendRetained = findViewById(R.id.buttonSendRetained);
        buttonDeleteRetained = findViewById(R.id.buttonDeleteRetained);

        aSwitch = findViewById(R.id.aSwitch);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            boolean lastCheck;

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (lastCheck == isChecked) return;
                lastCheck = isChecked;
                if (!Utils.hasInternetConnection()) {
                    lastCheck = false;
                    Notify.showToast(R.string.error_no_internet_connection);
                    aSwitch.setChecked(false);
                    textTimeCounter.setText((timerCounterValue / 1000) + "s");
                    return;
                }
                editTextMessage.setEnabled(!isChecked);
                editTextPeriodicity.setEnabled(!isChecked);
                if (isChecked) {
                    buttonSend.setEnabled(false);
                    buttonSendRetained.setEnabled(false);
                    buttonDeleteRetained.setEnabled(false);
                    if (!mainService.isTimerCounterRunning()) {
                        mainService.startTimerCounter(editTextTopicSend.getText().toString(), editTextMessage.getText().toString(), Long.parseLong(editTextPeriodicity.getText().toString()));
                    }
                } else {
                    mainService.stopTimerCounter();
                    String text = editTextMessage.getText().toString();
                    String text2 = editTextTopicSend.getText().toString();
                    boolean enable = text.length() > 0 && text2.length() > 3 && text2.contains("/") && text2.split("/").length > 1;
                    buttonSend.setEnabled(enable);
                    buttonSendRetained.setEnabled(enable);
                    buttonDeleteRetained.setEnabled(enable);
                    textTimeCounter.setText((timerCounterValue / 1000) + "s");
                }
            }
        });

        editTextMessage = findViewById(R.id.editTextMessage);
        editTextPeriodicity = findViewById(R.id.editTextPeriodicity);
        editTextPeriodicity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = editTextPeriodicity.getText().toString();
                if (s.length() <= 3) {
                    if (!aSwitch.isChecked()) aSwitch.setEnabled(false);
                    return;
                }
                long value = Long.parseLong(s);
                if (value < 1000) {
                    if (!aSwitch.isChecked()) aSwitch.setEnabled(false);
                    return;
                }
                timerCounterValue = value;
                if (mainService != null) mainService.setTimerCounterValue(timerCounterValue);
                textTimeCounter.setText((timerCounterValue / 1000) + "s");
                aSwitch.setEnabled(editTextMessage.getText().length() > 0);
                AppPreferences.setLong("periodicity", timerCounterValue);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editTextPeriodicity.setText(String.valueOf(value == 0 ? 10000 : value));

        editTextTopicReceive = findViewById(R.id.editTextTopicReceive);
        editTextTopicReceive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = editTextTopicReceive.getText().toString();
                buttonSubscribe.setEnabled(text.length() > 0 && text.contains("/") && text.split("/").length > 1);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        TextWatcher textWatcherMessage = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = editTextMessage.getText().toString();
                String text2 = editTextTopicSend.getText().toString();
                boolean enable = text.length() > 0 && text2.length() > 3 && text2.contains("/") && text2.split("/").length > 1;
                buttonSend.setEnabled(enable && !aSwitch.isChecked());
                buttonSendRetained.setEnabled(enable && !aSwitch.isChecked());
                buttonDeleteRetained.setEnabled(enable && !aSwitch.isChecked());
                String s = editTextPeriodicity.getText().toString();
                textTimeCounter.setText((timerCounterValue / 1000) + "s");
                if (s.length() <= 3) {
                    aSwitch.setEnabled(false);
                    return;
                }
                long value = Long.parseLong(s);
                if (value < 100) {
                    aSwitch.setEnabled(false);
                    return;
                }
                aSwitch.setEnabled(text.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        listAdapter = new MyListAdapter(this, new Publication[0]);
        listView = findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
        editTextTopicSend = findViewById(R.id.editTextTopicSend);
        editTextTopicSend.addTextChangedListener(textWatcherMessage);
        editTextMessage.addTextChangedListener(textWatcherMessage);
        editTextMessage.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    mainService.sendMessage(editTextTopicSend.getText().toString(), editTextMessage.getText().toString());
                    editTextMessage.getText().clear();
                    return true;
                }
                return false;
            }
        });

        Annotator annotator = new Annotator("Topics.json");
        try {
            JSONObject jsonObject;
            if (annotator.exists()) {
                jsonObject = new JSONObject(annotator.getContent());
            } else {
                jsonObject = new JSONObject();
            }
            if (jsonObject.has("topic_subscribed"))
                editTextTopicReceive.setText(jsonObject.getString("topic_subscribed"));
            if (jsonObject.has("topic_to_send_messages"))
                editTextTopicSend.setText(jsonObject.getString("topic_to_send_messages"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.buttonBack) {
                    finish();
                } else if (view.getId() == R.id.buttonSubscribe) {
                    buttonSubscribe.setEnabled(false);
                    if (mainService.isSubscribed()) {
                        buttonSubscribe.setText(R.string.subscribe);
                        mainService.unsubscribe(editTextTopicReceive.getText().toString());
                        editTextTopicReceive.setEnabled(true);
                    } else {
                        buttonSubscribe.setText(R.string.subscribing);
                        editTextTopicReceive.setEnabled(false);
                        try {
                            JSONObject jsonObject;
                            if (annotator.exists())
                                jsonObject = new JSONObject(annotator.getContent());
                            else jsonObject = new JSONObject();
                            jsonObject.put("topic_subscribed", editTextTopicReceive.getText().toString());
                            annotator.setContent(jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mainService.subscribe(editTextTopicReceive.getText().toString());
                    }
                } else if (view.getId() == R.id.buttonSend) {
                        /*
                        aSwitch.setEnabled(false);
                        buttonSend.setEnabled(false);
                        buttonDeleteRetained.setEnabled(false);
                        buttonSendRetained.setEnabled(false);
                         */
                    mainService.sendMessage(editTextTopicSend.getText().toString(), editTextMessage.getText().toString());
                } else if (view.getId() == R.id.buttonSendRetained) {
                        /*
                        aSwitch.setEnabled(false);
                        buttonSend.setEnabled(false);
                        buttonDeleteRetained.setEnabled(false);
                        buttonSendRetained.setEnabled(false);
                         */
                    mainService.sendRetainedMessage(editTextMessage.getText().toString(), editTextTopicSend.getText().toString());
                } else if (view.getId() == R.id.buttonDeleteRetained) {
                    aSwitch.setEnabled(false);
                    buttonSend.setEnabled(false);
                    buttonDeleteRetained.setEnabled(false);
                    buttonSendRetained.setEnabled(false);
                    mainService.deleteRetainedMessage(editTextTopicSend.getText().toString());
                    editTextMessage.getText().clear();
                }
            }
        };
        //findViewById(R.id.buttonBack).setOnClickListener(onClick);
        buttonSubscribe.setOnClickListener(onClick);
        buttonSend.setOnClickListener(onClick);
        buttonSendRetained.setOnClickListener(onClick);
        buttonDeleteRetained.setOnClickListener(onClick);

        ServiceController.getInstance(this, new ServiceController.OnServiceAvailableListener() {
            @Override
            public void onAvailable(MainService mainService) {
                TopicsActivity.this.mainService = mainService;
                mainService.setCallback(callback);
                mainService.stopForeground();
                mainService.setTimerCounterValue(timerCounterValue);
                if (mainService.getLastPublication() != null) {
                    editTextMessage.setText(mainService.getLastPublication());
                    aSwitch.setEnabled(true);
                }
                aSwitch.setChecked(mainService.isTimerCounterRunning());

                editTextTopicReceive.setEnabled(!mainService.isSubscribed());
                buttonSubscribe.setText(mainService.isSubscribed() ? R.string.unsubscribe : R.string.subscribe);
            }
        });

        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackPressedListener);
    }

    private final OnBackInvokedCallback onBackPressedListener = new OnBackInvokedCallback() {
        @Override
        public void onBackInvoked() {
            if (isFinishing()) return;
            anotherActivityStarted = true;
            startActivity(new Intent(TopicsActivity.this, ConnectActivity.class));
            finish();
        }
    };

    private void saveSendTopic() {
        Annotator annotator = new Annotator("Topics.json");
        try {
            JSONObject jsonObject;
            if (annotator.exists()) {
                jsonObject = new JSONObject(annotator.getContent());
            } else {
                jsonObject = new JSONObject();
            }
            jsonObject.put("topic_to_send_messages", editTextTopicSend.getText().toString());
            annotator.setContent(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        anotherActivityStarted = false;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackPressedListener);
        saveSendTopic();
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
            if (!isConnected) {
                anotherActivityStarted = true;
                Utils.vibrate();
                Notify.showToast(R.string.mqtt_disconnected);
                startActivity(new Intent(TopicsActivity.this, ConnectActivity.class));
                finish();
            }
        }

        @Override
        public void onSubscribeChanged(boolean subscribed) {
            Utils.vibrate();
            editTextTopicReceive.setEnabled(!subscribed);
            buttonSubscribe.setEnabled(true);
            buttonSubscribe.setText(subscribed ? R.string.unsubscribe : R.string.subscribe);
        }

        @Override
        public void onPublicationArrived() {
            // Utils.vibrate();
            if (!aSwitch.isChecked()) {
                editTextMessage.getText().clear();
                editTextMessage.setEnabled(true);
            }
            // Notify.showToast(R.string.publication_arrived);
            textTimeCounter.setText((timerCounterValue / 1000) + "s");
        }

        @Override
        public void onReceive(String topic, String message) {
            Utils.vibrate();
            listAdapter.addItem(new Publication(topic, message));
            listView.smoothScrollByOffset(listAdapter.getCount());
        }

        @Override
        public void onTimeChanged(long milliseconds) {
            textTimeCounter.setText((milliseconds / 1000) + "s");
        }

        @Override
        public void onGate() {
            textTimeCounter.setText(R.string.sending);
        }
    };
}
