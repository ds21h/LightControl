package jb.light.control;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Jan on 18-9-2015.
 */
public class ManageSwitchListAdapter extends ArrayAdapter<Switch> {
    private final List<Switch> mSwitches;
    private final int mLayout;
    private final Context mContext;

    ManageSwitchListAdapter(Context pContext, int pLayout, List<Switch> pSwitches) {
        super(pContext, pLayout, pSwitches);
        mLayout = pLayout;
        mContext = pContext;
        mSwitches = pSwitches;
    }

    @Override
    public @NonNull View getView(int pPos, View pView, @NonNull ViewGroup pGroup) {
        View lRow;
        SwitchListHandle lHandle;
        Object lTag = null;
        boolean lRecycle;

        if (pView == null) {
            lRecycle = false;
        } else {
            lTag = pView.getTag();
            lRecycle = lTag instanceof SwitchListHandle;
        }
        if (lRecycle) {
            lHandle = (SwitchListHandle)lTag;
            lRow = pView;
        } else {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            lRow = inflater.inflate(mLayout, pGroup, false);

            lHandle = new SwitchListHandle();
            lHandle.xSeq = lRow.findViewById(R.id.txtSeq);
            lHandle.xTxtName = lRow.findViewById(R.id.txtName);
            lHandle.xChkActive = lRow.findViewById(R.id.chkActive);
            lHandle.xTxtIP = lRow.findViewById(R.id.txtIP);
            lHandle.xTxtPause = lRow.findViewById(R.id.txtPause);

            lRow.setTag(lHandle);
        }

        sSetItem(lHandle, pPos);
        return lRow;
    }

    private void sSetItem(SwitchListHandle pHandle, int pPos) {
        pHandle.xSwitch = mSwitches.get(pPos);
        pHandle.xSeq.setText(String.valueOf(pHandle.xSwitch.xSeqNumber()));
        pHandle.xTxtName.setText(pHandle.xSwitch.xName());
        pHandle.xChkActive.setChecked(pHandle.xSwitch.xActive());
        pHandle.xTxtIP.setText(pHandle.xSwitch.xIP());
        pHandle.xTxtPause.setText(String.valueOf(pHandle.xSwitch.xPause()));
    }

    static class SwitchListHandle{
        Switch xSwitch;
        TextView xSeq;
        TextView xTxtName;
        CheckBox xChkActive;
        TextView xTxtIP;
        TextView xTxtPause;
    }
}
