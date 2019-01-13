package jb.light.control;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.ArrayList;

/**
 * Created by Jan on 5-10-2015.
 */
public class SelectServer extends Activity {
    public static final String cServerName = "ServerName";
    public static final String cServers = "Servers";
    private static final String cSelection = "Selection";

    private ArrayList<String> mServers;
    private int mSelection;
    private Spinner mSpServer;
    private ArrayAdapter<String> mAdpServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_server_layout);

        Bundle lBundle;

        if (savedInstanceState==null){
            lBundle = getIntent().getExtras();
            if (lBundle == null) {
                finish();
            } else {
                mServers = lBundle.getStringArrayList(cServers);
                mSelection = 0;
            }
        } else {
            mServers = savedInstanceState.getStringArrayList(cServers);
            mSelection = savedInstanceState.getInt(cSelection);
        }

        mSpServer = findViewById(R.id.spServer);
        mAdpServer = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mServers);
        mAdpServer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpServer.setAdapter(mAdpServer);
        mSpServer.setSelection(mSelection);
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putStringArrayList(cServers, mServers);
        savedInstanceState.putInt(cSelection, mSpServer.getSelectedItemPosition());

        super.onSaveInstanceState(savedInstanceState);
    }

    public void sProcessSelection(View pView){
        String lChoice;
        Intent lInt;
        Bundle lBundle;

        lChoice = (String)mSpServer.getSelectedItem();
        lInt = new Intent();
        lBundle = new Bundle();
        lBundle.putString(cServerName, lChoice);
        lInt.putExtras(lBundle);
        setResult(RESULT_OK, lInt);
        finish();
    }
}
