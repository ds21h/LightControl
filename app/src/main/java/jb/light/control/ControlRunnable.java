package jb.light.control;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControlRunnable implements Runnable {
    static final int cSurveyChange = 1;
    static final int cSwitchChange = 2;
    static final int cStatusChange = 4;

    private volatile boolean mStop;
    private final Handler mUpdateHandler;
    private final ResStatus mResStatus;
    private final Data mData;

//    private ExecutorService mSwitchExecutor;
    private CountDownLatch mSwitchCountDown;
    private SwitchRunnable mSwitchRunnable;

    ControlRunnable(Context pContext, Handler pUpdateHandler, ResStatus pResStatus){
        mUpdateHandler = pUpdateHandler;
        mResStatus = pResStatus;
        mStop = false;
        mData = Data.getInstance(pContext);
    }

    void xStop(){
        mStop = true;
    }

    @Override
    public void run() {
        int lChange;

//        mSwitchExecutor = Executors.newCachedThreadPool();

        while (!mStop){
            lChange = sGetOverview();
            mSwitchCountDown = new CountDownLatch(mResStatus.xSwitches.size());
            for (SwitchLocal lSwitch: mResStatus.xSwitches) {
                if (mStop){
                    break;
                }
                if (lSwitch.xActive()){
                    lSwitch.xChangeInit();
                    mSwitchRunnable = new SwitchRunnable(lSwitch, mSwitchCountDown);
                    LightControlApp.getInstance().xExecutor.execute(mSwitchRunnable);
//                    mSwitchExecutor.execute(mSwitchRunnable);
//                    if (sGetSwitchStatus(lSwitch)){
//                        lChange |= cStatusChange;
//                    }
                } else {
                    mSwitchCountDown.countDown();
                }
            }
            if (mStop){
                break;
            }
            try {
                mSwitchCountDown.await();
            } catch (InterruptedException pExc) {
                break;
            }
            for (SwitchLocal lSwitch: mResStatus.xSwitches) {
                if (lSwitch.xChanged()){
                        lChange |= cStatusChange;
                        break;
                }
            }
            if (lChange > 0){
                mUpdateHandler.sendEmptyMessage(lChange);
            }
        }
//        mSwitchExecutor.shutdownNow();
    }

    private int sGetOverview(){
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
        int lResult;

        lResult = 0;
//        mResStatus.xServer = mData.xServer(mResStatus.xServerName);
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
                        lResult |= cSurveyChange;
                    } else {
                        if (!lTempTime.isEqual(mResStatus.xSunset)){
                            mResStatus.xSunset = lTempTime;
                            lResult |= cSurveyChange;
                        }
                    }
                } catch (DateTimeParseException pExc) {
                }
                lTemp = lOutput.xReplyJ().optString("lightoff", "");
                try {
                    lTempTime = ZonedDateTime.parse(lTemp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    if (mResStatus.xLightOff == null){
                        mResStatus.xLightOff = lTempTime;
                        lResult |= cSurveyChange;
                    } else {
                        if (!lTempTime.isEqual(mResStatus.xLightOff)){
                            mResStatus.xLightOff = lTempTime;
                            lResult |= cSurveyChange;
                        }
                    }
                } catch (DateTimeParseException pExc) {
                }
                lTempInt =  lOutput.xReplyJ().optInt("fase", -1);
                if (lTempInt >= 0){
                    if (lTempInt != mResStatus.xFase){
                        mResStatus.xFase = lTempInt;
                        lResult |= cSurveyChange;
                    }
                }
                lTempInt =  lOutput.xReplyJ().optInt("lightreading", -1);
                if (lTempInt >= 0){
                    if (lTempInt != mResStatus.xLightReading){
                        mResStatus.xLightReading = lTempInt;
                        lResult |= cSurveyChange;
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
                        lResult |= cSwitchChange;
                    }
                }
            }
        }
        return lResult;
    }

/*    private Boolean sGetSwitchStatus(SwitchLocal pSwitch){
        String lRequest;
        RestAPI.RestResult lOutput;
        RestAPI lRestAPI;
        String lStatusString;
        boolean lResult;

        lResult = false;
        lRestAPI = new RestAPI();
        lRequest = "http://" + pSwitch.xIP() + URIs.UriSwitch;
        lRestAPI.xUrl(lRequest);
        lRestAPI.xMethod(RestAPI.cMethodGet);
        lRestAPI.xTimeOut(2000);
        lRestAPI.xMediaReply(RestAPI.cMediaJSON);
        lRestAPI.xAction("");
        lOutput = lRestAPI.xCallApi();
        if (lOutput.xResult()  == Result.cResultOK){
            lStatusString = lOutput.xReplyJ().optString("status", "");
            switch (lStatusString){
                case "on":{
                    if (pSwitch.xStatus() != SwitchLocal.StatusOn){
                        pSwitch.xStatus(SwitchLocal.StatusOn);
                        lResult = true;
                    }
                    break;
                }
                case "off":{
                    if (pSwitch.xStatus() != SwitchLocal.StatusOff){
                        pSwitch.xStatus(SwitchLocal.StatusOff);
                        lResult = true;
                    }
                    break;
                }
                default:{
                    if (pSwitch.xStatus() != SwitchLocal.StatusNone){
                        pSwitch.xStatus(SwitchLocal.StatusNone);
                        lResult = true;
                    }
                    break;
                }
            }
        } else {
            if (pSwitch.xStatus() != SwitchLocal.StatusNone){
                pSwitch.xStatus(SwitchLocal.StatusNone);
                lResult = true;
            }
        }
        return lResult;
    } */
}
