package net.sapienzastudents.matypist.openstud.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.adapters.NewsAdapter;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import com.mikepenz.materialdrawer.Drawer;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import matypist.openstud.driver.core.models.News;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class NewsActivity extends BaseDataActivity {
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
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
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private Drawer drawer;
    private LocalDateTime lastUpdate;
    private String locale;
    private NewsAdapter adapter;
    private List<News> news;
    private NewsHandler h = new NewsHandler(this);

    @OnClick(R.id.empty_button_reload)
    public void OnClick(View v) {
        refreshNews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        ThemeEngine.applyNewsTheme(this);
        setContentView(R.layout.activity_news);
        ButterKnife.bind(this);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        drawer = LayoutHelper.applyDrawer(this, toolbar, student);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.news);
        locale = getLocale();
        emptyText.setText(getResources().getString(R.string.no_news_found));
        news = new LinkedList<>();
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        adapter = new NewsAdapter(this, news, v -> {
            int itemPosition = rv.getChildLayoutPosition(v);
            if (itemPosition < news.size()) {
                News el = news.get(itemPosition);
                ClientHelper.createCustomTab(this, el.getUrl());
            }
        });
        rv.setAdapter(adapter);
        int refreshColorId = ThemeEngine.getSpinnerColorId(this);
        swipeRefreshLayout.setColorSchemeResources(refreshColorId, refreshColorId, refreshColorId);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeEngine.resolveColorFromAttribute(this, R.attr.SwipeSpinnerBackgroundColor, R.color.white));
        List<News> news_cached = InfoManager.getNewsCached(this, os, locale);
        if (news_cached != null && !news_cached.isEmpty()) {
            news.addAll(news_cached);
            adapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setOnRefreshListener(this::refreshNews);
        if (savedInstanceState == null) refreshNews();

    }

    private void refreshNews() {
        if (os == null) return;
        setRefreshing(true);
        setButtonReloadStatus(false);
        new Thread(() -> {
            List<News> update = null;
            try {
                update = InfoManager.getNews(this, os, locale);
                if (update == null)
                    h.sendEmptyMessage(ClientHelper.Status.UNEXPECTED_VALUE.getValue());
                else h.sendEmptyMessage(ClientHelper.Status.OK.getValue());

            } catch (OpenstudConnectionException e) {
                h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR.getValue());
                e.printStackTrace();
            } catch (OpenstudInvalidResponseException e) {
                if (e.isRateLimit()) h.sendEmptyMessage(ClientHelper.Status.RATE_LIMIT.getValue());
                else h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
                e.printStackTrace();
            }
            if (update == null) {
                setRefreshing(false);
                setButtonReloadStatus(true);
                swapViews(news);
                return;
            }
            updateTimer();
            refreshDataSet(update);
        }).start();
    }

    public synchronized void refreshDataSet(List<News> update) {
        boolean flag = false;
        if (update != null && !news.equals(update)) {
            flag = true;
            news.clear();
            news.addAll(update);
        }
        final boolean finalFlag = flag;
        runOnUiThread(() -> {
            if (finalFlag) adapter.notifyDataSetChanged();
            swapViews(news);
            swipeRefreshLayout.setRefreshing(false);
            emptyButton.setEnabled(true);
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();

    }

    private String getLocale() {
        if (!Locale.getDefault().getLanguage().equals("it")) return "en";
        else return "it";
    }

    private void setRefreshing(final boolean bool) {
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(bool));
    }

    private void setButtonReloadStatus(final boolean bool) {
        runOnUiThread(() -> emptyButton.setEnabled(bool));
    }

    private synchronized void updateTimer() {
        lastUpdate = LocalDateTime.now();
    }

    private synchronized LocalDateTime getTimer() {
        return lastUpdate;
    }

    private void swapViews(final List<News> news) {
        runOnUiThread(() -> {
            if (news == null || news.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            }
        });
    }

    /*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt("tabSelected", itemId);
        Parcelable recyclerViewState;
        recyclerViewState = rv.getLayoutManager().onSaveInstanceState();
        outState.putParcelable("recyclerState", recyclerViewState);
    }
    */

    protected void onRestart() {
        super.onRestart();
        LocalDateTime time = getTimer();
        if (time == null || Duration.between(time, LocalDateTime.now()).toMinutes() > 60)
            refreshNews();
    }

    private static class NewsHandler extends Handler {
        private final WeakReference<NewsActivity> activity;

        private NewsHandler(NewsActivity activity) {
            super(Looper.getMainLooper());
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final NewsActivity activity = this.activity.get();
            if (activity == null) return;
            View.OnClickListener ocl = v -> activity.refreshNews();
            if (msg.what == ClientHelper.Status.CONNECTION_ERROR.getValue()) {
                LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, ocl);
            } else if (msg.what == ClientHelper.Status.INVALID_RESPONSE.getValue()) {
                LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, ocl);
            } else if (msg.what == ClientHelper.Status.UNEXPECTED_VALUE.getValue()) {
                LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.invalid_response_error, Snackbar.LENGTH_LONG);
            } else if (msg.what == ClientHelper.Status.RATE_LIMIT.getValue()) {
                LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.rate_limit, R.string.retry, Snackbar.LENGTH_LONG, ocl);
            }
        }
    }
}
