package jb.light.control;

/*
 * Synchronise with class Setting in LightSupport
 *
 * Version 12-12-2018
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
    public static final String cTreshold = "treshold";
    public static final String cMax = "max";
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
    private int mSensorTreshold;
    private int mMaxSensor;
    private int mPeriodDark;
    private int mPeriodMinute;
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
            if (pLightOff.substring(2, 3).equals(":")) {
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
                } catch (NumberFormatException pExc) {
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

    public int xSensorTreshold() {
        return mSensorTreshold;
    }

    public int xSensorTreshold(int pTreshold) {
        if (pTreshold > 10) {
            return cResultError;
        }
        if (pTreshold < 0) {
            return cResultError;
        }

        mSensorTreshold = pTreshold;
        return cResultOK;
    }

    public int xMaxSensor() {
        return mMaxSensor;
    }

    public int xMaxSensor(int pMax) {
        if (pMax < 0) {
            return cResultError;
        } else {
            mMaxSensor = pMax;
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

    public int xPeriod() {
        return (mPeriodMinute * 60) + mPeriodSec;
    }

    public int xPeriodMinute() {
        return mPeriodMinute;
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
            lSensor.put(cTreshold, mSensorTreshold);
            lSensor.put(cMax, mMaxSensor);
            lPeriod = (mPeriodMinute * 60) + mPeriodSec;
            lSensor.put(cInterval, lPeriod);
            lSensor.put(cRepeat, mPeriodDark);
            lSetting.put(cSensor, lSensor);
        } catch (JSONException pExc) {
        }

        return lSetting;
    }

    public Setting() {
        mLongitude = 4.0;
        mLattitude = 52.0;
        mLightOffHour = 23;
        mLightOffMin = 0;
        mLightOffPeriod = 30;
        mSensorLimit = 25;
        mSensorTreshold = 2;
        mMaxSensor = 1000;
        mPeriodDark = 3;
        mPeriodMinute = 1;
        mPeriodSec = 0;
    }

    public Setting(double pLongitude, double pLattitude, int pLightOffHour, int pLightOffMin, int pLightOffPeriod, int pSensorLimit, int pSensorTreshold, int pMaxSensor, int pPeriodDark, int pPeriodMinute, int pPeriodSec) {
        mLongitude = pLongitude;
        mLattitude = pLattitude;
        mLightOffHour = pLightOffHour;
        mLightOffMin = pLightOffMin;
        mLightOffPeriod = pLightOffPeriod;
        mSensorLimit = pSensorLimit;
        mSensorTreshold = pSensorTreshold;
        mMaxSensor = pMaxSensor;
        mPeriodDark = pPeriodDark;
        mPeriodMinute = 0;
        mPeriodSec = (pPeriodMinute * 60) + pPeriodSec;
    }

    public Setting(JSONObject pSetting) {
        JSONObject lLocation;
        JSONObject lLightOff;
        JSONObject lSensor;
        String lLight;
        int lResult;

        lLocation = pSetting.optJSONObject(cLocation);
        lLightOff = pSetting.optJSONObject(cLightOff);
        lSensor = pSetting.optJSONObject(cSensor);

        if (lLocation == null) {
            mLongitude = 0d;
            mLattitude = 0d;
        } else {
            mLongitude = lLocation.optDouble(cLongitude, 0);
            mLattitude = lLocation.optDouble(cLattitude, 0);
        }

        if (lLightOff == null) {
            mLightOffHour = 0;
            mLightOffMin = 0;
        } else {
            lLight = lLightOff.optString(cPointInTime, "");
            lResult = xLightOff(lLight);
            if (lResult != cResultOK) {
                mLightOffHour = 0;
                mLightOffMin = 0;
            }
        }
        mLightOffPeriod = lLightOff.optInt(cPeriod, 0);

        mSensorLimit = lSensor.optInt(cLimit);
        mSensorTreshold = lSensor.optInt(cTreshold);
        mMaxSensor = lSensor.optInt(cMax);
        mPeriodSec = lSensor.optInt(cInterval);
        mPeriodMinute = 0;
        mPeriodDark = lSensor.optInt(cRepeat);
    }
}
