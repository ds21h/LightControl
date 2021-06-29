package jb.light.control;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class ModifyLightOff extends Activity {
    private final Context mContext = this;

    static final String cServerName = "ServerName";
    static final String cSetting = "Setting";
    private static final String cHour = "Hour";
    private static final String cMin = "Min";
    private static final String cPeriod = "Period";

    private String mServerName;

    private Data mData;
    private Server mServer;
    private Setting mSetting;

    private TextView mTxtLightOff;
    private EditText mEdtPeriod;

    private int mHour;
    private int mMinute;
    private int mPeriod;

    Handler mUpdateHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message pMessage) {
            if ((pMessage.what & PutSettingRunnable.cSettingProcessedOK) != 0){
                mHour = mSetting.xLightOffHour();
                mMinute = mSetting.xLightOffMin();
                mPeriod = mSetting.xLightOffPeriod();
                sFillScreen();
                Toast.makeText(mContext, getString(R.string.msg_update_OK), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, getString(R.string.msg_update_NOK), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_light_off_layout);

        Intent lInt;
        Bundle lBundle;
        JSONObject lSetting;

        mTxtLightOff = findViewById(R.id.txtLightOff);
        mEdtPeriod = findViewById(R.id.edtPeriod);

        mTxtLightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sSetTimeLightOff();
            }
        });

        mData = Data.getInstance(mContext);

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            if (lBundle == null){
                mServerName = "";
                mSetting = new Setting();
            } else {
                mServerName = lBundle.getString(cServerName, "");
                try{
                    lSetting = new JSONObject(lBundle.getString(cSetting));
                    mSetting = new Setting(lSetting);
                } catch (JSONException pExc){
                    mSetting = new Setting();
                }
            }
            mHour = mSetting.xLightOffHour();
            mMinute = mSetting.xLightOffMin();
            mPeriod = mSetting.xLightOffPeriod();

        } else {
            try{
                lSetting = new JSONObject(savedInstanceState.getString(cSetting));
                mSetting = new Setting(lSetting);
            } catch (JSONException pExc){
                mSetting = new Setting();
            }
            mServerName = savedInstanceState.getString(cServerName);
            mHour = savedInstanceState.getInt(cHour);
            mMinute = savedInstanceState.getInt(cMin);
            mPeriod = savedInstanceState.getInt(cPeriod);
        }
        sFillScreen();
        mServer = mData.xServer(mServerName);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        sReadScreen();
        savedInstanceState.putString(cSetting, mSetting.xSetting().toString());
        savedInstanceState.putString(cServerName, mServerName);
        savedInstanceState.putInt(cHour, mHour);
        savedInstanceState.putInt(cMin, mMinute);
        savedInstanceState.putInt(cPeriod, mPeriod);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        getMenuInflater().inflate(R.menu.modify_menu, pMenu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu pMenu){
        MenuItem lItem;

        lItem = pMenu.findItem(R.id.action_delete);
        lItem.setVisible(false);
        return true;
    }

    private  void sReadScreen(){
        try {
            mPeriod = Integer.parseInt(mEdtPeriod.getText().toString());
        } catch (NumberFormatException pExc){
        }
    }

    private boolean sCheckScreen(){
        boolean lResult;
        int lPeriod = 0;

        lResult = true;
        try {
            lPeriod = Integer.parseInt(mEdtPeriod.getText().toString());
        } catch (NumberFormatException pExc){
            Toast.makeText(mContext, getString(R.string.msg_period_format), Toast.LENGTH_SHORT).show();
            lResult = false;
        }
        if (lResult){
            if (lPeriod < 0 || lPeriod > 120){
                Toast.makeText(mContext, getString(R.string.msg_period_range), Toast.LENGTH_SHORT).show();
                lResult = false;
            }
        }
        return lResult;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void sFillScreen(){
        mTxtLightOff.setText(String.format("%02d", mHour) + ":" + String.format("%02d", mMinute));
        mEdtPeriod.setText(String.valueOf(mPeriod));
    }

    private void sSetTimeLightOff(){
        TimePickerDialog lPicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int pHour, int pMinute) {
                mHour = pHour;
                mMinute = pMinute;
                sFillScreen();
            }
        }, mHour, mMinute, true);
        lPicker.show();
    }

    public void sProcess(MenuItem pMenu){
        PutSettingRunnable lRunnable;

        if (sCheckScreen()){
            sReadScreen();
            mSetting.xLightOffHour(mHour);
            mSetting.xLightOffMin(mMinute);
            mSetting.xLightOffPeriod(mPeriod);
            lRunnable = new PutSettingRunnable(mServer.xAddress(), mSetting, mUpdateHandler, PutSettingRunnable.cSaveLightOff);
            LightControlApp.getInstance().xExecutor.execute(lRunnable);
        }
    }

    public void sRefresh(MenuItem pMenu){
        mHour = mSetting.xLightOffHour();
        mMinute = mSetting.xLightOffMin();
        mPeriod = mSetting.xLightOffPeriod();
        sFillScreen();
    }

    public void sDelete(MenuItem pMenu){

    }
}
