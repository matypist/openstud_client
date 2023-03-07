package net.sapienzastudents.matypist.openstud.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.adapters.FakeExamAdapter;
import net.sapienzastudents.matypist.openstud.fragments.BottomSheetStatsFragment;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import net.sapienzastudents.matypist.openstud.helpers.SimpleItemTouchHelperCallback;
import com.mikepenz.materialdrawer.Drawer;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.OpenstudHelper;
import matypist.openstud.driver.core.models.ExamDone;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class StatsActivity extends BaseDataActivity {

    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.arithmeticValue)
    TextView arithmeticValue;
    @BindView(R.id.weightedValue)
    TextView weightedValue;
    @BindView(R.id.totalCFU)
    TextView totalCFU;
    @BindView(R.id.baseGraduation)
    TextView baseGraduation;
    @BindView(R.id.graph)
    LineChart graph;
    @BindView(R.id.graph2)
    BarChart graph2;
    @BindView(R.id.graph_card)
    CardView graphCard;
    @BindView(R.id.graph2_card)
    CardView graphCard2;
    @BindView(R.id.recyclerView)
    RecyclerView rv;
    private Drawer drawer;
    private StatsHandler h = new StatsHandler(this);
    private List<ExamDone> exams = new LinkedList<>();
    private LocalDateTime lastUpdate;
    private boolean firstStart = true;
    private int laude;
    private boolean minMaxIgnoreInBase;
    private List<ExamDone> examsFake;
    private FakeExamAdapter adapter;
    private boolean showIcon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        ThemeEngine.applyStatsTheme(this);
        setContentView(R.layout.activity_stats);
        ThemeEngine.applyPaymentsTheme(this);
        ButterKnife.bind(this);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        drawer = LayoutHelper.applyDrawer(this, toolbar, student);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.stats);
        List<ExamDone> exams_cached = InfoManager.getExamsDoneCached(this, os);
        createRecyclerView(exams_cached);
        if (savedInstanceState == null) refreshExamsDone();
        else {
            NestedScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.scrollTo(0, 0);
        }

    }

    public void addFakeExam(ExamDone exam) {
        synchronized (this) {
            examsFake.add(exam);
            InfoManager.saveFakeExams(this, examsFake);
            adapter.notifyDataSetChanged();
        }
        updateStats();
        ClientHelper.updateGradesWidget(this, true);
    }

    public void onResume() {
        super.onResume();
        LocalDateTime time = getTimer();
        if (firstStart) firstStart = false;
        else if (PreferenceManager.getLaudeValue(this) != laude
                || PreferenceManager.isMinMaxExamIgnoredInBaseGraduation(this) != minMaxIgnoreInBase
                || time == null || Duration.between(time, LocalDateTime.now()).toMinutes() > 30)
            refreshExamsDone();
    }

    private void updateStats() {
        runOnUiThread(() -> {
            if ((exams == null || exams.isEmpty() || !ClientHelper.hasPassedExams(exams)) && (examsFake == null || examsFake.isEmpty())) {
                if (exams == null) setIconVisibility(false);
                else if (exams.isEmpty()) setIconVisibility(true);
                totalCFU.setText("--");
                arithmeticValue.setText("--");
                weightedValue.setText("--");
                baseGraduation.setText("--");
                graphCard.setVisibility(View.GONE);
                graphCard2.setVisibility(View.GONE);
                return;
            }
            setIconVisibility(true);
            showLaudeNotification();
            laude = PreferenceManager.getLaudeValue(this);
            minMaxIgnoreInBase = PreferenceManager.isMinMaxExamIgnoredInBaseGraduation(this);
            exams.removeAll(examsFake);
            exams.addAll(examsFake);
            updateGraphs();
        });
    }

    private void updateGraphs() {
        runOnUiThread(() -> {
            OpenstudHelper.sortExamByDate(exams, false);
            graphCard.setVisibility(View.VISIBLE);
            graphCard2.setVisibility(View.VISIBLE);
            NumberFormat numFormat = NumberFormat.getInstance();
            numFormat.setMaximumFractionDigits(2);
            numFormat.setMinimumFractionDigits(1);
            totalCFU.setText(String.valueOf(OpenstudHelper.getSumCFU(exams)));
            double arithmetic = OpenstudHelper.computeArithmeticAverage(exams, laude);
            double weighted = OpenstudHelper.computeWeightedAverage(exams, laude);
            int base = OpenstudHelper.computeBaseGraduation(exams, laude, minMaxIgnoreInBase);
            if (arithmetic == -1) {
                arithmeticValue.setText("--");
            } else arithmeticValue.setText(numFormat.format(arithmetic));
            if (weighted == -1) {
                weightedValue.setText("--");
            } else weightedValue.setText(numFormat.format(weighted));
            if (base == -1) {
                baseGraduation.setText("--");
            } else baseGraduation.setText(String.valueOf(base));
            makeAverageGraph();
            makeGradeBarGraph();
        });
    }

    private void makeAverageGraph() {
        graph.clear();
        List<Entry> serie1 = ClientHelper.generateMarksPoints(exams, laude);
        List<Entry> serie2 = ClientHelper.generateWeightPoints(exams, laude);
        LineDataSet datasetLine1 = new LineDataSet(serie1, getResources().getString(R.string.grades));
        LineDataSet datasetLineGraph2 = new LineDataSet(serie2, getResources().getString(R.string.weighted_average));
        datasetLineGraph2.setDrawCircles(false);
        datasetLine1.setColor(Color.parseColor("#0077CC"));
        datasetLine1.setCircleColor(Color.parseColor("#004a80"));
        datasetLineGraph2.setColor(Color.parseColor("#b40a23"));
        datasetLine1.setLineWidth(2);
        datasetLineGraph2.setLineWidth(2);
        LineData dataLine = new LineData();
        dataLine.addDataSet(datasetLine1);
        dataLine.addDataSet(datasetLineGraph2);
        dataLine.setDrawValues(false);
        dataLine.setHighlightEnabled(false);
        graph.getLegend().setTextColor(ThemeEngine.getPrimaryTextColor(this));
        graph.getLegend().setTextSize(Utils.convertDpToPixel(5));
        graph.getLegend().setXEntrySpace(20);
        graph.setData(dataLine);
        graph.invalidate();
        graph.getAxisRight().setEnabled(false);
        graph.setScaleEnabled(false);
        graph.getDescription().setEnabled(false);
        graph.getAxisLeft().setTextSize(Utils.convertDpToPixel(5));
        graph.getXAxis().setEnabled(false);
        graph.getAxisLeft().setTextColor(ThemeEngine.getPrimaryTextColor(this)); // left y-axis
        graph.getAxisLeft().setGridColor(ThemeEngine.getSecondaryTextColor(this));
        graph.getXAxis().setTextColor(ThemeEngine.getPrimaryTextColor(this));
        graph.getXAxis().setGridColor(ThemeEngine.getSecondaryTextColor(this));
        graph.getAxisLeft().setGranularity(2);
        graph.setScaleEnabled(false);
    }

    private void makeGradeBarGraph() {
        graph2.clear();
        ArrayList<BarEntry> entriesGraph2 = ClientHelper.generateMarksBar(exams);
        BarDataSet datasetBar = new BarDataSet(entriesGraph2, "Marks");
        BarData dataBar = new BarData(datasetBar);
        dataBar.setHighlightEnabled(false);
        dataBar.setDrawValues(false);
        datasetBar.setColor(Color.parseColor("#0077CC"));
        graph2.setData(dataBar);
        graph2.getAxisRight().setEnabled(false);
        graph2.setScaleEnabled(false);
        graph2.getDescription().setEnabled(false);
        graph2.getLegend().setEnabled(false);
        graph2.getAxisLeft().setTextSize(Utils.convertDpToPixel(5));
        graph2.getAxisLeft().setGranularity(1);
        graph2.getAxisLeft().setMinWidth(0);
        graph2.getXAxis().setTextSize(Utils.convertDpToPixel(4));
        graph2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        graph2.getXAxis().setGranularity(1);
        graph2.getAxisLeft().setTextColor(ThemeEngine.getPrimaryTextColor(this)); // left y-axis
        graph2.getAxisLeft().setGridColor(ThemeEngine.getSecondaryTextColor(this));
        graph2.getXAxis().setTextColor(ThemeEngine.getPrimaryTextColor(this));
        graph2.getXAxis().setGridColor(ThemeEngine.getSecondaryTextColor(this));
        graph2.getXAxis().setValueFormatter((value, axis) -> {
            if (value <= 30) {
                return String.valueOf((int) value);
            } else {
                return "30L";
            }
        });
    }

    private void refreshExamsDone() {
        if (os == null) return;
        setRefreshing(true);
        new Thread(() -> {
            List<ExamDone> update = null;
            try {
                update = InfoManager.getExamsDone(this, os);
                if (update == null)
                    h.sendEmptyMessage(ClientHelper.Status.UNEXPECTED_VALUE.getValue());
                else h.sendEmptyMessage(ClientHelper.Status.OK.getValue());

            } catch (OpenstudConnectionException e) {
                h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR.getValue());
                e.printStackTrace();
            } catch (OpenstudInvalidResponseException e) {
                if (e.isMaintenance())
                    h.sendEmptyMessage(ClientHelper.Status.MAINTENANCE.getValue());
                h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
                e.printStackTrace();
            } catch (OpenstudInvalidCredentialsException e) {
                h.sendEmptyMessage(ClientHelper.getStatusFromLoginException(e).getValue());
                e.printStackTrace();
            }
            if (update != null && !update.isEmpty()) {
                synchronized (this) {
                    exams.removeAll(examsFake);
                    if (exams.equals(update)) {
                        setRefreshing(false);
                        exams.addAll(examsFake);
                    } else {
                        exams.clear();
                        exams.addAll(update);
                        updateStats();
                        refreshFakeExams();
                        exams.addAll(examsFake);
                        ClientHelper.updateGradesWidget(this, true);
                    }
                }
                updateTimer();
                setRefreshing(false);
            } else {
                updateTimer();
                updateStats();
                setRefreshing(false);
            }
        }).start();
    }


    private void refreshFakeExams() {
        List<ExamDone> newFake = InfoManager.getFakeExams(this, os);
        if (newFake != null && !newFake.equals(examsFake)) {
            examsFake.clear();
            examsFake.addAll(newFake);
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }

    private void setRefreshing(final boolean bool) {
        this.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(bool));
    }

    private synchronized void updateTimer() {
        lastUpdate = LocalDateTime.now();
    }

    private synchronized LocalDateTime getTimer() {
        return lastUpdate;
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_stats, menu);
        Drawable drawable = menu.findItem(R.id.add_exam).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.add_exam).setIcon(drawable);
        if (!showIcon) {
            menu.findItem(R.id.add_exam).setVisible(false);
        } else {
            menu.findItem(R.id.add_exam).setVisible(true);
        }
        return true;
    }

    private void showLaudeNotification() {
        if (PreferenceManager.getStatsNotificationEnabled(this)) {
            LayoutHelper.createActionSnackBar(mainLayout, R.string.no_value_laude, R.string.edit, 4000, v -> {
                Intent i = new Intent(StatsActivity.this, SettingsPrefActivity.class);
                startActivity(i);
            });
            PreferenceManager.setStatsNotificationEnabled(this, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_exam:
                BottomSheetStatsFragment bottomSheetStatsFragment = BottomSheetStatsFragment.newInstance(InfoManager.getExamsDoableCached(this, os));
                bottomSheetStatsFragment.show(getSupportFragmentManager(), bottomSheetStatsFragment.getTag());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setIconVisibility(boolean visible) {
        if ((visible && !showIcon) || (!visible && showIcon)) {
            showIcon = visible;
            invalidateOptionsMenu();
        }
    }

    private void createRecyclerView(List<ExamDone> exams_cached) {
        examsFake = InfoManager.getFakeExams(this, os);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        adapter = new FakeExamAdapter(this, examsFake, (exam, position) -> {
            removeFakeExam(position);
        });
        rv.setAdapter(adapter);
        int refreshColorId = ThemeEngine.getSpinnerColorId(this);
        swipeRefreshLayout.setColorSchemeResources(refreshColorId, refreshColorId, refreshColorId);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeEngine.resolveColorFromAttribute(this, R.attr.SwipeSpinnerBackgroundColor, R.color.white));
        swipeRefreshLayout.setOnRefreshListener(this::refreshExamsDone);
        adapter.notifyDataSetChanged();
        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rv);
        if (exams_cached != null && !exams_cached.isEmpty()) {
            exams.addAll(exams_cached);
            updateStats();
        }
        adapter.notifyDataSetChanged();
    }

    public void removeFakeExam(int position) {
        synchronized (this) {
            if (position == -1 || position >= examsFake.size()) return;
            exams.remove(examsFake.get(position));
            examsFake.remove(position);
            adapter.notifyItemRemoved(position);
            InfoManager.saveFakeExams(this, examsFake);
        }
        updateStats();
        ClientHelper.updateGradesWidget(this, true);
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();

    }

    private static class StatsHandler extends Handler {
        private final WeakReference<StatsActivity> activity;

        private StatsHandler(StatsActivity activity) {
            super(Looper.getMainLooper());
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final StatsActivity activity = this.activity.get();
            if (activity == null) return;
            View.OnClickListener listener = v -> activity.refreshExamsDone();
            if (msg.what == ClientHelper.Status.CONNECTION_ERROR.getValue()) {
                LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, listener);
            } else if (msg.what == ClientHelper.Status.INVALID_RESPONSE.getValue()) {
                LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, listener);
            } else if (msg.what == ClientHelper.Status.MAINTENANCE.getValue()) {
                LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.infostud_maintenance, R.string.retry, Snackbar.LENGTH_LONG, listener);
            } else if (msg.what == ClientHelper.Status.USER_NOT_ENABLED.getValue()) {
                LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.user_not_enabled_error, Snackbar.LENGTH_LONG);
            } else if (msg.what == (ClientHelper.Status.INVALID_CREDENTIALS).getValue() || msg.what == ClientHelper.Status.EXPIRED_CREDENTIALS.getValue() || msg.what == ClientHelper.Status.ACCOUNT_BLOCKED.getValue()) {
                ClientHelper.rebirthApp(activity, msg.what);
            } else if (msg.what == ClientHelper.Status.UNEXPECTED_VALUE.getValue()) {
                LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.invalid_response_error, Snackbar.LENGTH_LONG);
            }
        }
    }

}
