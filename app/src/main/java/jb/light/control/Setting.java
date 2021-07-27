package jb.light.control;

/*
 * Synchronise with class Setting in LightSupport
 *
 * Version 20210726
 *
 */
import org.json.JSONException;
import org.json.JSONObject;

public class Setting {

    public static final String cSetting = "setting";
    public static final String cLocation = "location";
    public static final String cLongitude = "longitude";
    public static final String cLattitude = "lattitude";
    public static final String cLightOff = "lightoff";
    public static final String cPointInTime = "firsttime";
    public static final String cPeriod = "period";
    public static final String cSensor = "sensor";
    public static final String cLimit = "limit";
    public static final String cInterval = "interval";
    public static final String cRepeat = "repeat";

    public static final int cResultOK = 0;
    public static final int cResultError = 9;

    private double mLongitude;
    private double mLattitude;
    private int mLightOffHour;
    private int mLightOffMin;
    private int mLightOffPeriod;
    private int mSensorLimit;
    private int mPeriodDark;
    private int mPeriodSec;

    public double xLongitude() {
        return mLongitude;
    }

    public int xLongitude(double pLongitude) {
        if (pLongitude > 360d) {
            return cResultError;
        }
        if (pLongitude < -360d) {
            return cResultError;
        }

        mLongitude = pLongitude;
        return cResultOK;
    }

    public double xLattitude() {
        return mLattitude;
    }

    public int xLattitude(double pLattitude) {
        if (pLattitude > 90d) {
            return cResultError;
        }
        if (pLattitude < -90d) {
            return cResultError;
        }

        mLattitude = pLattitude;
        return cResultOK;
    }

    public String xLightOff() {
        return String.format("%02d", mLightOffHour) + ":" + String.format("%02d", mLightOffMin);
    }

    public int xLightOff(String pLightOff) {
        int lLightOffHour = 0;
        int lLightOffMin = 0;
        int lResult;

        lResult = cResultError;

        if (pLightOff.length() == 5) {
            if (pLightOff.charAt(2) == ':') {
                try {
                    lLightOffHour = Integer.parseInt(pLightOff.substring(0, 2));
                    lLightOffMin = Integer.parseInt(pLightOff.substring(3));
                    if (lLightOffHour >= 0) {
                        if (lLightOffHour < 24) {
                            if (lLightOffMin >= 0) {
                                if (lLightOffMin < 60) {
                                    lResult = cResultOK;
                                }
                            }
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (lResult == cResultOK) {
            mLightOffHour = lLightOffHour;
            mLightOffMin = lLightOffMin;
        }
        return lResult;
    }

    public int xLightOffHour() {
        return mLightOffHour;
    }

    public int xLightOffHour(int pHour) {
        if (pHour > 23) {
            return cResultError;
        }
        if (pHour < 0) {
            return cResultError;
        }

        mLightOffHour = pHour;
        return cResultOK;
    }

    public int xLightOffMin() {
        return mLightOffMin;
    }

    public int xLightOffMin(int pMin) {
        if (pMin > 59) {
            return cResultError;
        }
        if (pMin < 0) {
            return cResultError;
        }

        mLightOffMin = pMin;
        return cResultOK;
    }

    public int xLightOffPeriod() {
        return mLightOffPeriod;
    }

    public int xLightOffPeriod(int pPeriod) {
        if (pPeriod > 120) {
            return cResultError;
        }
        if (pPeriod < 0) {
            return cResultError;
        }

        mLightOffPeriod = pPeriod;
        return cResultOK;
    }

    public int xSensorLimit() {
        return mSensorLimit;
    }

    public int xSensorLimit(int pLimit) {
        if (pLimit < 0) {
            return cResultError;
        } else {
            mSensorLimit = pLimit;
            return cResultOK;
        }
    }

    public int xPeriodDark() {
        return mPeriodDark;
    }

    public int xPeriodDark(int pPeriod) {
        if (pPeriod > 10) {
            return cResultError;
        }
        if (pPeriod < 0) {
            return cResultError;
        }

        mPeriodDark = pPeriod;
        return cResultOK;
    }

    public int xPeriodSec() {
        return mPeriodSec;
    }

    public int xPeriodSec(int pSec) {
        if (pSec > 300) {
            return cResultError;
        }
        if (pSec < 0) {
            return cResultError;
        }

        mPeriodSec = pSec;
        return cResultOK;
    }

    public JSONObject xSetting() {
        JSONObject lSetting;
        JSONObject lLocation;
        JSONObject lLightOff;
        JSONObject lSensor;
        String lPointInTime;
        int lPeriod;

        lSetting = new JSONObject();
        lLocation = new JSONObject();
        try {
            lLocation.put(cLongitude, mLongitude);
            lLocation.put(cLattitude, mLattitude);
            lSetting.put(cLocation, lLocation);
            lLightOff = new JSONObject();
            lPointInTime = String.format("%02d", mLightOffHour) + ":" + String.format("%02d", mLightOffMin);
            lLightOff.put(cPointInTime, lPointInTime);
            lLightOff.put(cPeriod, mLightOffPeriod);
            lSetting.put(cLightOff, lLightOff);
            lSensor = new JSONObject();
            lSensor.put(cLimit, mSensorLimit);
            lPeriod = mPeriodSec;
            lSensor.put(cInterval, lPeriod);
            lSensor.put(cRepeat, mPeriodDark);
            lSetting.put(cSensor, lSensor);
        } catch (JSONException ignored) {
        }

        return lSetting;
    }

    public Setting() {
        sInitSetting();
    }

    private void sInitSetting(){
        mLongitude = 4.0;
        mLattitude = 52.0;
        mLightOffHour = 23;
        mLightOffMin = 0;
        mLightOffPeriod = 30;
        mSensorLimit = 4000;
        mPeriodDark = 3;
        mPeriodSec = 60;
    }

    public Setting(double pLongitude, double pLattitude, int pLightOffHour, int pLightOffMin, int pLightOffPeriod, int pSensorLimit, int pPeriodDark, int pPeriodSec) {
        mLongitude = pLongitude;
        mLattitude = pLattitude;
        mLightOffHour = pLightOffHour;
        mLightOffMin = pLightOffMin;
        mLightOffPeriod = pLightOffPeriod;
        mSensorLimit = pSensorLimit;
        mPeriodDark = pPeriodDark;
        mPeriodSec = pPeriodSec;
    }

    public Setting(JSONObject pSetting) {
        sInitSetting();
        xSetting(pSetting);
    }

    public void xSetting(JSONObject pSetting) {
        JSONObject lLocation;
        JSONObject lLightOff;
        JSONObject lSensor;
        String lLight;
        int lResult;

        lLocation = pSetting.optJSONObject(cLocation);
        lLightOff = pSetting.optJSONObject(cLightOff);
        lSensor = pSetting.optJSONObject(cSensor);

        if (lLocation != null) {
            mLongitude = lLocation.optDouble(cLongitude, 4.0);
            mLattitude = lLocation.optDouble(cLattitude, 52.0);
        }

        if (lLightOff != null) {
            lLight = lLightOff.optString(cPointInTime, "");
            lResult = xLightOff(lLight);
            if (lResult != cResultOK) {
                mLightOffHour = 23;
                mLightOffMin = 0;
            }
            mLightOffPeriod = lLightOff.optInt(cPeriod, 30);
        }

        if (lSensor != null){
            mSensorLimit = lSensor.optInt(cLimit, 4000);
            mPeriodSec = lSensor.optInt(cInterval, 60);
            mPeriodDark = lSensor.optInt(cRepeat, 3);
        }
    }
}
