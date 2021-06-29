package jb.light.control;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

public class SwitchRunnable implements Runnable {
    static final int cSwitchProcessed = 1;
    static final int cSwitchChanged = 2;

    private final SwitchLocal mSwitch;
    private final CountDownLatch mCountDown;

    SwitchRunnable(SwitchLocal pSwitch, CountDownLatch pCountDown){
        mSwitch = pSwitch;
        mCountDown = pCountDown;
    }

    @Override

    public void run() {
        String lRequest;
        RestAPI.RestResult lOutput;
        RestAPI lRestAPI;
        String lStatusString;
        JSONObject lCommand;
        String lCommandS;

        lCommand = new JSONObject();
        lRestAPI = new RestAPI();
        lRequest = "http://" + mSwitch.xIP() + URIs.UriSwitch;
        lRestAPI.xUrl(lRequest);
        try {
            switch (mSwitch.xAction()) {
                case SwitchLocal.ActionOn: {
                    lCommand.put("status", "on");
                    lCommandS = lCommand.toString();
                    lRestAPI.xMethod(RestAPI.cMethodPut);
                    lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
                    break;
                }
                case SwitchLocal.ActionOff: {
                    lCommand.put("status", "off");
                    lCommandS = lCommand.toString();
                    lRestAPI.xMethod(RestAPI.cMethodPut);
                    lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
                    break;
                }
                default: {
                    lCommandS = "";
                    lRestAPI.xMethod(RestAPI.cMethodGet);
                    break;
                }
            }
        } catch (JSONException pExc) {
            lCommandS = "";
            lRestAPI.xMethod(RestAPI.cMethodGet);
        }
        lRestAPI.xMediaReply(RestAPI.cMediaJSON);
        lRestAPI.xAction(lCommandS);
        lRestAPI.xTimeOut(2000);
        lRestAPI.xMediaReply(RestAPI.cMediaJSON);
        lOutput = lRestAPI.xCallApi();
        if (lOutput.xResult()  == Result.cResultOK){
            lStatusString = lOutput.xReplyJ().optString("status", "");
            switch (lStatusString){
                case "on":{
                    mSwitch.xStatus(SwitchLocal.StatusOn);
                    break;
                }
                case "off":{
                    mSwitch.xStatus(SwitchLocal.StatusOff);
                    break;
                }
                default:{
                    mSwitch.xStatus(SwitchLocal.StatusNone);
                    break;
                }
            }
        } else {
            mSwitch.xStatus(SwitchLocal.StatusNone);
        }
        mCountDown.countDown();
    }
}
