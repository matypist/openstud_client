package net.sapienzastudents.matypist.openstud.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.adapters.CustomCourseAdapter;
import net.sapienzastudents.matypist.openstud.data.CustomCourse;
import net.sapienzastudents.matypist.openstud.data.PreferenceManager;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomCourseListActivity extends AppCompatActivity {

    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
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
    private CustomCourseAdapter adapter;
    private List<CustomCourse> courses;
    private boolean firstStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeEngine.applyCustomCourseTheme(this);
        setContentView(R.layout.activity_custom_course_list);
        ButterKnife.bind(this);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.courses_added);
        emptyText.setText(getResources().getString(R.string.no_course_added));
        emptyButton.setVisibility(View.GONE);
        courses = new LinkedList<>();
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        adapter = new CustomCourseAdapter(this, courses, new CustomCourseAdapter.CustomCourseListener() {
            @Override
            public void onDeleteClick(CustomCourse lesson, int position) {
                courses.remove(lesson);
                adapter.notifyItemRemoved(position);
                PreferenceManager.setCustomCourses(CustomCourseListActivity.this, courses);
                swapViews(courses);
            }

            public void onClick(View v) {
                int itemPosition = rv.getChildLayoutPosition(v);
                if (itemPosition < courses.size()) {
                    Intent intent = new Intent(CustomCourseListActivity.this, AddCustomCourseActivity.class);
                    Type listType = new TypeToken<List<CustomCourse>>() {
                    }.getType();
                    intent.putExtra("list", new Gson().toJson(courses, listType));
                    intent.putExtra("position", itemPosition);
                    startActivity(intent);
                }
            }
        });
        rv.setAdapter(adapter);
        List<CustomCourse> saved = PreferenceManager.getCustomCourses(this);
        if (saved != null && !saved.isEmpty()) {
            courses.addAll(saved);
        }
        swapViews(courses);
        adapter.notifyDataSetChanged();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_add, menu);
        Drawable drawable = menu.findItem(R.id.add).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.add).setIcon(drawable);
        menu.findItem(R.id.add).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent i = new Intent(this, AddCustomCourseActivity.class);
                Gson gson = new Gson();
                Type listType = new TypeToken<List<CustomCourse>>() {
                }.getType();
                synchronized (this) {
                    i.putExtra("list", gson.toJson(courses, listType));
                }
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        if (firstStart) firstStart = false;
        else {
            List<CustomCourse> saved = PreferenceManager.getCustomCourses(this);
            if (saved != null && !saved.equals(courses)) {
                courses.clear();
                courses.addAll(saved);
                adapter.notifyDataSetChanged();
                swapViews(courses);
            }
        }
    }

    private void swapViews(final List<CustomCourse> courses) {
        runOnUiThread(() -> {
            if (courses == null || courses.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            }
        });
    }

}
