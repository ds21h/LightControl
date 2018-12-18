package jb.light.control;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class ModifyLightOff extends Activity {
    private final Context mContext = this;

    public static final String cServerName = "ServerName";
    private static final String cHour = "Hour";
    private static final String cMin = "Min";
    private static final String cPeriod = "Period";

    private String mServerName;

    private Data mData;
    private Server mServer;

    private TextView mTxtLightOff;
    private EditText mEdtPeriod;

    private int mHour;
    private int mMinute;
    private int mPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_light_off_layout);

        Intent lInt;
        Bundle lBundle;

        mTxtLightOff = (TextView) findViewById(R.id.txtLightOff);
        mEdtPeriod = (EditText)findViewById(R.id.edtPeriod);

        mData = Data.getInstance(mContext);

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            mServerName = lBundle.getString(cServerName, "");
            mHour = 0;
            mMinute = 0;
            mPeriod = 0;

        } else {
            mServerName = savedInstanceState.getString(cServerName);
            mHour = savedInstanceState.getInt(cHour);
            mMinute = savedInstanceState.getInt(cMin);
            mPeriod = savedInstanceState.getInt(cPeriod);
        }
        sFillScreen();
        mServer = mData.xServer(mServerName);

        new GetLightOff().execute();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        sReadScreen();
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
            mPeriod = 0;
        }
    }

    private void sFillScreen(){
        mTxtLightOff.setText(String.format("%02d", mHour) + ":" + String.format("%02d", mMinute));
        mEdtPeriod.setText(String.valueOf(mPeriod));
    }

    public void sSetTimeLightOff(View pView){
        sReadScreen();

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
        sReadScreen();
        new SetLightOff().execute();
    }

    public void sRefresh(MenuItem pMenu){

    }

    public void sDelete(MenuItem pMenu){

    }

    private class GetLightOff extends AsyncTask<Void, Void, RestAPI.RestResult> {

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
            SettingItem lItem;

            switch (pOutput.xResult()){
                case Result.cResultOK:
                    lResult = pOutput.xReplyJ();
                    lSetting = new Setting(lResult);

                    mHour = lSetting.xLightOffHour();
                    mMinute = lSetting.xLightOffMin();
                    mPeriod = lSetting.xLightOffPeriod();
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

    private class SetLightOff extends AsyncTask<Void, Void, RestAPI.RestResult> {

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            JSONObject lLightOff;
            JSONObject lAction;

            lLightOff = new JSONObject();
            lAction = new JSONObject();
            try {
                lLightOff.put(Setting.cPointInTime, String.format("%02d", mHour) + ":" + String.format("%02d", mMinute));
                lLightOff.put(Setting.cPeriod, String.valueOf(mPeriod));
                lAction.put(Setting.cLightOff, lLightOff);
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
                        lText = lResult.getString("omschrijving");
                        lSettingJ = lResult.getJSONObject(Setting.cSetting);
                        lSetting = new Setting(lSettingJ);

                        mHour = lSetting.xLightOffHour();
                        mMinute = lSetting.xLightOffMin();
                        mPeriod = lSetting.xLightOffPeriod();
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
