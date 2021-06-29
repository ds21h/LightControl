package jb.light.control;

public class ServerActionRunnable implements Runnable {
    private final Action mAction;

    ServerActionRunnable(Action pAction) {
        mAction = pAction;
    }

    @Override
    public void run() {
        String lRequest;
        RestAPI lRestAPI;
        String lCommand;

        lCommand = "";
        switch (mAction.xAction) {
            case Action.ActionSwitchOn:
                lCommand = "switchon=" + mAction.xValue;
                break;
            case Action.ActionSwitchOff:
                lCommand = "switchoff=" + mAction.xValue;
                break;
            case Action.ActionLightsOff:
                lCommand = "lightoff=" + mAction.xValue;
                break;
        }
        lRequest = mAction.xIP + URIs.UriServerAction;
        lRestAPI = new RestAPI();
        lRestAPI.xMethod(RestAPI.cMethodPut);
        lRestAPI.xMediaRequest(RestAPI.cMediaText);
        lRestAPI.xMediaReply(RestAPI.cMediaJSON);
        lRestAPI.xUrl(lRequest);
        lRestAPI.xAction(lCommand);
        lRestAPI.xCallApi();
    }
}
