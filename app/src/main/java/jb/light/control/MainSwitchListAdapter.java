package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Jan on 18-9-2015.
 */
public class MainSwitchListAdapter extends ArrayAdapter<Switch> {
    private List<Switch> mSwitches;
    private int mLayout;
    private Context mContext;

    MainSwitchListAdapter(Context pContext, int pLayout, List<Switch> pSwitches) {
        super(pContext, pLayout, pSwitches);
        mLayout = pLayout;
        mContext = pContext;
        mSwitches = pSwitches;
    }

    @Override
    public @NonNull View getView(int pPos, View pView, @NonNull ViewGroup pGroup) {
        View lRow;
        SwitchItemHandle lHandle;
        Object lTag = null;
        boolean lRecycle;

        if (pView == null) {
            lRecycle = false;
        } else {
            lTag = pView.getTag();
            lRecycle = lTag instanceof SwitchItemHandle;
        }
        if (lRecycle) {
            lHandle = (SwitchItemHandle)lTag;
            lRow = pView;
        } else {

            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            lRow = inflater.inflate(mLayout, pGroup, false);

            lHandle = new SwitchItemHandle();
            lHandle.xChkSelect = lRow.findViewById(R.id.chkSelect);
            lHandle.xTxtName = lRow.findViewById(R.id.txtName);
            lHandle.xImgStatus = lRow.findViewById(R.id.imgLight);
            lHandle.xTxtName.setTag(lHandle.xSwitch);

            lRow.setTag(lHandle);
        }
        sSetItem(lHandle, pPos);
        return lRow;
    }

    private void sSetItem(SwitchItemHandle pHandle, int pPos) {
        boolean lActive;

        pHandle.xSwitch = mSwitches.get(pPos);
        pHandle.xTxtName.setText(pHandle.xSwitch.xName());
        lActive = pHandle.xSwitch.xActive();
        pHandle.xChkSelect.setEnabled(lActive);
    }

    static class SwitchItemHandle {
        Switch xSwitch;
        CheckBox xChkSelect;
        TextView xTxtName;
        ImageView xImgStatus;
    }
}
