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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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
    private SwitchLocal mSwitch;
    private boolean mSwitchDeleted;
    private boolean mButton;

    private String mAction = "";
    private String mTitle = "";
    private boolean mChangeName = false;

    private String mSwitchSeqNumber;
    private String mSwitchId;
    private String mSwitchIP;
    private String mSwitchPause;
    private boolean mButtonActive;
    private boolean mSwitchActive;
    private int mSwitchAction;

    private static final String cTitle = "Title";
    private static final String cButton = "Button";
    private static final String cSwitchSeqNumber = "SwitchSeqNumber";
    private static final String cSwitchName = "SwitchName";
    private static final String cSwitchId = "SwitchId";
    private static final String cSwitchIP = "SwitchIP";
    private static final String cSwitchPause = "SwitchPause";
    private static final String cButtonActive = "ButtonActive";
    private static final String cSwitchActive = "SwitchActive";
    private static final String cSwitchAction = "SwitchAction";
    private static final String cSwitchDeleted = "SwitchDeleted";

    private EditText mEdtSeqNumber;
    private EditText mEdtName;
    private EditText mEdtIP;
    private EditText mEdtPause;
    private CheckBox mChkActive;
    private CheckBox mChkButton;

    Handler mUpdateHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message pMessage) {
            SwitchLocal lSwitch;
            String lErrMsg;

            if ((pMessage.what & HandlerCode.cServerSwitch) != 0) {
                lErrMsg = HandlerCode.xCheckCode(mContext, pMessage.what);
                if (lErrMsg == null) {
                    switch (mSwitchAction) {
                        case SwitchLocal.ActionNew: {
                            Toast.makeText(mContext, R.string.msg_switch_added, Toast.LENGTH_SHORT).show();
                            lSwitch = sProcessScreen();
                            if (lSwitch != null) {
                                mSwitch = lSwitch;
                                mData.xNewSwitch(lSwitch, mServerName);
                            }
                            sFillScreen();
                            break;
                        }
                        case SwitchLocal.ActionModify: {
                            Toast.makeText(mContext, R.string.msg_switch_updated, Toast.LENGTH_SHORT).show();
                            lSwitch = sProcessScreen();
                            if (lSwitch != null) {
                                mSwitch = lSwitch;
                                mData.xModifySwitch(lSwitch, mServerName);
                            }
                            sFillScreen();
                            break;
                        }
                        case SwitchLocal.ActionDelete: {
                            Toast.makeText(mContext, R.string.msg_switch_deleted, Toast.LENGTH_SHORT).show();
                            mData.xDeleteSwitch(mSwitchId, mServerName);
                            mSwitchDeleted = true;
                            sFillScreen();
                            break;
                        }
                    }
                } else {
                    Toast.makeText(mContext, lErrMsg, Toast.LENGTH_SHORT).show();
                }
            }
            if ((pMessage.what & HandlerCode.cSwitchSetting) != 0) {
                lErrMsg = HandlerCode.xCheckCode(mContext, pMessage.what);
                if (lErrMsg == null) {
                    if (mSwitch.xStatus() != SwitchLocal.StatusNone) {
                        mButtonActive = mSwitch.xButton();
                        mChkButton.setChecked(mButtonActive);
                        Toast.makeText(mContext, R.string.msg_button_set, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, lErrMsg, Toast.LENGTH_SHORT).show();
                }
            }
            if ((pMessage.what & HandlerCode.cSwitch) != 0) {
                lErrMsg = HandlerCode.xCheckCode(mContext, pMessage.what);
                if (lErrMsg == null) {
                    if (mSwitch.xStatus() != SwitchLocal.StatusNone) {
                        mButtonActive = mSwitch.xButton();
                        mChkButton.setChecked(mButtonActive);
                    }
                } else {
                    Toast.makeText(mContext, lErrMsg, Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_switch_layout);

        Intent lInt;
        Uri lUri;
        Bundle lBundle;
        SwitchRunnable lSwitchRunnable;

        mData = Data.getInstance(mContext);

        mEdtSeqNumber = findViewById(R.id.edtSeqNumber);
        mEdtName = findViewById(R.id.edtName);
        mEdtIP = findViewById(R.id.edtIP);
        mEdtPause = findViewById(R.id.edtPause);
        mChkActive = findViewById(R.id.chkActive);
        mChkButton = findViewById(R.id.chkButton);

        if (savedInstanceState == null) {
            lInt = getIntent();
            lBundle = lInt.getExtras();
            if (lBundle == null) {
                finish();
            } else {
                mServerName = lBundle.getString(cServerName);
                mAction = lInt.getAction();
                mServer = mData.xServer(mServerName);
                mButton = false;
                if (mAction.equals(Intent.ACTION_EDIT)) {
                    lUri = lInt.getData();
                    if (lUri == null) {
                        finish();
                    } else {
                        mSwitchName = lUri.getLastPathSegment();
                        mChangeName = false;
                        mTitle = getString(R.string.title_modify_switch);
                        mSwitch = mData.xSwitch(mServer.xName(), mSwitchName);
                        lSwitchRunnable = new SwitchRunnable(mSwitch, mUpdateHandler);
                        LightControlApp.getInstance().xExecutor.execute(lSwitchRunnable);
                    }
                } else {
                    mSwitchName = "";
                    mSwitch = new SwitchLocal();
                    mChangeName = true;
                    mTitle = getString(R.string.title_new_switch);
                }
                mSwitchSeqNumber = String.valueOf(mSwitch.xSeqNumber());
                mSwitchId = mSwitch.xName();
                mSwitchIP = mSwitch.xIP();
                mSwitchPause = String.valueOf(mSwitch.xPause());
                mButtonActive = false;
                mSwitchActive = mSwitch.xActive();
                mSwitchAction = SwitchLocal.ActionNone;
                mSwitchDeleted = false;
            }
        } else {
            mTitle = savedInstanceState.getString(cTitle);
            mButton = savedInstanceState.getBoolean(cButton);
            mServerName = savedInstanceState.getString(cServerName);
            mSwitchSeqNumber = savedInstanceState.getString(cSwitchSeqNumber);
            mSwitchId = savedInstanceState.getString(cSwitchId);
            mSwitchName = savedInstanceState.getString(cSwitchName);
            mSwitchIP = savedInstanceState.getString(cSwitchIP);
            mSwitchPause = savedInstanceState.getString(cSwitchPause);
            mButtonActive = savedInstanceState.getBoolean(cButtonActive);
            mSwitchActive = savedInstanceState.getBoolean(cSwitchActive);
            mSwitchAction = savedInstanceState.getInt(cSwitchAction);
            mSwitchDeleted = savedInstanceState.getBoolean(cSwitchDeleted);

            mServer = mData.xServer(mServerName);
            if (mSwitchName.equals("")) {
                mSwitch = new SwitchLocal();
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
        savedInstanceState.putString(cSwitchIP, mSwitchIP);
        savedInstanceState.putString(cSwitchPause, mSwitchPause);
        savedInstanceState.putBoolean(cButtonActive, mButtonActive);
        savedInstanceState.putBoolean(cSwitchActive, mSwitchActive);
        savedInstanceState.putInt(cSwitchAction, mSwitchAction);
        savedInstanceState.putBoolean(cSwitchDeleted, mSwitchDeleted);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        super.onCreateOptionsMenu(pMenu);

        getMenuInflater().inflate(R.menu.modify_menu, pMenu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu pMenu) {
        super.onPrepareOptionsMenu(pMenu);

        MenuItem lItem;

        lItem = pMenu.findItem(R.id.action_delete);
        lItem.setVisible(!mAction.equals(Intent.ACTION_INSERT));
        return true;
    }

    @Override
    public void onStop() {
        mUpdateHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    public void sRefresh(MenuItem pItem) {
        SwitchRunnable lSwitchRunnable;

        mSwitchSeqNumber = String.valueOf(mSwitch.xSeqNumber());
        mSwitchId = mSwitch.xName();
        mSwitchIP = mSwitch.xIP();
        mSwitchPause = String.valueOf(mSwitch.xPause());
        mButtonActive = false;
        mSwitchActive = mSwitch.xActive();
        sFillScreen();
        lSwitchRunnable = new SwitchRunnable(mSwitch, mUpdateHandler);
        LightControlApp.getInstance().xExecutor.execute(lSwitchRunnable);
    }

    public void sProcess(MenuItem pItem) {
        SwitchLocal lSwitch;
        SwitchSettingRunnable lSwitchSettingRunnable;
        ServerSwitchRunnable lServerSwitchRunnable;

        sReadScreen();
        lSwitch = sProcessScreen();
        if (lSwitch != null) {
            if (lSwitch.xIsEqual(mSwitch)) {
                if (mAction.equals(Intent.ACTION_EDIT)) {
                    if (mButtonActive == mSwitch.xButton()) {
                        Toast.makeText(this, R.string.msg_nochange, Toast.LENGTH_SHORT).show();
                    } else {
                        if (mButtonActive) {
                            mSwitch.xAction(SwitchLocal.ActionButtonOn);
                        } else {
                            mSwitch.xAction(SwitchLocal.ActionButtonOff);
                        }
                        lSwitchSettingRunnable = new SwitchSettingRunnable(mSwitch, mUpdateHandler);
                        LightControlApp.getInstance().xExecutor.execute(lSwitchSettingRunnable);
                    }
                } else {
                    Toast.makeText(this, R.string.msg_nochange, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (mAction.equals((Intent.ACTION_INSERT))) {
                    lSwitch.xAction(SwitchLocal.ActionNew);
                } else {
                    lSwitch.xAction(SwitchLocal.ActionModify);
                }
                mSwitchAction = lSwitch.xAction();
                lServerSwitchRunnable = new ServerSwitchRunnable(lSwitch, mServer.xAddress(), mUpdateHandler);
                LightControlApp.getInstance().xExecutor.execute(lServerSwitchRunnable);
            }
        }
    }

    public void sDelete(MenuItem pItem) {
        SwitchLocal lSwitch;
        ServerSwitchRunnable lServerSwitchRunnable;

        if (!mSwitchDeleted) {
            sReadScreen();
            lSwitch = new SwitchLocal(mSwitchId);
            lSwitch.xAction(SwitchLocal.ActionDelete);
            mSwitchAction = lSwitch.xAction();
            lServerSwitchRunnable = new ServerSwitchRunnable(lSwitch, mServer.xAddress(), mUpdateHandler);
            LightControlApp.getInstance().xExecutor.execute(lServerSwitchRunnable);
        }
    }

    private void sFillScreen() {
        if (mSwitchDeleted) {
            mEdtSeqNumber.setText("");
            mEdtSeqNumber.setEnabled(false);
            mEdtName.setText("");
            mEdtName.setEnabled(false);
            mEdtIP.setText("");
            mEdtIP.setEnabled(false);
            mEdtPause.setText("");
            mEdtPause.setEnabled(false);
            mChkActive.setEnabled(false);
            mChkButton.setEnabled(false);
        } else {
            mEdtSeqNumber.setText(mSwitchSeqNumber);
            mEdtName.setText(mSwitchId);
            mEdtIP.setText(mSwitchIP);
            mEdtPause.setText(mSwitchPause);
            mChkActive.setChecked(mSwitchActive);
            mChkButton.setChecked(mButtonActive);
        }
    }

    private void sReadScreen() {
        mSwitchSeqNumber = mEdtSeqNumber.getText().toString();
        mSwitchId = mEdtName.getText().toString();
        mSwitchIP = mEdtIP.getText().toString();
        mSwitchPause = mEdtPause.getText().toString();
        mSwitchActive = mChkActive.isChecked();
        mButtonActive = mChkButton.isChecked();
    }

    private SwitchLocal sProcessScreen() {
        int lPause;
        int lSeqNumber;
        String lTest;
        int lNotProvided;
        int lError;
        SwitchLocal lSwitch;
        int lResult;
        String lText;

        lNotProvided = 0;
        lError = 0;

        lSwitch = new SwitchLocal();
        if (mSwitchSeqNumber.trim().equals("")) {
            Toast.makeText(this, R.string.msg_seqmandatory, Toast.LENGTH_SHORT).show();
            lNotProvided++;
            lError++;
            lSeqNumber = 0;
        } else {
            try {
                lSeqNumber = Integer.parseInt(mSwitchSeqNumber);
            } catch (NumberFormatException pExc) {
                Toast.makeText(this, R.string.msg_seqmandatory, Toast.LENGTH_SHORT).show();
                lError++;
                lSeqNumber = 0;
            }
        }
        lResult = lSwitch.xSeqNumber(lSeqNumber);
        if (lResult != Switch.ResultOK) {
            Toast.makeText(this, R.string.msg_seqmandatory, Toast.LENGTH_SHORT).show();
            lError++;
        }
        lResult = lSwitch.xName(mSwitchId);
        if (lResult != Switch.ResultOK) {
            Toast.makeText(this, R.string.msg_namewrong, Toast.LENGTH_SHORT).show();
            lError++;
        }
        if (mSwitchPause.trim().equals("")) {
            Toast.makeText(this, R.string.msg_pausemandatory, Toast.LENGTH_SHORT).show();
            lNotProvided++;
            lError++;
            lPause = 0;
        } else {
            try {
                lPause = Integer.parseInt(mSwitchPause);
            } catch (NumberFormatException pExc) {
                Toast.makeText(this, R.string.msg_pauseinteger, Toast.LENGTH_SHORT).show();
                lError++;
                lPause = 0;
            }
        }
        lResult = lSwitch.xPause(lPause);
        if (lResult != Switch.ResultOK) {
            Toast.makeText(this, R.string.msg_pauseinteger, Toast.LENGTH_SHORT).show();
            lError++;
        }
        lTest = mSwitchIP.trim().toUpperCase();
        lResult = lSwitch.xIP(lTest);
        if (lResult != Switch.ResultOK) {
            Toast.makeText(this, R.string.msg_iperror, Toast.LENGTH_SHORT).show();
            lError++;
        }
        lSwitch.xActive(mSwitchActive);
        if (mSwitchActive) {
            if (lNotProvided > 0) {
                Toast.makeText(this, R.string.msg_activeincomplete, Toast.LENGTH_SHORT).show();
                lError++;
            }
        }
        if (lError == 0) {
            lResult = lSwitch.xTestSwitch();
            switch (lResult) {
                case Switch.ResultOK:
                    lText = getString(R.string.msg_switchok);
                    break;
                case Switch.ResultNameError:
                    lText = getString(R.string.msg_namewrong);
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
            if (lError > 0) {
                Toast.makeText(this, lText, Toast.LENGTH_SHORT).show();
            }
        }
        if (lError > 0) {
            lSwitch = null;
        }
        return lSwitch;
    }
}
