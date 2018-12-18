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
 * Created by Jan on 28-11-2015.
 */
public class SettingListAdapter extends ArrayAdapter<SettingItem> {

    protected static final String LOG_TAG = SettingListAdapter.class.getSimpleName();

    private List<SettingItem> mItems;
    private int mListItem;
    private Context mContext;

    public SettingListAdapter(Context pContext, int pListItem, List<SettingItem> pItems) {
        super(pContext, pListItem, pItems);
        mListItem = pListItem;
        mContext = pContext;
        mItems = pItems;
    }

    @Override
    public View getView(int pPos, View pView, ViewGroup pGroup) {
        View lRow = pView;
        TextView TxtTitle;
        TextView TxtAttr1Name;
        TextView TxtAttr1Value;
        TextView TxtAttr2Name;
        TextView TxtAttr2Value;
        TextView TxtAttr3Name;
        TextView TxtAttr3Value;
        TextView TxtAttr4Name;
        TextView TxtAttr4Value;
        SettingItem lItem;

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        lRow = inflater.inflate(mListItem, pGroup, false);

        TxtTitle = (TextView)lRow.findViewById(R.id.txtTitle);
        TxtAttr1Name = (TextView)lRow.findViewById(R.id.txtAttr1Name);
        TxtAttr1Value = (TextView)lRow.findViewById(R.id.txtAttr1Value);
        TxtAttr2Name = (TextView)lRow.findViewById(R.id.txtAttr2Name);
        TxtAttr2Value = (TextView)lRow.findViewById(R.id.txtAttr2Value);
        TxtAttr3Name = (TextView)lRow.findViewById(R.id.txtAttr3Name);
        TxtAttr3Value = (TextView)lRow.findViewById(R.id.txtAttr3Value);
        TxtAttr4Name = (TextView)lRow.findViewById(R.id.txtAttr4Name);
        TxtAttr4Value = (TextView)lRow.findViewById(R.id.txtAttr4Value);

        lItem = mItems.get(pPos);
        switch (lItem.xType()){
            case SettingItem.TypeLocation:
                TxtTitle.setText(R.string.title_location);
                TxtAttr1Name.setText(R.string.lb_longitude);
                TxtAttr1Value.setText(lItem.xAttr1());
                TxtAttr2Name.setText(R.string.lb_latitude);
                TxtAttr2Value.setText(lItem.xAttr2());
                TxtAttr3Name.setHeight(0);
                TxtAttr3Value.setHeight(0);
                TxtAttr4Name.setHeight(0);
                TxtAttr4Value.setHeight(0);
                break;
            case SettingItem.TypeLightsOff:
                TxtTitle.setText(R.string.title_lightsoff);
                TxtAttr1Name.setText(R.string.lb_starttime);
                TxtAttr1Value.setText(lItem.xAttr1());
                TxtAttr2Name.setText(R.string.lb_offperiod);
                TxtAttr2Value.setText(lItem.xAttr2());
                TxtAttr3Name.setHeight(0);
                TxtAttr3Value.setHeight(0);
                TxtAttr4Name.setHeight(0);
                TxtAttr4Value.setHeight(0);
                break;
            case SettingItem.TypeSensor:
                TxtTitle.setText(R.string.title_lightsensor);
                TxtAttr1Name.setText(R.string.lb_treshold);
                TxtAttr1Value.setText(lItem.xAttr1());
                TxtAttr2Name.setText(R.string.lb_maximum);
                TxtAttr2Value.setText(lItem.xAttr2());
                TxtAttr3Name.setText(R.string.lb_interval);
                TxtAttr3Value.setText(lItem.xAttr3());
                TxtAttr4Name.setText(R.string.lb_repeat);
                TxtAttr4Value.setText(lItem.xAttr4());
                break;
            default:
                TxtTitle.setText(R.string.title_unknown);
                TxtAttr1Name.setText("");
                TxtAttr1Value.setText("");
                TxtAttr2Name.setText("");
                TxtAttr2Value.setText("");
                break;
        }

        lRow.setTag(lItem);

        return lRow;
    }
}
