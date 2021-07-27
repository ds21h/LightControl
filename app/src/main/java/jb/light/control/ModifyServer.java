package jb.light.control;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * Created by Jan on 21-10-2015.
 */
public class ModifyServer extends Activity implements YesNoDialog.YesNoDialogListener {
    private final Context mContext = this;
    private static final String cAction = "Action";
    private static final String cTitle = "Title";
    private static final String cChangeName = "ChangeName";
    private static final String cServerId = "ServerId";
    private static final String cSSId = "SSId";
    private static final String cIP = "IP";
    private static final String cPort = "Port";
    private static final String cManager = "Manager";

    private String mAction = "";
    private String mTitle = "";
    private boolean mChangeName = false;
    private String mServerId = "";
    private String mSSId = "";
    private String mIP = "";
    private String mPort = "";
    private boolean mManager = false;

    private EditText mEdtServerName;
    private EditText mEdtNetwork;
    private EditText mEdtIP;
    private EditText mEdtPort;
    private CheckBox mChkManager;

    private Data mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_server_layout);

        Intent lInt;
        Uri lUri;
        int lResult;

        mData = Data.getInstance(mContext);

        if (savedInstanceState == null) {
            lInt = getIntent();
            if (lInt == null){
                sShutDown();
            } else {
                mAction = lInt.getAction();
                if (mAction == null){
                    sShutDown();
                } else {
                    if (mAction.equals(Intent.ACTION_EDIT)){
                        lUri = lInt.getData();
                        if (lUri == null){
                            sShutDown();
                        } else {
                            mServerId = lUri.getLastPathSegment();
                            mChangeName = false;
                            mTitle = getString(R.string.title_modify_server);
                        }
                    } else {
                        mServerId = "";
                        mChangeName = true;
                        mTitle = getString(R.string.title_newserver);
                    }
                    lResult = sInitVar();
                    if (lResult!=0){
                        sShutDown();
                    }
                }
            }
        } else {
            mAction = savedInstanceState.getString(cAction);
            mChangeName = savedInstanceState.getBoolean(cChangeName);
            mTitle = savedInstanceState.getString(cTitle);
            mServerId = savedInstanceState.getString(cServerId);
            mSSId = savedInstanceState.getString(cSSId);
            mIP = savedInstanceState.getString(cIP);
            mPort = savedInstanceState.getString(cPort);
            mManager = savedInstanceState.getBoolean(cManager);
        }

        mEdtServerName = findViewById(R.id.edtServerName);
        mEdtNetwork = findViewById(R.id.edtNetwork);
        mEdtIP = findViewById(R.id.edtIPaddress);
        mEdtPort = findViewById(R.id.edtPort);
        mChkManager = findViewById(R.id.chkManager);

        mEdtServerName.setEnabled(mChangeName);
        setTitle(mTitle);

        sMakeScreen();
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        sReadScreen();
        savedInstanceState.putString(cAction, mAction);
        savedInstanceState.putBoolean(cChangeName, mChangeName);
        savedInstanceState.putString(cTitle, mTitle);
        savedInstanceState.putString(cServerId, mServerId);
        savedInstanceState.putString(cSSId, mSSId);
        savedInstanceState.putString(cIP, mIP);
        savedInstanceState.putString(cPort, mPort);
        savedInstanceState.putBoolean(cManager, mManager);
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
        lItem.setVisible(!mAction.equals(Intent.ACTION_INSERT));
        return true;
    }

    @Override
    public void onYesNoDialogEnd(String pResult) {
        int lResult;

        if (pResult.equals(YesNoDialog.cYesResult)){
            lResult = mData.xDeleteServer(mServerId);
            if (lResult==Result.cResultOK){
                sShutDown();
            }
        }
    }

    public void sRefresh(MenuItem pItem){
        sInitVar();
        sMakeScreen();
    }

    public void sProcess(MenuItem pItem){
        Server lServer;

        sReadScreen();
        if (mServerId.equals("")){
            Toast.makeText(this, R.string.msg_emptyserver, Toast.LENGTH_SHORT).show();
        } else {
            lServer = mData.xServer(mServerId);
            if (mAction.equals(Intent.ACTION_EDIT)){
                if (lServer!=null){
                    lServer.xSSId(mSSId);
                    lServer.xIP(mIP);
                    lServer.xPort(mPort);
                    lServer.xManager(mManager);
                    mData.xModifyServer(lServer);
                }
                sShutDown();
            } else {
                if (lServer==null){
                    lServer = new Server();
                    lServer.xName(mServerId);
                    lServer.xSSId(mSSId);
                    lServer.xIP(mIP);
                    lServer.xPort(mPort);
                    lServer.xManager(mManager);
                    mData.xNewServer(lServer);
                    sShutDown();
                } else {
                    Toast.makeText(this, R.string.msg_duplicateserver, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void sDelete(MenuItem pItem){
        DialogFragment lDialog;
        Bundle lParameters;
        String lMessageIn;
        String lMessageOut;
        String cServer = "%%Server";
        int lServerIndex;

        lDialog = new YesNoDialog();
        lParameters = new Bundle();
        lParameters.putString(YesNoDialog.cTitle, getString(R.string.title_deleteserver));
        lMessageIn = getString(R.string.qmsg_deleteserver);
        lServerIndex = lMessageIn.indexOf(cServer);
        if (lServerIndex < 0){
            lMessageOut = lMessageIn;
        } else {
            lMessageOut = lMessageIn.substring(0, lServerIndex) + mServerId + lMessageIn.substring(lServerIndex + cServer.length());
        }
        lParameters.putString(YesNoDialog.cMessage, lMessageOut);
        lParameters.putString(YesNoDialog.cYesButton, getString(R.string.lb_ok));
        lParameters.putString(YesNoDialog.cNoButton, getString(R.string.lb_cancel));
        lDialog.setArguments(lParameters);
        lDialog.show(getFragmentManager(), "Delete");
    }

    private int sInitVar(){
        Server lServer;

        if (mAction.equals(Intent.ACTION_EDIT)){
            lServer = mData.xServer(mServerId);
            if (lServer==null){
                return -1;
            } else {
                mSSId = lServer.xSSId();
                mIP = lServer.xIP();
                mPort = lServer.xPort();
                mManager = lServer.xManager();
                return  0;
            }
        } else {
            mServerId = "";
            mSSId = "";
            mIP = "";
            mPort = "";
            mManager = false;
            return  0;
        }
    }

    private void sMakeScreen(){
        mEdtServerName.setText(mServerId);
        mEdtNetwork.setText(mSSId);
        mEdtIP.setText(mIP);
        mEdtPort.setText(mPort);
        mChkManager.setChecked(mManager);
    }

    private void sReadScreen(){
        mServerId = mEdtServerName.getText().toString();
        mSSId = mEdtNetwork.getText().toString();
        mIP = mEdtIP.getText().toString();
        mPort = mEdtPort.getText().toString();
        mManager = mChkManager.isChecked();
    }

    public void sSetCurrentNetwork(View pView) {
        ConnectivityManager lConnect;
        NetworkInfo lNet;
        WifiManager lWifi;
        WifiInfo lInfo;
        String lSSId;
        boolean lConnected;

        lConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (lConnect == null){
            Toast.makeText(this, R.string.msg_nonetwork, Toast.LENGTH_SHORT).show();
        } else {
            lNet = lConnect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (lNet != null){
                lConnected = lNet.isConnected();
                if (lConnected) {
                    lWifi =  (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
                    if (lWifi == null){
                        Toast.makeText(this, R.string.msg_nonetwork, Toast.LENGTH_SHORT).show();
                    } else {
                        lInfo = lWifi.getConnectionInfo();
                        lSSId = lInfo.getSSID();
                        if (lSSId.startsWith("\"")) {
                            lSSId = lSSId.substring(1, lSSId.length() - 1);
                        }
                        mEdtNetwork.setText(lSSId);
                    }
                } else {
                    Toast.makeText(this, R.string.msg_nonetwork, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void sShutDown(){
        finish();
    }
}
