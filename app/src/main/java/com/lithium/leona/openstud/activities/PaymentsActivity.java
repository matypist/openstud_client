package com.lithium.leona.openstud.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.lithium.leona.openstud.R;
import com.lithium.leona.openstud.data.InfoManager;
import com.lithium.leona.openstud.fragments.TabFragment;
import com.lithium.leona.openstud.helpers.ClientHelper;
import com.lithium.leona.openstud.helpers.LayoutHelper;
import com.lithium.leona.openstud.helpers.ThemeEngine;
import com.lithium.leona.openstud.listeners.DelayedDrawerListener;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import lithium.openstud.driver.core.Openstud;
import lithium.openstud.driver.core.Student;


public class PaymentsActivity extends AppCompatActivity {
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private DelayedDrawerListener ddl;
    private NavigationView nv;
    private Openstud os;
    private Student student;
    private SparseArray<Snackbar> snackBarMap = new SparseArray<>();
    private int selectedItem = -1;
    private TabFragment tabFrag;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeEngine.applyPaymentsTheme(this);
        setContentView(R.layout.activity_payments);
        ButterKnife.bind(this);
        os = InfoManager.getOpenStud(this);
        student = InfoManager.getInfoStudentCached(this, os);
        if (os == null || student == null) {
            InfoManager.clearSharedPreferences(getApplication());
            Intent i = new Intent(PaymentsActivity.this, LauncherActivity.class);
            startActivity(i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            return;
        }
        nv = LayoutHelper.setupNavigationDrawer(this, mDrawerLayout);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.payments);
        setupDrawerListener();
        View headerLayout = nv.getHeaderView(0);
        TextView navTitle = headerLayout.findViewById(R.id.nav_title);
        TextView navSubtitle = headerLayout.findViewById(R.id.nav_subtitle);
        navTitle.setText(getString(R.string.fullname, student.getFirstName(), student.getLastName()));
        navSubtitle.setText(student.getStudentID());
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt("tabSelected", -1);
        }
        tabFrag = TabFragment.newInstance(selectedItem);
        fragmentManager.beginTransaction().replace(R.id.content_frame, tabFrag).commit();

    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupDrawerListener() {
        ddl = new DelayedDrawerListener() {
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                int item = getItemPressedAndReset();
                if (item == -1) return;
                ClientHelper.startDrawerActivity(item, PaymentsActivity.this);
            }

        };
        mDrawerLayout.addDrawerListener(ddl);
        nv.setNavigationItemSelectedListener(
                item -> {
                    mDrawerLayout.closeDrawers();
                    ddl.setItemPressed(item.getItemId());
                    return true;
                });
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    public synchronized void createTextSnackBar(int string_id, int length) {
        if (snackBarMap.get(string_id, null) != null) return;
        Snackbar snackbar = LayoutHelper.createTextSnackBar(mDrawerLayout, string_id, length);
        snackBarMap.put(string_id, snackbar);
    }

    public synchronized void createActionSnackBar(final int string_id, int length, View.OnClickListener listener) {
        if (snackBarMap.get(string_id, null) != null) return;
        Snackbar snackbar = Snackbar
                .make(mDrawerLayout, getResources().getString(string_id), length).setAction(R.string.retry, listener);
        snackBarMap.put(string_id, snackbar);
        snackbar.addCallback(new Snackbar.Callback() {
            public void onDismissed(Snackbar snackbar, int event) {
                removeKeyFromMap(string_id);
            }
        });
        snackbar.show();
    }

    private synchronized void removeKeyFromMap(int id) {
        snackBarMap.remove(id);
    }

    public void updateSelectTab(int item) {
        selectedItem = item;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tabSelected", selectedItem);
    }
}
