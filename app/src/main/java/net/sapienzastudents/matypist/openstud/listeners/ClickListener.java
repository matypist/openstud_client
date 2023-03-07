package net.sapienzastudents.matypist.openstud.listeners;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class ClickListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        // triggers after onDown only for long press
        super.onLongPress(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        // triggers first for both single tap and long press
        return false;
    }

}
