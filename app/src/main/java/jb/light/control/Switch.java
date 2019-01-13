package jb.light.control;

/*
 *
 * Synchronise with class Switch in LightSupport
 *
 * Version 20181212-01
 *
 */
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;

public class Switch {

    public static final String cSeqNumber = "seqnumber";
    public static final String cName = "name";
    public static final String cType = "type";
    public static final String cGroup = "group";
    public static final String cPoint = "point";
    public static final String cPause = "pause";
    public static final String cIP = "ip";
    public static final String cActive = "active";

    public static final int ResultOK = 0;
    public static final int ResultWrongValue = 8;
    public static final int ResultNotSupported = 9;
    public static final int ResultNameError = 10;
    public static final int ResultTypeError = 20;
    public static final int ResultGroupError = 30;
    public static final int ResultPointError = 40;
    public static final int ResultIpError = 50;
    public static final int ResultPauseError = 60;

    public static final String[] Types = {"kaku", "elro", "esp"};

    private int mSeqNumber;
    private String mName;
    private boolean mActive;
    private String mType;
    private String mGroup;
    private String mPoint;
    private int mPause;
    private String mIP;

    public Switch(int pSeqNumber, String pName, boolean pActive, String pType, String pGroup, String pPoint, int pPause, String pIP) {
        mSeqNumber = pSeqNumber;
        mName = pName;
        mActive = pActive;
        mType = pType;
        mGroup = pGroup;
        mPoint = pPoint;
        mPause = pPause;
        mIP = pIP;
    }

    public Switch(JSONObject pSwitch) {
        mSeqNumber = pSwitch.optInt(cSeqNumber, 0);
        mName = pSwitch.optString(cName, "");
        mType = pSwitch.optString(cType, "");
        mGroup = pSwitch.optString(cGroup, "");
        mPoint = pSwitch.optString(cPoint, "");
        mPause = pSwitch.optInt(cPause, 0);
        mIP = pSwitch.optString(cIP, "");
        mActive = pSwitch.optBoolean(cActive, false);
    }

    public Switch() {
        mSeqNumber = 0;
        mName = "";
        mActive = false;
        mType = "";
        mGroup = "";
        mPoint = "";
        mPause = 0;
        mIP = "";
    }

    public JSONObject xSwitch() {
        JSONObject lSwitch;

        lSwitch = new JSONObject();
        try {
            lSwitch.put(cSeqNumber, mSeqNumber);
            lSwitch.put(cName, mName);
            lSwitch.put(cType, mType);
            if (!mGroup.equals("")) {
                lSwitch.put(cGroup, mGroup);
            }
            if (!mPoint.equals("")) {
                lSwitch.put(cPoint, mPoint);
            }
            lSwitch.put(cPause, mPause);
            if (!mIP.equals("")) {
                lSwitch.put(cIP, mIP);
            }
            lSwitch.put(cActive, mActive);
        } catch (JSONException ignored) {
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

    public String xType() {
        return mType;
    }

    public int xType(String pType) {
        int lResult;

        if (pType.equals("")) {
            lResult = ResultOK;
        } else {
            if (Arrays.asList(Types).contains(pType)) {
                lResult = ResultOK;
            } else {
                lResult = ResultNotSupported;
            }
        }
        if (lResult == ResultOK) {
            mType = pType;
        }
        return lResult;
    }

    public String xGroup() {
        return mGroup;
    }

    public int xGroup(String pGroup) {
        int lResult;

        if (pGroup.equals("")) {
            lResult = ResultOK;
        } else {
            lResult = sTestLetter(pGroup, "A", "D");
            if (lResult != ResultOK) {
                lResult = sTestNumber(pGroup, 1, 31);
            }
        }
        if (lResult == ResultOK) {
            mGroup = pGroup;
        }
        return lResult;
    }

    public String xPoint() {
        return mPoint;
    }

    public int xPoint(String pPoint) {
        int lResult;

        if (pPoint.equals("")) {
            lResult = ResultOK;
        } else {
            lResult = sTestLetter(pPoint, "A", "D");
            if (lResult != ResultOK) {
                lResult = sTestNumber(pPoint, 1, 31);
            }
        }
        if (lResult == ResultOK) {
            mPoint = pPoint;
        }
        return lResult;
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
                    if (mType.equals(pSwitch.xType())) {
                        if (mGroup.equals(pSwitch.xGroup())) {
                            if (mPoint.equals(pSwitch.xPoint())) {
                                if (mIP.equals(pSwitch.xIP())) {
                                    if (mPause == pSwitch.xPause()) {
                                        lEqual = true;
                                    }
                                }
                            }
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
                    switch (mType) {
                        case "kaku":
                            lResult = sTestLetter(mGroup, "A", "D");
                            if (lResult == ResultOK) {
                                lResult = sTestNumber(mPoint, 1, 3);
                                if (lResult != ResultOK) {
                                    lResult = ResultPointError;
                                }
                            } else {
                                lResult = ResultGroupError;
                            }
                            break;
                        case "elro":
                            lResult = sTestNumber(mGroup, 1, 31);
                            if (lResult == ResultOK) {
                                lResult = sTestLetter(mPoint, "A", "D");
                                if (lResult != ResultOK) {
                                    lResult = ResultPointError;
                                }
                            } else {
                                lResult = ResultGroupError;
                            }
                            break;
                        case "esp":
                            lResult = sTestIP(mIP);
                            if (lResult != ResultOK) {
                                lResult = ResultIpError;
                            }
                            break;
                        default:
                            lResult = ResultTypeError;
                            break;
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

    private int sTestLetter(String pValue, String pMin, String pMax) {
        int lResult;

        if (pValue.length() > 1) {
            lResult = ResultWrongValue;
        } else {
            if (pValue.compareTo(pMin) < 0) {
                lResult = ResultWrongValue;
            } else {
                if (pValue.compareTo(pMax) > 0) {
                    lResult = ResultWrongValue;
                } else {
                    lResult = ResultOK;
                }
            }
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
