package jb.light.control;

/**
 * Created by Jan on 28-11-2015.
 */
public class SettingItem {
    public static final String TypeLocation = "location";
    public static final String TypeLightsOff = "lightsoff";
    public static final String TypeSensor = "sensor";

    private String mType;
    private String mAttr1;
    private String mAttr2;
    private String mAttr3;
    private String mAttr4;

    public SettingItem(){
        mType = "";
        mAttr1 = "";
        mAttr2 = "";
        mAttr3 = "";
        mAttr4 = "";
    }

    public void xType(String pType){
        mType = pType;
    }

    public String xType(){
        return mType;
    }

    public void xAttr1(String pAttr1){
        mAttr1 = pAttr1;
    }

    public String xAttr1(){
        return mAttr1;
    }

    public void xAttr2(String pAttr2){
        mAttr2 = pAttr2;
    }

    public String xAttr2(){
        return mAttr2;
    }

    public void xAttr3(String pAttr3){
        mAttr3 = pAttr3;
    }

    public String xAttr3(){
        return mAttr3;
    }

    public void xAttr4(String pAttr4){
        mAttr4 = pAttr4;
    }

    public String xAttr4(){
        return mAttr4;
    }
}
