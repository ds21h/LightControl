package jb.light.control;

/**
 * Created by Jan on 6-10-2015.
 */
class Server {
    private String mSSId;
    private String mName;
    private String mIP;
    private String mPort;
    private boolean mManager;

    Server(){
        mSSId = "";
        mName = "";
        mIP = "";
        mPort = "8080";
        mManager = false;
    }

    Server (String pName, String pSSId, String pIP, String pPort, boolean pManager){
        mName = pName;
        mSSId = pSSId;
        mIP = pIP;
        mPort = pPort;
        mManager = pManager;
    }
    void xName(String pName){
        mName = pName;
    }

    String xName(){
        return mName;
    }

    void xSSId (String pSSId){
        mSSId = pSSId;
    }

    String xSSId(){
        return mSSId;
    }

    void xIP (String pIP){
        mIP = pIP;
    }

    String xIP(){
        return mIP;
    }

    void xPort (String pPort){
        mPort = pPort;
    }

    String xPort(){
        return mPort;
    }

    void xManager(boolean pManager){
        mManager = pManager;
    }

    boolean xManager(){
        return mManager;
    }

    boolean xOK(){
        return !mName.equals("");
    }

    String xAddress(){
        if (mPort.equals("")){
            return "http://" + mIP;
        } else {
            return "http://" + mIP + ":" + mPort;
        }
    }
}
