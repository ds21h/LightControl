package jb.light.control;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jan on 18-9-2015.
 */
public class Action implements Parcelable {
    private static final int ActionNone = 0;
    public static final int ActionSwitchOn = 1;
    public static final int ActionSwitchOff = 2;
    public static final int ActionLightsOff = 3;

    private int mAction;
    private String mValue;
    private String mIP;
    private boolean mProcessed[] = {false};

    @Override
    public void writeToParcel(Parcel pOut, int pFlags) {
        pOut.writeInt(mAction);
        pOut.writeString(mValue);
        pOut.writeString(mIP);
        pOut.writeBooleanArray(mProcessed);
    }

    private Action(Parcel pIn){
        mAction = pIn.readInt();
        mValue = pIn.readString();
        mIP = pIn.readString();
        pIn.readBooleanArray(mProcessed);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    public static final Parcelable.Creator<Action> CREATOR
            = new Parcelable.Creator<Action>() {
        @Override
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }
        @Override
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };

    public Action(int pAction){
        mAction = pAction;
        mValue = "";
        mIP = "";
        mProcessed[0] = false;
    }

    public int xAction(){
        return mAction;
    }

    public String xValue(){
        return mValue;
    }

    public void xValue(String pValue){
        mValue = pValue;
    }

    public String xIP(){
        return mIP;
    }

    public void xIP(String pIP){
        mIP = pIP;
    }

    public boolean xProcessed(){
        return mProcessed[0];
    }

    public void xProcessed(boolean pProcessed){
        mProcessed[0] = pProcessed;
    }

    public boolean xToServer(){
        if (mAction == ActionSwitchOn || mAction == ActionSwitchOff){
            if (mIP.equals("")){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
