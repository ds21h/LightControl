package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
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
    private List<Switch> mSwitches;
    private int mLayout;
    private Context mContext;

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
            lHandle.xTxtName = lRow.findViewById(R.id.txtName);
            lHandle.xTxtType = lRow.findViewById(R.id.txtType);
            lHandle.xChkActive = lRow.findViewById(R.id.chkActive);
            lHandle.xTxtGroup = lRow.findViewById(R.id.txtGroup);
            lHandle.xTxtPoint = lRow.findViewById(R.id.txtPoint);
            lHandle.xTxtIP = lRow.findViewById(R.id.txtIP);
            lHandle.xTxtPause = lRow.findViewById(R.id.txtPause);
            lHandle.xLyoFM = lRow.findViewById((R.id.lyoFM));
            lHandle.xLyoIot = lRow.findViewById((R.id.lyoIot));

            lHandle.xParHide = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            lHandle.xParShow = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            lRow.setTag(lHandle);
        }

        sSetItem(lHandle, pPos);
        return lRow;
    }

    private void sSetItem(SwitchListHandle pHandle, int pPos) {
        pHandle.xSwitch = mSwitches.get(pPos);
        pHandle.xTxtName.setText(pHandle.xSwitch.xName());
        pHandle.xTxtType.setText(pHandle.xSwitch.xType());
        pHandle.xChkActive.setChecked(pHandle.xSwitch.xActive());
        if (pHandle.xSwitch.xType().equals("esp")){
            pHandle.xLyoFM.setLayoutParams(pHandle.xParHide);
            pHandle.xLyoIot.setLayoutParams(pHandle.xParShow);
            pHandle.xTxtIP.setText(pHandle.xSwitch.xIP());
            pHandle.xTxtGroup.setText("");
            pHandle.xTxtPoint.setText("");
        } else {
            pHandle.xLyoFM.setLayoutParams(pHandle.xParShow);
            pHandle.xLyoIot.setLayoutParams(pHandle.xParHide);
            pHandle.xTxtIP.setText("");
            pHandle.xTxtGroup.setText(pHandle.xSwitch.xGroup());
            pHandle.xTxtPoint.setText(pHandle.xSwitch.xPoint());
        }
        pHandle.xTxtPause.setText(String.valueOf(pHandle.xSwitch.xPause()));
    }

    static class SwitchListHandle{
        Switch xSwitch;
        TextView xTxtName;
        TextView xTxtType;
        CheckBox xChkActive;
        TextView xTxtGroup;
        TextView xTxtPoint;
        TextView xTxtIP;
        TextView xTxtPause;
        LinearLayout xLyoFM;
        LinearLayout xLyoIot;
        LinearLayout.LayoutParams xParHide;
        LinearLayout.LayoutParams xParShow;
    }
}
