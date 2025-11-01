package net.sapienzastudents.matypist.openstud.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.fragments.BottomSheetPersonalIdentifier;
import net.sapienzastudents.matypist.openstud.fragments.BottomSheetStudentCard;
import net.sapienzastudents.matypist.openstud.helpers.ThemeEngine;
import net.sapienzastudents.matypist.openstud.data.InfoManager;
import net.sapienzastudents.matypist.openstud.fragments.BottomSheetCertificateFragment;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;
import com.mikepenz.materialdrawer.Drawer;

import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import matypist.openstud.driver.core.Openstud;
import matypist.openstud.driver.core.models.Isee;
import matypist.openstud.driver.core.models.Student;
import matypist.openstud.driver.exceptions.OpenstudConnectionException;
import matypist.openstud.driver.exceptions.OpenstudInvalidCredentialsException;
import matypist.openstud.driver.exceptions.OpenstudInvalidResponseException;

public class ProfileActivity extends BaseDataActivity {

    @BindView(R.id.main_layout)
    CoordinatorLayout mainLayout;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.birthDate)
    TextView studentId;
    @BindView(R.id.studentId)
    TextView birthDate;
    @BindView(R.id.birthPlace)
    TextView birthPlace;
    @BindView(R.id.isee)
    TextView isee_field;
    @BindView(R.id.email)
    TextView email_field;
    @BindView(R.id.personal_email_field)
    TextView personal_email_field;
    @BindView(R.id.departmentDescription)
    TextView departmentDescription;
    @BindView(R.id.courseDescription)
    TextView courseDescription;
    @BindView(R.id.courseCode)
    TextView courseCode;
    @BindView(R.id.courseYear)
    TextView courseYear;
    @BindView(R.id.studentStatus)
    TextView studentStatus;
    @BindView(R.id.cfu)
    TextView cfu;
    @BindView(R.id.socialSecurityNumber)
    TextView socialSecurityNumber;
    @BindView(R.id.socialSecurityNumberLayout)
    RelativeLayout socialSecurityNumberLayout;
    @BindView(R.id.certificateLayout)
    RelativeLayout certificatesButton;

    @OnClick(R.id.emailLayout)
    void onEmailClick() {
        ClientHelper.createWebViewActivity(this, "E-mail", student.getEmail(), os.getConfig().getEmailURL(), ClientHelper.WebViewType.EMAIL);
    }

    private Drawer drawer;
    private Isee isee;
    private ProfileEventHandler h = new ProfileEventHandler(this);
    private LocalDateTime lastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initData()) return;
        ThemeEngine.applyProfileTheme(this);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        isee = InfoManager.getIseeCached(getApplication(), os);
        LayoutHelper.setupToolbar(this, toolbar, R.drawable.ic_baseline_arrow_back);
        drawer = LayoutHelper.applyDrawer(this, toolbar, student);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        applyInfos(student, isee);
        swipeRefreshLayout.setNestedScrollingEnabled(true);
        int refreshColorId = ThemeEngine.getSpinnerColorId(this);
        swipeRefreshLayout.setColorSchemeResources(refreshColorId, refreshColorId, refreshColorId);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeEngine.resolveColorFromAttribute(this, R.attr.SwipeSpinnerBackgroundColor, R.color.white));
        certificatesButton.setOnClickListener(v -> {
            BottomSheetCertificateFragment filterFrag = BottomSheetCertificateFragment.newInstance();
            filterFrag.show(getSupportFragmentManager(), filterFrag.getTag());
        });
        socialSecurityNumberLayout.setOnClickListener(view -> {
            String socialNumber = student.getSocialSecurityNumber();
            BottomSheetPersonalIdentifier identifierFrag = BottomSheetPersonalIdentifier.newInstance(socialNumber);
            identifierFrag.show(getSupportFragmentManager(), identifierFrag.getTag());
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Thread t1 = new Thread(() -> refresh(os));
            t1.start();
        });
        if (savedInstanceState == null) new Thread(() -> refresh(os)).start();
    }

    protected void onRestart() {
        super.onRestart();
        LocalDateTime time = getTimer();
        if (time == null || Duration.between(time, LocalDateTime.now()).toMinutes() > 60) {
            new Thread(() -> refresh(os)).start();
        }
    }

    private void refresh(Openstud os) {
        setRefreshing(true);
        try {
            student = InfoManager.getInfoStudent(getApplication(), os);
            isee = InfoManager.getIsee(getApplication(), os);
            runOnUiThread(() -> applyInfos(student, isee));
        } catch (OpenstudConnectionException e) {
            h.sendEmptyMessage(ClientHelper.Status.CONNECTION_ERROR.getValue());
            e.printStackTrace();
        } catch (OpenstudInvalidResponseException e) {
            if (e.isMaintenance()) h.sendEmptyMessage(ClientHelper.Status.MAINTENANCE.getValue());
            h.sendEmptyMessage(ClientHelper.Status.INVALID_RESPONSE.getValue());
            e.printStackTrace();
        } catch (OpenstudInvalidCredentialsException e) {
            h.sendEmptyMessage(ClientHelper.getStatusFromLoginException(e).getValue());
            e.printStackTrace();
        }
        updateTimer();
        setRefreshing(false);
    }

    private void applyInfos(Student st, Isee isee) {
        if (st == null) return;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        collapsingToolbarLayout.setTitle(st.getDisplayFirstName() + " " + st.getDisplayLastName());
        studentId.setText(st.getStudentID());
        birthDate.setText((st.getBirthDate().format(formatter)));
        birthPlace.setText(st.getBirthPlace());
        socialSecurityNumber.setText(st.getSocialSecurityNumber());
        if (isee == null) isee_field.setText(getResources().getString(R.string.isee_not_available));
        else isee_field.setText(String.valueOf(isee.getValue()));
        String email = st.getEmail();
        if (email == null) email_field.setText(getResources().getString(R.string.email_not_activated));
        else email_field.setText(email);
        String personalEmail = st.getPersonalEmail();
        if (personalEmail == null) personal_email_field.setText(getResources().getString(R.string.personal_email_not_entered));
        else personal_email_field.setText(personalEmail);
        if (st.getCourseName() != null && !st.getCourseName().equals("")) {
            departmentDescription.setText(st.getDepartmentName());
            courseDescription.setText(st.getCourseName());
            courseCode.setText(String.valueOf(st.getCodeCourse()));
            if (Locale.getDefault().getLanguage().equals("it"))
                courseYear.setText(getResources().getString(R.string.year_corse_profile, st.getCourseYear() + "°"));
            else {
                String year = st.getCourseYear();
                if (StringUtils.isNumeric(st.getCourseYear())) {
                    int number = Integer.parseInt(year);
                    if (number == 1) year = year + "st";
                    else if (number == 2) year = year + "nd";
                    else if (number == 3) year = year + "rd";
                    else year = year + "th";
                }
                courseYear.setText(getResources().getString(R.string.year_corse_profile, year));
            }
            studentStatus.setText(st.getStudentStatus());
            cfu.setText(String.valueOf(st.getCfu()));
        } else {
            courseDescription.setText(getResources().getString(R.string.not_enrolled));
            courseCode.setText(getResources().getString(R.string.not_enrolled));
            LinearLayout linearLayout = findViewById(R.id.course_extra_info);
            linearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_profile, menu);
        Drawable drawable = menu.findItem(R.id.barcode).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.barcode).setIcon(drawable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.barcode) {
            BottomSheetStudentCard cardFrag = BottomSheetStudentCard.newInstance();
            cardFrag.show(getSupportFragmentManager(), cardFrag.getTag());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private synchronized void updateTimer() {
        lastUpdate = LocalDateTime.now();
    }

    private synchronized LocalDateTime getTimer() {
        return lastUpdate;
    }

    private void setRefreshing(boolean refreshing) {
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(refreshing));
    }

    private static class ProfileEventHandler extends Handler {
        private final WeakReference<ProfileActivity> mActivity;

        ProfileEventHandler(ProfileActivity activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ProfileActivity activity = mActivity.get();
            if (activity != null) {
                View.OnClickListener listener = v -> new Thread(() -> activity.refresh(activity.os)).start();
                if (msg.what == ClientHelper.Status.CONNECTION_ERROR.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.connection_error, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.INVALID_RESPONSE.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.invalid_response_error, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.MAINTENANCE.getValue()) {
                    LayoutHelper.createActionSnackBar(activity.mainLayout, R.string.infostud_maintenance, R.string.retry, Snackbar.LENGTH_LONG, listener);
                } else if (msg.what == ClientHelper.Status.USER_NOT_ENABLED.getValue()) {
                    LayoutHelper.createTextSnackBar(activity.mainLayout, R.string.user_not_enabled_error, Snackbar.LENGTH_LONG);
                } else if (msg.what == ClientHelper.Status.INVALID_CREDENTIALS.getValue() || msg.what == ClientHelper.Status.EXPIRED_CREDENTIALS.getValue() || msg.what == ClientHelper.Status.ACCOUNT_BLOCKED.getValue()) {
                    ClientHelper.rebirthApp(activity, msg.what);
                }
            }
        }
    }
}
