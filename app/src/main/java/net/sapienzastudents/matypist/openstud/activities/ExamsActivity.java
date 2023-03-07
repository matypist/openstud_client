package net.sapienzastudents.matypist.openstud.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.fragments.BottomSheetOpisFragment;
import net.sapienzastudents.matypist.openstud.fragments.ExamDoableFragment;
import net.sapienzastudents.matypist.openstud.fragments.ExamsDoneFragment;
import net.sapienzastudents.matypist.openstud.fragments.ReservationsFragment;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import com.mikepenz.materialdrawer.Drawer;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExamsActivity extends BaseDataActivity {
    @BindView(R.id.container)
    ConstraintLayout mainLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private Fragment active;
    private ExamsDoneFragment fragDone;
    private ExamDoableFragment fragDoable;
    private ReservationsFragment fragRes;
    private Drawer drawer;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fm = getSupportFragmentManager();
            switch (item.getItemId()) {
                case R.id.navigation_completed:
                    switchToExamsCompletedFragment();
                    active = fm.findFragmentByTag("completed");
                    return true;
                case R.id.navigation_reservations:
                    switchToExamsReservationsFragment();
                    active = fm.findFragmentByTag("reservations");
                    return true;
                case R.id.navigation_search:
                    switchToExamsSearchFragment();
                    active = fm.findFragmentByTag("doable");
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        ThemeEngine.applyExamTheme(this);
        setContentView(R.layout.activity_exams);
        ButterKnife.bind(this);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_menu);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.exams);
        drawer = LayoutHelper.applyDrawer(this, toolbar, student);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState != null) {
            fragDone = (ExamsDoneFragment) fm.getFragment(savedInstanceState, "completed");
            fragRes = (ReservationsFragment) fm.getFragment(savedInstanceState, "reservations");
            fragDoable = (ExamDoableFragment) fm.getFragment(savedInstanceState, "doable");
            active = fm.getFragment(savedInstanceState, "active");
            if (active != null) fm.beginTransaction().show(active).commit();
            else {
                fm.beginTransaction().show(fragDone).commit();
                active = fragDone;
            }
        } else {
            fragRes = new ReservationsFragment();
            fragDone = new ExamsDoneFragment();
            fragDoable = new ExamDoableFragment();
            fm.beginTransaction().add(R.id.content_frame, fragRes, "reservations").hide(fragRes).commit();
            fm.beginTransaction().add(R.id.content_frame, fragDoable, "doable").hide(fragDoable).commit();
            fm.beginTransaction().add(R.id.content_frame, fragDone, "completed").commit();
            active = fragDone;
            analyzeExtras(getIntent().getExtras());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_exam, menu);
        Drawable drawableSort = menu.findItem(R.id.sort).getIcon();
        drawableSort = DrawableCompat.wrap(drawableSort);
        DrawableCompat.setTint(drawableSort, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.sort).setIcon(drawableSort);
        Drawable drawableOpis = menu.findItem(R.id.opis).getIcon();
        drawableOpis = DrawableCompat.wrap(drawableOpis);
        DrawableCompat.setTint(drawableOpis, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.opis).setIcon(drawableOpis);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (active == null) return true;
        if (active == fragDoable) {
            menu.findItem(R.id.sort).setVisible(false);
            menu.findItem(R.id.opis).setVisible(true);
        } else if (active == fragDone) {
            menu.findItem(R.id.sort).setVisible(true);
            menu.findItem(R.id.opis).setVisible(false);
        } else {
            menu.findItem(R.id.sort).setVisible(false);
            menu.findItem(R.id.opis).setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                showSortDialog();
                return true;
            case R.id.opis:
                showOpisDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();

    }

    private void showSortDialog() {
        Context context = this;
        int themeId = ThemeEngine.getAlertDialogTheme(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, themeId));
        builder.setTitle(getResources().getString(R.string.sort_by));
        builder.setSingleChoiceItems(R.array.sort, InfoManager.getSortType(context), (dialogInterface, i) -> {
            InfoManager.setSortType(context, i);
            fragDone.sortList(ClientHelper.Sort.getSort(i));
            dialogInterface.dismiss();
        });
        builder.show();
    }

    private void showOpisDialog() {
        BottomSheetOpisFragment opisFrag = BottomSheetOpisFragment.newInstance();
        opisFrag.show(getSupportFragmentManager(), opisFrag.getTag());
    }


    private void switchToExamsCompletedFragment() {
        FragmentManager manager = getSupportFragmentManager();
        if (fragDone != null && fragDone != active) {
            if (active != null) manager.beginTransaction().show(fragDone).hide(active).commit();
            else manager.beginTransaction().show(fragDone).commit();
        }
        invalidateOptionsMenu();
    }

    private void switchToExamsReservationsFragment() {
        FragmentManager manager = getSupportFragmentManager();
        if (fragRes != null && fragRes != active) {
            if (active != null) manager.beginTransaction().show(fragRes).hide(active).commit();
            else manager.beginTransaction().show(fragRes).commit();
        }
        invalidateOptionsMenu();
    }

    private void switchToExamsSearchFragment() {
        FragmentManager manager = getSupportFragmentManager();
        if (fragDoable != null && fragDoable != active) {
            if (active != null) manager.beginTransaction().show(fragDoable).hide(active).commit();
            else manager.beginTransaction().show(fragDoable).commit();
        }
        invalidateOptionsMenu();
    }


    public void createTextSnackBar(int string_id, int length) {
        LayoutHelper.createTextSnackBar(mainLayout, string_id, length);
    }

    public void createRetrySnackBar(final int string_id, int length, View.OnClickListener listener) {
        LayoutHelper.createActionSnackBar(mainLayout, string_id, R.string.retry, length, listener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt("tabSelected", itemId);
        getSupportFragmentManager().putFragment(outState, "completed", fragDone);
        getSupportFragmentManager().putFragment(outState, "reservations", fragRes);
        getSupportFragmentManager().putFragment(outState, "doable", fragDoable);
        if (active != null) getSupportFragmentManager().putFragment(outState, "active", active);
    }

    private void analyzeExtras(Bundle bdl) {
        if (bdl == null) return;
        int error = bdl.getInt("error", -1);
        if (error == -1) return;
        if (error == ClientHelper.Status.NO_BIOMETRICS.getValue())
            LayoutHelper.createTextSnackBar(mainLayout, R.string.login_no_biometrics_found, Snackbar.LENGTH_LONG);
        else if (error == ClientHelper.Status.NO_BIOMETRIC_HW.getValue()) {
            LayoutHelper.createTextSnackBar(mainLayout, R.string.login_no_biometric_hw_found, Snackbar.LENGTH_LONG);
        }
    }
}
