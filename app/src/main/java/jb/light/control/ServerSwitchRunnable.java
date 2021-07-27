package jb.light.control;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class ServerSwitchRunnable implements Runnable {
    private final SwitchLocal mSwitch;
    private final String mServerAddress;
    private final Handler mHandler;

    ServerSwitchRunnable(SwitchLocal pSwitch, String pServerAddress, Handler pHandler){
        mSwitch = pSwitch;
        mServerAddress = pServerAddress;
        mHandler = pHandler;
    }

    @Override

    public void run() {
        String lRequest;
        RestAPI.RestResult lOutput;
        RestAPI lRestAPI;
        String lResultString;
        JSONObject lCommand;
        String lCommandS;
        int lResult;

        lResult = HandlerCode.cServerSwitch;
        lCommand = new JSONObject();
        try{
            lCommand.put("action", mSwitch.xActionText());
            lCommand.put("lang", Locale.getDefault().getLanguage());
            lCommand.put("switch", mSwitch.xSwitch());
            lCommandS = lCommand.toString();
        } catch (JSONException pExc){
            lCommandS = "";
        }
        if (!lCommandS.equals("")){
            lResult |= HandlerCode.cRequestOK;
            lRequest = mServerAddress + URIs.UriServerSwitch + mSwitch.xName();
            lRestAPI = new RestAPI();
            lRestAPI.xMethod(RestAPI.cMethodPut);
            lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
            lRestAPI.xMediaReply(RestAPI.cMediaJSON);
            lRestAPI.xUrl(lRequest);
            lRestAPI.xAction(lCommandS);
            lOutput = lRestAPI.xCallApi();

            if (lOutput.xResult()  == Result.cResultOK){
                lResult |= HandlerCode.cCommunicationOK;
                lResultString = lOutput.xReplyJ().optString("result", "NOK");
                if (lResultString.equals("OK")){
                    lResult |= HandlerCode.cProcessOK;
                }
            }
        }
        mHandler.sendEmptyMessage(lResult);
    }
}
