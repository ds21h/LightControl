package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Created by Jan on 22-9-2015.
 */

public class ModifySwitch extends Activity {
    private final Context mContext = this;

    public static final String cServerName = "ServerName";

    private String mServerName = "";
    private String mSwitchName = "";

    private Data mData;
    private Server mServer;
    private Switch mSwitch;
    private boolean mButton;

    private String mAction = "";
    private String mTitle = "";
    private boolean mChangeName = false;

    private String mSwitchSeqNumber;
    private String mSwitchId;
    private String mSwitchType;
    private String mSwitchGroup;
    private String mSwitchPoint;
    private String mSwitchIP;
    private String mSwitchPause;
    private boolean mButtonActive;
    private boolean mSwitchActive;

    private static final String cTitle = "Title";
    private static final String cButton = "Button";
    private static final String cSwitchSeqNumber = "SwitchSeqNumber";
    private static final String cSwitchName = "SwitchName";
    private static final String cSwitchId = "SwitchId";
    private static final String cSwitchType = "SwitchType";
    private static final String cSwitchGroup = "SwitchGroup";
    private static final String cSwitchPoint = "SwitchPoint";
    private static final String cSwitchIP = "SwitchIP";
    private static final String cSwitchPause = "SwitchPause";
    private static final String cButtonActive = "ButtonActive";
    private static final String cSwitchActive = "SwitchActive";

    private EditText mEdtSeqNumber;
    private EditText mEdtName;
    private Spinner mSpType;
    private EditText mEdtGroup;
    private EditText mEdtPoint;
    private EditText mEdtIP;
    private EditText mEdtPause;
    private CheckBox mChkActive;
    private CheckBox mChkButton;
    private LinearLayout mLyoGroup;
    private LinearLayout mLyoPoint;
    private LinearLayout mLyoIP;

