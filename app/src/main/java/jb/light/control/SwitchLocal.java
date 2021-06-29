package jb.light.control;

import androidx.annotation.NonNull;

import org.json.JSONObject;

/**
 * Created by Jan on 17-11-2015.
 */
class SwitchLocal extends Switch {
    public static final String cAction = "action";

    static final String ActionNone = "none";
    static final String ActionNew = "new";
    static final String ActionModify = "modify";
    static final String ActionDelete = "delete";
    static final String ActionOn = "switchon";
    static final String ActionOff = "switchoff";

    static final int StatusOff = 0;
    static final int StatusOn = 1;
    static final int StatusNone = 9;

    private String mAction;
    private int mStatus;
    private boolean mChanged;

    SwitchLocal(int pSeqNumber, String pName, boolean pActive, int pPause, String pIP) {
        super(pSeqNumber, pName, pActive, pPause, pIP);
        mAction = ActionNone;
        mStatus = StatusNone;
        mChanged = false;
    }

    SwitchLocal(JSONObject pSwitch) {
        super(pSwitch);
        mAction = ActionNone;
        mStatus = StatusNone;
        mChanged = false;
    }

    SwitchLocal() {
        super();
        mAction = ActionNone;
        mStatus = StatusNone;
        mChanged = false;
    }

    SwitchLocal(String pName) {
        super();
        xName(pName);
        mAction = ActionNone;
        mStatus = StatusNone;
        mChanged = false;
    }

    SwitchLocal(@NonNull SwitchLocal pSwitch) {
        super(pSwitch.xSeqNumber(), pSwitch.xName(), pSwitch.xActive(), pSwitch.xPause(), pSwitch.xIP());
        mAction = pSwitch.mAction;
        mStatus = pSwitch.mStatus;
        mChanged = false;
    }

    String xAction() {
        return mAction;
    }

    void xAction(String pAction) {
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

    void xChangeInit() {
        mChanged = false;
    }

    boolean xChanged() {
        return mChanged;
    }

}
