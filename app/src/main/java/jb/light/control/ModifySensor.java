package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class ModifySensor extends Activity {
    private final Context mContext = this;

    public static final String cServerName = "ServerName";
    private static final String cTreshold = "Treshold";
    private static final String cMax = "Max";
    private static final String cInterval = "Interval";
    private static final String cRepeat = "Repeat";

    private String mServerName;

    private Data mData;
    private Server mServer;

    private EditText mEdtTreshold;
    private EditText mEdtMax;
    private EditText mEdtInterval;
    private EditText mEdtRepeat;

    private int mTreshold;
    private int mMax;
    private int mInterval;
    private int mRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_sensor_layout);

        Intent lInt;
        Bundle lBundle;

        mEdtTreshold = findViewById(R.id.edtTreshold);
        mEdtMax = findViewById(R.id.edtMax);
        mEdtInterval = findViewById(R.id.edtInterval);
        mEdtRepeat = findViewById(R.id.edtRepeat);

        mData = Data.getInstance(mContext);

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            if (lBundle == null){
                mServerName = "";
            } else {
                mServerName = lBundle.getString(cServerName, "");
            }
            mTreshold = 0;
            mMax = 0;
            mInterval = 0;
            mRepeat = 0;
        } else {
            mServerName = savedInstanceState.getString(cServerName);
            mTreshold = savedInstanceState.getInt(cTreshold);
            mMax = savedInstanceState.getInt(cMax);
            mInterval = savedInstanceState.getInt(cInterval);
            mRepeat = savedInstanceState.getInt(cRepeat);
        }
        sFillScreen();
        mServer = mData.xServer(mServerName);

        if (mServer != null){
            new GetSensor(this).execute();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        sReadScreen();
        savedInstanceState.putString(cServerName, mServerName);
        savedInstanceState.putInt(cTreshold, mTreshold);
        savedInstanceState.putInt(cMax, mMax);
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
            mMax = Integer.parseInt(mEdtMax.getText().toString());
            mInterval = Integer.parseInt(mEdtInterval.getText().toString());
            mRepeat = Integer.parseInt(mEdtRepeat.getText().toString());
        } catch (NumberFormatException pExc){
            mTreshold = 0;
            mMax = 0;
            mInterval = 0;
            mRepeat = 0;
        }
    }

    private void sProcessResponse(int pStatus, String pMessage, JSONObject pResult, boolean pMessageOK){
        Setting lSetting;

        switch (pStatus){
            case Result.cResultOK:
                lSetting = new Setting(pResult);

                mTreshold = lSetting.xSensorLimit();
                mMax = lSetting.xMaxSensor();
                mInterval = lSetting.xPeriodSec();
                mRepeat = lSetting.xPeriodDark();
                sFillScreen();
                if (pMessageOK){
                    Toast.makeText(mContext, pMessage, Toast.LENGTH_SHORT).show();
                }
                break;
            case Result.cResultConnectTimeOut:
                Toast.makeText(mContext, "Connect Time-Out", Toast.LENGTH_SHORT).show();
                break;
            case Result.cResultReadTimeOut:
                Toast.makeText(mContext, "Read Time-Out", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(mContext, pMessage, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void sFillScreen(){
        mEdtTreshold.setText(String.valueOf(mTreshold));
        mEdtMax.setText(String.valueOf(mMax));
        mEdtInterval.setText(String.valueOf(mInterval));
        mEdtRepeat.setText(String.valueOf(mRepeat));
    }

    public void sProcess(MenuItem pMenu){
        sReadScreen();
        new SetSensor(this).execute();
    }

    public void sRefresh(MenuItem pMenu){

    }

    public void sDelete(MenuItem pMenu){

    }

    private static class GetSensor extends AsyncTask<Void, Void, RestAPI.RestResult> {
        private WeakReference<ModifySensor> mRefMain;

        private GetSensor(ModifySensor pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            ModifySensor lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;

            lMain = mRefMain.get();
            if (lMain == null){
                return null;
            } else {
                lRequest = lMain.mServer.xAddress() + URIs.UriServerSetting;
                lRestAPI = new RestAPI();
                lRestAPI.xMethod(RestAPI.cMethodGet);
                lRestAPI.xMediaRequest(RestAPI.cMediaText);
                lRestAPI.xMediaReply(RestAPI.cMediaJSON);
                lRestAPI.xUrl(lRequest);
                lRestAPI.xAction("");
                lOutput = lRestAPI.xCallApi();
                return lOutput;
            }
        }

        protected void onPostExecute(RestAPI.RestResult pOutput) {
            ModifySensor lMain;

            if (pOutput != null){
                lMain = mRefMain.get();
                if (lMain != null){
                    lMain.sProcessResponse(pOutput.xResult(), pOutput.xText(), pOutput.xReplyJ(), false);
                }
            }
        }
    }

    private static class SetSensor extends AsyncTask<Void, Void, RestAPI.RestResult> {
        private WeakReference<ModifySensor> mRefMain;

        private SetSensor(ModifySensor pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            ModifySensor lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            JSONObject lSensor;
            JSONObject lAction;
            String lActionS;

            lMain = mRefMain.get();
            if (lMain == null){
                return null;
            } else {
                lSensor = new JSONObject();
                lAction = new JSONObject();
                try {
                    lSensor.put(Setting.cLimit, String.valueOf(lMain.mTreshold));
                    lSensor.put(Setting.cMax, String.valueOf(lMain.mMax));
                    lSensor.put(Setting.cInterval, String.valueOf(lMain.mInterval));
                    lSensor.put(Setting.cRepeat, String.valueOf(lMain.mRepeat));
                    lAction.put(Setting.cSensor, lSensor);
                    lAction.put("lang", Locale.getDefault().getLanguage());
                    lActionS = lAction.toString();
                } catch (JSONException pExc){
                    lActionS = "";
                }

                lRequest = lMain.mServer.xAddress() + URIs.UriServerSetting;
                lRestAPI = new RestAPI();
                lRestAPI.xMethod(RestAPI.cMethodPut);
                lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
                lRestAPI.xMediaReply(RestAPI.cMediaJSON);
                lRestAPI.xUrl(lRequest);
                lRestAPI.xAction(lActionS);
                lOutput = lRestAPI.xCallApi();
                return lOutput;
            }
        }

        protected void onPostExecute(RestAPI.RestResult pOutput) {
            ModifySensor lMain;
            String lText;
            JSONObject lResult;
            JSONObject lSettingJ;
            int lStatus;
            final String cError = "Invalid reply!";

            if (pOutput != null){
                lMain = mRefMain.get();
                if (lMain != null){
                    if (pOutput.xResult() == Result.cResultOK){
                        lResult = pOutput.xReplyJ();
                        lText = lResult.optString("descr", cError);
                        if (lText.equals(cError)){
                            lStatus = Result.cResultJSONError;
                            lSettingJ = null;
                        } else {
                            lStatus = Result.cResultOK;
                            lSettingJ = lResult.optJSONObject(Setting.cSetting);
                        }
                        lMain.sProcessResponse(lStatus, lText, lSettingJ, true);
                    } else {
                        lMain.sProcessResponse(pOutput.xResult(), pOutput.xText(), null, false);
                    }
                }
            }
        }
    }
}
