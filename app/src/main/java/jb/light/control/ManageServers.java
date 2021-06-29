package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

/**
 * Created by Jan on 20-10-2015.
 */
public class ManageServers extends Activity {
    private final Context mContext = this;
    private Data mData;

    ListView mList;
    ServerListAdapter mListAdapter;
    ImageButton mIbtnNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_servers_layout);

        mList = findViewById(R.id.lstServers);
        mIbtnNew = findViewById(R.id.ibtnNew);

        mData = Data.getInstance(mContext);
        mListAdapter = new ServerListAdapter(this, R.layout.manage_server_list_item, mData.xServers());
        mList.setAdapter(mListAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View pView, int position, long id) {
                ServerListAdapter.ServerHandle lHandle;
                Server lServer;

                lHandle = (ServerListAdapter.ServerHandle)pView.getTag();
                lServer = lHandle.xServer;
                sModifyServer(lServer.xName());
            }
        });
        mIbtnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sNewServer();
            }
        });
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onResume(){
        super.onResume();
    }

    private void sModifyServer(String pServerName){
        Intent lInt;
        Uri lUri;

        lUri = Uri.parse("/servers/" + pServerName);
        lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifyServer.class);
        startActivity(lInt);
        }

    private void sNewServer(){
        Intent lInt;
        Uri lUri;

        lUri = Uri.parse("/servers/");
        lInt = new Intent(Intent.ACTION_INSERT, lUri, this, ModifyServer.class);
        startActivity(lInt);
    }
}
