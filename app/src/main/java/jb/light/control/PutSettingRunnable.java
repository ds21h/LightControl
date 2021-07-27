package jb.light.control;

import android.annotation.SuppressLint;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class PutSettingRunnable implements Runnable {
    static final int cSaveLocation = 1;
    static final int cSaveLightOff = 2;
    static final int cSaveSensor = 4;

    private final Setting mSetting;
    private final String mServerAddress;
    private final Handler mHandler;
    private final int mAction;

    PutSettingRunnable(String pServerAddress, Setting pSetting, Handler pHandler, int pAction) {
        mServerAddress = pServerAddress;
        mSetting = pSetting;
        mHandler = pHandler;
        mAction = pAction;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void run() {
        String lRequest;
        RestAPI.RestResult lOutput;
        RestAPI lRestAPI;
        JSONObject lLocation;
        JSONObject lAction;
        JSONObject lSetting;
        JSONObject lLightOff;
        JSONObject lSensor;
        String lActionS = "";
        int lResult;
        boolean lRequestOK;

        lResult = HandlerCode.cPutServerSetting;
        if (mAction != 0){
            lAction = new JSONObject();
            lRequestOK = true;
            if ((mAction & cSaveLocation) != 0){
                lLocation = new JSONObject();
                try {
                    lLocation.put(Setting.cLongitude, String.valueOf(mSetting.xLongitude()));
                    lLocation.put(Setting.cLattitude, String.valueOf(mSetting.xLattitude()));
                    lAction.put(Setting.cLocation, lLocation);
                } catch (JSONException pExc) {
                    lRequestOK = false;
                }
            }
            if ((mAction & cSaveLightOff) != 0){
                lLightOff = new JSONObject();
                try {
                    lLightOff.put(Setting.cPointInTime, String.format("%02d", mSetting.xLightOffHour()) + ":" + String.format("%02d", mSetting.xLightOffMin()));
                    lLightOff.put(Setting.cPeriod, String.valueOf(mSetting.xLightOffPeriod()));
                    lAction.put(Setting.cLightOff, lLightOff);
                } catch (JSONException pExc){
                    lRequestOK = false;
                }
            }
            if ((mAction & cSaveSensor) != 0){
                lSensor = new JSONObject();
                try {
                    lSensor.put(Setting.cLimit, String.valueOf(mSetting.xSensorLimit()));
                    lSensor.put(Setting.cInterval, String.valueOf(mSetting.xPeriodSec()));
                    lSensor.put(Setting.cRepeat, String.valueOf(mSetting.xPeriodDark()));
                    lAction.put(Setting.cSensor, lSensor);
                } catch (JSONException pExc){
                    lRequestOK = false;
                }
            }
            try {
                lAction.put("lang", Locale.getDefault().getLanguage());
                lActionS = lAction.toString();
            } catch (JSONException pExc) {
                lRequestOK = false;
            }

            if (lRequestOK){
                lResult |= HandlerCode.cRequestOK;
                lRequest = mServerAddress + URIs.UriServerSetting;
                lRestAPI = new RestAPI();
                lRestAPI.xMethod(RestAPI.cMethodPut);
                lRestAPI.xMediaRequest(RestAPI.cMediaJSON);
                lRestAPI.xMediaReply(RestAPI.cMediaJSON);
                lRestAPI.xUrl(lRequest);
                lRestAPI.xAction(lActionS);
                lOutput = lRestAPI.xCallApi();
                if (lOutput.xResult() == Result.cResultOK) {
                    lResult |= HandlerCode.cCommunicationOK;
                    lSetting = lOutput.xReplyJ().optJSONObject("setting");
                    if (lSetting != null){
                        mSetting.xSetting(lSetting);
                        lResult |= HandlerCode.cProcessOK;
                    }
                }
            }
        }
        mHandler.sendEmptyMessage(lResult);
    }
}
