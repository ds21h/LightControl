package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 20-10-2015.
 */
public class ManageSwitches extends Activity {
    private final Context mContext = this;

    static final String cServerName = "ServerName";

    private String mServerName;
    private Data mData;
    private Server mServer;
    List<Switch> mSwitches;


    private ListView mList;
    private ManageSwitchListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_switches_layout);

        Intent lInt;
        Bundle lBundle;

        mData = Data.getInstance(mContext);
        mSwitches = new ArrayList<>();

        if (savedInstanceState==null){
            lInt = getIntent();
            lBundle = lInt.getExtras();
            if (lBundle == null) {
                finish();
            } else {
                mServerName = lBundle.getString(cServerName, "");
            }
        } else {
            mServerName = savedInstanceState.getString(cServerName);
        }
        mServer = mData.xServer(mServerName);

        mList = findViewById(R.id.lstSwitches);

        mListAdapter = new ManageSwitchListAdapter(this, R.layout.manage_switch_list_item, new ArrayList<Switch>());
        mList.setAdapter(mListAdapter);
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putString(cServerName, mServerName);

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onResume(){
        super.onResume();

        mSwitches = mData.xSwitches(mServerName);
        mListAdapter.clear();
        mListAdapter.addAll(mSwitches);
    }

    public void sModifySwitch(View pView){
        Intent lInt;
        Uri lUri;
        Bundle lBundle;
        ManageSwitchListAdapter.SwitchListHandle lHandle;

        lHandle = (ManageSwitchListAdapter.SwitchListHandle)pView.getTag();

        lBundle = new Bundle();
        lBundle.putString(ModifySwitch.cServerName, mServer.xName());
        lUri = Uri.parse("/switches/" + lHandle.xSwitch.xName());
        lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifySwitch.class);
        lInt.putExtras(lBundle);
        startActivity(lInt);
        }

    public void sNewSwitch(View pView){
        Intent lInt;
        Uri lUri;
        Bundle lBundle;

        lBundle = new Bundle();
        lBundle.putString(ModifySwitch.cServerName, mServer.xName());
        lUri = Uri.parse("/switches/");
        lInt = new Intent(Intent.ACTION_INSERT, lUri, this, ModifySwitch.class);
        lInt.putExtras(lBundle);
        startActivity(lInt);
    }
}
