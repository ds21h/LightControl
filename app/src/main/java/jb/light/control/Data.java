package jb.light.control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 14-5-2017.
 */

class Data extends SQLiteOpenHelper {
    private static Data mInstance = null;

    private static final String cDBName = "LightControl.db";
    private static final int cDBVersion = 2;
    private static String mExternalFilesDir;

    static Data getInstance(Context pContext) {
        Context lContext;
        File lExternalFilesDir;

        if (mInstance == null) {
            synchronized (Data.class) {
                if (mInstance == null) {
                    lContext = pContext.getApplicationContext();
                    lExternalFilesDir = lContext.getExternalFilesDir(null);
                    if (lExternalFilesDir == null) {
                        mExternalFilesDir = "";
                    } else {
                        mExternalFilesDir = lExternalFilesDir.getAbsolutePath();
                    }
                    mInstance = new Data(lContext);
                }
            }
        }
        return mInstance;
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private Data(Context pContext) {
        super(pContext, mExternalFilesDir + "/" + cDBName, null, cDBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase pDB) {
        sDefineServer(pDB);
        sDefineSwitch(pDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase pDB, int pOldVersion, int pNewVersion) {
        switch (pOldVersion){
            case 1:{
                sUpgradeSwitch1_2(pDB);
                break;
            }
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

    private void sUpgradeSwitch1_2(SQLiteDatabase pDB){
        pDB.execSQL(
                "CREATE TABLE Switch_temp AS SELECT * " +
                        "FROM Switch"
        );
        pDB.execSQL(
                "DROP TABLE Switch"
        );
        sDefineSwitch(pDB);
        pDB.execSQL(
                "INSERT INTO Switch (" +
                        "_ID, " +
                        "Server, " +
                        "SeqNumber, " +
                        "Name, " +
                        "Active, " +
                        "IP, " +
                        "Pause" +
                ") " +
                "SELECT _ID, " +
                "Server, " +
                "SeqNumber, " +
                "Name, " +
                "Active, " +
                "IP, " +
                "Pause " +
                "FROM Switch_temp"
        );
        pDB.execSQL(
                "DROP TABLE Switch_temp"
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
                        "IP Text, " +
                        "Pause Integer Not Null" +
                        ")"
        );
    }

    Server xServer(String pName){
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
            lServer = new Server(lName, lSSID, lIP, lPort, lManager != 0);
        }
        lCursor.close();
        lDB.close();

        return lServer;
    }

    List<Server> xServers(String pSSID){
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
            lServer = new Server(lName, lSSID, lIP, lPort, lManager != 0);
            lServers.add(lServer);
        }
        lCursor.close();
        lDB.close();

        return lServers;
    }

    List<Server> xServers(){
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
            lServer = new Server(lName, lSSID, lIP, lPort, lManager != 0);
            lServers.add(lServer);
        }
        lCursor.close();
        lDB.close();

        return lServers;
    }

    int xNewServer(Server pServer){
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

    int xModifyServer(Server pServer){
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

    int xDeleteServer(String pServerName){
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

    List<SwitchLocal> xSwitches(String pServer){
        List<SwitchLocal> lSwitches;
        SwitchLocal lSwitch;
        SQLiteDatabase lDB;
        Cursor lCursor;
        String[] lColumns;
        String lSelection;
        String[] lSelectionArgs;
        String lSequence;
        int lSeqNumber;
        String lName;
        int lActive;
        String lIP;
        int lPause;

        lColumns = new String[] {"SeqNumber", "Name", "Active", "IP", "Pause"};
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
            lIP = lCursor.getString(3);
            lPause = lCursor.getInt(4);
            lSwitch = new SwitchLocal(lSeqNumber, lName, lActive != 0, lPause, lIP);
            lSwitches.add(lSwitch);
        }
        lCursor.close();
        lDB.close();

        return lSwitches;
    }

    SwitchLocal xSwitch(String pServer, String pName){
        SwitchLocal lSwitch = null;
        SQLiteDatabase lDB;
        Cursor lCursor;
        String[] lColumns;
        String lSelection;
        String[] lSelectionArgs;
        int lSeqNumber;
        String lName;
        int lActive;
        String lIP;
        int lPause;

        lColumns = new String[] {"SeqNumber", "Name", "Active", "IP", "Pause"};
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
            lIP = lCursor.getString(3);
            lPause = lCursor.getInt(4);
            lSwitch = new SwitchLocal(lSeqNumber, lName, lActive != 0, lPause, lIP);
        }
        lCursor.close();
        lDB.close();

        return lSwitch;
    }

    int xReplaceSwitches(List<SwitchLocal> pSwitches, String pServer){
        SQLiteDatabase lDB;
        String lSelection;
        String[] lSelectionArgs;
        int lCount;
        SwitchLocal lSwitch;
        ContentValues lValues;
        long lRow;
        int lResult;

        lResult = Result.cResultOK;
        lDB = this.getWritableDatabase();
        lDB.beginTransaction();

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
            lValues.put("IP", lSwitch.xIP());
            lValues.put("Pause", lSwitch.xPause());
            lRow = lDB.insert("Switch", null, lValues);
            if (lRow < 0){
                lResult = Result.cResultError;
                break;
            }
        }
        lDB.setTransactionSuccessful();
        lDB.endTransaction();
        lDB.close();

        return lResult;
    }

    int xModifySwitch(SwitchLocal pSwitch, String pServerName){
        SQLiteDatabase lDB;
        ContentValues lValues;
        String lSelection;
        String[] lSelectionArgs;
        int lNumber;
        int lResult;

        lValues = new ContentValues();
        lValues.put("SeqNumber", pSwitch.xSeqNumber());
        lValues.put("Active", (pSwitch.xActive()) ? 1 : 0);
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

    int xNewSwitch(SwitchLocal pSwitch, String pServerName){
        SQLiteDatabase lDB;
        ContentValues lValues;
        long lRow;
        int lResult;

        lValues = new ContentValues();
        lValues.put("Server", pServerName);
        lValues.put("SeqNumber", pSwitch.xSeqNumber());
        lValues.put("Name", pSwitch.xName());
        lValues.put("Active", (pSwitch.xActive()) ? 1 : 0);
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

    int xDeleteSwitch(String pSwitchName, String pServerName){
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
