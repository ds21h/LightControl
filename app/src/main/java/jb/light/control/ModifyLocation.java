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

        mEdtLongitude = (EditText)findViewById(R.id.edtLongitude);
        mEdtLattitude = (EditText)findViewById(R.id.edtLattitude);

        mData = Data.getInstance(mContext);

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            mServerName = lBundle.getString(cServerName, "");
            mLongitude = 0.0;
            mLattitude = 0.0;

        } else {
            mServerName = savedInstanceState.getString(cServerName);
            mLongitude = savedInstanceState.getDouble(cLongitude);
            mLattitude = savedInstanceState.getDouble(cLattitude);
        }
        sFillScreen();
        mServer = mData.xServer(mServerName);

        new GetLocation().execute();
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

    private void sFillScreen(){
        mEdtLongitude.setText(String.valueOf(mLongitude));
        mEdtLattitude.setText(String.valueOf(mLattitude));
    }

    public void sProcess(MenuItem pMenu){
        sReadScreen();
        new SetLocation().execute();
    }

    public void sRefresh(MenuItem pMenu){

    }

    public void sDelete(MenuItem pMenu){

    }

    private class GetLocation extends AsyncTask<Void, Void, RestAPI.RestResult> {

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

                    mLongitude = lSetting.xLongitude();
                    mLattitude = lSetting.xLattitude();
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

    private class SetLocation extends AsyncTask<Void, Void, RestAPI.RestResult> {

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            JSONObject lLocation;
            JSONObject lAction;

            lLocation = new JSONObject();
            lAction = new JSONObject();
            try {
                lLocation.put(Setting.cLongitude, String.valueOf(mLongitude));
                lLocation.put(Setting.cLattitude, String.valueOf(mLattitude));
                lAction.put(Setting.cLocation, lLocation);
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

                        mLongitude = lSetting.xLongitude();
                        mLattitude = lSetting.xLattitude();
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
