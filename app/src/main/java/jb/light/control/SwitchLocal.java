package jb.light.control;

import androidx.annotation.NonNull;

import org.json.JSONObject;

/**
 * Created by Jan on 17-11-2015.
 */
class SwitchLocal extends Switch {
    public static final String cAction = "action";

    static final int ActionNone = 0;
    static final int ActionNew = 1;
    static final int ActionModify = 2;
    static final int ActionDelete = 3;
    static final int ActionOn = 4;
    static final int ActionOff = 5;
    static final int ActionButtonOn = 6;
    static final int ActionButtonOff = 7;

    static final int StatusOff = 0;
    static final int StatusOn = 1;
    static final int StatusNone = 9;

    private final String[] cActionText = {"none", "new", "modify", "delete", "on", "off", "buttonon", "buttonoff"};

    private int mAction;
    private int mStatus;
    private boolean mButton;
    private boolean mChanged;

    SwitchLocal(int pSeqNumber, String pName, boolean pActive, int pPause, String pIP) {
        super(pSeqNumber, pName, pActive, pPause, pIP);
        mAction = ActionNone;
        mStatus = StatusNone;
        mButton = false;
        mChanged = false;
    }

    SwitchLocal(JSONObject pSwitch) {
        super(pSwitch);
        mAction = ActionNone;
        mStatus = StatusNone;
        mButton = false;
        mChanged = false;
    }

    SwitchLocal() {
        super();
        mAction = ActionNone;
        mStatus = StatusNone;
        mButton = false;
        mChanged = false;
    }

    SwitchLocal(String pName) {
        super();
        xName(pName);
        mAction = ActionNone;
        mStatus = StatusNone;
        mButton = false;
        mChanged = false;
    }

    SwitchLocal(@NonNull SwitchLocal pSwitch) {
        super(pSwitch.xSeqNumber(), pSwitch.xName(), pSwitch.xActive(), pSwitch.xPause(), pSwitch.xIP());
        mAction = pSwitch.mAction;
        mStatus = pSwitch.mStatus;
        mButton = pSwitch.mButton;
        mChanged = false;
    }

    int xAction() {
        return mAction;
    }

    String xActionText(){
        if (mAction < 0 || mAction >= cActionText.length){
            return "";
        } else {
            return cActionText[mAction];
        }
    }

    void xAction(int pAction) {
        mAction = pAction;
    }

    int xStatus() {
        return mStatus;
    }

    void xStatus(int pStatus) {
        if (mStatus != pStatus){
            mStatus = pStatus;
            mChanged = true;
        }
    }

    boolean xButton(){
        return mButton;
    }

    void xButton(boolean pButton){
        mButton = pButton;
    }

    void xChangeInit() {
        mChanged = false;
    }

    boolean xChanged() {
        return mChanged;
    }

}
