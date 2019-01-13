package jb.light.control;

/**
 * Created by Jan on 28-11-2015.
 */
class SettingItem {
    static final String TypeLocation = "location";
    static final String TypeLightsOff = "lightsoff";
    static final String TypeSensor = "sensor";

    private String mType;
    private String mAttr1;
    private String mAttr2;
    private String mAttr3;
    private String mAttr4;

    SettingItem(){
        mType = "";
        mAttr1 = "";
        mAttr2 = "";
        mAttr3 = "";
        mAttr4 = "";
    }

    void xType(String pType){
        mType = pType;
    }

    String xType(){
        return mType;
    }

    void xAttr1(String pAttr1){
        mAttr1 = pAttr1;
    }

    String xAttr1(){
        return mAttr1;
    }

    void xAttr2(String pAttr2){
        mAttr2 = pAttr2;
    }

    String xAttr2(){
        return mAttr2;
    }

    void xAttr3(String pAttr3){
        mAttr3 = pAttr3;
    }

    String xAttr3(){
        return mAttr3;
    }

    void xAttr4(String pAttr4){
        mAttr4 = pAttr4;
    }

    String xAttr4(){
        return mAttr4;
    }
}
