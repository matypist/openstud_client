package net.sapienzastudents.matypist.openstud.activities;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.fragments.TabFragment;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import com.mikepenz.materialdrawer.Drawer;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PaymentsActivity extends BaseDataActivity {
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private Drawer drawer;
    private SparseArray<Snackbar> snackBarMap = new SparseArray<>();
    private int selectedItem = -1;
    private TabFragment tabFrag;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        ThemeEngine.applyPaymentsTheme(this);
        setContentView(R.layout.activity_payments);
        ButterKnife.bind(this);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        drawer = LayoutHelper.applyDrawer(this, toolbar, student);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.payments);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt("tabSelected", -1);
            tabFrag = (TabFragment) getSupportFragmentManager().getFragment(savedInstanceState, "tab");
        } else tabFrag = TabFragment.newInstance(selectedItem);
        fragmentManager.beginTransaction().replace(R.id.content_frame, tabFrag).commit();

    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();

    }


    public synchronized void createTextSnackBar(int string_id, int length) {
        if (snackBarMap.get(string_id, null) != null) return;
        Snackbar snackbar = LayoutHelper.createTextSnackBar(mainLayout, string_id, length);
        snackBarMap.put(string_id, snackbar);
    }

    public synchronized void createActionSnackBar(final int string_id, int length, View.OnClickListener listener) {
        if (snackBarMap.get(string_id, null) != null) return;
        Snackbar snackbar = Snackbar
                .make(mainLayout, getResources().getString(string_id), length).setAction(R.string.retry, listener);
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
        getSupportFragmentManager().putFragment(outState, "tab", tabFrag);
    }
}
