package jb.light.control;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Jan on 17-9-2015.
 */
class RestAPI {
    static final String cMethodGet = "GET";
    static final String cMethodPut = "PUT";

    static final String cMediaJSON = "application/json";
    static final String cMediaText = "text/plain";

    private String mMethod;
    private String mMediaRequest;
    private String mMediaReply;
    private String mUrl;
    private String mAction;
    private int mTimeOut;
    private final int cTimeOutMin = 1000;
    private final int cTimeOutMax = 5000;
    private final int cTimeOutDef = 5000;

    RestAPI(){
        mMethod = cMethodGet;
        mMediaRequest = cMediaText;
        mMediaReply = cMediaJSON;
        mUrl = "";
        mAction = "";
        mTimeOut = cTimeOutDef;
    }

    void xMethod(String pMethod){
        mMethod = pMethod;
    }

    void xMediaRequest(String pMedia){
        mMediaRequest = pMedia;
    }

    void xMediaReply(String pMedia){
        mMediaReply = pMedia;
    }

    void xUrl(String pUrl){
        mUrl = pUrl;
    }

    void xAction(String pAction){
        mAction = pAction;
    }

    void xTimeOut(int pTimeOut){
        if (pTimeOut >= cTimeOutMin && pTimeOut <= cTimeOutMax){
            mTimeOut = pTimeOut;
        }
    }

    RestResult xCallApi(){
        String lOutput;
        StringBuilder lStr;
        String lUrlS;
        String lAction;
        URL lUrl;
        HttpURLConnection lConn = null;
        DataOutputStream lOutStream;
        BufferedReader lBufRead;
        String lLine;
        int lRespCode;
        RestResult lRestResult;

        lStr = new StringBuilder();
        if (mMethod.equals(cMethodGet)){
            if (!mAction.equals("")){
                lUrlS = mUrl + "?" + mAction;
                lAction = "";
            } else {
                lUrlS = mUrl;
                lAction = "";
            }
        } else {
            lUrlS = mUrl;
            lAction = mAction;
        }
        try {
            lUrl = new URL(lUrlS);
            lConn = (HttpURLConnection) lUrl.openConnection();
            lConn.setRequestMethod(mMethod);
            lConn.setRequestProperty("Accept", mMediaReply);
            lConn.setConnectTimeout(mTimeOut );
            lConn.setReadTimeout(mTimeOut );
            if (!lAction.equals("")) {
                lConn.setRequestProperty("Content-Type", mMediaRequest);
                lConn.setDoOutput(true);
                lOutStream = new DataOutputStream(lConn.getOutputStream());
                lOutStream.writeBytes(lAction);
                lOutStream.flush();
                lOutStream.close();
            }

            lRespCode = lConn.getResponseCode();
            if (lRespCode == 200) {
                lBufRead = new BufferedReader(new InputStreamReader(
                        (lConn.getInputStream())));
                while ((lLine = lBufRead.readLine()) != null) {
                    lStr.append(lLine);
                }
                lOutput = lStr.toString();
                lRestResult = new RestResult(Result.cResultOK, lOutput);
            } else {
                lRestResult = new RestResult("Unexpected responsecode: " + lRespCode, Result.cResultError);
            }
        } catch (MalformedURLException e) {
            lRestResult = new RestResult("URL not correct: " + e.getLocalizedMessage(), Result.cResultError);
        } catch (SocketTimeoutException pExc){
            if (pExc.getLocalizedMessage() == null){
                lRestResult = new RestResult("Read Time-Out", Result.cResultReadTimeOut);
            } else {
                lRestResult = new RestResult("Connect Time-Out", Result.cResultConnectTimeOut);
            }
        } catch (IOException e) {
            lRestResult = new RestResult("IO Exception:" + e.getLocalizedMessage(), Result.cResultError);
        } finally {
            if (lConn != null){
                lConn.disconnect();
            }
        }
        return lRestResult;
    }

    class RestResult {
        private int mResult;
        private String mText;
        private JSONObject mReplyJ;
        private String mReplyS;
        private boolean mJson;

        int xResult(){
            return mResult;
        }

        String xText() {
            return mText;
        }

        JSONObject xReplyJ(){
            if (mJson){
                return mReplyJ;
            } else {
                return null;
            }
        }

        String xReplyS(){
            return mReplyS;
        }

        RestResult(int pResult, String pOutput){
            mReplyS = pOutput;
            if (mMediaReply.equals(cMediaJSON)){
                try {
                    mReplyJ = new JSONObject(pOutput);
                    mResult = pResult;
                    mText = "OK";
                    mJson = true;
                }catch (JSONException pExc){
                    mResult = Result.cResultOutputFout;
                    mText = "Invalid JSON Object received";
                    mReplyJ = null;
                    mJson = false;
                }
            } else {
                mReplyJ = null;
                mResult = Result.cResultOK;
                mText = "OK";
                mJson = false;
            }
        }

        RestResult(String pText, int pResult){
            mReplyJ = null;
            mReplyS = null;
            mResult = pResult;
            mText = pText;
        }
    }
}
