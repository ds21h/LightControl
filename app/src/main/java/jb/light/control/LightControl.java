package jb.light.control;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.net.Uri;
import android.widget.Toast;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LightControl extends Activity {
    private static final String cServerName = "ServerName";

    private static final int cRequestLocation = 1;

    private final Context mContext = this;

    private final static DateTimeFormatter cFormatTime = DateTimeFormatter.ofPattern("HH:mm");

    private ListView mLstSwitch;
    private MainSwitchListAdapter mListAdapter;
    private TextView mTxtSunset;
    private TextView mLbReading;
    private TextView mTxtReading;
    private TextView mTxtLightsOff;
    private CheckBox mChkAll;
    private ImageButton mIbtnSelectOn;
    private ImageButton mIbtnSelectOff;

    private Data mData;
    private ResStatus mResStatus;

    private String mServerName = "";

    private ControlRunnable mControlRunnable;

    Handler mUpdateHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message pMessage) {
            if ((pMessage.what & HandlerCode.cControl) != 0){
                if ((pMessage.what & HandlerCode.cSurveyChange) != 0){
                    if (mResStatus.xSunset == null){
                        mTxtSunset.setText("");
                    } else {
                        mTxtSunset.setText(mResStatus.xSunset.format(cFormatTime));
                    }
                    if (mResStatus.xLightOff == null){
                        mTxtLightsOff.setText("");
                    } else {
                        mTxtLightsOff.setText(mResStatus.xLightOff.format(cFormatTime));
                    }
                    if (mResStatus.xFase == 2) {
                        mTxtReading.setText(String.valueOf(mResStatus.xLightReading));
                        mLbReading.setVisibility(View.VISIBLE);
                        mTxtReading.setVisibility(View.VISIBLE);
                    } else {
                        mLbReading.setVisibility(View.INVISIBLE);
                        mTxtReading.setVisibility(View.INVISIBLE);
                        mTxtReading.setText("");
                    }
                }
                if ((pMessage.what & (HandlerCode.cSwitchChange | HandlerCode.cStatusChange)) != 0) {
                    mListAdapter.clear();
                    mListAdapter.addAll(mResStatus.xSwitches);
                }
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light_control_layout);

        mTxtSunset = findViewById(R.id.txtSunset);
        mLbReading = findViewById(R.id.lbReading);
        mTxtReading = findViewById(R.id.txtReading);
        mTxtLightsOff = findViewById(R.id.txtLightOff);
        mChkAll = findViewById(R.id.chkAll);
        mLstSwitch = findViewById(R.id.lstSwitches);
        mIbtnSelectOn = findViewById(R.id.ibtnSelectOn);
        mIbtnSelectOff = findViewById(R.id.ibtnSelectOff);

        mTxtLightsOff.setOnClickListener(pView -> sSetTimeLightOff());
        mChkAll.setOnClickListener(v -> sSelectAll());
        mIbtnSelectOn.setOnClickListener(v -> sSetSelect(true));
        mIbtnSelectOff.setOnClickListener(v -> sSetSelect(false));

        mTxtSunset.setText("");
        mLbReading.setVisibility(View.INVISIBLE);
        mTxtReading.setVisibility(View.INVISIBLE);
        mTxtReading.setText("");
        mTxtLightsOff.setText("");
        mListAdapter = new MainSwitchListAdapter(LightControl.this, R.layout.main_switch_list_item, new ArrayList<>());
        mLstSwitch.setAdapter(mListAdapter);

        mData = Data.getInstance(mContext);

        if (savedInstanceState == null) {
            sCheckPermissions();
            mServerName = sGetServerName();
        } else {
            mServerName = savedInstanceState.getString(cServerName);
        }
        mResStatus = new ResStatus(mContext, mServerName);
        mListAdapter.addAll(mResStatus.xSwitches);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(cServerName, mServerName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        super.onCreateOptionsMenu(pMenu);
        getMenuInflater().inflate(R.menu.light_control_menu, pMenu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu pMenu) {
        super.onPrepareOptionsMenu(pMenu);

        MenuItem lManageSwitches;
        MenuItem lManageSettings;
        boolean lManageEnabled;

        lManageSettings = pMenu.findItem(R.id.action_settings);
        lManageSwitches = pMenu.findItem(R.id.action_schakelaars);
        if (mResStatus.xServer == null) {
            lManageEnabled = false;
        } else {
            lManageEnabled = mResStatus.xServer.xManager();
        }
        lManageSettings.setEnabled(lManageEnabled);
        lManageSwitches.setEnabled(lManageEnabled);
        return true;
    }

    @Override
    protected void onActivityResult(int pRequest, int pResult, Intent pInt) {
        Bundle lBundle;

        if (pRequest == 1) {
            if (pResult == RESULT_OK) {
                lBundle = pInt.getExtras();
                if (lBundle != null) {
                    mServerName = lBundle.getString(SelectServer.cServerName);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mControlRunnable = new ControlRunnable(mUpdateHandler, mResStatus);
        LightControlApp.getInstance().xExecutor.execute(mControlRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        mControlRunnable.xStop();
        mUpdateHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int pRequest, @NonNull String[] permissions, @NonNull int[] pResults) {
        switch (pRequest) {
            case cRequestLocation: {
                // If request is cancelled, the result arrays are empty.
                if (pResults.length > 0 && pResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sGetServerName();
                } else {
                    Toast.makeText(mContext, R.string.msg_Insuff_perm, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void sCheckPermissions() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    cRequestLocation);
        }
    }

    private String sGetServerName() {
        ConnectivityManager lConnect;
        NetworkInfo lNet;
        WifiManager lWifi;
        WifiInfo lInfo;
        String lSSId;
        boolean lConnected;
        List<Server> lServerList;
        String lServerName;

        lServerName = "";
        lConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (lConnect != null) {
            lNet = lConnect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (lNet != null){
                lConnected = lNet.isConnected();
                if (lConnected) {
                    lWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    if (lWifi != null) {
                        lInfo = lWifi.getConnectionInfo();
                        lSSId = lInfo.getSSID();
                        if (lSSId.startsWith("\"")) {
                            lSSId = lSSId.substring(1, lSSId.length() - 1);
                        }

                        lServerList = mData.xServers(lSSId);
                        if (lServerList.size() < 1) {
                            Toast.makeText(mContext, R.string.msg_nolightserver, Toast.LENGTH_SHORT).show();
                        } else {
                            lServerName = lServerList.get(0).xName();
                            if (lServerList.size() > 1) {
                                sSelectServer(lServerList);
                            }
                        }
                    } else {
                        Toast.makeText(mContext, R.string.msg_nonetwork, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, R.string.msg_nonetwork, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(mContext, R.string.msg_nonetwork, Toast.LENGTH_SHORT).show();
        }
        return lServerName;
    }

    private void sSelectServer(List<Server> pServers) {
        Intent lInt;
        Bundle lBundle;
        ArrayList<String> lServers;
        int lCount;

        lServers = new ArrayList<>();
        for (lCount = 0; lCount < pServers.size(); lCount++) {
            lServers.add(pServers.get(lCount).xName());
        }
        lBundle = new Bundle();
        lBundle.putStringArrayList(SelectServer.cServers, lServers);

        lInt = new Intent();
        lInt.setClass(this, SelectServer.class);
        lInt.putExtras(lBundle);
        startActivityForResult(lInt, 1);
    }

    public void sManageServers(MenuItem pItem) {
        Uri lUri;
        Intent lInt;

        lUri = Uri.parse("/servers");
        lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ManageServers.class);
        startActivity(lInt);
    }

    public void sManageSwitches(MenuItem pItem) {
        Uri lUri;
        Intent lInt;
        Bundle lBundle;

        if (mResStatus.xServer != null) {
            lUri = Uri.parse("/switches");
            lBundle = new Bundle();
            lBundle.putString(ManageSwitches.cServerName, mResStatus.xServer.xName());
            lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ManageSwitches.class);
            lInt.putExtras(lBundle);
            startActivity(lInt);
        }
    }

    public void sManageSettings(MenuItem pItem) {
        Uri lUri;
        Intent lInt;
        Bundle lBundle;

        if (mResStatus.xServer != null) {
            lUri = Uri.parse("/settings");
            lBundle = new Bundle();
            lBundle.putString(ManageSettings.cServerName, mResStatus.xServer.xName());
            lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ManageSettings.class);
            lInt.putExtras(lBundle);
            startActivity(lInt);
        }
    }

    private void sSelectAll() {
        boolean lSelect;
        int lNumberRow;
        int lCount;
        View lView;
        MainSwitchListAdapter.SwitchItemHandle lHandle;

        lSelect = mChkAll.isChecked();

        lNumberRow = mLstSwitch.getChildCount();
        for (lCount = 0; lCount < lNumberRow; lCount++) {
            lView = mLstSwitch.getChildAt(lCount);
            lHandle = (MainSwitchListAdapter.SwitchItemHandle) lView.getTag();
            if (lHandle.xSwitch.xActive()) {
                lHandle.xChkSelect.setChecked(lSelect);
            }
        }
    }

    public void sModifySelection(View pView) {
        int lNumberRow;
        int lCount;
        int lCheck;
        int lActive;
        View lView;
        MainSwitchListAdapter.SwitchItemHandle lHandle;

        lNumberRow = mLstSwitch.getChildCount();
        lCheck = 0;
        lActive = 0;
        for (lCount = 0; lCount < lNumberRow; lCount++) {
            lView = mLstSwitch.getChildAt(lCount);
            lHandle = (MainSwitchListAdapter.SwitchItemHandle) lView.getTag();
            if (lHandle.xChkSelect.isChecked()) {
                lCheck++;
            }
            if (lHandle.xSwitch.xActive()) {
                lActive++;
            }
        }
        mChkAll.setChecked(lCheck >= lActive);
    }

    private void sSetSelect(boolean pAan) {
        int lNumberRow;
        int lCount;
        View lView;
        MainSwitchListAdapter.SwitchItemHandle lHandle;

        lNumberRow = mLstSwitch.getChildCount();
        for (lCount = 0; lCount < lNumberRow; lCount++) {
            lView = mLstSwitch.getChildAt(lCount);
            lHandle = (MainSwitchListAdapter.SwitchItemHandle) lView.getTag();
            if (lHandle.xSwitch.xActive()) {
                if (lHandle.xChkSelect.isChecked()) {
                    lHandle.xChkSelect.setChecked(false);
                    if (pAan) {
                        lHandle.xSwitch.xAction(SwitchLocal.ActionOn);
                    } else {
                        lHandle.xSwitch.xAction(SwitchLocal.ActionOff);
                    }
                }
            }
        }
        mChkAll.setChecked(false);
    }

    private void sSetTimeLightOff() {
        int lHour;
        int lMinute;

        if (mResStatus.xLightOff != null) {
            lHour = mResStatus.xLightOff.getHour();
            lMinute = mResStatus.xLightOff.getMinute();
            TimePickerDialog lPicker = new TimePickerDialog(this, (view, pHour, pMinute) -> {
                Action lAction;
                ZonedDateTime lLightOff;

                lLightOff = ZonedDateTime.from(mResStatus.xLightOff);
                if (lHour != pHour){
                    lLightOff = lLightOff.withHour(pHour);
                }
                if (lMinute != pMinute){
                    lLightOff = lLightOff.withMinute(pMinute);
                }
                if (lHour < 10){
                    if (pHour >= 10){
                        lLightOff = lLightOff.minusDays(1);
                    }
                } else {
                    if (pHour < 10){
                        lLightOff = lLightOff.plusDays(1);
                    }
                }

                lAction = new Action(Action.ActionLightsOff);
                lAction.xValue = lLightOff.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
                lAction.xIP = mResStatus.xServer.xAddress();
                LightControlApp.getInstance().xExecutor.execute(new ServerActionRunnable(lAction));
            }, lHour, lMinute, true);
            lPicker.show();
        }
    }
}
