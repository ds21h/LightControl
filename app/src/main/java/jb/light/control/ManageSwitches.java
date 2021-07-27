package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.ImageButton;
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
    List<SwitchLocal> mSwitches;

    private ListView mList;
    private ManageSwitchListAdapter mListAdapter;
    ImageButton mIbtnNew;

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
        mIbtnNew = findViewById(R.id.ibtnNew);

        mListAdapter = new ManageSwitchListAdapter(this, R.layout.manage_switch_list_item, new ArrayList<>());
        mList.setAdapter(mListAdapter);
        mList.setOnItemClickListener((parent, pView, position, id) -> {
            ManageSwitchListAdapter.SwitchListHandle lHandle;
            Switch lSwitch;

            lHandle = (ManageSwitchListAdapter.SwitchListHandle)pView.getTag();
            lSwitch = lHandle.xSwitch;
            sModifySwitch(lSwitch.xName());
        });
        mIbtnNew.setOnClickListener(v -> sNewSwitch());
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

    private void sModifySwitch(String pSwitchName){
        Intent lInt;
        Uri lUri;
        Bundle lBundle;

        lBundle = new Bundle();
        lBundle.putString(ModifySwitch.cServerName, mServer.xName());
        lUri = Uri.parse("/switches/" + pSwitchName);
        lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifySwitch.class);
        lInt.putExtras(lBundle);
        startActivity(lInt);
        }

    private void sNewSwitch(){
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
