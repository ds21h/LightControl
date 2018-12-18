package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Jan on 18-9-2015.
 */
public class ServerListAdapter extends ArrayAdapter<Server> {

    protected static final String LOG_TAG = ServerListAdapter.class.getSimpleName();

    private List<Server> mServers;
    private int mServerItem;
    private Context mContext;

    public ServerListAdapter(Context pContext, int pServerItem, List<Server> pServers) {
        super(pContext, pServerItem, pServers);
        mServerItem = pServerItem;
        mContext = pContext;
        mServers = pServers;
    }

    @Override
    public View getView(int pPos, View pView, ViewGroup pGroup) {
        View lRow = pView;
        ServerHandle lHandle = null;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        lRow = inflater.inflate(mServerItem, pGroup, false);

        lHandle = new ServerHandle();
        lHandle.xTxtName = (TextView)lRow.findViewById(R.id.txtName);
        lHandle.xTxtNet = (TextView)lRow.findViewById(R.id.txtNet);
        lHandle.xTxtIp = (TextView)lRow.findViewById(R.id.txtIPaddress);
        lHandle.xTxtPort = (TextView)lRow.findViewById(R.id.txtPort);
        lHandle.xServer = mServers.get(pPos);

        lRow.setTag(lHandle);

        sSetItem(lHandle);
        return lRow;
    }

    private void sSetItem(ServerHandle pHandle) {
        pHandle.xTxtName.setText(pHandle.xServer.xName());
        pHandle.xTxtNet.setText(pHandle.xServer.xSSId());
        pHandle.xTxtIp.setText(pHandle.xServer.xIP());
        pHandle.xTxtPort.setText(pHandle.xServer.xPort());
    }

    public class ServerHandle {
        Server xServer;
        TextView xTxtName;
        TextView xTxtNet;
        TextView xTxtIp;
        TextView xTxtPort;
    }
}
