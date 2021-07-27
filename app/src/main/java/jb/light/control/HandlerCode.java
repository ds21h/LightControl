package jb.light.control;

import android.content.Context;
import android.widget.Toast;

class HandlerCode {
//  Runnable ID
    static final int cControl = 0x0100;
    static final int cServerSwitch = 0x0200;
    static final int cSwitch = 0x0400;
    static final int cSwitchSetting = 0x0800;
    static final int cGetServerSetting = 0x1000;
    static final int cPutServerSetting = 0x2000;

//  Process code
    static final int cRequestOK = 0x01;
    static final int cCommunicationOK = 0x02;
    static final int cProcessOK = 0x04;

    static final int cSurveyChange = 0x10;
    static final int cSwitchChange = 0x20;
    static final int cStatusChange = 0x40;

    private HandlerCode(){}

    static String xCheckCode(Context pContext, int pWhat){
        String lResult;

        if ((pWhat & HandlerCode.cRequestOK) != 0) {
            if ((pWhat & HandlerCode.cCommunicationOK) != 0) {
                if ((pWhat & HandlerCode.cProcessOK) != 0) {
                    lResult = null;
                } else {
                    lResult = pContext.getString(R.string.msg_server_error);
                }
            } else {
                lResult = pContext.getString(R.string.msg_comm_error);
            }
        } else {
            lResult = pContext.getString(R.string.msg_int_error);
        }
        return lResult;
    }
}
