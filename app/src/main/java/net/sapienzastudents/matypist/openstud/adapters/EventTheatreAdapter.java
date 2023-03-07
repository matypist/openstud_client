package net.sapienzastudents.matypist.openstud.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import net.sapienzastudents.matypist.openstud.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.models.Event;

public class EventTheatreAdapter extends RecyclerView.Adapter<EventTheatreAdapter.EventHolder> {

    private List<Event> events;
    private Context context;
    private EventAdapterListener eal;

    public EventTheatreAdapter(Context context, List<Event> events, EventAdapterListener eal) {
        this.events = events;
        this.context = context;
        this.eal = eal;
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_event_theatre, parent, false);
        EventHolder holder = new EventHolder(view);
        holder.setContext(context);
        view.setOnClickListener(v -> eal.onItemClick(v));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        Event res = events.get(position);
        holder.setDetails(res);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void setupMenu(Event ev, ImageView options) {
        Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        PopupMenu popup = new PopupMenu(wrapper, options);
        popup.inflate(R.menu.event_generic_menu);
        options.setOnClickListener(v -> {
            popup.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.calendar_menu:
                        eal.addCalendarOnClick(ev);
                        break;
                }
                return false;
            });
            popup.show();
        });
    }

    public interface EventAdapterListener {
        void addCalendarOnClick(Event ev);

        void onItemClick(View v);
    }

    class EventHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView txtName;
        @BindView(R.id.description)
        TextView txtDescription;
        @BindView(R.id.when)
        TextView txtWhen;
        @BindView(R.id.where)
        TextView txtWhere;

        @BindView(R.id.event_options)
        ImageView options;
        private Context context;

        EventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setContext(Context context) {
            this.context = context;
        }

        @SuppressLint({"ResourceType", "DefaultLocale"})
        void setDetails(final Event ev) {
            setupMenu(ev, options);
            txtWhere.setVisibility(View.VISIBLE);
            txtName.setText(ev.getTitle());
            txtDescription.setText(ev.getDescription());
            if ((ev.getRoom() == null || ev.getRoom().trim().isEmpty()) && (ev.getWhere() == null || ev.getWhere().trim().isEmpty()))
                txtWhere.setVisibility(View.GONE);
            else if (ev.getRoom() == null || ev.getRoom().trim().isEmpty())
                txtWhere.setText(context.getResources().getString(R.string.event_theatre_where, ev.getWhere()));
            else if (ev.getWhere() == null || ev.getWhere().trim().isEmpty())
                txtWhere.setText(context.getResources().getString(R.string.event_theatre_where, ev.getRoom()));
            else
                txtWhere.setText(context.getResources().getString(R.string.event_theatre_where, String.format("%s, %s", ev.getRoom(), ev.getWhere())));
            txtWhen.setText(context.getResources().getString(R.string.event_theatre_when, String.format("%02d:%02d", ev.getStart().getHour(), ev.getStart().getMinute())));
            txtWhere.setVisibility(View.GONE); // hide for now
            txtWhen.setVisibility(View.GONE); // hide for now
        }
    }
}
