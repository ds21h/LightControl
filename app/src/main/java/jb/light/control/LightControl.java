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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class LightControl extends Activity {
    private static final String cSwitchesInit = "SwitchesInit";
    private static final String cServerName = "ServerName";
    private static final String cActions = "Actions";
    private static final String cActionReady = "ActionReady";
    private static final String cCurrentAction = "CurrentAction";
    private static final String cActionsActive = "ActionsActive";
    private static final String cNumberCorrect = "NumberCorrect";
    private static final String cNumberIncorrect = "NumberIncorrect";

    private static final int cRequestLocation = 1;

    private final Context mContext = this;

    private final static SimpleDateFormat mFormatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final static SimpleDateFormat mFormatTime = new SimpleDateFormat("HH:mm");
    private Date mLightsOff = null;
    private String mSunset = "";
    private int mHourLightsOff = -1;
    private int mMinuteLightsOff = -1;

    private ListView mList;
    private MainSwitchListAdapter mListAdapter;
    private TextView mTxtSunset;
    private TextView mLbReading;
    private TextView mTxtReading;
    private TextView mTxtLightsOff;
    private CheckBox mChkAll;

    private Server mServer;
    private Data mData;

    private boolean mServerReply = false;
    private String mServerName;

    private boolean mSwitchesInit;

    ArrayList<Action> mActions;
    private int mCurrentAction;
    private boolean mActionsActive;
    private int mNumberCorrect;
    private int mNumberIncorrect;

    private boolean mActionReady;

    Handler mTimerHandler = new Handler();
    Runnable mTimerRunnable = new Runnable() {

        @Override
        public void run() {
            if (mActionReady) {
                sActionStep();
            } else {
                mTimerHandler.postDelayed(this, 100);
            }
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
        mList = findViewById(R.id.lstSwitches);

        mTxtSunset.setText("");
        mLbReading.setVisibility(View.INVISIBLE);
        mTxtReading.setVisibility(View.INVISIBLE);
        mTxtReading.setText("");
        mTxtLightsOff.setText("");
        mListAdapter = new MainSwitchListAdapter(LightControl.this, R.layout.main_switch_list_item, new ArrayList<Switch>());
        mList.setAdapter(mListAdapter);

        mData = Data.getInstance(mContext);
//        mData.xInitServers();

        if (savedInstanceState == null) {
            mSwitchesInit = false;
            mServerName = "";
            mActions = new ArrayList<>();
            mActionReady = true;
            mCurrentAction = 0;
            mActionsActive = false;
            mNumberCorrect = 0;
            mNumberIncorrect = 0;
            if (sCheckPermissions()){
                sInitServer();
            }
        } else {
            mSwitchesInit = savedInstanceState.getBoolean(cSwitchesInit);
            mServerName = savedInstanceState.getString(cServerName);
            mActions = savedInstanceState.getParcelableArrayList(cActions);
            mActionReady = savedInstanceState.getBoolean(cActionReady);
            mCurrentAction = savedInstanceState.getInt(cCurrentAction);
            mActionsActive = savedInstanceState.getBoolean(cActionsActive);
            mNumberCorrect = savedInstanceState.getInt(cNumberCorrect);
            mNumberIncorrect = savedInstanceState.getInt(cNumberIncorrect);

            mServer = mData.xServer(mServerName);
        }

        if (mServer != null) {
            new GetOverview().execute();
        }
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        String lName;

        savedInstanceState.putBoolean(cSwitchesInit, mSwitchesInit);
        if (mServer == null) {
            lName = "";
        } else {
            lName = mServer.xName();
        }
        savedInstanceState.putString(cServerName, lName);
        savedInstanceState.putParcelableArrayList(cActions, mActions);
        savedInstanceState.putBoolean(cActionReady, mActionReady);
        savedInstanceState.putInt(cCurrentAction, mCurrentAction);
        savedInstanceState.putBoolean(cActionsActive, mActionsActive);
        savedInstanceState.putInt(cNumberCorrect, mNumberCorrect);
        savedInstanceState.putInt(cNumberIncorrect, mNumberIncorrect);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimerHandler.removeCallbacks(mTimerRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        getMenuInflater().inflate(R.menu.light_control_menu, pMenu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu pMenu) {
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
                mServerName = lBundle.getString(SelectServer.cServerName);
                mServerReply = true;
            }
            if (pResult == RESULT_CANCELED) {
                mServerReply = true;
            }
        }
    }

    @Override
    public void onResume() {
        List<Switch> lSwitches;

        super.onResume();

        mServer = mData.xServer(mServerName);
        if (mServerReply) {
            mServerReply = false;
            invalidateOptionsMenu();
            new GetOverview().execute();
        } else {
            if (mActionsActive) {
                mTimerHandler.postDelayed(mTimerRunnable, 1);
            }
        }
        if (mSwitchesInit) {
            lSwitches = mData.xSwitches(mServer.xName());
            mListAdapter.clear();
            mListAdapter.addAll(lSwitches);
        }
    }

    @Override
    public void onRequestPermissionsResult(int pRequest, String permissions[], int[] pResults) {
        switch (pRequest) {
            case cRequestLocation: {
                // If request is cancelled, the result arrays are empty.
                if (pResults.length > 0 && pResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sInitServer();
                    if (mServer != null) {
                        new GetOverview().execute();
                    }
                } else {
                    Toast.makeText(mContext, R.string.msg_Insuff_perm, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private boolean sCheckPermissions() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    cRequestLocation);
            return false;
        } else {
            return true;
        }
    }

    private void sInitServer() {
        ConnectivityManager lConnect;
        NetworkInfo lNet;
        WifiManager lWifi;
        WifiInfo lInfo;
        String lSSId;
        boolean lConnected;
        List<Server> lServerList;

        lConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        lNet = lConnect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        lConnected = lNet.isConnected();
        if (lConnected) {
            lWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            lInfo = lWifi.getConnectionInfo();
            lSSId = lInfo.getSSID();
            if (lSSId.startsWith("\"")) {
                lSSId = lSSId.substring(1, lSSId.length() - 1);
            }
            lServerList = mData.xServers(lSSId);
            if (lServerList.size() < 1) {
                Toast.makeText(mContext, R.string.msg_nolightserver, Toast.LENGTH_SHORT).show();
                mServer = null;
            } else {
                if (lServerList.size() > 1) {
                    mServer = null;
                    sSelectServer(lServerList);
                } else {
                    mServer = lServerList.get(0);
                    mServerName = mServer.xName();
                }
            }
        } else {
            Toast.makeText(mContext, R.string.msg_nonetwork, Toast.LENGTH_SHORT).show();
            mServer = null;
        }
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

        mServerName = lServers.get(0);
        lInt = new Intent();
        lInt.setClass(this, SelectServer.class);
        lInt.putExtras(lBundle);
        startActivityForResult(lInt, 1);
    }

    public void sRefresh(MenuItem pItem) {
        new GetOverview().execute();
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

        if (mSwitchesInit) {
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

    public void sSelectAll(View pView) {
        boolean lSelect;
        int lNumberRow;
        int lCount;
        View lView;
        MainSwitchListAdapter.SwitchItemHandle lHandle;

        lSelect = mChkAll.isChecked();

        lNumberRow = mList.getChildCount();
        for (lCount = 0; lCount < lNumberRow; lCount++) {
            lView = mList.getChildAt(lCount);
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

        lNumberRow = mList.getChildCount();
        lCheck = 0;
        lActive = 0;
        for (lCount = 0; lCount < lNumberRow; lCount++) {
            lView = mList.getChildAt(lCount);
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

        mActions.clear();

        lNumberRow = mList.getChildCount();
        for (lCount = 0; lCount < lNumberRow; lCount++) {
            lView = mList.getChildAt(lCount);
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
                    } else {
                        lAction.xIP("");
                    }
                    mActions.add(lAction);
                }
            }
        }
        sProcessActions();
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

                    mActions.clear();
                    lAction = new Action(Action.ActionLightsOff);
                    lAction.xValue(lLightOffS);
                    mActions.add(lAction);
                    sProcessActions();
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

    private void sProcessActions() {
        mCurrentAction = -1;
        mNumberCorrect = 0;
        mNumberIncorrect = 0;
        sActionStep();
    }

    private void sActionStep() {
        Action lAction;
        int lCount;
        boolean lActive;
        String lText;

        lActive = false;
        for (lCount = 0; lCount < mActions.size(); lCount++) {
            lAction = mActions.get(lCount);
            if (!lAction.xProcessed()) {
                mCurrentAction = lCount;
                mActionReady = false;
                mActionsActive = true;
                lActive = true;
                mTimerHandler.postDelayed(mTimerRunnable, 100);
                if (lAction.xToServer()) {
                    mNumberCorrect = -1;
                    new SetAction().execute();
                } else {
                    new SetActionIOT().execute();
                }
                break;
            }
        }
        if (!lActive) {
            mActionsActive = false;
            if (mNumberCorrect >= 0) {
                if (mNumberCorrect == 0) {
                    if (mNumberIncorrect == 0) {
                        lText = getString(R.string.msg_noaction);
                    } else {
                        lText = getString(R.string.msg_actionfailed);
                    }
                } else {
                    if (mNumberIncorrect == 0) {
                        lText = getString(R.string.msg_actionsucceed);
                    } else {
                        lText = getString(R.string.msg_actionpartsucceed);
                    }
                }
                Toast.makeText(mContext, lText, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetOverview extends AsyncTask<Void, Void, RestAPI.RestResult> {

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;

            lRequest = mServer.xAddress() + URIs.UriServerSurvey;
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
            String lSunset = "";
            Date lSunsetDate = null;
            String lLightOff = "";
            int lFase = 0;
            int lReading = 0;
            JSONObject lOverview;
            JSONArray lSwitchesJ;
            JSONObject lSwitchJ;
            List<Switch> lSwitches;
            Switch lSwitch;
            int lCount;

            switch (pOutput.xResult()) {
                case Result.cResultOK:
                    try {
                        lOverview = pOutput.xReplyJ();
                        mSunset = lOverview.optString("sunset", "");
                        if (mSunset.equals("")) {
                            lLightOff = "";
                            lFase = 0;
                            lReading = 0;
                            Toast.makeText(mContext, "Parse-error sunset", Toast.LENGTH_SHORT).show();
                        } else {
                            lLightOff = lOverview.optString("lightoff", "");
                            lFase = lOverview.optInt("fase", 0);
                            lReading = lOverview.optInt("lightreading", 0);
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
                            lSwitchesJ = lOverview.getJSONArray("switches");
                            for (lCount = 0; lCount < lSwitchesJ.length(); lCount++) {
                                lSwitchJ = lSwitchesJ.getJSONObject(lCount);
                                lSwitch = new Switch(lSwitchJ);
                                lSwitches.add(lSwitch);
                            }
                            mData.xSaveSwitches(lSwitches, mServer.xName());
                            mSwitchesInit = true;
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
                    Toast.makeText(mContext, pOutput.xText(), Toast.LENGTH_SHORT).show();
                    break;
            }

            mTxtSunset.setText(lSunset);
            mTxtLightsOff.setText(lLightOff);
            lSwitches = mData.xSwitches(mServer.xName());
            mListAdapter.clear();
            mListAdapter.addAll(lSwitches);
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
    }

    private class SetAction extends AsyncTask<Void, Void, RestAPI.RestResult> {

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            Action lAction;
            String lCommand;
            String lCommandStep;
            int lCount;

            lCommand = "";
            for (lCount = 0; lCount < mActions.size(); lCount++) {
                lAction = mActions.get(lCount);
                if (!lAction.xProcessed()) {
                    lAction.xProcessed(true);
                    switch (lAction.xAction()) {
                        case Action.ActionSwitchOn:
                            lCommandStep = "switchon=" + lAction.xValue();
                            break;
                        case Action.ActionSwitchOff:
                            lCommandStep = "switchoff=" + lAction.xValue();
                            break;
                        case Action.ActionLightsOff:
                            lCommandStep = "lightoff=" + lAction.xValue();
                            break;
                        default:
                            lCommandStep = "";
                            break;
                    }
                    if (lCommand.equals("")) {
                        lCommand = lCommandStep;
                    } else {
                        if (!lCommandStep.equals("")) {
                            lCommand = lCommand + "&" + lCommandStep;
                        }
                    }
                }
            }
            lRequest = mServer.xAddress() + URIs.UriServerAction;
            lRestAPI = new RestAPI();
            lRestAPI.xMethod(RestAPI.cMethodPut);
            lRestAPI.xMediaRequest(RestAPI.cMediaText);
            lRestAPI.xMediaReply(RestAPI.cMediaJSON);
            lRestAPI.xUrl(lRequest);
            lRestAPI.xAction(lCommand);
            lOutput = lRestAPI.xCallApi();

            return lOutput;
        }

        protected void onPostExecute(RestAPI.RestResult pOutput) {
            String lResult;
            JSONObject lActionResult;

            switch (pOutput.xResult()) {
                case Result.cResultOK:
                    try {
                        lActionResult = pOutput.xReplyJ();
                        lResult = lActionResult.getString("descr");
                    } catch (JSONException pExc) {
                        lResult = "JSON error: " + pExc.getLocalizedMessage();
                    }
                    Toast.makeText(mContext, lResult, Toast.LENGTH_SHORT).show();
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
            mActionReady = true;
        }
    }

    private class SetActionIOT extends AsyncTask<Void, Void, RestAPI.RestResult> {

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            Action lAction;
            JSONObject lCommand;

            lAction = mActions.get(mCurrentAction);
            lAction.xProcessed(true);
            lCommand = new JSONObject();
            try {
                if (lAction.xAction() == Action.ActionSwitchOn) {
                    lCommand.put("status", "on");
                } else {
                    lCommand.put("status", "off");
                }

            } catch (JSONException pExc) {
                lCommand = null;
            }
            lRequest = "http://" + lAction.xIP() + URIs.UriSwitch;
            lRestAPI = new RestAPI();
            lRestAPI.xMethod(RestAPI.cMethodPut);
            lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
            lRestAPI.xMediaReply(RestAPI.cMediaJSON);
            lRestAPI.xUrl(lRequest);
            lRestAPI.xAction(lCommand.toString());
            lOutput = lRestAPI.xCallApi();

            return lOutput;
        }

        protected void onPostExecute(RestAPI.RestResult pOutput) {
            String lResult;
            JSONObject lActionResult;
            final String cError = "error";

            if (pOutput.xResult() == Result.cResultOK) {
                lActionResult = pOutput.xReplyJ();
                lResult = lActionResult.optString("status", cError);
                if (lResult.equals(cError)) {
                    mNumberIncorrect++;
                } else {
                    mNumberCorrect++;
                }
            } else {
                mNumberIncorrect++;
            }
            mActionReady = true;
        }
    }
}
