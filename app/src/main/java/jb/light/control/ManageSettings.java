package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Jan on 26-11-2015.
 */
public class ManageSettings extends Activity {
    private final Context mContext = this;

    static final String cServerName = "ServerName";

    private String mServerName;

    private Data mData;
    private Server mServer;
    private Setting mSetting;

    private ListView mList;
    private SettingListAdapter mListAdapter;

    Handler mSettingHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message pMessage) {
            SettingItem lItem;

            if ((pMessage.what & GetSettingRunnable.cSettingRetrieved) != 0){
                mListAdapter.clear();
//                      Location
                lItem = new SettingItem();
                lItem.xType(SettingItem.TypeLocation);
                lItem.xAttr1(String.valueOf(mSetting.xLongitude()));
                lItem.xAttr2(String.valueOf(mSetting.xLattitude()));
                mListAdapter.add(lItem);
//                      Lights off
                lItem = new SettingItem();
                lItem.xType(SettingItem.TypeLightsOff);
                lItem.xAttr1(mSetting.xLightOff());
                lItem.xAttr2(String.valueOf(mSetting.xLightOffPeriod()));
                mListAdapter.add(lItem);
//                      Sensor
                lItem = new SettingItem();
                lItem.xType(SettingItem.TypeSensor);
                lItem.xAttr1(String.valueOf(mSetting.xSensorLimit()));
                lItem.xAttr2(String.valueOf(mSetting.xPeriodSec()));
                lItem.xAttr3(String.valueOf(mSetting.xPeriodDark()));
                mListAdapter.add(lItem);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_settings_layout);

        Intent lInt;
        Bundle lBundle;

        mData = Data.getInstance(mContext);
        mSetting = new Setting();

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            mServerName = lBundle.getString(cServerName, "");
        } else {
            mServerName = savedInstanceState.getString(cServerName);
        }
        mServer = mData.xServer(mServerName);

        mList = findViewById(R.id.lstSettings);

        mListAdapter = new SettingListAdapter(this, R.layout.manage_setting_list_item, new ArrayList<SettingItem>());
        mList.setAdapter(mListAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View pView, int position, long id) {
                SettingListAdapter.SettingListHandle lHandle;

                lHandle = (SettingListAdapter.SettingListHandle)pView.getTag();
                sModifySetting(lHandle.xItem.xType());
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putString(cServerName, mServerName);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        GetSettingRunnable lGetSettingRunnable;

        super.onStart();
        lGetSettingRunnable = new GetSettingRunnable(mSettingHandler, mSetting, mServer.xAddress());
        LightControlApp.getInstance().xExecutor.execute(lGetSettingRunnable);
    }

    @Override
    public void onResume(){
        super.onResume();

//        new FetchSettings(this).execute();
    }

    @Override
    protected void onStop() {
        mSettingHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    private void sModifySetting(String pItemType){
        Intent lInt;
        Uri lUri;
        Bundle lBundle;

        lBundle = new Bundle();
        lBundle.putString(ManageSettings.cServerName, mServer.xName());
        lBundle.putString("Setting", mSetting.xSetting().toString());
        switch (pItemType){
            case SettingItem.TypeLocation:
                lUri = Uri.parse("/settings/location");
                lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifyLocation.class);
                lInt.putExtras(lBundle);
                startActivity(lInt);
                break;
            case SettingItem.TypeLightsOff:
                lUri = Uri.parse("/settings/lightsoff");
                lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifyLightOff.class);
                lInt.putExtras(lBundle);
                startActivity(lInt);
                break;
            case SettingItem.TypeSensor:
                lUri = Uri.parse("/settings/sensor");
                lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifySensor.class);
                lInt.putExtras(lBundle);
                startActivity(lInt);
                break;
            default:
                Toast.makeText(mContext, R.string.msg_unknown_group, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
