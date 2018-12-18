package jb.light.control;

/**
 * Created by Jan on 17-11-2015.
 */
public class SwitchX extends Switch {
    public static final String cAction = "action";

    public static final String ActionNone = "none";
    public static final String ActionNew = "new";
    public static final String ActionModify = "modify";
    public static final String ActionDelete = "delete";

    private String mAction = ActionNone;

    public SwitchX(String pName){
        super();
        xName(pName);
        mAction = ActionNone;
    }

    public SwitchX(Switch pSwitch) {
        super(pSwitch.xSeqNumber(), pSwitch.xName(), pSwitch.xActive(), pSwitch.xType(), pSwitch.xGroup(), pSwitch.xPoint(), pSwitch.xPause(), pSwitch.xIP());
        mAction = ActionNone;
    }

    public String xAction(){
        return mAction;
    }

    public void xAction(String pAction){
        mAction = pAction;
    }
}
