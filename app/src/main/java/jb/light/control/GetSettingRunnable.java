package jb.light.control;

import android.os.Handler;

public class GetSettingRunnable implements Runnable {
    private final Handler mHandler;
    private final Setting mSetting;
    String mServerAddress;

    GetSettingRunnable(Handler pHandler, Setting pSetting, String pServerAddress) {
        mHandler = pHandler;
        mSetting = pSetting;
        mServerAddress = pServerAddress;
    }

    @Override
    public void run() {
        String lRequest;
        RestAPI.RestResult lOutput;
        RestAPI lRestAPI;
        int lResult;

        lResult = HandlerCode.cGetServerSetting;
        lRequest = mServerAddress + URIs.UriServerSetting;
        lRestAPI = new RestAPI();
        lRestAPI.xMethod(RestAPI.cMethodGet);
        lRestAPI.xMediaRequest(RestAPI.cMediaText);
        lRestAPI.xMediaReply(RestAPI.cMediaJSON);
        lRestAPI.xUrl(lRequest);
        lRestAPI.xAction("");
        lResult |= HandlerCode.cRequestOK;
        lOutput = lRestAPI.xCallApi();
        if (lOutput.xResult() == Result.cResultOK) {
            lResult |= HandlerCode.cCommunicationOK;
            lResult |= HandlerCode.cProcessOK;
            mSetting.xSetting(lOutput.xReplyJ());
        }
        mHandler.sendEmptyMessage(lResult);
    }
}
