package jb.light.control;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

class ResStatus {
    String xServerName;
    Server xServer;
    ZonedDateTime xSunset;
    ZonedDateTime xLightOff;
    int xFase;
    int xLightReading;
    List<SwitchLocal> xSwitches;
    private final Data mData;

    ResStatus(Context pContext, String pServerName){
        mData = Data.getInstance(pContext);
        xServerName = pServerName;
        xSunset = null;
        xLightOff = null;
        xFase = 0;
        xLightReading = 0;
        xServer = mData.xServer(pServerName);
        xSwitches = mData.xSwitches(pServerName);
    }

    boolean xProcessSwitches(List<SwitchLocal> pSwitches){
        int lCountOld;
        int lCountNew;
        Switch lSwitchOld;
        Switch lSwitchNew;
        boolean lChanged;

        lChanged = false;
        if (xSwitches.size() == pSwitches.size()){
            for (lCountNew = 0; lCountNew < pSwitches.size(); lCountNew++){
                lSwitchNew = pSwitches.get(lCountNew);
                lChanged = true;
                for (lCountOld = 0; lCountOld < xSwitches.size(); lCountOld++){
                    lSwitchOld = xSwitches.get(lCountOld);
                    if (lSwitchOld.xIsEqual(lSwitchNew)){
                        lChanged = false;
                        break;
                    }
                }
                if (lChanged){
                    break;
                }
            }
        } else {
            lChanged = true;
        }
        if (lChanged){
            xSwitches = pSwitches;
            mData.xReplaceSwitches(xSwitches, xServerName);
        }
        return lChanged;
    }
}
