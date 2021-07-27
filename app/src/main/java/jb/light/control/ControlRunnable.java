package jb.light.control;

import android.os.Handler;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ControlRunnable implements Runnable {
    private volatile boolean mStop;
    private final Handler mUpdateHandler;
    private final ResStatus mResStatus;

    ControlRunnable(Handler pUpdateHandler, ResStatus pResStatus){
        mUpdateHandler = pUpdateHandler;
        mResStatus = pResStatus;
        mStop = false;
    }

    void xStop(){
        mStop = true;
    }

    @Override
    public void run() {
        CountDownLatch lSwitchCountDown;
        SwitchRunnable lSwitchRunnable;
        int lResult;

        while (!mStop){
            lResult = HandlerCode.cControl;
            lResult = sGetOverview(lResult);
            lSwitchCountDown = new CountDownLatch(mResStatus.xSwitches.size());
            for (SwitchLocal lSwitch: mResStatus.xSwitches) {
                if (mStop){
                    break;
                }
                if (lSwitch.xActive()){
                    lSwitch.xChangeInit();
                    lSwitchRunnable = new SwitchRunnable(lSwitch, lSwitchCountDown);
                    LightControlApp.getInstance().xExecutor.execute(lSwitchRunnable);
                } else {
                    lSwitchCountDown.countDown();
                }
            }
            if (mStop){
                break;
            }
            try {
                lSwitchCountDown.await();
            } catch (InterruptedException pExc) {
                break;
            }
            for (SwitchLocal lSwitch: mResStatus.xSwitches) {
                if (lSwitch.xChanged()){
                        lResult |= HandlerCode.cStatusChange;
                        break;
                }
            }
            if (lResult != HandlerCode.cControl){
                mUpdateHandler.sendEmptyMessage(lResult);
            }
        }
    }

    private int sGetOverview(int pResult){
        String lRequest;
        RestAPI.RestResult lOutput;
        RestAPI lRestAPI;
        String lTemp;
        ZonedDateTime lTempTime;
        int lTempInt;
        List<SwitchLocal> lSwitches;
        JSONArray lSwitchesJ;
        int lCount;
        JSONObject lSwitchJ;
        SwitchLocal lSwitch;
        boolean lError;

        if (mResStatus.xServer != null){
            lRequest = mResStatus.xServer.xAddress() + URIs.UriServerSurvey;
            lRestAPI = new RestAPI();
            lRestAPI.xMethod(RestAPI.cMethodGet);
            lRestAPI.xMediaRequest(RestAPI.cMediaText);
            lRestAPI.xMediaReply(RestAPI.cMediaJSON);
            lRestAPI.xUrl(lRequest);
            lRestAPI.xAction("");
            lOutput = lRestAPI.xCallApi();

            if (lOutput.xResult() == Result.cResultOK){
                lTemp = lOutput.xReplyJ().optString("sunset", "");
                try {
                    lTempTime = ZonedDateTime.parse(lTemp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    if (mResStatus.xSunset == null){
                        mResStatus.xSunset = lTempTime;
                        pResult |= HandlerCode.cSurveyChange;
                    } else {
                        if (!lTempTime.isEqual(mResStatus.xSunset)){
                            mResStatus.xSunset = lTempTime;
                            pResult |= HandlerCode.cSurveyChange;
                        }
                    }
                } catch (DateTimeParseException ignored) {
                }
                lTemp = lOutput.xReplyJ().optString("lightoff", "");
                try {
                    lTempTime = ZonedDateTime.parse(lTemp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    if (mResStatus.xLightOff == null){
                        mResStatus.xLightOff = lTempTime;
                        pResult |= HandlerCode.cSurveyChange;
                    } else {
                        if (!lTempTime.isEqual(mResStatus.xLightOff)){
                            mResStatus.xLightOff = lTempTime;
                            pResult |= HandlerCode.cSurveyChange;
                        }
                    }
                } catch (DateTimeParseException ignored) {
                }
                lTempInt =  lOutput.xReplyJ().optInt("fase", -1);
                if (lTempInt >= 0){
                    if (lTempInt != mResStatus.xFase){
                        mResStatus.xFase = lTempInt;
                        pResult |= HandlerCode.cSurveyChange;
                    }
                }
                lTempInt =  lOutput.xReplyJ().optInt("lightreading", -1);
                if (lTempInt >= 0){
                    if (lTempInt != mResStatus.xLightReading){
                        mResStatus.xLightReading = lTempInt;
                        pResult |= HandlerCode.cSurveyChange;
                    }
                }

                lSwitches = new ArrayList<>();
                lError = false;
                lSwitchesJ = lOutput.xReplyJ().optJSONArray("switches");
                if (lSwitchesJ == null) {
                    lError = true;
                } else {
                    for (lCount = 0; lCount < lSwitchesJ.length(); lCount++) {
                        lSwitchJ = lSwitchesJ.optJSONObject(lCount);
                        if (lSwitchJ == null) {
                            lError = true;
                        } else {
                            lSwitch = new SwitchLocal(lSwitchJ);
                            lSwitches.add(lSwitch);
                        }
                    }
                }
                if (!lError){
                    if (mResStatus.xProcessSwitches(lSwitches)){
                        pResult |= HandlerCode.cSwitchChange;
                    }
                }
            }
        }
        return pResult;
    }
}
