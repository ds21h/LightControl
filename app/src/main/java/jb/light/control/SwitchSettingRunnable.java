package jb.light.control;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class SwitchSettingRunnable implements Runnable {
    private final SwitchLocal mSwitch;
    private final Handler mHandler;

    SwitchSettingRunnable(SwitchLocal pSwitch, Handler pHandler) {
        mSwitch = pSwitch;
        mHandler = pHandler;
    }

    @Override
    public void run() {
        String lRequest;
        RestAPI.RestResult lOutput;
        RestAPI lRestAPI;
        String lResultString;
        String lButtonString;
        JSONObject lCommand;
        String lCommandS = "";
        boolean lActionCall;
        boolean lRequestOK;
        int lResult;

        lResult = HandlerCode.cSwitchSetting;
        lCommand = new JSONObject();
        lRestAPI = new RestAPI();
        lRequest = "http://" + mSwitch.xIP() + URIs.UriSetting;
        lRestAPI.xUrl(lRequest);
        lActionCall = false;
        try {
            if (mSwitch.xAction() == SwitchLocal.ActionButtonOn) {
                lCommand.put("button", "on");
                lActionCall = true;
            } else {
                if (mSwitch.xAction() == SwitchLocal.ActionButtonOff) {
                    lCommand.put("button", "off");
                    lActionCall = true;
                }
            }
            if (lActionCall) {
                lCommandS = lCommand.toString();
                lRestAPI.xMethod(RestAPI.cMethodPut);
                lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
            } else {
                lCommandS = "";
                lRestAPI.xMethod(RestAPI.cMethodGet);
            }
            lRequestOK = true;
        } catch (JSONException pExc) {
            lRequestOK = false;
        }
        if (lRequestOK){
            lResult |= HandlerCode.cRequestOK;
            mSwitch.xAction(SwitchLocal.ActionNone);
            lRestAPI.xMediaReply(RestAPI.cMediaJSON);
            lRestAPI.xAction(lCommandS);
            lRestAPI.xTimeOut(2000);
            lRestAPI.xMediaReply(RestAPI.cMediaJSON);
            lOutput = lRestAPI.xCallApi();
            if (lOutput.xResult() == Result.cResultOK) {
                lResult |= HandlerCode.cCommunicationOK;
                lResultString = lOutput.xReplyJ().optString("result", "NOK");
                if (lResultString.equals("OK")){
                    lButtonString = lOutput.xReplyJ().optString("button", "");
                    mSwitch.xButton(lButtonString.equals("on"));
                    lResult |= HandlerCode.cProcessOK;
                }
            }

        }
        mHandler.sendEmptyMessage(lResult);
    }

}
