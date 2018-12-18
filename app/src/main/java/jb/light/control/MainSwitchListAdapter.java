package jb.light.control;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Jan on 18-9-2015.
 */
public class MainSwitchListAdapter extends ArrayAdapter<Switch> {

    protected static final String LOG_TAG = MainSwitchListAdapter.class.getSimpleName();

    private List<Switch> mSwitches;
    private int mSwitchItem;
    private Context mContext;

    public MainSwitchListAdapter(Context pContext, int pSwitchItem, List<Switch> pSwitches) {
        super(pContext, pSwitchItem, pSwitches);
        mSwitchItem = pSwitchItem;
        mContext = pContext;
        mSwitches = pSwitches;
    }

    @Override
    public View getView(int pPos, View pView, ViewGroup pGroup) {
        View lRow = pView;
        SwitchItemHandle lHandle = null;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        lRow = inflater.inflate(mSwitchItem, pGroup, false);

        lHandle = new SwitchItemHandle();
        lHandle.xChkSelect = (CheckBox)lRow.findViewById(R.id.chkSelect);
        lHandle.xTxtName = (TextView)lRow.findViewById(R.id.txtName);
        lHandle.xSwitch = mSwitches.get(pPos);
        lHandle.xTxtName.setTag(lHandle.xSwitch);

        lRow.setTag(lHandle);

        sSetItem(lHandle);
        return lRow;
    }

    private void sSetItem(SwitchItemHandle pHandle) {
        boolean lActive;

        pHandle.xTxtName.setText(pHandle.xSwitch.xName());
        lActive = pHandle.xSwitch.xActive();
        pHandle.xChkSelect.setEnabled(lActive);
    }

    public class SwitchItemHandle {
        Switch xSwitch;
        CheckBox xChkSelect;
        TextView xTxtName;
    }
}
