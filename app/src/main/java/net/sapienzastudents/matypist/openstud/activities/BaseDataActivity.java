package net.sapienzastudents.matypist.openstud.activities;

import androidx.appcompat.app.AppCompatActivity;

import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;

import matypist.openstud.driver.core.Openstud;
import matypist.openstud.driver.core.models.Student;

abstract class BaseDataActivity extends AppCompatActivity {
    Student student;
    Openstud os;
    ThemeEngine.Theme currentTheme;
    public BaseDataActivity() {
        super();
    }

    public boolean initData() {
        currentTheme = ThemeEngine.resolveTheme(this, PreferenceManager.getTheme(this));
        os = InfoManager.getOpenStud(this);
        student = InfoManager.getInfoStudentCached(this, os);
        if (os == null || student == null) {
            ClientHelper.rebirthApp(this, null);
            return false;
        }
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        ThemeEngine.Theme newTheme = ThemeEngine.resolveTheme(this, PreferenceManager.getTheme(this));
        if (newTheme != currentTheme) this.recreate();
    }
}
