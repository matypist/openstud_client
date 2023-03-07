package net.sapienzastudents.matypist.openstud.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Bundle bdl = getIntent().getExtras();
        if (!InfoManager.getSaveFlag(getApplication()))
            InfoManager.clearSharedPreferences(getApplication());
        if (InfoManager.hasLogin(getApplication())) {
            Intent intent = new Intent(LauncherActivity.this, ExamsActivity.class);
            if (bdl != null) intent.putExtras(bdl);
            if (!PreferenceManager.isBiometricsEnabled(this)) startActivity(intent);
            else startLogin(bdl);
        } else {
            Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
            if (bdl != null) intent.putExtras(bdl);
            startActivity(intent);
        }
    }


    private void startLogin(Bundle bundle) {
        Intent intent = new Intent(LauncherActivity.this, LoginActivity.class);
        if (bundle != null) intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        LauncherActivity.this.finish();
    }
}