    private ArrayAdapter mAdpType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_switch_layout);

        Intent lInt;
        Uri lUri;
        Bundle lBundle;
        int lCount;

        mData = Data.getInstance(mContext);

        mEdtSeqNumber = findViewById(R.id.edtSeqNumber);
        mEdtName = findViewById(R.id.edtName);
        mSpType = findViewById(R.id.spType);
        mEdtGroup = findViewById(R.id.edtGroup);
        mEdtPoint = findViewById(R.id.edtPoint);
        mEdtIP = findViewById(R.id.edtIP);
        mEdtPause = findViewById(R.id.edtPause);
        mChkActive = findViewById(R.id.chkActive);
        mChkButton = findViewById(R.id.chkButton);
        mLyoGroup = findViewById(R.id.lyoGroup);
        mLyoPoint = findViewById(R.id.lyoPoint);
        mLyoIP = findViewById(R.id.lyoIP);

        mAdpType = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        mAdpType.add("Not set");
        for (lCount = 0; lCount < Switch.Types.length; lCount++){
            mAdpType.add(Switch.Types[lCount]);
        }
        mAdpType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpType.setAdapter(mAdpType);

        mSpType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> pAdapter, View view, int pPosition, long id) {
                String lChoice;
                LinearLayout.LayoutParams lParGroup;
                LinearLayout.LayoutParams lParPoint;
                LinearLayout.LayoutParams lParIP;
                LinearLayout.LayoutParams lParButton;

                if (pPosition == 0) {
                    lParGroup = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lParPoint = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lParIP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lParButton = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                } else {
                    lChoice = (String)mAdpType.getItem(pPosition);
                    if (lChoice != null && lChoice.equals("esp")){
                        lParGroup = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                        lParPoint = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                        lParIP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lParButton = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        mChkButton.setVisibility(View.VISIBLE);
                    } else {
                        lParGroup = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lParPoint = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lParIP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                        lParButton = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
                    }
                }
                mLyoGroup.setLayoutParams(lParGroup);
                mLyoPoint.setLayoutParams(lParPoint);
                mLyoIP.setLayoutParams(lParIP);
                mChkButton.setLayoutParams(lParButton);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (savedInstanceState == null) {
            lInt = getIntent();
            lBundle = lInt.getExtras();
            if (lBundle == null){
                finish();
            } else {
                mServerName = lBundle.getString(cServerName);
                mAction = lInt.getAction();
                mServer = mData.xServer(mServerName);
                mButton = false;
                if (mAction.equals(Intent.ACTION_EDIT)){
                    lUri = lInt.getData();
                    if (lUri == null){
                        finish();
                    } else {
                        mSwitchName = lUri.getLastPathSegment();
                        mChangeName = false;
                        mTitle = getString(R.string.title_modify_switch);
                        mSwitch = mData.xSwitch(mServer.xName(), mSwitchName);
                        if (mSwitch.xType().equals("esp")){
                            new GetButton(this).execute();
                        }
                    }
                } else {
                    mSwitchName = "";
                    mSwitch = new Switch();
                    mChangeName = true;
                    mTitle = getString(R.string.title_new_switch);
                }
                mSwitchSeqNumber = String.valueOf(mSwitch.xSeqNumber());
                mSwitchId = mSwitch.xName();
                mSwitchType = mSwitch.xType();
                mSwitchGroup = mSwitch.xGroup();
                mSwitchPoint = mSwitch.xPoint();
                mSwitchIP = mSwitch.xIP();
                mSwitchPause = String.valueOf(mSwitch.xPause());
                mButtonActive = false;
                mSwitchActive = mSwitch.xActive();
            }
        } else {
            mTitle = savedInstanceState.getString(cTitle);
            mButton = savedInstanceState.getBoolean(cButton);
            mServerName = savedInstanceState.getString(cServerName);
            mSwitchSeqNumber = savedInstanceState.getString(cSwitchSeqNumber);
            mSwitchId = savedInstanceState.getString(cSwitchId);
            mSwitchName = savedInstanceState.getString(cSwitchName);
            mSwitchType = savedInstanceState.getString(cSwitchType);
            mSwitchGroup = savedInstanceState.getString(cSwitchGroup);
            mSwitchPoint = savedInstanceState.getString(cSwitchPoint);
            mSwitchIP = savedInstanceState.getString(cSwitchIP);
            mSwitchPause = savedInstanceState.getString(cSwitchPause);
            mButtonActive = savedInstanceState.getBoolean(cButtonActive);
            mSwitchActive = savedInstanceState.getBoolean(cSwitchActive);

            mServer = mData.xServer(mServerName);
            if (mSwitchName.equals("")){
                mSwitch = new Switch();
            } else {
                mSwitch = mData.xSwitch(mServer.xName(), mSwitchId);
            }
        }
        sFillScreen();

        mEdtName.setEnabled(mChangeName);
        setTitle(mTitle);
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        sReadScreen();

        savedInstanceState.putString(cTitle, mTitle);
        savedInstanceState.putBoolean(cButton, mButton);
        savedInstanceState.putString(cServerName, mServerName);
        savedInstanceState.putString(cSwitchSeqNumber, mSwitchSeqNumber);
        savedInstanceState.putString(cSwitchName, mSwitchName);
        savedInstanceState.putString(cSwitchId, mSwitchId);
        savedInstanceState.putString(cSwitchType, mSwitchType);
        savedInstanceState.putString(cSwitchGroup, mSwitchGroup);
        savedInstanceState.putString(cSwitchPoint, mSwitchPoint);
        savedInstanceState.putString(cSwitchIP, mSwitchIP);
        savedInstanceState.putString(cSwitchPause, mSwitchPause);
        savedInstanceState.putBoolean(cButtonActive, mButtonActive);
        savedInstanceState.putBoolean(cSwitchActive, mSwitchActive);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        super.onCreateOptionsMenu(pMenu);

        getMenuInflater().inflate(R.menu.modify_menu, pMenu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu pMenu){
        super.onPrepareOptionsMenu(pMenu);

        MenuItem lItem;

        lItem = pMenu.findItem(R.id.action_delete);
        if (mAction.equals(Intent.ACTION_INSERT)){
            lItem.setVisible(false);
        } else {
            lItem.setVisible(true);
        }
        return true;
    }

    public void sRefresh(MenuItem pItem){
        mSwitchSeqNumber = String.valueOf(mSwitch.xSeqNumber());
        mSwitchId = mSwitch.xName();
        mSwitchType = mSwitch.xType();
        mSwitchGroup = mSwitch.xGroup();
        mSwitchPoint = mSwitch.xPoint();
        mSwitchIP = mSwitch.xIP();
        mSwitchPause = String.valueOf(mSwitch.xPause());
        mButtonActive = false;
        mSwitchActive = mSwitch.xActive();
        sFillScreen();
        if (mSwitch.xType().equals("esp")){
            new GetButton(this).execute();
        }
    }

    public void sProcess(MenuItem pItem){
        Switch lSwitch;
        SwitchX[] lSwitches;

        sReadScreen();
        lSwitch = sProcessScreen();
        if (lSwitch !=null){
            if (lSwitch.xIsEqual(mSwitch)){
                if (mAction.equals(Intent.ACTION_EDIT)){
                    if (mButtonActive == mButton){
                        Toast.makeText(this, R.string.msg_nochange, Toast.LENGTH_SHORT).show();
                    } else {
                        new SetButton(this).execute();
                    }
                } else {
                    Toast.makeText(this, R.string.msg_nochange, Toast.LENGTH_SHORT).show();
                }
            } else {
                lSwitches = new SwitchX[1];
                lSwitches[0] = new SwitchX(lSwitch);
                if (mAction.equals((Intent.ACTION_INSERT))){
                    lSwitches[0].xAction(SwitchX.ActionNew);
                    mData.xNewSwitch(lSwitch, mServerName);
                } else {
                    lSwitches[0].xAction(SwitchX.ActionModify);
                    mData.xModifySwitch(lSwitch, mServerName);
                }
                new SetSwitch(this).execute(lSwitches);
            }
        }
    }

    public void sDelete(MenuItem pItem){
        SwitchX[] lSwitches;

        sReadScreen();
        lSwitches = new SwitchX[1];
        lSwitches[0] = new SwitchX(mSwitchId);
        lSwitches[0].xAction(SwitchX.ActionDelete);
        new SetSwitch(this).execute(lSwitches);

        mData.xDeleteSwitch(mSwitchId, mServerName);
        finish();
    }

    private void sFillButton(int pStatus, String pMessage, JSONObject pResult, boolean pMessageOK){
        String lButton;
        final String cError = "error";

        switch (pStatus){
            case Result.cResultOK:
                lButton = pResult.optString("button", cError);
                if (lButton.equals(cError)){
                    Toast.makeText(mContext, "Wrong answer!", Toast.LENGTH_SHORT).show();
                } else {
                    mButton = lButton.equals("on");
                    mButtonActive = mButton;
                    mChkButton.setChecked(mButtonActive);
                    if (pMessageOK){
                        Toast.makeText(mContext, R.string.msg_button_set, Toast.LENGTH_SHORT).show();
                    }
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
        int lIndex;

        mEdtSeqNumber.setText(mSwitchSeqNumber);
        mEdtName.setText(mSwitchId);
        lIndex = mAdpType.getPosition(mSwitchType);
        mSpType.setSelection(lIndex);
        mEdtGroup.setText(mSwitchGroup);
        mEdtPoint.setText(mSwitchPoint);
        mEdtIP.setText(mSwitchIP);
        mEdtPause.setText(mSwitchPause);
        mChkActive.setChecked(mSwitchActive);
        mChkButton.setChecked(mButtonActive);
    }

    private void sReadScreen(){
        mSwitchSeqNumber = mEdtSeqNumber.getText().toString();
        mSwitchId = mEdtName.getText().toString();
        if (mSpType.getSelectedItemPosition() > 0){
            mSwitchType = (String)mSpType.getSelectedItem();
        } else {
            mSwitchType = "";
        }
        mSwitchGroup = mEdtGroup.getText().toString();
        mSwitchPoint = mEdtPoint.getText().toString();
        mSwitchIP = mEdtIP.getText().toString();
        mSwitchPause = mEdtPause.getText().toString();
        mSwitchActive = mChkActive.isChecked();
        mButtonActive = mChkButton.isChecked();
    }

    private Switch sProcessScreen(){
        int lPause;
        int lSeqNumber;
        String lTest;
        int lNotProvided;
        int lError;
        Switch lSwitch;
        int lResult;
        String lText;

        lNotProvided = 0;
        lError = 0;

        lSwitch = new Switch();
        if (mSwitchSeqNumber.trim().equals("")){
            Toast.makeText(this, R.string.msg_seqmandatory, Toast.LENGTH_SHORT).show();
            lNotProvided++;
            lError++;
            lSeqNumber = 0;
        } else {
            try {
                lSeqNumber = Integer.parseInt(mSwitchSeqNumber);
            } catch (NumberFormatException pExc){
                Toast.makeText(this, R.string.msg_seqmandatory, Toast.LENGTH_SHORT).show();
                lError++;
                lSeqNumber = 0;
            }
        }
        lResult = lSwitch.xSeqNumber(lSeqNumber);
        if (lResult != Switch.ResultOK){
            Toast.makeText(this, R.string.msg_seqmandatory, Toast.LENGTH_SHORT).show();
            lError++;
        }
        lResult = lSwitch.xName(mSwitchId);
        if (lResult != Switch.ResultOK){
            Toast.makeText(this, R.string.msg_namewrong, Toast.LENGTH_SHORT).show();
            lError++;
        }
        if (mSwitchPause.trim().equals("")){
            Toast.makeText(this, R.string.msg_pausemandatory, Toast.LENGTH_SHORT).show();
            lNotProvided++;
            lError++;
            lPause = 0;
        } else {
            try {
                lPause = Integer.parseInt(mSwitchPause);
            } catch (NumberFormatException pExc){
                Toast.makeText(this, R.string.msg_pauseinteger, Toast.LENGTH_SHORT).show();
                lError++;
                lPause = 0;
            }
        }
        lResult = lSwitch.xPause(lPause);
        if (lResult != Switch.ResultOK){
            Toast.makeText(this, R.string.msg_pauseinteger, Toast.LENGTH_SHORT).show();
            lError++;
        }
        if (mSwitchType.equals("")){
            lNotProvided++;
        }
        lResult = lSwitch.xType(mSwitchType);
        if (lResult != Switch.ResultOK){
            Toast.makeText(this, R.string.msg_unsupportedtype, Toast.LENGTH_SHORT).show();
            lError++;
        }
        lTest = mSwitchGroup.trim().toUpperCase();
        lResult = lSwitch.xGroup(lTest);
        if (lResult != Switch.ResultOK){
            Toast.makeText(this, R.string.msg_grouperror, Toast.LENGTH_SHORT).show();
            lError++;
        }
        lTest = mSwitchPoint.trim().toUpperCase();
        lResult = lSwitch.xPoint(lTest);
        if (lResult != Switch.ResultOK){
            Toast.makeText(this, R.string.msg_pointerror, Toast.LENGTH_SHORT).show();
            lError++;
        }
        lTest = mSwitchIP.trim().toUpperCase();
        lResult = lSwitch.xIP(lTest);
        if (lResult != Switch.ResultOK){
            Toast.makeText(this, R.string.msg_iperror, Toast.LENGTH_SHORT).show();
            lError++;
        }
        lSwitch.xActive(mSwitchActive);
        if (mSwitchActive){
            if (lNotProvided>0){
                Toast.makeText(this, R.string.msg_activeincomplete, Toast.LENGTH_SHORT).show();
                lError++;
            }
        }
        if (lError==0){
            lResult = lSwitch.xTestSwitch();
            switch (lResult) {
                case Switch.ResultOK:
                    lText = getString(R.string.msg_switchok);
                    break;
                case Switch.ResultNameError:
                    lText = getString(R.string.msg_namewrong);
                    lError++;
                    break;
                case Switch.ResultTypeError:
                    lText = getString(R.string.msg_unsupportedtype);
                    lError++;
                    break;
                case Switch.ResultGroupError:
                    lText = getString(R.string.msg_grouperror);
                    lError++;
                    break;
                case Switch.ResultPointError:
                    lText = getString(R.string.msg_pointerror);
                    lError++;
                    break;
                case Switch.ResultIpError:
                    lText = getString(R.string.msg_iperror);
                    lError++;
                    break;
                case Switch.ResultPauseError:
                    lText = getString(R.string.msg_pauseinteger);
                    lError++;
                    break;
                default:
                    lText = getString(R.string.msg_unknownerror);
                    lError++;
                    break;
            }
            if (lError>0){
                Toast.makeText(this, lText, Toast.LENGTH_SHORT).show();
            }
        }
        if (lError>0){
            lSwitch = null;
        }
        return lSwitch;
    }

    private static class SetSwitch extends AsyncTask<SwitchX, Void, RestAPI.RestResult> {
        private WeakReference<ModifySwitch> mRefMain;

        private SetSwitch(ModifySwitch pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(SwitchX... pPars) {
            ModifySwitch lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            SwitchX lSwitchX;
            JSONObject lAction;
            String lActionS;

            lMain = mRefMain.get();
            if (lMain == null){
                return null;
            } else {
                if (pPars.length>0){
                    lSwitchX = pPars[0];
                    lAction = new JSONObject();
                    try{
                        lAction.put("action", lSwitchX.xAction());
                        lAction.put("lang", Locale.getDefault().getLanguage());
                        lAction.put("switch", lSwitchX.xSwitch());
                        lActionS = lAction.toString();
                    } catch (JSONException pExc){
                        lActionS = "";
                    }
                    lRequest = lMain.mServer.xAddress() + URIs.UriServerSwitch + lSwitchX.xName();
                    lRestAPI = new RestAPI();
                    lRestAPI.xMethod(RestAPI.cMethodPut);
                    lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
                    lRestAPI.xMediaReply(RestAPI.cMediaJSON);
                    lRestAPI.xUrl(lRequest);
                    lRestAPI.xAction(lActionS);
                    lOutput = lRestAPI.xCallApi();
                } else {
                    lOutput = null;
                }

                return lOutput;
            }
        }

        protected void onPostExecute(RestAPI.RestResult pOutput) {
            ModifySwitch lMain;
            String lResultS;
            JSONObject lResult;

            if (pOutput != null){
                lMain = mRefMain.get();
                if (lMain != null){
                    switch (pOutput.xResult()) {
                        case Result.cResultOK:
                            lResult = pOutput.xReplyJ();
                            lResultS = lResult.optString("descr", "JSON error");
                            Toast.makeText(lMain.mContext, lResultS, Toast.LENGTH_SHORT).show();
                            break;
                        case Result.cResultConnectTimeOut:
                            Toast.makeText(lMain.mContext, "Connect Time-Out", Toast.LENGTH_SHORT).show();
                            break;
                        case Result.cResultReadTimeOut:
                            Toast.makeText(lMain.mContext, "Read Time-Out", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(lMain.mContext, pOutput.xText(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }
    }

    private static class GetButton extends AsyncTask<Void, Void, RestAPI.RestResult> {
        private WeakReference<ModifySwitch> mRefMain;

        private GetButton(ModifySwitch pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            ModifySwitch lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;

            lMain = mRefMain.get();
            if (lMain == null){
                return null;
            } else {
                lRequest = "http://" + lMain.mSwitch.xIP() + URIs.UriSwitch;
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
            ModifySwitch lMain;

            if (pOutput != null){
                lMain = mRefMain.get();
                if (lMain != null){
                    lMain.sFillButton(pOutput.xResult(), pOutput.xText(), pOutput.xReplyJ(), false);
                }
            }
        }
    }

    private static class SetButton extends AsyncTask<Void, Void, RestAPI.RestResult> {
        private WeakReference<ModifySwitch> mRefMain;

        private SetButton(ModifySwitch pMain){
            mRefMain = new WeakReference<>(pMain);
        }

        @Override
        protected RestAPI.RestResult doInBackground(Void... params) {
            ModifySwitch lMain;
            String lRequest;
            RestAPI.RestResult lOutput;
            RestAPI lRestAPI;
            JSONObject lAction;
            String lActionS;

            lMain = mRefMain.get();
            if (lMain == null){
                return null;
            } else {
                lAction = new JSONObject();
                try {
                    lAction.put("button", (lMain.mButtonActive) ? "on" : "off");
                    lActionS = lAction.toString();
                } catch (JSONException pExc){
                    lActionS = "";
                }

                lRequest = "http://" + lMain.mSwitch.xIP() + URIs.UriSetting;
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
            ModifySwitch lMain;

            if (pOutput != null){
                lMain = mRefMain.get();
                if (lMain != null){
                    lMain.sFillButton(pOutput.xResult(), pOutput.xText(), pOutput.xReplyJ(), true);
                }
            }
        }
    }
}

