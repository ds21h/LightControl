package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class ModifySensor extends Activity {
    private final Context mContext = this;

    static final String cSetting = "Setting";
    public static final String cServerName = "ServerName";
    private static final String cTreshold = "Treshold";
    private static final String cInterval = "Interval";
    private static final String cRepeat = "Repeat";

    private String mServerName;

    private Data mData;
    private Server mServer;
    private Setting mSetting;

    private EditText mEdtTreshold;
    private EditText mEdtInterval;
    private EditText mEdtRepeat;

    private int mTreshold;
    private int mInterval;
    private int mRepeat;

    Handler mUpdateHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message pMessage) {
            String lErrMsg;

            if ((pMessage.what & HandlerCode.cPutServerSetting) != 0){
                lErrMsg = HandlerCode.xCheckCode(mContext, pMessage.what);
                if (lErrMsg == null){
                    mTreshold = mSetting.xSensorLimit();
                    mInterval = mSetting.xPeriodSec();
                    mRepeat = mSetting.xPeriodDark();
                    sFillScreen();
                    Toast.makeText(mContext, getString(R.string.msg_update_OK), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, lErrMsg, Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_sensor_layout);

        Intent lInt;
        Bundle lBundle;
        JSONObject lSetting;

        mEdtTreshold = findViewById(R.id.edtTreshold);
        mEdtInterval = findViewById(R.id.edtInterval);
        mEdtRepeat = findViewById(R.id.edtRepeat);

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
                } catch (Exception pExc){
                    mSetting = new Setting();
                }
            }
            mTreshold = mSetting.xSensorLimit();
            mInterval = mSetting.xPeriodSec();
            mRepeat = mSetting.xPeriodDark();
        } else {
            try{
                lSetting = new JSONObject(savedInstanceState.getString(cSetting));
                mSetting = new Setting(lSetting);
            } catch (Exception pExc){
                mSetting = new Setting();
            }
            mServerName = savedInstanceState.getString(cServerName);
            mTreshold = savedInstanceState.getInt(cTreshold);
            mInterval = savedInstanceState.getInt(cInterval);
            mRepeat = savedInstanceState.getInt(cRepeat);
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
        savedInstanceState.putInt(cTreshold, mTreshold);
        savedInstanceState.putInt(cInterval, mInterval);
        savedInstanceState.putInt(cRepeat, mRepeat);
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
            mTreshold = Integer.parseInt(mEdtTreshold.getText().toString());
            mInterval = Integer.parseInt(mEdtInterval.getText().toString());
            mRepeat = Integer.parseInt(mEdtRepeat.getText().toString());
        } catch (NumberFormatException ignored){
        }
    }

    private boolean sCheckScreen(){
        boolean lResult;
        int lTreshold = 0;
        int lInterval = 0;
        int lRepeat = 0;

        lResult = true;
        try {
            lTreshold = Integer.parseInt(mEdtTreshold.getText().toString());
        } catch (NumberFormatException pExc){
            Toast.makeText(mContext, getString(R.string.msg_treshold_format), Toast.LENGTH_SHORT).show();
            lResult = false;
        }
        if (lResult){
            if (lTreshold < 0 || lTreshold > 10000){
                Toast.makeText(mContext, getString(R.string.msg_treshold_range), Toast.LENGTH_SHORT).show();
                lResult = false;
            } else {
                try {
                    lInterval = Integer.parseInt(mEdtInterval.getText().toString());
                } catch (NumberFormatException pExc){
                    Toast.makeText(mContext, getString(R.string.msg_interval_format), Toast.LENGTH_SHORT).show();
                    lResult = false;
                }
                if (lResult){
                    if (lInterval < 10 || lInterval > 300){
                        Toast.makeText(mContext, getString(R.string.msg_interval_range), Toast.LENGTH_SHORT).show();
                        lResult = false;
                    } else {
                        try {
                            lRepeat = Integer.parseInt(mEdtRepeat.getText().toString());
                        } catch (NumberFormatException pExc){
                            Toast.makeText(mContext, getString(R.string.msg_repeat_format), Toast.LENGTH_SHORT).show();
                            lResult = false;
                        }
                        if (lResult) {
                            if (lRepeat < 0 || lRepeat > 10) {
                                Toast.makeText(mContext, getString(R.string.msg_repeat_range), Toast.LENGTH_SHORT).show();
                                lResult = false;
                            }
                        }
                    }
                }
            }
        }
        return lResult;
    }

    private void sFillScreen(){
        mEdtTreshold.setText(String.valueOf(mTreshold));
        mEdtInterval.setText(String.valueOf(mInterval));
        mEdtRepeat.setText(String.valueOf(mRepeat));
    }

    public void sProcess(MenuItem pMenu){
        PutSettingRunnable lRunnable;

        if (sCheckScreen()){
            sReadScreen();
            mSetting.xSensorLimit(mTreshold);
            mSetting.xPeriodSec(mInterval);
            mSetting.xPeriodDark(mRepeat);
            lRunnable = new PutSettingRunnable(mServer.xAddress(), mSetting, mUpdateHandler, PutSettingRunnable.cSaveSensor);
            LightControlApp.getInstance().xExecutor.execute(lRunnable);
        }
    }

    public void sRefresh(MenuItem pMenu){
        mTreshold = mSetting.xSensorLimit();
        mInterval = mSetting.xPeriodSec();
        mRepeat = mSetting.xPeriodDark();
        sFillScreen();
    }

    public void sDelete(MenuItem pMenu){

    }
}
