package jb.light.control;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jan on 18-9-2015.
 */
class Action {
    private static final int ActionNone = 0;
    static final int ActionSwitchOn = 1;
    static final int ActionSwitchOff = 2;
    static final int ActionLightsOff = 3;
    static final int ActionInqSwitch = 4;

    private int mAction;
    private String mValue;
    private String mIP;

    Action(int pAction){
        mAction = pAction;
        mValue = "";
        mIP = "";
    }

    int xAction(){
        return mAction;
    }

    String xValue(){
        return mValue;
    }

    void xValue(String pValue){
        mValue = pValue;
    }

    String xIP(){
        return mIP;
    }

    void xIP(String pIP){
        mIP = pIP;
    }
}
