package jb.light.control;

/*
 *
 * Synchronise with class Switch in LightSupport
 *
 *
 * Version 20210611-01
 *
 */
import org.json.JSONException;
import org.json.JSONObject;

public class Switch {

    public static final String cSeqNumber = "seqnumber";
    public static final String cName = "name";
    public static final String cPause = "pause";
    public static final String cIP = "ip";
    public static final String cActive = "active";

    public static final int ResultOK = 0;
    public static final int ResultWrongValue = 8;
    public static final int ResultNotSupported = 9;
    public static final int ResultNameError = 10;
    public static final int ResultIpError = 50;
    public static final int ResultPauseError = 60;

    private int mSeqNumber;
    private String mName;
    private boolean mActive;
    private int mPause;
    private String mIP;

    public Switch(int pSeqNumber, String pName, boolean pActive, int pPause, String pIP) {
        mSeqNumber = pSeqNumber;
        mName = pName;
        mActive = pActive;
        mPause = pPause;
        mIP = pIP;
    }

    public Switch(JSONObject pSwitch) {
        mSeqNumber = pSwitch.optInt(cSeqNumber, 0);
        mName = pSwitch.optString(cName, "");
        mPause = pSwitch.optInt(cPause, 0);
        mIP = pSwitch.optString(cIP, "");
        mActive = pSwitch.optBoolean(cActive, false);
    }

    public Switch() {
        mSeqNumber = 0;
        mName = "";
        mActive = false;
        mPause = 0;
        mIP = "";
    }

    public JSONObject xSwitch() {
        JSONObject lSwitch;

        lSwitch = new JSONObject();
        try {
            lSwitch.put(cSeqNumber, mSeqNumber);
            lSwitch.put(cName, mName);
            lSwitch.put(cPause, mPause);
            if (!mIP.equals("")) {
                lSwitch.put(cIP, mIP);
            }
            lSwitch.put(cActive, mActive);
        } catch (JSONException pExc) {
        }

        return lSwitch;
    }

    public int xSeqNumber() {
        return mSeqNumber;
    }

    public int xSeqNumber(int pSeqNumber) {
        if (pSeqNumber < 0) {
            return ResultWrongValue;
        } else {
            mSeqNumber = pSeqNumber;
            return ResultOK;
        }
    }

    public String xName() {
        return mName;
    }

    public int xName(String pName) {
        if (pName.equals("")) {
            return ResultWrongValue;
        } else {
            mName = pName;
            return ResultOK;
        }
    }

    public boolean xActive() {
        return mActive;
    }

    public int xActive(boolean pActive) {
        mActive = pActive;
        return ResultOK;
    }


    public int xPause() {
        return mPause;
    }

    public int xPause(int pPause) {
        int lResult;

        lResult = sTestIntRange(pPause, 0, 1000);
        if (lResult == ResultOK) {
            mPause = pPause;
        }
        return lResult;
    }

    public String xIP() {
        return mIP;
    }

    public int xIP(String pIP) {
        int lResult;

        if (pIP.equals("")) {
            lResult = ResultOK;
        } else {
            lResult = sTestIP(pIP);
        }
        if (lResult == ResultOK) {
            mIP = pIP;
        }
        return lResult;
    }

    public boolean xIsEqual(Switch pSwitch) {
        boolean lEqual;

        lEqual = false;
        if (mSeqNumber == pSwitch.xSeqNumber()) {
            if (mName.equals(pSwitch.xName())) {
                if (mActive == pSwitch.xActive()) {
                    if (mIP.equals(pSwitch.xIP())) {
                        if (mPause == pSwitch.xPause()) {
                            lEqual = true;
                        }
                    }
                }
            }
        }
        return lEqual;
    }

    public int xTestSwitch() {
        int lResult;

        if (mActive) {
            if (mName.length() > 0) {
                lResult = sTestIntRange(mPause, 0, 1000);
                if (lResult == ResultOK) {
                    lResult = sTestIP(mIP);
                    if (lResult != ResultOK) {
                        lResult = ResultIpError;
                    }
                } else {
                    lResult = ResultPauseError;
                }
            } else {
                lResult = ResultNameError;
            }
            if (lResult != 0) {
                mActive = false;
            }
        } else {
            lResult = ResultOK;
        }
        return lResult;
    }

    private int sTestNumber(String pValue, int pMin, int pMax) {
        int lTestInt = 0;
        boolean lNumeric;
        int lResult;

        try {
            lTestInt = Integer.parseInt(pValue);
            lNumeric = true;
        } catch (NumberFormatException pExc) {
            lNumeric = false;
        }

        if (lNumeric) {
            lResult = sTestIntRange(lTestInt, pMin, pMax);
        } else {
            lResult = ResultWrongValue;
        }
        return lResult;
    }

    private int sTestIntRange(int pValue, int pMin, int pMax) {
        int lResult;

        if (pValue < pMin) {
            lResult = ResultWrongValue;
        } else {
            if (pValue > pMax) {
                lResult = ResultWrongValue;
            } else {
                lResult = ResultOK;
            }
        }
        return lResult;
    }

    private int sTestIP(String pIP) {
        int lResult = 0;
        int lPoint;
        int lCount;
        String lTemp;
        String[] lNumber;

        lTemp = pIP;
        lNumber = new String[4];
        lCount = 0;
        while (true) {
            lPoint = lTemp.indexOf(".");
            if (lPoint < 0) {
                if (lCount < 4) {
                    lNumber[lCount] = lTemp;
                }
                lCount++;
                break;
            }
            if (lCount < 4) {
                lNumber[lCount] = lTemp.substring(0, lPoint);
                lTemp = lTemp.substring(lPoint + 1);
                lCount++;
            } else {
                lCount = -1;
                break;
            }
        }

        if (lCount == 4) {
            for (lCount = 0; lCount < 4; lCount++) {
                lResult = sTestNumber(lNumber[lCount], 0, 255);
                if (lResult != ResultOK) {
                    break;
                }
            }
        } else {
            lResult = ResultWrongValue;
        }

        return lResult;
    }
}
