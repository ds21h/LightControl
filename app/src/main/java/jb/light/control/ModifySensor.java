package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

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

        mEdtTreshold = (EditText)findViewById(R.id.edtTreshold);
        mEdtMax = (EditText)findViewById(R.id.edtMax);
        mEdtInterval = (EditText)findViewById(R.id.edtInterval);
        mEdtRepeat = (EditText)findViewById(R.id.edtRepeat);

        mData = Data.getInstance(mContext);

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            mServerName = lBundle.getString(cServerName, "");
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

        new GetSensor().execute();
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

    private void sFillScreen(){
        mEdtTreshold.setText(String.valueOf(mTreshold));
        mEdtMax.setText(String.valueOf(mMax));
        mEdtInterval.setText(String.valueOf(mInterval));
        mEdtRepeat.setText(String.valueOf(mRepeat));
    }

    public void sProcess(MenuItem pMenu){
        sReadScreen();
        new SetSensor().execute();
    }

    public void sRefresh(MenuItem pMenu){

    }

    public void sDelete(MenuItem pMenu){

    }

    private class GetSensor extends AsyncTask<Void, Void, RestAPI.RestResult> {

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;

            lRequest = mServer.xAddress() + URIs.UriServerSetting;
            lRestAPI = new RestAPI();
            lRestAPI.xMethod(RestAPI.cMethodGet);
            lRestAPI.xMediaRequest(RestAPI.cMediaText);
            lRestAPI.xMediaReply(RestAPI.cMediaJSON);
            lRestAPI.xUrl(lRequest);
            lRestAPI.xAction("");
            lOutput = lRestAPI.xCallApi();
            return lOutput;
        }

        protected void onPostExecute(RestAPI.RestResult pOutput) {
            JSONObject lResult;
            Setting lSetting;

            switch (pOutput.xResult()){
                case Result.cResultOK:
                    lResult = pOutput.xReplyJ();
                    lSetting = new Setting(lResult);

                    mTreshold = lSetting.xSensorLimit();
                    mMax = lSetting.xMaxSensor();
                    mInterval = lSetting.xPeriodSec();
                    mRepeat = lSetting.xPeriodDark();
                    sFillScreen();
                    break;
                case Result.cResultConnectTimeOut:
                    Toast.makeText(mContext, "Connect Time-Out", Toast.LENGTH_SHORT).show();
                    break;
                case Result.cResultReadTimeOut:
                    Toast.makeText(mContext, "Read Time-Out", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(mContext, pOutput.xText(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private class SetSensor extends AsyncTask<Void, Void, RestAPI.RestResult> {

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            JSONObject lSensor;
            JSONObject lAction;

            lSensor = new JSONObject();
            lAction = new JSONObject();
            try {
                lSensor.put(Setting.cLimit, String.valueOf(mTreshold));
                lSensor.put(Setting.cMax, String.valueOf(mMax));
                lSensor.put(Setting.cInterval, String.valueOf(mInterval));
                lSensor.put(Setting.cRepeat, String.valueOf(mRepeat));
                lAction.put(Setting.cSensor, lSensor);
                lAction.put("lang", Locale.getDefault().getLanguage());
            } catch (JSONException pExc){
                lAction = null;
            }

            lRequest = mServer.xAddress() + URIs.UriServerSetting;
            lRestAPI = new RestAPI();
            lRestAPI.xMethod(RestAPI.cMethodPut);
            lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
            lRestAPI.xMediaReply(RestAPI.cMediaJSON);
            lRestAPI.xUrl(lRequest);
            lRestAPI.xAction(lAction.toString());
            lOutput = lRestAPI.xCallApi();
            return lOutput;
        }

        protected void onPostExecute(RestAPI.RestResult pOutput) {
            JSONObject lResult;
            JSONObject lSettingJ;
            Setting lSetting;
            String lText;

            switch (pOutput.xResult()){
                case Result.cResultOK:
                    lResult = pOutput.xReplyJ();
                    try {
                        lText = lResult.getString("descr");
                        lSettingJ = lResult.getJSONObject(Setting.cSetting);
                        lSetting = new Setting(lSettingJ);

                        mTreshold = lSetting.xSensorLimit();
                        mMax = lSetting.xMaxSensor();
                        mInterval = lSetting.xPeriodSec();
                        mRepeat = lSetting.xPeriodDark();
                        sFillScreen();
                    } catch (JSONException pExc){
                        lText = "Wrong answer";
                    }
                    Toast.makeText(mContext, lText, Toast.LENGTH_SHORT).show();
                    break;
                case Result.cResultConnectTimeOut:
                    Toast.makeText(mContext, "Connect Time-Out", Toast.LENGTH_SHORT).show();
                    break;
                case Result.cResultReadTimeOut:
                    Toast.makeText(mContext, "Read Time-Out", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(mContext, pOutput.xText(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
