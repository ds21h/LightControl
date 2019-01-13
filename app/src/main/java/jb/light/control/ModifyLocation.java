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

import java.lang.ref.WeakReference;
import java.util.Locale;

public class ModifyLocation extends Activity {
    private final Context mContext = this;

    public static final String cServerName = "ServerName";
    private static final String cLongitude = "Longitude";
    private static final String cLattitude = "Lattitude";

    private String mServerName;

    private Data mData;
    private Server mServer;

    private EditText mEdtLongitude;
    private EditText mEdtLattitude;

    private double mLongitude;
    private double mLattitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_location_layout);

        Intent lInt;
        Bundle lBundle;

        mEdtLongitude = findViewById(R.id.edtLongitude);
        mEdtLattitude = findViewById(R.id.edtLattitude);

        mData = Data.getInstance(mContext);

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            if (lBundle == null){
                mServerName = "";
            } else {
                mServerName = lBundle.getString(cServerName, "");
            }
            mLongitude = 0.0;
            mLattitude = 0.0;

        } else {
            mServerName = savedInstanceState.getString(cServerName);
            mLongitude = savedInstanceState.getDouble(cLongitude);
            mLattitude = savedInstanceState.getDouble(cLattitude);
        }
        sFillScreen();
        mServer = mData.xServer(mServerName);

        if (mServer != null){
            new GetLocation(this).execute();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        sReadScreen();
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
            mLongitude = 0.0;
            mLattitude = 0.0;
        }
    }

    private void sProcessResponse(int pStatus, String pMessage, JSONObject pResult, boolean pMessageOK){
        Setting lSetting;

        switch (pStatus){
            case Result.cResultOK:
                lSetting = new Setting(pResult);

                mLongitude = lSetting.xLongitude();
                mLattitude = lSetting.xLattitude();
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
        mEdtLongitude.setText(String.valueOf(mLongitude));
        mEdtLattitude.setText(String.valueOf(mLattitude));
    }

    public void sProcess(MenuItem pMenu){
        sReadScreen();
        new SetLocation(this).execute();
    }

    public void sRefresh(MenuItem pMenu){
    }

    public void sDelete(MenuItem pMenu){
    }

    private static class GetLocation extends AsyncTask<Void, Void, RestAPI.RestResult> {
        private WeakReference<ModifyLocation> mRefMain;

        private GetLocation(ModifyLocation pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            ModifyLocation lMain;
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
            ModifyLocation lMain;

            if (pOutput != null){
                lMain = mRefMain.get();
                if (lMain != null){
                    lMain.sProcessResponse(pOutput.xResult(), pOutput.xText(), pOutput.xReplyJ(), false);
                }
            }
        }
    }

    private static class SetLocation extends AsyncTask<Void, Void, RestAPI.RestResult> {
        private WeakReference<ModifyLocation> mRefMain;

        private SetLocation(ModifyLocation pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            ModifyLocation lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            JSONObject lLocation;
            JSONObject lAction;
            String lActionS;

            lMain = mRefMain.get();
            if (lMain == null){
                return null;
            } else {
                lLocation = new JSONObject();
                lAction = new JSONObject();
                try {
                    lLocation.put(Setting.cLongitude, String.valueOf(lMain.mLongitude));
                    lLocation.put(Setting.cLattitude, String.valueOf(lMain.mLattitude));
                    lAction.put(Setting.cLocation, lLocation);
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
            ModifyLocation lMain;
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
