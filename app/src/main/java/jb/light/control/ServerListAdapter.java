package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
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
    private List<Server> mServers;
    private int mLayout;
    private Context mContext;

    ServerListAdapter(Context pContext, int pLayout, List<Server> pServers) {
        super(pContext, pLayout, pServers);
        mLayout = pLayout;
        mContext = pContext;
        mServers = pServers;
    }

    @Override
    public @NonNull View getView(int pPos, View pView, @NonNull ViewGroup pGroup) {
        View lRow;
        ServerHandle lHandle;
        Object lTag = null;
        boolean lRecycle;

        if (pView == null) {
            lRecycle = false;
        } else {
            lTag = pView.getTag();
            lRecycle = lTag instanceof ServerHandle;
        }
        if (lRecycle) {
            lHandle = (ServerHandle)lTag;
            lRow = pView;
        } else {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            lRow = inflater.inflate(mLayout, pGroup, false);

            lHandle = new ServerHandle();
            lHandle.xTxtName = lRow.findViewById(R.id.txtName);
            lHandle.xTxtNet = lRow.findViewById(R.id.txtNet);
            lHandle.xTxtIp = lRow.findViewById(R.id.txtIPaddress);
            lHandle.xTxtPort = lRow.findViewById(R.id.txtPort);

            lRow.setTag(lHandle);
        }

        sSetItem(lHandle, pPos);
        return lRow;
    }

    private void sSetItem(ServerHandle pHandle, int pPos) {
        pHandle.xServer = mServers.get(pPos);
        pHandle.xTxtName.setText(pHandle.xServer.xName());
        pHandle.xTxtNet.setText(pHandle.xServer.xSSId());
        pHandle.xTxtIp.setText(pHandle.xServer.xIP());
        pHandle.xTxtPort.setText(pHandle.xServer.xPort());
    }

    static class ServerHandle {
        Server xServer;
        TextView xTxtName;
        TextView xTxtNet;
        TextView xTxtIp;
        TextView xTxtPort;
    }
}
