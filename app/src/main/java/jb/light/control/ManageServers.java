package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;
import java.util.ArrayList;

/**
 * Created by Jan on 20-10-2015.
 */
public class ManageServers extends Activity {
    private final Context mContext = this;
    private Data mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_servers_layout);

        mData = Data.getInstance(mContext);
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
    }


    @Override
    public void onResume(){
        super.onResume();

        ListView lList;
        ServerListAdapter lListAdapter;

        lList = (ListView) findViewById(R.id.lstServers);

        lListAdapter = new ServerListAdapter(this, R.layout.manage_server_list_item, ((ArrayList) mData.xServers()));
        lList.setAdapter(lListAdapter);
    }

    public void sModifyServer(View pView){
        Intent lInt;
        ServerListAdapter.ServerHandle lHandle;
        Server lServer;
        Uri lUri;

        lHandle = (ServerListAdapter.ServerHandle)pView.getTag();
        lServer = lHandle.xServer;
        lUri = Uri.parse("/servers/" + lServer.xName());
        lInt = new Intent(Intent.ACTION_EDIT, lUri, this, ModifyServer.class);
        startActivity(lInt);
        }

    public void sNewServer(View pView){
        Intent lInt;
        Uri lUri;

        lUri = Uri.parse("/servers/");
        lInt = new Intent(Intent.ACTION_INSERT, lUri, this, ModifyServer.class);
        startActivity(lInt);
    }
}
