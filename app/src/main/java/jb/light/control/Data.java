package jb.light.control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jan on 14-5-2017.
 */

public class Data extends SQLiteOpenHelper {
    private static Data mInstance = null;

    private static final String cDBName = "LightControl.db";
    private static final int cDBVersion = 1;

    public static Data getInstance(Context pContext) {
        /**
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new Data(pContext.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private Data(Context pContext) {
        super(pContext, pContext.getExternalFilesDir(null).getAbsolutePath() + "/" + cDBName, null, cDBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase pDB) {
        sDefineServer(pDB);
        sDefineSwitch(pDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase pDB, int pOldVersion, int pNewVersion) {
        switch (pOldVersion){
            default:{
                pDB.execSQL("DROP TABLE IF EXISTS Server");
                pDB.execSQL("DROP TABLE IF EXISTS Switch");
                onCreate(pDB);
                break;
            }
        }
    }

    private void sDefineServer(SQLiteDatabase pDB){
        pDB.execSQL(
                "CREATE TABLE Server " +
                        "(_ID Integer primary key, " +
                        "Name Text Not Null Unique, " +
                        "SSID Text Not Null, " +
                        "IP Text Not Null, " +
                        "Port Text Not Null, " +
                        "Manager Integer Not Null)"
        );
    }

    private void sDefineSwitch(SQLiteDatabase pDB){
        pDB.execSQL(
                "CREATE TABLE Switch " +
                        "(_ID Integer primary key, " +
                        "Server Text Not Null, " +
                        "SeqNumber Integer, " +
                        "Name Text Not Null, " +
                        "Active Integer Not Null, " +
                        "Type Text Not Null, " +
                        "Grp Text, " +
                        "Point Text, " +
                        "IP Text, " +
                        "Pause Integer Not Null" +
                        ")"
        );
    }

    public Server xServer(String pName){
        Server lServer = null;
        SQLiteDatabase lDB;
        Cursor lCursor;
        String[] lColumns;
        String lSelection;
        String[] lSelectionArgs;
        String lName;
        String lSSID;
        String lIP;
        String lPort;
        int lManager;

        lColumns = new String[] {"Name", "SSID", "IP", "Port", "Manager"};
        lSelection = "Name = ?";
        lSelectionArgs = new String[] {pName};

        lDB = this.getReadableDatabase();

        lCursor = lDB.query("Server", lColumns, lSelection, lSelectionArgs, null, null, null);
        if (lCursor.moveToNext()){
            lName = lCursor.getString(0);
            lSSID = lCursor.getString(1);
            lIP = lCursor.getString(2);
            lPort = lCursor.getString(3);
            lManager = lCursor.getInt(4);
            lServer = new Server(lName, lSSID, lIP, lPort, (lManager == 0) ? false : true);
        }
        lCursor.close();
        lDB.close();

        return lServer;
    }

    public List<Server> xServers(String pSSID){
        List<Server> lServers;
        Server lServer;
        SQLiteDatabase lDB;
        Cursor lCursor;
        String[] lColumns;
        String lSelection;
        String[] lSelectionArgs;
        String lName;
        String lSSID;
        String lIP;
        String lPort;
        int lManager;

        lColumns = new String[] {"Name", "SSID", "IP", "Port", "Manager"};
        lSelection = "SSID = ?";
        lSelectionArgs = new String[] {pSSID};

        lServers = new ArrayList<>();

        lDB = this.getReadableDatabase();

        lCursor = lDB.query("Server", lColumns, lSelection, lSelectionArgs, null, null, null);
        while (lCursor.moveToNext()){
            lName = lCursor.getString(0);
            lSSID = lCursor.getString(1);
            lIP = lCursor.getString(2);
            lPort = lCursor.getString(3);
            lManager = lCursor.getInt(4);
            lServer = new Server(lName, lSSID, lIP, lPort, (lManager == 0) ? false : true);
            lServers.add(lServer);
        }
        lCursor.close();
        lDB.close();

        return lServers;
    }

    public List<Server> xServers(){
        List<Server> lServers;
        Server lServer;
        SQLiteDatabase lDB;
        Cursor lCursor;
        String[] lColumns;
        String lName;
        String lSSID;
        String lIP;
        String lPort;
        int lManager;

        lColumns = new String[] {"Name", "SSID", "IP", "Port", "Manager"};

        lServers = new ArrayList<>();

        lDB = this.getReadableDatabase();

        lCursor = lDB.query("Server", lColumns, null, null, null, null, null);
        while (lCursor.moveToNext()){
            lName = lCursor.getString(0);
            lSSID = lCursor.getString(1);
            lIP = lCursor.getString(2);
            lPort = lCursor.getString(3);
            lManager = lCursor.getInt(4);
            lServer = new Server(lName, lSSID, lIP, lPort, (lManager == 0) ? false : true);
            lServers.add(lServer);
        }
        lCursor.close();
        lDB.close();

        return lServers;
    }

    public int xNewServer(Server pServer){
        SQLiteDatabase lDB;
        ContentValues lValues;
        long lRow;
        int lResult;

        lDB = this.getWritableDatabase();

        lValues = new ContentValues();
        lValues.put("Name", pServer.xName());
        lValues.put("SSID", pServer.xSSId());
        lValues.put("IP", pServer.xIP());
        lValues.put("Port", pServer.xPort());
        lValues.put("Manager", (pServer.xManager()) ? 1 : 0);
        lRow = lDB.insert("Server", null, lValues);
        if (lRow < 0){
            lResult = Result.cResultError;
        } else {
            lResult = Result.cResultOK;
        }
        lDB.close();

        return lResult;
    }

    public int xModifyServer(Server pServer){
        SQLiteDatabase lDB;
        ContentValues lValues;
        String lSelection;
        String[] lSelectionArgs;
        int lNumber;
        int lResult;

        lValues = new ContentValues();
        lValues.put("SSID", pServer.xSSId());
        lValues.put("IP", pServer.xIP());
        lValues.put("Port", pServer.xPort());
        lValues.put("Manager", (pServer.xManager()) ? 1 : 0);
        lSelection = "Name = ?";
        lSelectionArgs = new String[] {pServer.xName()};

        lDB = this.getWritableDatabase();

        lNumber = lDB.update("Server", lValues, lSelection, lSelectionArgs);
        if (lNumber == 1){
            lResult = Result.cResultOK;
        } else {
            lResult = Result.cResultError;
        }

        lDB.close();

        return  lResult;
    }

    public int xDeleteServer(String pServerName){
        SQLiteDatabase lDB;
        String lSelection;
        String[] lSelectionArgs;
        int lNumber;
        int lResult;

        lSelection = "Name = ?";
        lSelectionArgs = new String[] {pServerName};

        lDB = this.getWritableDatabase();

        lNumber = lDB.delete("Server", lSelection, lSelectionArgs);
        if (lNumber == 1){
            lResult = Result.cResultOK;
        } else {
            lResult = Result.cResultError;
        }

        lDB.close();

        return  lResult;
    }

    public List<Switch> xSwitches(String pServer){
        List<Switch> lSwitches;
        Switch lSwitch;
        SQLiteDatabase lDB;
        Cursor lCursor;
        String[] lColumns;
        String lSelection;
        String[] lSelectionArgs;
        String lSequence;
        int lSeqNumber;
        String lName;
        int lActive;
        String lType;
        String lGroup;
        String lPoint;
        String lIP;
        int lPause;

        lColumns = new String[] {"SeqNumber", "Name", "Active", "Type", "Grp", "Point", "IP", "Pause"};
        lSelection = "Server = ?";
        lSelectionArgs = new String[] {pServer};
        lSequence = "SeqNumber, Name";

        lSwitches = new ArrayList<>();

        lDB = this.getReadableDatabase();

        lCursor = lDB.query("Switch", lColumns, lSelection, lSelectionArgs, null, null, lSequence);
        while (lCursor.moveToNext()){
            if (lCursor.isNull(0)){
                lSeqNumber = 0;
            } else {
                lSeqNumber = lCursor.getInt(0);
            }
            lName = lCursor.getString(1);
            lActive = lCursor.getInt(2);
            lType = lCursor.getString(3);
            lGroup = lCursor.getString(4);
            lPoint = lCursor.getString(5);
            lIP = lCursor.getString(6);
            lPause = lCursor.getInt(7);
            lSwitch = new Switch(lSeqNumber, lName, (lActive == 0) ? false : true, lType, lGroup, lPoint, lPause, lIP);
            lSwitches.add(lSwitch);
        }
        lCursor.close();
        lDB.close();

        return lSwitches;
    }

    public Switch xSwitch(String pServer, String pName){
        Switch lSwitch = null;
        SQLiteDatabase lDB;
        Cursor lCursor;
        String[] lColumns;
        String lSelection;
        String[] lSelectionArgs;
        int lSeqNumber;
        String lName;
        int lActive;
        String lType;
        String lGroup;
        String lPoint;
        String lIP;
        int lPause;

        lColumns = new String[] {"SeqNumber", "Name", "Active", "Type", "Grp", "Point", "IP", "Pause"};
        lSelection = "Server = ? AND Name = ?";
        lSelectionArgs = new String[] {pServer, pName};

        lDB = this.getReadableDatabase();

        lCursor = lDB.query("Switch", lColumns, lSelection, lSelectionArgs, null, null, null);
        if (lCursor.moveToNext()){
            if (lCursor.isNull(0)){
                lSeqNumber = 0;
            } else {
                lSeqNumber = lCursor.getInt(0);
            }
            lName = lCursor.getString(1);
            lActive = lCursor.getInt(2);
            lType = lCursor.getString(3);
            lGroup = lCursor.getString(4);
            lPoint = lCursor.getString(5);
            lIP = lCursor.getString(6);
            lPause = lCursor.getInt(7);
            lSwitch = new Switch(lSeqNumber, lName, (lActive == 0) ? false : true, lType, lGroup, lPoint, lPause, lIP);
        }
        lCursor.close();
        lDB.close();

        return lSwitch;
    }

    public void xSaveSwitches(List<Switch> pSwitches, String pServerName){
        List<Switch> lSwitches;
        int lCountOld;
        int lCountNew;
        Switch lSwitchOld;
        Switch lSwitchNew;
        boolean lEqual;

        lSwitches = xSwitches(pServerName);
        if (lSwitches.size() == pSwitches.size()){
            lEqual = true;
            for (lCountNew = 0; lCountNew < pSwitches.size(); lCountNew++){
                lSwitchNew = pSwitches.get(lCountNew);
                lEqual = false;
                for (lCountOld = 0; lCountOld < lSwitches.size(); lCountOld++){
                    lSwitchOld = lSwitches.get(lCountOld);
                    if (lSwitchOld.xIsEqual(lSwitchNew)){
                        lEqual = true;
                        break;
                    }
                }
                if (!lEqual){
                    break;
                }
            }
        } else {
            lEqual = false;
        }

        if (!lEqual){
            sReplaceSwitches(pSwitches, pServerName);
        }
    }

    private int sReplaceSwitches(List<Switch> pSwitches, String pServer){
        SQLiteDatabase lDB;
        String lSelection;
        String[] lSelectionArgs;
        int lCount;
        Switch lSwitch;
        ContentValues lValues;
        long lRow;
        int lResult;

        lResult = Result.cResultOK;
        lDB = this.getWritableDatabase();

        lSelection = "Server = ?";
        lSelectionArgs = new String[] {pServer};
        lDB.delete("Switch", lSelection, lSelectionArgs);

        lValues = new ContentValues();
        for (lCount = 0; lCount < pSwitches.size(); lCount++){
            lSwitch = pSwitches.get(lCount);
            lValues.clear();
            lValues.put("Server", pServer);
            lValues.put("SeqNumber", lSwitch.xSeqNumber());
            lValues.put("Name", lSwitch.xName());
            lValues.put("Active", (lSwitch.xActive()) ? 1 : 0);
            lValues.put("Type", lSwitch.xType());
            lValues.put("Grp", lSwitch.xGroup());
            lValues.put("Point", lSwitch.xPoint());
            lValues.put("IP", lSwitch.xIP());
            lValues.put("Pause", lSwitch.xPause());
            lRow = lDB.insert("Switch", null, lValues);
            if (lRow < 0){
                lResult = Result.cResultError;
                break;
            }
        }
        lDB.close();

        return lResult;
    }

    public int xModifySwitch(Switch pSwitch, String pServerName){
        SQLiteDatabase lDB;
        ContentValues lValues;
        String lSelection;
        String[] lSelectionArgs;
        int lNumber;
        int lResult;

        lValues = new ContentValues();
        lValues.put("SeqNumber", pSwitch.xSeqNumber());
        lValues.put("Active", (pSwitch.xActive()) ? 1 : 0);
        lValues.put("Type", pSwitch.xType());
        lValues.put("Grp", pSwitch.xGroup());
        lValues.put("Point", pSwitch.xPoint());
        lValues.put("IP", pSwitch.xIP());
        lValues.put("Pause", pSwitch.xPause());
        lSelection = "Server = ? AND Name = ?";
        lSelectionArgs = new String[] {pServerName, pSwitch.xName()};

        lDB = this.getWritableDatabase();

        lNumber = lDB.update("Switch", lValues, lSelection, lSelectionArgs);
        if (lNumber == 1){
            lResult = Result.cResultOK;
        } else {
            lResult = Result.cResultError;
        }

        lDB.close();

        return  lResult;
    }

    public int xNewSwitch(Switch pSwitch, String pServerName){
        SQLiteDatabase lDB;
        ContentValues lValues;
        long lRow;
        int lResult;

        lValues = new ContentValues();
        lValues.put("Server", pServerName);
        lValues.put("SeqNumber", pSwitch.xSeqNumber());
        lValues.put("Name", pSwitch.xName());
        lValues.put("Active", (pSwitch.xActive()) ? 1 : 0);
        lValues.put("Type", pSwitch.xType());
        lValues.put("Grp", pSwitch.xGroup());
        lValues.put("Point", pSwitch.xPoint());
        lValues.put("IP", pSwitch.xIP());
        lValues.put("Pause", pSwitch.xPause());

        lDB = this.getWritableDatabase();

        lRow = lDB.insert("Switch", null, lValues);
        if (lRow < 0){
            lResult = Result.cResultError;
        } else {
            lResult = Result.cResultOK;
        }

        lDB.close();

        return  lResult;
    }

    public int xDeleteSwitch(String pSwitchName, String pServerName){
        SQLiteDatabase lDB;
        String lSelection;
        String[] lSelectionArgs;
        int lNumber;
        int lResult;

        lDB = this.getWritableDatabase();

        lSelection = "Server = ? AND Name = ?";
        lSelectionArgs = new String[] {pServerName, pSwitchName};
        lNumber = lDB.delete("Switch", lSelection, lSelectionArgs);
        if (lNumber == 1){
            lResult = Result.cResultOK;
        } else {
            lResult = Result.cResultError;
        }

        lDB.close();

        return lResult;
    }
}
