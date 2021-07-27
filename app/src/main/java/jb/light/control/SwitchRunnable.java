package jb.light.control;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class SwitchRunnable implements Runnable {
    private final SwitchLocal mSwitch;
    private final CountDownLatch mCountDown;
    private final Handler mHandler;

    SwitchRunnable(SwitchLocal pSwitch, CountDownLatch pCountDown){
        mSwitch = pSwitch;
        mCountDown = pCountDown;
        mHandler = null;
    }

    SwitchRunnable(SwitchLocal pSwitch, Handler pHandler){
        mSwitch = pSwitch;
        mCountDown = null;
        mHandler = pHandler;
    }

    @Override

    public void run() {
        String lRequest;
        RestAPI.RestResult lOutput;
        RestAPI lRestAPI;
        String lResultString;
        String lStatusString;
        String lButtonString;
        JSONObject lCommand;
        String lCommandS = "";
        boolean lActionCall;
        int lResult;

        lResult = HandlerCode.cSwitch;
        lCommand = new JSONObject();
        lRestAPI = new RestAPI();
        lRequest = "http://" + mSwitch.xIP() + URIs.UriSwitch;
        lRestAPI.xUrl(lRequest);
        lActionCall = false;
        boolean lRequestOK;
        try {
            if (mSwitch.xAction() == SwitchLocal.ActionOn){
                lCommand.put("status", "on");
                lActionCall = true;
            } else {
                if (mSwitch.xAction() == SwitchLocal.ActionOff){
                    lCommand.put("status", "off");
                    lActionCall = true;
                }
            }
            if (lActionCall){
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
            if (lOutput.xResult()  == Result.cResultOK){
                lResult |= HandlerCode.cCommunicationOK;
                lResultString = lOutput.xReplyJ().optString("result", "NOK");
                if (lResultString.equals("OK")){
                    lStatusString = lOutput.xReplyJ().optString("status", "");
                    lButtonString = lOutput.xReplyJ().optString("button", "");
                    mSwitch.xStatus(lStatusString.equals("on") ? SwitchLocal.StatusOn : SwitchLocal.StatusOff);
                    mSwitch.xButton(lButtonString.equals(("on")));
                    lResult |= HandlerCode.cProcessOK;
                } else {
                    mSwitch.xStatus(SwitchLocal.StatusNone);
                }
            } else {
                mSwitch.xStatus(SwitchLocal.StatusNone);
            }
        }
        if (mCountDown != null){
            mCountDown.countDown();
        }
        if (mHandler != null){
            mHandler.sendEmptyMessage(lResult);
        }
    }
}
