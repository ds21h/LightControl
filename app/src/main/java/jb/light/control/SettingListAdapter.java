package jb.light.control;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

/**
 * Created by Jan on 28-11-2015.
 */
public class SettingListAdapter extends ArrayAdapter<SettingItem> {
    private final List<SettingItem> mItems;
    private final int mLayout;
    private final Context mContext;

    SettingListAdapter(Context pContext, int pLayout, List<SettingItem> pItems) {
        super(pContext, pLayout, pItems);
        mLayout = pLayout;
        mContext = pContext;
        mItems = pItems;
    }

    @Override
    public @NonNull View getView(int pPos, View pView, @NonNull ViewGroup pGroup) {
        View lRow;
        SettingListHandle lHandle;
        Object lTag = null;
        boolean lRecycle;
        LinearLayout.LayoutParams lBaseParam;

        if (pView == null) {
            lRecycle = false;
        } else {
            lTag = pView.getTag();
            lRecycle = lTag instanceof SettingListHandle;
        }
        if (lRecycle) {
            lHandle = (SettingListHandle)lTag;
            lRow = pView;
        } else {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            lRow = inflater.inflate(mLayout, pGroup, false);

            lHandle = new SettingListHandle();
            lHandle.xTxtTitle = lRow.findViewById(R.id.txtTitle);
            lHandle.xTxtAttr1Name = lRow.findViewById(R.id.txtAttr1Name);
            lHandle.xTxtAttr1Value = lRow.findViewById(R.id.txtAttr1Value);
            lHandle.xTxtAttr2Name = lRow.findViewById(R.id.txtAttr2Name);
            lHandle.xTxtAttr2Value = lRow.findViewById(R.id.txtAttr2Value);
            lHandle.xTxtAttr3Name = lRow.findViewById(R.id.txtAttr3Name);
            lHandle.xTxtAttr3Value = lRow.findViewById(R.id.txtAttr3Value);
            lHandle.xTxtAttr4Name = lRow.findViewById(R.id.txtAttr4Name);
            lHandle.xTxtAttr4Value = lRow.findViewById(R.id.txtAttr4Value);
            lHandle.xLyoLine3 = lRow.findViewById(R.id.lyoLine3);
            lHandle.xLyoLine4 = lRow.findViewById(R.id.lyoLine4);

            lBaseParam = (LinearLayout.LayoutParams) lHandle.xLyoLine3.getLayoutParams();
            lHandle.xParHide = new LinearLayout.LayoutParams(lHandle.xLyoLine3.getLayoutParams());
            lHandle.xParHide.height = 0;
            lHandle.xParShow = new LinearLayout.LayoutParams(lHandle.xLyoLine3.getLayoutParams());
            lHandle.xParShow.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            lHandle.xParShow.leftMargin = lBaseParam.leftMargin;

            lRow.setTag(lHandle);
        }

        sSetItem(lHandle, pPos);
        return lRow;
    }

    private void sSetItem(SettingListHandle pHandle, int pPos) {
        pHandle.xItem = mItems.get(pPos);
        switch (pHandle.xItem.xType()){
            case SettingItem.TypeLocation:
                pHandle.xTxtTitle.setText(R.string.title_location);
                pHandle.xTxtAttr1Name.setText(R.string.lb_longitude);
                pHandle.xTxtAttr1Value.setText(pHandle.xItem.xAttr1());
                pHandle.xTxtAttr2Name.setText(R.string.lb_latitude);
                pHandle.xTxtAttr2Value.setText(pHandle.xItem.xAttr2());
                pHandle.xLyoLine3.setLayoutParams(pHandle.xParHide);
                pHandle.xLyoLine4.setLayoutParams(pHandle.xParHide);
                break;
            case SettingItem.TypeLightsOff:
                pHandle.xTxtTitle.setText(R.string.title_lightsoff);
                pHandle.xTxtAttr1Name.setText(R.string.lb_starttime);
                pHandle.xTxtAttr1Value.setText(pHandle.xItem.xAttr1());
                pHandle.xTxtAttr2Name.setText(R.string.lb_offperiod);
                pHandle.xTxtAttr2Value.setText(pHandle.xItem.xAttr2());
                pHandle.xLyoLine3.setLayoutParams(pHandle.xParHide);
                pHandle.xLyoLine4.setLayoutParams(pHandle.xParHide);
                break;
            case SettingItem.TypeSensor:
                pHandle.xTxtTitle.setText(R.string.title_lightsensor);
                pHandle.xTxtAttr1Name.setText(R.string.lb_treshold);
                pHandle.xTxtAttr1Value.setText(pHandle.xItem.xAttr1());
                pHandle.xTxtAttr2Name.setText(R.string.lb_interval);
                pHandle.xTxtAttr2Value.setText(pHandle.xItem.xAttr2());
                pHandle.xTxtAttr3Name.setText(R.string.lb_repeat);
                pHandle.xLyoLine3.setLayoutParams(pHandle.xParShow);
                pHandle.xTxtAttr3Value.setText(pHandle.xItem.xAttr3());
                pHandle.xLyoLine4.setLayoutParams(pHandle.xParHide);
                break;
            default:
                pHandle.xTxtTitle.setText(R.string.title_unknown);
                pHandle.xTxtAttr1Name.setText("");
                pHandle.xTxtAttr1Value.setText("");
                pHandle.xTxtAttr2Name.setText("");
                pHandle.xTxtAttr2Value.setText("");
                pHandle.xLyoLine3.setLayoutParams(pHandle.xParHide);
                pHandle.xLyoLine4.setLayoutParams(pHandle.xParHide);
                break;
        }
    }

    static class SettingListHandle{
        SettingItem xItem;
        TextView xTxtTitle;
        TextView xTxtAttr1Name;
        TextView xTxtAttr1Value;
        TextView xTxtAttr2Name;
        TextView xTxtAttr2Value;
        TextView xTxtAttr3Name;
        TextView xTxtAttr3Value;
        TextView xTxtAttr4Name;
        TextView xTxtAttr4Value;
        LinearLayout xLyoLine3;
        LinearLayout xLyoLine4;
        LinearLayout.LayoutParams xParHide;
        LinearLayout.LayoutParams xParShow;
    }
}
