package jb.light.control;

import android.app.Activity;
import android.content.Context;
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

    protected static final String LOG_TAG = ManageSwitchListAdapter.class.getSimpleName();

    private List<Switch> mSwitches;
    private int mSwitchItem;
    private Context mContext;

    public ManageSwitchListAdapter(Context pContext, int pSwitchItem, List<Switch> pSwitches) {
        super(pContext, pSwitchItem, pSwitches);
        mSwitchItem = pSwitchItem;
        mContext = pContext;
        mSwitches = pSwitches;
    }

    @Override
    public View getView(int pPos, View pView, ViewGroup pGroup) {
        View lRow = pView;
        TextView lTxtName;
        TextView lTxtType;
        CheckBox lChkActive;
        TextView lTxtGroup;
        TextView lTxtPoint;
        TextView lTxtIP;
        TextView lTxtPause;
        Switch lSwitch;
        LinearLayout lLyoFM;
        LinearLayout lLyoIot;
        LinearLayout.LayoutParams lParams;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        lRow = inflater.inflate(mSwitchItem, pGroup, false);

        lTxtName = (TextView)lRow.findViewById(R.id.txtName);
        lTxtType = (TextView)lRow.findViewById(R.id.txtType);
        lChkActive = (CheckBox)lRow.findViewById(R.id.chkActive);
        lTxtGroup = (TextView)lRow.findViewById(R.id.txtGroup);
        lTxtPoint = (TextView)lRow.findViewById(R.id.txtPoint);
        lTxtIP = (TextView)lRow.findViewById(R.id.txtIP);
        lTxtPause = (TextView)lRow.findViewById(R.id.txtPause);
        lLyoFM = (LinearLayout)lRow.findViewById((R.id.lyoFM));
        lLyoIot = (LinearLayout)lRow.findViewById((R.id.lyoIot));

        lSwitch = mSwitches.get(pPos);
        lTxtName.setText(lSwitch.xName());
        lTxtType.setText(lSwitch.xType());
        lChkActive.setChecked(lSwitch.xActive());
        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        if (lSwitch.xType().equals("esp")){
            lLyoFM.setLayoutParams(lParams);
            lTxtIP.setText(lSwitch.xIP());
        } else {
            lLyoIot.setLayoutParams(lParams);
            lTxtGroup.setText(lSwitch.xGroup());
            lTxtPoint.setText(lSwitch.xPoint());
        }
        lTxtPause.setText(String.valueOf(lSwitch.xPause()));

        lRow.setTag(lSwitch);

        return lRow;
    }
}
