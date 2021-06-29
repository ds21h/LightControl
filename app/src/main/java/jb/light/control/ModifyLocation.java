package jb.light.control;

import android.app.Activity;
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
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class ModifyLocation extends Activity {
    private final Context mContext = this;

    static final String cSetting = "Setting";
    static final String cServerName = "ServerName";
    private static final String cLongitude = "Longitude";
    private static final String cLattitude = "Lattitude";

    private String mServerName;

    private Data mData;
    private Server mServer;
    private Setting mSetting;

    private EditText mEdtLongitude;
    private EditText mEdtLattitude;

    private double mLongitude;
    private double mLattitude;

    Handler mUpdateHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message pMessage) {
            if ((pMessage.what & PutSettingRunnable.cSettingProcessedOK) != 0){
                mLongitude = mSetting.xLongitude();
                mLattitude = mSetting.xLattitude();
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
        setContentView(R.layout.modify_location_layout);

        Intent lInt;
        Bundle lBundle;
        JSONObject lSetting;

        mEdtLongitude = findViewById(R.id.edtLongitude);
        mEdtLattitude = findViewById(R.id.edtLattitude);

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
            mLongitude = mSetting.xLongitude();
            mLattitude = mSetting.xLattitude();
        } else {
            try{
                lSetting = new JSONObject(savedInstanceState.getString(cSetting));
                mSetting = new Setting(lSetting);
            } catch (JSONException pExc){
                mSetting = new Setting();
            }
            mServerName = savedInstanceState.getString(cServerName);
            mLongitude = savedInstanceState.getDouble(cLongitude);
            mLattitude = savedInstanceState.getDouble(cLattitude);
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
        savedInstanceState.putDouble(cLongitude, mLongitude);
        savedInstanceState.putDouble(cLattitude, mLattitude);
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
            mLongitude = Double.parseDouble(mEdtLongitude.getText().toString());
            mLattitude = Double.parseDouble(mEdtLattitude.getText().toString());
        } catch (NumberFormatException pExc){
        }
    }

    private boolean sCheckScreen(){
        boolean lResult;
        double lLongitude = 0;
        double lLattitude = 0;

        lResult = true;
        try {
            lLongitude = Double.parseDouble(mEdtLongitude.getText().toString());
        } catch (NumberFormatException pExc){
            Toast.makeText(mContext, getString(R.string.msg_long_format), Toast.LENGTH_SHORT).show();
            lResult = false;
        }
        if (lResult){
            if (lLongitude < -180D || lLongitude > 180D){
                Toast.makeText(mContext, getString(R.string.msg_long_range), Toast.LENGTH_SHORT).show();
                lResult = false;
            } else {
                try {
                    lLattitude = Double.parseDouble(mEdtLattitude.getText().toString());
                } catch (NumberFormatException pExc){
                    Toast.makeText(mContext, getString(R.string.msg_latt_format), Toast.LENGTH_SHORT).show();
                    lResult = false;
                }
                if (lResult){
                    if (lLattitude < -90D || lLattitude > 90D){
                        Toast.makeText(mContext, getString(R.string.msg_latt_range), Toast.LENGTH_SHORT).show();
                        lResult = false;
                    }
                }
            }
        }
        return lResult;
    }

    private void sFillScreen(){
        mEdtLongitude.setText(String.valueOf(mLongitude));
        mEdtLattitude.setText(String.valueOf(mLattitude));
    }

    public void sProcess(MenuItem pMenu){
        PutSettingRunnable lRunnable;

        if (sCheckScreen()){
            sReadScreen();
            mSetting.xLongitude(mLongitude);
            mSetting.xLattitude(mLattitude);
            lRunnable = new PutSettingRunnable(mServer.xAddress(), mSetting, mUpdateHandler, PutSettingRunnable.cSaveLocation);
            LightControlApp.getInstance().xExecutor.execute(lRunnable);
        }
    }

    public void sRefresh(MenuItem pMenu){
        mLongitude = mSetting.xLongitude();
        mLattitude = mSetting.xLattitude();
        sFillScreen();
    }

    public void sDelete(MenuItem pMenu){
    }
}
