package jb.light.control;

import androidx.annotation.NonNull;

/**
 * Created by Jan on 17-11-2015.
 */
class SwitchX extends Switch {
    public static final String cAction = "action";

    static final String ActionNone = "none";
    static final String ActionNew = "new";
    static final String ActionModify = "modify";
    static final String ActionDelete = "delete";

    private String mAction = ActionNone;

    SwitchX(String pName){
        super();
        xName(pName);
        mAction = ActionNone;
    }

    SwitchX(@NonNull Switch pSwitch) {
        super(pSwitch.xSeqNumber(), pSwitch.xName(), pSwitch.xActive(), pSwitch.xType(), pSwitch.xGroup(), pSwitch.xPoint(), pSwitch.xPause(), pSwitch.xIP());
        mAction = ActionNone;
    }

    String xAction(){
        return mAction;
    }

    void xAction(String pAction){
        mAction = pAction;
    }
}
