package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by Jan on 26-11-2015.
 */
public class ManageSettings extends Activity {
    private final Context mContext = this;

    public static final String cServerName = "ServerName";

    private String mServerName;

    private Data mData;
    private Server mServer;

    private ListView mList;
    private SettingListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_settings_layout);

        Intent lInt;
        Bundle lBundle;

        mData = Data.getInstance(mContext);

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            mServerName = lBundle.getString(cServerName, "");
        } else {
            mServerName = savedInstanceState.getString(cServerName);
        }
        mServer = mData.xServer(mServerName);

        mList = (ListView) findViewById(R.id.lstSettings);

        mListAdapter = new SettingListAdapter(this, R.layout.manage_setting_list_item, new ArrayList<SettingItem>());
        mList.setAdapter(mListAdapter);
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putString(cServerName, mServerName);
    }


    @Override
    public void onResume(){
        super.onResume();

        new FetchSettings().execute();
    }

    public void sModifySetting(View pView){
        Intent lInt;
        SettingItem lItem;
        Uri lUri;
        Bundle lBundle;

        lItem = (SettingItem) pView.getTag();
        switch (lItem.xType()){
            case SettingItem.TypeLocation:
                lUri = Uri.parse("/settings/location");
                lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifyLocation.class);
                lBundle = new Bundle();
                lBundle.putString(ManageSettings.cServerName, mServer.xName());
                lInt.putExtras(lBundle);
                startActivity(lInt);
                break;
            case SettingItem.TypeLightsOff:
                lUri = Uri.parse("/settings/lightsoff");
                lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifyLightOff.class);
                lBundle = new Bundle();
                lBundle.putString(ManageSettings.cServerName, mServer.xName());
                lInt.putExtras(lBundle);
                startActivity(lInt);
                break;
            case SettingItem.TypeSensor:
                lUri = Uri.parse("/settings/sensor");
                lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifySensor.class);
                lBundle = new Bundle();
                lBundle.putString(ManageSettings.cServerName, mServer.xName());
                lInt.putExtras(lBundle);
                startActivity(lInt);
                break;
            default:
                Toast.makeText(mContext, R.string.msg_unknown_group, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private class FetchSettings extends AsyncTask<Void, Void, RestAPI.RestResult> {

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
            Setting lSettings;
            SettingItem lItem;

            switch (pOutput.xResult()){
                case Result.cResultOK:
                    lResult = pOutput.xReplyJ();
                    mListAdapter.clear();
                    lSettings = new Setting(lResult);
//                      Location
                    lItem = new SettingItem();
                    lItem.xType(SettingItem.TypeLocation);
                    lItem.xAttr1(String.valueOf(lSettings.xLongitude()));
                    lItem.xAttr2(String.valueOf(lSettings.xLattitude()));
                    mListAdapter.add(lItem);
//                      Lights off
                    lItem = new SettingItem();
                    lItem.xType(SettingItem.TypeLightsOff);
                    lItem.xAttr1(lSettings.xLightOff());
                    lItem.xAttr2(String.valueOf(lSettings.xLightOffPeriod()));
                    mListAdapter.add(lItem);
//                      Sensor
                    lItem = new SettingItem();
                    lItem.xType(SettingItem.TypeSensor);
                    lItem.xAttr1(String.valueOf(lSettings.xSensorLimit()));
                    lItem.xAttr2(String.valueOf(lSettings.xMaxSensor()));
                    lItem.xAttr3(String.valueOf(lSettings.xPeriod()));
                    lItem.xAttr4(String.valueOf(lSettings.xPeriodDark()));
                    mListAdapter.add(lItem);
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
