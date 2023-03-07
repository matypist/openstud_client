package net.sapienzastudents.matypist.openstud.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.adapters.ExamDoableAdapter;
import net.sapienzastudents.matypist.openstud.activities.ExamsActivity;
import net.sapienzastudents.matypist.openstud.activities.SearchSessionsResultActivity;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import matypist.openstud.driver.core.models.ExamDoable;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class ExamDoableFragment extends BaseDataFragment {

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView rv;
    @BindView(R.id.empty_layout)
    LinearLayout emptyView;
    @BindView(R.id.empty_button_reload)
    Button emptyButton;
    @BindView(R.id.empty_text)
    TextView emptyText;
    private List<ExamDoable> examsDoable;
    private ExamDoableAdapter adapter;
    private LocalDateTime lastUpdate;
    private boolean firstStart = true;
    private ExamsDoableHandler h = new ExamsDoableHandler(this);

    @OnClick(R.id.empty_button_reload)
    public void OnClick(View v) {
        refreshExamsDoable();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.base_swipe_fragment, null);
        Activity activity = getActivity();
        if (!initData() || activity == null) return v;
        ButterKnife.bind(this, v);
        examsDoable = new LinkedList<>();
        emptyText.setText(getResources().getString(R.string.no_exams_doable_found));
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        rv.setLayoutManager(llm);
        adapter = new ExamDoableAdapter(activity, examsDoable, v1 -> {
            int itemPosition = rv.getChildLayoutPosition(v1);
            if (itemPosition < examsDoable.size()) {
                ExamDoable exam = examsDoable.get(itemPosition);
                Intent intent = new Intent(activity, SearchSessionsResultActivity.class);
                intent.putExtra("exam", new Gson().toJson(exam, ExamDoable.class));
                activity.startActivity(intent);
            }
        });
        rv.setAdapter(adapter);
        int refreshId = ThemeEngine.getSpinnerColorId(activity);
        swipeRefreshLayout.setColorSchemeResources(refreshId, refreshId, refreshId);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeEngine.resolveColorFromAttribute(activity, R.attr.SwipeSpinnerBackgroundColor, R.color.white));
        swipeRefreshLayout.setOnRefreshListener(this::refreshExamsDoable);
        swipeRefreshLayout.setEnabled(false);
        new Thread(() -> {
            List<ExamDoable> exams_cached = InfoManager.getExamsDoableCached(activity, os);
            if (exams_cached != null && !exams_cached.isEmpty()) {
                examsDoable.addAll(exams_cached);
            } else swapViews(exams_cached);
            activity.runOnUiThread(() -> {
                swipeRefreshLayout.setEnabled(true);
                adapter.notifyDataSetChanged();
            });
            if (savedInstanceState == null) {
                try {
                    Thread.sleep(500);
                    refreshExamsDoable();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return v;
    }

    public void onResume() {
        super.onResume();
        LocalDateTime time = getTimer();
        Activity activity = getActivity();
        if (firstStart) {
            firstStart = false;
            setRefreshing(false);
        }
        else if (activity != null && (time == null || Duration.between(time, LocalDateTime.now()).toMinutes() > 30))
            refreshExamsDoable();
    }

    private void refreshExamsDoable() {
        final Activity activity = getActivity();
        if (activity == null || os == null) return;
        setRefreshing(true);
        setButtonReloadStatus(false);
        new Thread(() -> {
            List<ExamDoable> update = null;
            try {
                update = InfoManager.getExamsDoable(activity, os);
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

            if (update == null) {
                setRefreshing(false);
                setButtonReloadStatus(true);
                swapViews(examsDoable);
                return;
            }
            updateTimer();
            refreshDataSet(update);
        }).start();
    }

    private synchronized void refreshDataSet(List<ExamDoable> update) {
        boolean flag = false;
        if (update != null && !examsDoable.equals(update)) {
            flag = true;
            examsDoable.clear();
            examsDoable.addAll(update);
        }
        final boolean finalFlag = flag;
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (finalFlag) adapter.notifyDataSetChanged();
            swapViews(examsDoable);
            setRefreshing(false);
            emptyButton.setEnabled(true);
        });
    }

    private void setRefreshing(final boolean bool) {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> swipeRefreshLayout.setRefreshing(bool));
    }

    private void setButtonReloadStatus(final boolean bool) {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> emptyButton.setEnabled(bool));
    }

    private void swapViews(final List<ExamDoable> exams) {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (exams == null || exams.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            }
        });
    }

    private synchronized void updateTimer() {
        lastUpdate = LocalDateTime.now();
    }

    private synchronized LocalDateTime getTimer() {
        return lastUpdate;
    }

    private static class ExamsDoableHandler extends Handler {
        private final WeakReference<ExamDoableFragment> frag;

        private ExamsDoableHandler(ExamDoableFragment frag) {
            super(Looper.getMainLooper());
            this.frag = new WeakReference<>(frag);
        }

        @Override
        public void handleMessage(Message msg) {
            final ExamDoableFragment examsDoableFrag = frag.get();
            if (examsDoableFrag == null) return;
            ExamsActivity activity = (ExamsActivity) examsDoableFrag.getActivity();
            if (activity != null) {
                View.OnClickListener listener = v -> examsDoableFrag.refreshExamsDoable();
                if (msg.what == ClientHelper.Status.CONNECTION_ERROR.getValue()) {
                    activity.createRetrySnackBar(R.string.connection_error, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.INVALID_RESPONSE.getValue()) {
                    activity.createRetrySnackBar(R.string.invalid_response_error, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.MAINTENANCE.getValue()) {
                    activity.createRetrySnackBar(R.string.infostud_maintenance, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.USER_NOT_ENABLED.getValue()) {
                    activity.createTextSnackBar(R.string.user_not_enabled_error, Snackbar.LENGTH_LONG);
                } else if (msg.what == (ClientHelper.Status.INVALID_CREDENTIALS).getValue() || msg.what == ClientHelper.Status.EXPIRED_CREDENTIALS.getValue() || msg.what == ClientHelper.Status.ACCOUNT_BLOCKED.getValue()) {
                    ClientHelper.rebirthApp(activity, msg.what);
                } else if (msg.what == ClientHelper.Status.UNEXPECTED_VALUE.getValue()) {
                    activity.createTextSnackBar(R.string.invalid_response_error, Snackbar.LENGTH_LONG);
                }
            }
        }
    }

}
