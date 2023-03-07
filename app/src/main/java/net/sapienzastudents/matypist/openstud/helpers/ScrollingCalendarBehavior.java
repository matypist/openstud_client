package net.sapienzastudents.matypist.openstud.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

@SuppressWarnings("unused")
public class ScrollingCalendarBehavior extends AppBarLayout.Behavior {

    public ScrollingCalendarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        return false;/*super.onInterceptTouchEvent(parent, child, ev);*/
    }
}