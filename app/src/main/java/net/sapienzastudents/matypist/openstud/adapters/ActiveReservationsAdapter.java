package net.sapienzastudents.matypist.openstud.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.ClientHelper;
import net.sapienzastudents.matypist.openstud.helpers.LayoutHelper;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.models.ExamReservation;

public class ActiveReservationsAdapter extends RecyclerView.Adapter<ActiveReservationsAdapter.ActiveReservationsHolder> {

    private List<ExamReservation> reservations;
    private Context context;
    private ReservationAdapterListener ral;
    private RecyclerView rv;
    private int lastExpandedItem = -1;
    public ActiveReservationsAdapter(Context context, List<ExamReservation> reservations, ReservationAdapterListener ral, RecyclerView recyclerView) {
        this.reservations = reservations;
        this.context = context;
        this.ral = ral;
        this.rv = recyclerView;
    }

    @NonNull
    @Override
    public ActiveReservationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_active_reservation, parent, false);
        ActiveReservationsHolder holder = new ActiveReservationsHolder(view);
        holder.setContext(context);
        return holder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ActiveReservationsHolder holder, int position) {
        ExamReservation res = reservations.get(position);
        holder.setDetails(res);
        holder.titleLayout.setOnTouchListener((view, motionEvent) -> {
            handleExpansion(holder,position);
            return false;
        });

        holder.fixedLayout.setOnTouchListener((view, motionEvent) -> {
            if (!holder.expandableLayout.isExpanded()) handleExpansion(holder,position);
            return false;
        });
    }

    private void handleExpansion(ActiveReservationsHolder holder, int position){
        if (lastExpandedItem != -1 && lastExpandedItem!=position) {
            ActiveReservationsHolder lastExpandedHolder = (ActiveReservationsHolder) rv.findViewHolderForAdapterPosition(lastExpandedItem);
            if (lastExpandedHolder!=holder && lastExpandedHolder!= null && lastExpandedHolder.expandableLayout.isExpanded()) {
                lastExpandedHolder.expandableLayout.collapse();
                lastExpandedHolder.iconExpand.setImageResource(R.drawable.ic_expand_more_black_24dp);
            }
        }
        if (!holder.expandableLayout.isExpanded()) {
            lastExpandedItem = position;
            holder.expandableLayout.expand();
            holder.iconExpand.setImageResource(R.drawable.ic_expand_less_black_24dp);
        }
        else {
            holder.expandableLayout.collapse();
            holder.iconExpand.setImageResource(R.drawable.ic_expand_more_black_24dp);
        }
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public interface ReservationAdapterListener {
        void deleteReservationOnClick(ExamReservation res);

        void downloadReservationOnClick(ExamReservation res);

        void addCalendarOnClick(ExamReservation res);
    }

    class ActiveReservationsHolder extends RecyclerView.ViewHolder implements ExpandableLayout.OnExpansionUpdateListener {
        @BindView(R.id.spacing1)
        TextView txtSpacing1;
        @BindView(R.id.spacing2)
        TextView txtSpacing2;
        @BindView(R.id.spacing3)
        TextView txtSpacing3;
        @BindView(R.id.deleteReservationTip)
        TextView txtDeleteReservationTip;
        @BindView(R.id.nameExam)
        TextView txtName;
        @BindView(R.id.code)
        TextView txtCode;
        @BindView(R.id.report)
        TextView txtReport;
        @BindView(R.id.nameTeacher)
        TextView txtTeacher;
        @BindView(R.id.dateExam)
        TextView txtDate;
        @BindView(R.id.reservationNumber)
        TextView txtNumber;
        @BindView(R.id.reservationMode)
        TextView txtMode;
        @BindView(R.id.ssdExam)
        TextView txtSSD;
        @BindView(R.id.cfuExam)
        TextView txtCFU;
        @BindView(R.id.reservationInfo)
        TextView txtInfo;
        @BindView(R.id.icon_expand)
        ImageView iconExpand;
        @BindView(R.id.title_layout)
        LinearLayout titleLayout;
        @BindView(R.id.get_reservation)
        Button getButton;
        @BindView(R.id.reservation_options)
        ImageView options;
        @BindView(R.id.expandable_layout)
        ExpandableLayout expandableLayout;
        @BindView(R.id.fixed_description_layout)
        LinearLayout fixedLayout;
        private Context context;
        ActiveReservationsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            expandableLayout.setOnExpansionUpdateListener(this);
        }

        private void setContext(Context context) {
            this.context = context;
        }

        @SuppressLint("ResourceType")
        void setDetails(final ExamReservation res) {
            if (expandableLayout.isExpanded()) {
                iconExpand.setImageResource(R.drawable.ic_expand_less_black_24dp);
            } else {
                iconExpand.setImageResource(R.drawable.ic_expand_more_black_24dp);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            txtName.setText(res.getExamSubject());
            txtCode.setText(context.getResources().getString(R.string.teaching_code, String.valueOf(res.getModule())));
            txtReport.setText(context.getResources().getString(R.string.report_id, String.valueOf(res.getReportID())));
            txtTeacher.setText(context.getResources().getString(R.string.teacher_reservation, res.getTeacher()));
            txtDate.setText(context.getResources().getString(R.string.date_exam, res.getExamDate().format(formatter)));
            txtSSD.setText(context.getResources().getString(R.string.ssd_exams, res.getSsd()));
            txtCFU.setText(context.getResources().getString(R.string.cfu_exams, String.valueOf(res.getCfu())));
            txtNumber.setText(context.getResources().getString(R.string.number_reservation, String.valueOf(res.getReservationNumber())));

            if (res.getAttendingMode() == null || res.getAttendingMode().trim().isEmpty())
                txtMode.setVisibility(View.GONE);
            else {
                txtMode.setText(context.getResources().getString(R.string.mode_reservation, res.getAttendingMode()));
            }

            if (res.getNote() == null || res.getNote().trim().isEmpty())
                txtInfo.setVisibility(View.GONE);
            else {
                String infos = context.getResources().getString(R.string.description_reservation, res.getNote());
                if (!infos.endsWith(".")) infos = infos + ".";
                txtInfo.setText(infos);
            }

            if (!ClientHelper.canDeleteReservation(res)) {
                txtSpacing3.setVisibility(View.GONE);
                txtDeleteReservationTip.setVisibility(View.GONE);
            } else
                txtDeleteReservationTip.setText(context.getResources().getString(R.string.delete_reservation_tip));

            Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
            PopupMenu popup = new PopupMenu(wrapper, options);
            popup.inflate(R.menu.reservation_menu);
            getButton.setCompoundDrawablesWithIntrinsicBounds(LayoutHelper.getDrawableWithColorAttr(context, R.drawable.ic_get_small, R.attr.colorButtonNav, android.R.color.darker_gray), null, null, null);
            options.setOnClickListener(v -> {
                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.delete_menu:
                            ral.deleteReservationOnClick(res);
                            break;
                        case R.id.calendar_menu:
                            ral.addCalendarOnClick(res);
                            break;
                    }
                    return false;
                });
                //displaying the popup
                popup.show();
            });
            getButton.setOnClickListener(v -> ral.downloadReservationOnClick(res));
        }

        @Override
        public void onExpansionUpdate(float expansionFraction, int state) {
            if (state == ExpandableLayout.State.EXPANDING) {
                rv.smoothScrollToPosition(getAdapterPosition());
            }
        }
    }
}
