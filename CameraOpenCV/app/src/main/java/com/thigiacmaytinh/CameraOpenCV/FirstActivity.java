package com.thigiacmaytinh.CameraOpenCV;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class FirstActivity extends AppCompatActivity {
    public static String TAG_CASCADES = "CASCADES";
    public static int TAG_CASCADES_HAAR = 1;
    public static int TAG_CASCADES_LBP = 2;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        Button btnHaar = (Button) findViewById(R.id.btnHaar);
        btnHaar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnHaar_onClick(v);
            }
        });
        Button btnLbp = (Button) findViewById(R.id.btnLbp);
        btnLbp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLbp_onClick(v);
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    public void btnHaar_onClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(TAG_CASCADES,TAG_CASCADES_HAAR);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void btnLbp_onClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(TAG_CASCADES,TAG_CASCADES_LBP);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
