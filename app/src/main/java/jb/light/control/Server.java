package jb.light.control;

/**
 * Created by Jan on 6-10-2015.
 */
public class Server {
    private String mSSId;
    private String mName;
    private String mIP;
    private String mPort;
    private boolean mManager;

    public Server(){
        mSSId = "";
        mName = "";
        mIP = "";
        mPort = "8080";
        mManager = false;
    }

    public Server (String pName, String pSSId, String pIP, String pPort, boolean pManager){
        mName = pName;
        mSSId = pSSId;
        mIP = pIP;
        mPort = pPort;
        mManager = pManager;
    }
    public void xName(String pName){
        mName = pName;
    }

    public String xName(){
        return mName;
    }

    public void xSSId (String pSSId){
        mSSId = pSSId;
    }

    public String xSSId(){
        return mSSId;
    }

    public void xIP (String pIP){
        mIP = pIP;
    }

    public String xIP(){
        return mIP;
    }

    public void xPort (String pPort){
        mPort = pPort;
    }

    public String xPort(){
        return mPort;
    }

    public void xManager(boolean pManager){
        mManager = pManager;
    }

    public boolean xManager(){
        return mManager;
    }

    public boolean xOK(){
        if (mName.equals("")){
            return false;
        }
        return true;
    }

    public String xAddress(){
        if (mPort.equals("")){
            return "http://" + mIP;
        } else {
            return "http://" + mIP + ":" + mPort;
        }
    }
}
