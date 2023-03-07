package net.sapienzastudents.matypist.openstud.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ExamsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ExamsFactory(this.getApplicationContext(), intent);
    }
}
