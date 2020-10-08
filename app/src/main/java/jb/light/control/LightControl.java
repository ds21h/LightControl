package jb.light.control;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class LightControl extends Activity {
    private static final String cServerName = "ServerName";

    private static final int cRequestLocation = 1;

    private final Context mContext = this;

    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat mFormatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat mFormatTime = new SimpleDateFormat("HH:mm");
    private Date mLightsOff = null;
    private String mSunset = "";
    private int mHourLightsOff = -1;
    private int mMinuteLightsOff = -1;

    private ListView mLstSwitch;
    private MainSwitchListAdapter mListAdapter;
    private TextView mTxtSunset;
    private TextView mLbReading;
    private TextView mTxtReading;
    private TextView mTxtLightsOff;
    private CheckBox mChkAll;

    private Server mServer;
    private Data mData;

    private String mServerName = "";

    Handler mRefreshHandler = new Handler();
    Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            int lNumberRow;
            int lCount;
            View lView;
            MainSwitchListAdapter.SwitchItemHandle lHandle;
            Action lAction;

            if (mServer != null) {
                new GetOverview(LightControl.this).execute();
            }
            lNumberRow = mLstSwitch.getChildCount();
            for (lCount = 0; lCount < lNumberRow; lCount++) {
                lView = mLstSwitch.getChildAt(lCount);
                lHandle = (MainSwitchListAdapter.SwitchItemHandle) lView.getTag();
                if (lHandle.xSwitch.xActive()) {
                    if (lHandle.xSwitch.xType().equals("esp")) {
                        lAction = new Action(Action.ActionInqSwitch);
                        lAction.xValue(lHandle.xSwitch.xName());
                        lAction.xIP(lHandle.xSwitch.xIP());
                        new EspSwitch(LightControl.this).execute(lAction);
                    }
                }
            }
            mRefreshHandler.postDelayed(this, 10000);
        }
    };

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

        mTxtSunset.setText("");
        mLbReading.setVisibility(View.INVISIBLE);
        mTxtReading.setVisibility(View.INVISIBLE);
        mTxtReading.setText("");
        mTxtLightsOff.setText("");
        mListAdapter = new MainSwitchListAdapter(LightControl.this, R.layout.main_switch_list_item, new ArrayList<Switch>());
        mLstSwitch.setAdapter(mListAdapter);

        mData = Data.getInstance(mContext);

        if (savedInstanceState == null) {
            sCheckPermissions();
            mServerName = sGetServerName();
        } else {
            mServerName = savedInstanceState.getString(cServerName);
        }
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
        if (mServer == null) {
            lManageEnabled = false;
        } else {
            lManageEnabled = mServer.xManager();
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
    public void onResume() {
        super.onResume();

        List<Switch> lSwitches;

        mServer = mData.xServer(mServerName);
        invalidateOptionsMenu();
        if (mServer != null) {
            lSwitches = mData.xSwitches(mServer.xName());
            mListAdapter.clear();
            mListAdapter.addAll(lSwitches);
            mRefreshHandler.postDelayed(mRefreshRunnable, 100);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mRefreshHandler.removeCallbacks(mRefreshRunnable);
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

        lServerName = "";        lConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (lConnect != null) {
            lNet = lConnect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
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

        if (mServer != null) {
            lUri = Uri.parse("/switches");
            lBundle = new Bundle();
            lBundle.putString(ManageSwitches.cServerName, mServer.xName());
            lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ManageSwitches.class);
            lInt.putExtras(lBundle);
            startActivity(lInt);
        }
    }

    public void sManageSettings(MenuItem pItem) {
        Uri lUri;
        Intent lInt;
        Bundle lBundle;

        if (mServer != null) {
            lUri = Uri.parse("/settings");
            lBundle = new Bundle();
            lBundle.putString(ManageSettings.cServerName, mServer.xName());
            lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ManageSettings.class);
            lInt.putExtras(lBundle);
            startActivity(lInt);
        }
    }

    private void sProcessOverview(int pResult, String pMessage, JSONObject pOverview) {
        String lLightOff = "";
        int lFase = 0;
        int lReading = 0;
        Date lSunsetDate;
        String lSunset = "";
        List<Switch> lSwitches;
        JSONArray lSwitchesJ;
        int lCount;
        JSONObject lSwitchJ;
        Switch lSwitch;
        boolean lSaveResult;

        switch (pResult) {
            case Result.cResultOK:
                try {
                    mSunset = pOverview.optString("sunset", "");
                    if (mSunset.equals("")) {
                        Toast.makeText(mContext, "Parse-error sunset", Toast.LENGTH_SHORT).show();
                    } else {
                        lLightOff = pOverview.optString("lightoff", "");
                        lFase = pOverview.optInt("fase", 0);
                        lReading = pOverview.optInt("lightreading", 0);
                        try {
                            lSunsetDate = mFormatDate.parse(mSunset);
                            mLightsOff = mFormatDate.parse(lLightOff);
                            lSunset = mFormatTime.format(lSunsetDate);
                            lLightOff = mFormatTime.format(mLightsOff);
                            mHourLightsOff = -1;
                            mMinuteLightsOff = -1;
                        } catch (ParseException pExc) {
                            lLightOff = "";
                            mSunset = "";
                            mLightsOff = null;
                            Toast.makeText(mContext, "Parse-error sunset", Toast.LENGTH_SHORT).show();
                        }
                        lSwitches = new ArrayList<>();
                        lSwitchesJ = pOverview.getJSONArray("switches");
                        for (lCount = 0; lCount < lSwitchesJ.length(); lCount++) {
                            lSwitchJ = lSwitchesJ.getJSONObject(lCount);
                            lSwitch = new Switch(lSwitchJ);
                            lSwitches.add(lSwitch);
                        }
                        lSaveResult = mData.xSaveSwitches(lSwitches, mServer.xName());
                        if (lSaveResult) {
                            lSwitches = mData.xSwitches(mServer.xName());
                            mListAdapter.clear();
                            mListAdapter.addAll(lSwitches);
                        }
                    }
                } catch (JSONException pExc) {
                    Toast.makeText(mContext, "JSON error: " + pExc.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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

        mTxtSunset.setText(lSunset);
        mTxtLightsOff.setText(lLightOff);
        if (lFase == 2) {
            mTxtReading.setText(String.valueOf(lReading));
            mLbReading.setVisibility(View.VISIBLE);
            mTxtReading.setVisibility(View.VISIBLE);
        } else {
            mLbReading.setVisibility(View.INVISIBLE);
            mTxtReading.setVisibility(View.INVISIBLE);
            mTxtReading.setText("");
        }
    }

    private void sSetSwitchStatus(Action pAction, JSONObject pSwitchStatus) {
        String lStatus;
        int lNumberRow;
        int lCount;
        View lView;
        MainSwitchListAdapter.SwitchItemHandle lHandle = null;
        boolean lFound = false;

        if (pAction != null){
            lNumberRow = mLstSwitch.getChildCount();
            for (lCount = 0; lCount < lNumberRow; lCount++) {
                lView = mLstSwitch.getChildAt(lCount);
                lHandle = (MainSwitchListAdapter.SwitchItemHandle) lView.getTag();
                if (lHandle.xSwitch.xActive()) {
                    if (lHandle.xSwitch.xName().equals(pAction.xValue())) {
                        lFound = true;
                        break;
                    }
                }
            }
            if (lFound) {
                if (pSwitchStatus == null){
                    lHandle.xImgStatus.setImageResource(R.mipmap.question);
                } else {
                    lStatus = pSwitchStatus.optString("status", "");
                    if (lStatus.equals("on")) {
                        lHandle.xImgStatus.setImageResource(R.mipmap.light_on);
                    } else {
                        if (lStatus.equals("off")) {
                            lHandle.xImgStatus.setImageResource(R.mipmap.light_off);
                        } else {
                            lHandle.xImgStatus.setImageResource(R.mipmap.question);
                        }
                    }
                }
            }
        }
    }

    public void sSelectAll(View pView) {
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
        if (lCheck < lActive) {
            mChkAll.setChecked(false);
        } else {
            mChkAll.setChecked(true);
        }
    }

    public void sSelectOn(View pView) {
        sSetSelect(true);
    }

    public void sSelectOff(View pView) {
        sSetSelect(false);
    }

    private void sSetSelect(boolean pAan) {
        int lNumberRow;
        int lCount;
        View lView;
        MainSwitchListAdapter.SwitchItemHandle lHandle;
        Action lAction;

        lNumberRow = mLstSwitch.getChildCount();
        for (lCount = 0; lCount < lNumberRow; lCount++) {
            lView = mLstSwitch.getChildAt(lCount);
            lHandle = (MainSwitchListAdapter.SwitchItemHandle) lView.getTag();
            if (lHandle.xSwitch.xActive()) {
                if (lHandle.xChkSelect.isChecked()) {
                    lHandle.xChkSelect.setChecked(false);
                    if (pAan) {
                        lAction = new Action(Action.ActionSwitchOn);
                    } else {
                        lAction = new Action(Action.ActionSwitchOff);
                    }
                    lAction.xValue(lHandle.xSwitch.xName());
                    if (lHandle.xSwitch.xType().equals("esp")) {
                        lAction.xIP(lHandle.xSwitch.xIP());
                        new EspSwitch(this).execute(lAction);
                    } else {
                        lAction.xIP("");
                        new SetAction(this).execute(lAction);
                    }
                }
            }
        }
        mChkAll.setChecked(false);
    }

    public void sSetTimeLightOff(View pView) {
        final GregorianCalendar lLightOff;
        int lHour;
        int lMinute;

        if (mLightsOff != null) {
            if (mHourLightsOff < 0) {
                lLightOff = new GregorianCalendar();
                lLightOff.setTime(mLightsOff);
                lHour = lLightOff.get(Calendar.HOUR_OF_DAY);
                lMinute = lLightOff.get(Calendar.MINUTE);
            } else {
                lHour = mHourLightsOff;
                lMinute = mMinuteLightsOff;
            }
            TimePickerDialog lPicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int pHour, int pMinute) {
                    String lLightOffS;
                    Action lAction;

                    mHourLightsOff = pHour;
                    mMinuteLightsOff = pMinute;

                    lLightOffS = sMakeDateTime(pHour, pMinute);

                    lAction = new Action(Action.ActionLightsOff);
                    lAction.xValue(lLightOffS);
                    new SetAction(LightControl.this).execute(lAction);
                }
            }, lHour, lMinute, true);
            lPicker.show();
        }
    }

    private String sMakeDateTime(int pHour, int pMinute) {
        GregorianCalendar lLightOff;
        Date lSunset;
        String lResult;
        ParsePosition lPos;
        int lStartZone;
        String lZone;

        lPos = new ParsePosition(0);
        lSunset = mFormatDate.parse(mSunset, lPos);
        lStartZone = lPos.getIndex();
        if (lStartZone > 0) {
            if (lStartZone < mSunset.length()) {
                lZone = mSunset.substring(lStartZone);
            } else {
                lZone = "";
            }
            lLightOff = new GregorianCalendar();
            lLightOff.setLenient(true);
            lLightOff.setTime(lSunset);

            lLightOff.set(Calendar.HOUR_OF_DAY, pHour);
            lLightOff.set(Calendar.MINUTE, pMinute);
            if (pHour < 10) {
                lLightOff.add(Calendar.DAY_OF_MONTH, 1);
            }
            lResult = mFormatDate.format(lLightOff.getTime()) + lZone;
        } else {
            lResult = "";
        }
        return lResult;
    }

    private static class GetOverview extends AsyncTask<Void, Void, RestAPI.RestResult> {
        private WeakReference<LightControl> mRefMain;

        private GetOverview(LightControl pMain) {
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            LightControl lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;

            lMain = mRefMain.get();
            if (lMain == null) {
                return null;
            } else {
                lRequest = lMain.mServer.xAddress() + URIs.UriServerSurvey;
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
            LightControl lMain;

            lMain = mRefMain.get();
            if (lMain != null) {
                lMain.sProcessOverview(pOutput.xResult(), pOutput.xText(), pOutput.xReplyJ());
            }
        }
    }

    private static class SetAction extends AsyncTask<Action, Void, RestAPI.RestResult> {
        private WeakReference<LightControl> mRefMain;

        private SetAction(LightControl pMain) {
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Action... pActions) {
            LightControl lMain;
            String lRequest;
            RestAPI.RestResult lOutput = null;
            RestAPI lRestAPI;
            Action lAction;
            String lCommand;

            if (pActions.length > 0) {
                lMain = mRefMain.get();
                if (lMain != null) {
                    lAction = pActions[0];
                    lCommand = "";
                    switch (lAction.xAction()) {
                        case Action.ActionSwitchOn:
                            lCommand = "switchon=" + lAction.xValue();
                            break;
                        case Action.ActionSwitchOff:
                            lCommand = "switchoff=" + lAction.xValue();
                            break;
                        case Action.ActionLightsOff:
                            lCommand = "lightoff=" + lAction.xValue();
                            break;
                    }
                    lRequest = lMain.mServer.xAddress() + URIs.UriServerAction;
                    lRestAPI = new RestAPI();
                    lRestAPI.xMethod(RestAPI.cMethodPut);
                    lRestAPI.xMediaRequest(RestAPI.cMediaText);
                    lRestAPI.xMediaReply(RestAPI.cMediaJSON);
                    lRestAPI.xUrl(lRequest);
                    lRestAPI.xAction(lCommand);
                    lOutput = lRestAPI.xCallApi();
                }
            }
            return lOutput;
        }
    }

    private static class EspSwitch extends AsyncTask<Action, Void, RestAPI.RestResult> {
       private WeakReference<LightControl> mRefMain;
       private Action mAction;

        private EspSwitch(LightControl pMain) {
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Action... pActions) {
            LightControl lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            JSONObject lCommand;
            String lCommandS;

            lMain = mRefMain.get();
            if (lMain == null) {
                lOutput = null;
                mAction = null;
            } else {
                if (pActions.length > 0) {
                    mAction = pActions[0];
                    lCommand = new JSONObject();
                    lRestAPI = new RestAPI();
                    lRequest = "http://" + mAction.xIP() + URIs.UriSwitch;
                    lRestAPI.xUrl(lRequest);
                    try {
                        switch (mAction.xAction()) {
                            case Action.ActionSwitchOn: {
                                lCommand.put("status", "on");
                                lCommandS = lCommand.toString();
                                lRestAPI.xMethod(RestAPI.cMethodPut);
                                lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
                                break;
                            }
                            case Action.ActionSwitchOff: {
                                lCommand.put("status", "off");
                                lCommandS = lCommand.toString();
                                lRestAPI.xMethod(RestAPI.cMethodPut);
                                lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
                                break;
                            }
                            case Action.ActionInqSwitch: {
                                lCommandS = "";
                                lRestAPI.xMethod(RestAPI.cMethodGet);
                                lRestAPI.xTimeOut(2000);
                                break;
                            }
                            default: {
                                lCommandS = "";
                                break;
                            }
                        }
                    } catch (JSONException pExc) {
                        lCommandS = "";
                        lRestAPI.xMethod(RestAPI.cMethodGet);
                    }
                    lRestAPI.xMediaReply(RestAPI.cMediaJSON);
                    lRestAPI.xAction(lCommandS);
                    lOutput = lRestAPI.xCallApi();
                } else {
                    lOutput = null;
                    mAction = null;
                }
            }
            return lOutput;
        }

        protected void onPostExecute(RestAPI.RestResult pOutput) {
            LightControl lMain;

            lMain = mRefMain.get();
            if (lMain != null) {
                if (pOutput != null) {
                    lMain.sSetSwitchStatus(mAction, pOutput.xReplyJ());
                }
            }
        }
    }
}
