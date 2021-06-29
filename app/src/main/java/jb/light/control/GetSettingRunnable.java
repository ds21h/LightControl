package jb.light.control;

import android.os.Handler;

public class GetSettingRunnable implements Runnable {
    public static final int cSettingRetrieved = 1;

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

        lRequest = mServerAddress + URIs.UriServerSetting;
        lRestAPI = new RestAPI();
        lRestAPI.xMethod(RestAPI.cMethodGet);
        lRestAPI.xMediaRequest(RestAPI.cMediaText);
        lRestAPI.xMediaReply(RestAPI.cMediaJSON);
        lRestAPI.xUrl(lRequest);
        lRestAPI.xAction("");
        lOutput = lRestAPI.xCallApi();
        if (lOutput.xResult() == Result.cResultOK) {
            mSetting.xSetting(lOutput.xReplyJ());
        }
        mHandler.sendEmptyMessage(cSettingRetrieved);
    }
}
