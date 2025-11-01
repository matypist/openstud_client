package net.sapienzastudents.matypist.openstud.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.snackbar.Snackbar;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.listeners.DelayedDrawerListener;
import net.sapienzastudents.matypist.openstud.activities.CalendarActivity;
import net.sapienzastudents.matypist.openstud.activities.EventsActivity;
import net.sapienzastudents.matypist.openstud.activities.ExamsActivity;
import net.sapienzastudents.matypist.openstud.activities.NewsActivity;
import net.sapienzastudents.matypist.openstud.activities.PaymentsActivity;
import net.sapienzastudents.matypist.openstud.activities.ProfileActivity;
import net.sapienzastudents.matypist.openstud.activities.SearchClassroomActivity;
import net.sapienzastudents.matypist.openstud.activities.StatsActivity;
import net.sapienzastudents.matypist.openstud.activities.WebViewActivity;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.DimenHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import java.util.Objects;

import matypist.openstud.driver.core.models.Student;


public class LayoutHelper {

    public static void setupToolbar(AppCompatActivity aca, Toolbar toolbar, int icon) {
        aca.setSupportActionBar(toolbar);
        ActionBar actionbar = aca.getSupportActionBar();
        Objects.requireNonNull(actionbar).setDisplayHomeAsUpEnabled(true);
        Drawable drawable = ResourcesCompat.getDrawable(aca.getResources(), icon, null);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, Color.WHITE);
            actionbar.setHomeAsUpIndicator(drawable);
        }
    }


    public static Snackbar createTextSnackBar(View v, int string_id, int length) {
        Snackbar snackbar = Snackbar
                .make(v, string_id, length);
        snackbar.show();
        return snackbar;
    }

    public static Snackbar createActionSnackBar(View v, final int string_id, final int action_id, int length, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar
                .make(v, string_id, length).setAction(action_id, listener);
        snackbar.show();
        return snackbar;
    }

    public static Snackbar createTextSnackBar(View v, String text, final int action_id, int length, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar
                .make(v, text, length).setAction(action_id, listener);
        snackbar.show();
        return snackbar;
    }

    public static Snackbar createTextSnackBar(View v, String text,int length) {
        Snackbar snackbar = Snackbar
                .make(v, text, length);
        snackbar.show();
        return snackbar;
    }

    public static void createCalendarNotification(Context context, int styleId) {
        new AlertDialog.Builder(new ContextThemeWrapper(context, styleId))
                .setTitle(context.getResources().getString(R.string.experimental_feature))
                .setMessage(context.getResources().getString(R.string.calendar_feature_description))
                .setPositiveButton(context.getResources().getString(R.string.ok), (dialog, which) -> {
                })
                .show();
    }

    public static void createSearchClassroomNotification(Context context, int styleId) {
        new AlertDialog.Builder(new ContextThemeWrapper(context, styleId))
                .setTitle(context.getResources().getString(R.string.experimental_feature))
                .setMessage(context.getResources().getString(R.string.classroom_feature_description))
                .setPositiveButton(context.getResources().getString(R.string.ok), (dialog, which) -> {
                })
                .show();
    }

    public static Drawable getDrawableWithColorAttr(Context context, int drawable_id, int color_attr, int fallback_color_id) {
        int tintColor;
        TypedValue tV = new TypedValue();
        Resources.Theme theme = context.getTheme();
        boolean success = theme.resolveAttribute(color_attr, tV, true);
        if (success) tintColor = tV.data;
        else tintColor = ContextCompat.getColor(context, fallback_color_id);
        Drawable drawable = ContextCompat.getDrawable(context, drawable_id);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable.mutate(), tintColor);
        return drawable;
    }

    public static Drawable getDrawableWithColorId(Context context, int drawable_id, int color_id) {
        int tintColor;
        tintColor = ContextCompat.getColor(context, color_id);
        Drawable drawable = ContextCompat.getDrawable(context, drawable_id);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable.mutate(), tintColor);
        return drawable;
    }

    public static int getColorByAttr(Context context, int color_id, int fallback_color_id) {
        int tintColor;
        TypedValue tV = new TypedValue();
        Resources.Theme theme = context.getTheme();
        boolean success = theme.resolveAttribute(color_id, tV, true);
        if (success) tintColor = tV.data;
        else tintColor = ContextCompat.getColor(context, fallback_color_id);
        return tintColor;
    }


    public static void setColorSrcAtop(Drawable drawable, int tintColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Objects.requireNonNull(drawable).setColorFilter(new BlendModeColorFilter(tintColor, BlendMode.SRC_ATOP));
        } else {
            Objects.requireNonNull(drawable).setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static Drawer applyDrawer(Activity activity, Toolbar toolbar, Student student) {
        int codeCourse = -1;
        if(student != null) codeCourse = student.getCodeCourse();
        int finalCodeCourse = codeCourse;

        int primaryColor = ThemeEngine.getPrimaryTextColor(activity);
        DelayedDrawerListener ddl = new DelayedDrawerListener() {
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                long item = getItemPressedAndReset();
                if (item == -1) return;
                ClientHelper.startDrawerActivity(item, activity, finalCodeCourse);
            }
        };
        int activityIdentifier = LayoutHelper.getIdentifier(activity);
        PrimaryDrawerItem profile = new PrimaryDrawerItem().withIdentifier(Selection.PROFILE.getValue()).withIcon(R.drawable.ic_baseline_account_circle).withName(R.string.profile).withTextColor(primaryColor).withIconColor(primaryColor).withSelectable(activityIdentifier == Selection.PROFILE.getValue()).withIconTintingEnabled(true);
        PrimaryDrawerItem exams = new PrimaryDrawerItem().withIdentifier(Selection.EXAMS.getValue()).withIcon(R.drawable.ic_baseline_class).withName(R.string.exams).withTextColor(primaryColor).withIconColor(primaryColor).withSelectable(activityIdentifier == Selection.EXAMS.getValue()).withIconTintingEnabled(true);
        PrimaryDrawerItem stats = new PrimaryDrawerItem().withIdentifier(Selection.STATS.getValue()).withIcon(R.drawable.ic_timeline_black_24dp).withName(R.string.stats).withTextColor(primaryColor).withIconColor(primaryColor).withSelectable(activityIdentifier == Selection.STATS.getValue()).withIconTintingEnabled(true);
        PrimaryDrawerItem calendar = new PrimaryDrawerItem().withIdentifier(Selection.CALENDAR.getValue()).withIcon(R.drawable.ic_date_range_black).withName(R.string.calendar).withTextColor(primaryColor).withIconColor(primaryColor).withSelectable(activityIdentifier == Selection.CALENDAR.getValue()).withIconTintingEnabled(true);
        PrimaryDrawerItem news = new PrimaryDrawerItem().withIdentifier(Selection.NEWS.getValue()).withIcon(R.drawable.ic_newspaper).withName(R.string.news).withTextColor(primaryColor).withIconColor(primaryColor).withSelectable(activityIdentifier == Selection.NEWS.getValue()).withIconTintingEnabled(true);
        PrimaryDrawerItem events = new PrimaryDrawerItem().withIdentifier(Selection.EVENTS.getValue()).withIcon(R.drawable.ic_stage_24dp).withName(R.string.events).withTextColor(primaryColor).withIconColor(primaryColor).withSelectable(activityIdentifier == Selection.EVENTS.getValue()).withIconTintingEnabled(true);
        // PrimaryDrawerItem classrooms = new PrimaryDrawerItem().withIdentifier(Selection.CLASSROOMS.getValue()).withIcon(R.drawable.ic_location_city_black_24dp).withName(R.string.classrooms).withTextColor(primaryColor).withIconColor(primaryColor).withSelectable(activityIdentifier == Selection.CLASSROOMS.getValue()).withIconTintingEnabled(true);
        PrimaryDrawerItem tax = new PrimaryDrawerItem().withIdentifier(Selection.TAX.getValue()).withIcon(R.drawable.ic_baseline_payment).withName(R.string.payments).withTextColor(primaryColor).withIconColor(primaryColor).withSelectable(activityIdentifier == Selection.TAX.getValue()).withIconTintingEnabled(true);
        PrimaryDrawerItem sapienzastudentsnet = new PrimaryDrawerItem().withIdentifier(Selection.SAPIENZASTUDENTSNET.getValue()).withIcon(R.drawable.sapienzastudentsnet_logo).withTextColor(primaryColor).withIconColor(primaryColor).withName(R.string.sapienzastudentsnet_acronym).withSelectable(false);
        PrimaryDrawerItem settings = new PrimaryDrawerItem().withIdentifier(Selection.SETTINGS.getValue()).withIcon(R.drawable.ic_baseline_settings).withTextColor(primaryColor).withIconColor(primaryColor).withName(R.string.settings).withSelectable(false).withIconTintingEnabled(true);
        PrimaryDrawerItem about = new PrimaryDrawerItem().withIdentifier(Selection.ABOUT.getValue()).withIcon(R.drawable.ic_baseline_info).withTextColor(primaryColor).withIconColor(primaryColor).withName(R.string.about).withSelectable(false).withIconTintingEnabled(true);
        PrimaryDrawerItem exit = new PrimaryDrawerItem().withIdentifier(Selection.EXIT.getValue()).withIcon(R.drawable.ic_baseline_exit_to_app).withTextColor(primaryColor).withIconColor(primaryColor).withName(R.string.exit).withIconTintingEnabled(true);
        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .addDrawerItems(
                        profile, exams, stats, calendar, news, events, /*classrooms,*/ tax, sapienzastudentsnet,
                        new DividerDrawerItem(),
                        settings, about, exit
                ).withOnDrawerListener(ddl)
                .withHeader(R.layout.nav_header)
                .withSelectedItem(activityIdentifier)
                .withHeaderHeight(DimenHolder.fromDp(125))
                .withDrawerWidthDp(285)
                .withActionBarDrawerToggle(false)
                .build();
        result.setSelection(activityIdentifier);
        result.getDrawerItem(activityIdentifier).withSetSelected(true);
        result.setSelection(activityIdentifier);
        result.setOnDrawerItemClickListener((view, position, drawerItem) -> {
            ddl.setItemPressed(drawerItem.getIdentifier());
            result.closeDrawer();
            return true;
        });
        View headerLayout = result.getHeader();
        if (student != null) {
            TextView navTitle = headerLayout.findViewById(R.id.nav_title);
            navTitle.setText(activity.getString(R.string.fullname, student.getDisplayFirstName(), student.getDisplayLastName()));
            TextView navSubtitle = headerLayout.findViewById(R.id.nav_subtitle);
            navSubtitle.setText(student.getStudentID());
        }
        return result;
    }

    private static int getIdentifier(Activity activity) {
        if (activity instanceof ProfileActivity) return Selection.PROFILE.getValue();
        else if (activity instanceof ExamsActivity) return Selection.EXAMS.getValue();
        else if (activity instanceof StatsActivity) return Selection.STATS.getValue();
        else if (activity instanceof CalendarActivity) return Selection.CALENDAR.getValue();
        else if (activity instanceof SearchClassroomActivity)
            return Selection.CLASSROOMS.getValue();
        else if (activity instanceof NewsActivity) return Selection.NEWS.getValue();
        else if (activity instanceof EventsActivity) return Selection.EVENTS.getValue();
        else if (activity instanceof PaymentsActivity) return Selection.TAX.getValue();
        else if (activity instanceof WebViewActivity) return Selection.SAPIENZASTUDENTSNET.getValue();
        else return -1;
    }

    public enum Selection {
        PROFILE(1), EXAMS(2), STATS(3), CALENDAR(4), CLASSROOMS(5), NEWS(6), EVENTS(7), TAX(8), SAPIENZASTUDENTSNET(9), SETTINGS(10), ABOUT(11), EXIT(12);
        private final int value;

        Selection(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }
}
