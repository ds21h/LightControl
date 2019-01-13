package jb.light.control;

import android.annotation.SuppressLint;
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

import java.lang.ref.WeakReference;
import java.util.Locale;

public class ModifyLightOff extends Activity {
    private final Context mContext = this;

    static final String cServerName = "ServerName";
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

        mTxtLightOff = findViewById(R.id.txtLightOff);
        mEdtPeriod = findViewById(R.id.edtPeriod);

        mData = Data.getInstance(mContext);

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            if (lBundle == null){
                mServerName = "";
            } else {
                mServerName = lBundle.getString(cServerName, "");
            }
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
        if (mServer != null){
            new GetLightOff(this).execute();
        }
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

    private void sProcessResponse(int pStatus, String pMessage, JSONObject pResult, boolean pMessageOK){
        Setting lSetting;

        switch (pStatus){
            case Result.cResultOK:
                lSetting = new Setting(pResult);

                mHour = lSetting.xLightOffHour();
                mMinute = lSetting.xLightOffMin();
                mPeriod = lSetting.xLightOffPeriod();
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

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
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
        new SetLightOff(this).execute();
    }

    public void sRefresh(MenuItem pMenu){

    }

    public void sDelete(MenuItem pMenu){

    }

    private static class GetLightOff extends AsyncTask<Void, Void, RestAPI.RestResult> {
        WeakReference<ModifyLightOff> mRefMain;

        private GetLightOff(ModifyLightOff pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            ModifyLightOff lMain;
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
            ModifyLightOff lMain;

            if (pOutput != null){
                lMain = mRefMain.get();
                if (lMain != null){
                    lMain.sProcessResponse(pOutput.xResult(), pOutput.xText(), pOutput.xReplyJ(), false);
                }
            }
        }
    }

    private static class SetLightOff extends AsyncTask<Void, Void, RestAPI.RestResult> {
        WeakReference<ModifyLightOff> mRefMain;

        private SetLightOff(ModifyLightOff pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            ModifyLightOff lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            JSONObject lLightOff;
            JSONObject lAction;
            String lActionS;

            lMain = mRefMain.get();
            if (lMain == null){
                return null;
            } else {
                lLightOff = new JSONObject();
                lAction = new JSONObject();
                try {
                    lLightOff.put(Setting.cPointInTime, String.format("%02d", lMain.mHour) + ":" + String.format("%02d", lMain.mMinute));
                    lLightOff.put(Setting.cPeriod, String.valueOf(lMain.mPeriod));
                    lAction.put(Setting.cLightOff, lLightOff);
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
            ModifyLightOff lMain;
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
