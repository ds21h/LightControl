package jb.light.control;

/**
 * Created by Jan on 18-9-2015.
 */
class Action {
    static final int ActionNone = 0;
    static final int ActionSwitchOn = 1;
    static final int ActionSwitchOff = 2;
    static final int ActionLightsOff = 3;
    static final int ActionInqSwitch = 4;

    int xAction;
    String xValue;
    String xIP;

    Action(int pAction){
        xAction = pAction;
        xValue = "";
        xIP = "";
    }
}
