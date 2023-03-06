package com.lithium.leona.openstud.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lithium.leona.openstud.R;
import com.lithium.leona.openstud.helpers.ClientHelper;
import com.lithium.leona.openstud.helpers.LayoutHelper;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lithium.openstud.driver.core.models.ExamReservation;

public class AvaiableReservationsAdapter extends RecyclerView.Adapter<AvaiableReservationsAdapter.AvailableReservationsHolder> {
    private List<ExamReservation> reservations;
    private Activity activity;
    private ReservationAdapterListener ral;
    private List<ExamReservation> activeReservations;
    private RecyclerView rv;
    private int lastExpandedItem = -1;
    public AvaiableReservationsAdapter(Activity activity, List<ExamReservation> reservations, List<ExamReservation> activeReservations, ReservationAdapterListener ral, RecyclerView recyclerView) {
        this.reservations = reservations;
        this.activity = activity;
        this.ral = ral;
        this.activeReservations = activeReservations;
        this.rv = recyclerView;
    }

    private boolean existActiveReservations(ExamReservation res) {
        if (activeReservations == null) return true;
        for (ExamReservation active : activeReservations) {
            if (res.getExamSubject().equals(active.getExamSubject()) && res.getChannel().equals(active.getChannel())
                    && res.getCourseCode() == active.getCourseCode() && res.getReportID() == active.getReportID()
                    && res.getSessionID() == active.getSessionID()) return true;
        }
        return false;
    }

    @NonNull
    @Override
    public AvailableReservationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_row_avaiable_reservation, parent, false);
        AvailableReservationsHolder holder = new AvailableReservationsHolder(view);
        holder.setActivity(activity);
        return holder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull AvailableReservationsHolder holder, int position) {
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

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    private void handleExpansion(AvailableReservationsHolder holder, int position){
        if (lastExpandedItem != -1 && lastExpandedItem!=position) {
            AvailableReservationsHolder lastExpandedHolder = (AvailableReservationsHolder) rv.findViewHolderForAdapterPosition(lastExpandedItem);
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

    public interface ReservationAdapterListener {
        boolean placeReservation(ExamReservation res);
    }

    class AvailableReservationsHolder extends RecyclerView.ViewHolder implements ExpandableLayout.OnExpansionUpdateListener {
        @BindView(R.id.expandable_layout)
        ExpandableLayout expandableLayout;
        @BindView(R.id.fixed_description_layout)
        LinearLayout fixedLayout;
        @BindView(R.id.title_layout)
        LinearLayout titleLayout;
        @BindView(R.id.icon_expand)
        ImageView iconExpand;
        @BindView(R.id.nameExam)
        TextView txtName;
        @BindView(R.id.nameTeacher)
        TextView txtTeacher;
        @BindView(R.id.dateExam)
        TextView txtDate;
        @BindView(R.id.channelReservation)
        TextView txtChannel;
        @BindView(R.id.infosExtra)
        TextView txtInfo;
        @BindView(R.id.reservationPeriod)
        TextView txtReservationPeriod;
        @BindView(R.id.academicYear)
        TextView txtYear;
        @BindView(R.id.choose_attending_mode)
        Spinner chooseAttendingModeSpinner;
        @BindView(R.id.place_reservation)
        Button placeButton;
        private Activity activity;

        AvailableReservationsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setActivity(Activity activity) {
            this.activity = activity;
        }

        private void setPlaceButtonEnabled(boolean enabled, boolean alreadyExist) {
            int tintColor;
            Drawable placeDrawable;
            if (alreadyExist) {
                placeDrawable = LayoutHelper.getDrawableWithColorAttr(activity, R.drawable.ic_check_black_24dp, R.attr.certifiedExamColor, android.R.color.darker_gray);
                placeButton.setText(activity.getResources().getString(R.string.already_placed_button));
                tintColor = LayoutHelper.getColorByAttr(activity, R.attr.certifiedExamColor, android.R.color.darker_gray);
            } else if (!enabled) {
                placeDrawable = LayoutHelper.getDrawableWithColorId(activity, R.drawable.ic_library_add_small, android.R.color.darker_gray);
                placeButton.setText(activity.getResources().getString(R.string.place_reservation));
                tintColor = ContextCompat.getColor(activity, android.R.color.darker_gray);
            } else {
                placeDrawable = LayoutHelper.getDrawableWithColorAttr(activity, R.drawable.ic_library_add_small, R.attr.colorButtonNav, android.R.color.darker_gray);
                placeButton.setText(activity.getResources().getString(R.string.place_reservation));
                tintColor = LayoutHelper.getColorByAttr(activity, R.attr.colorButtonNav, android.R.color.darker_gray);
            }
            placeButton.setEnabled(enabled);
            placeButton.setTextColor(tintColor);
            if (placeDrawable != null)
                placeButton.setCompoundDrawablesWithIntrinsicBounds(placeDrawable, null, null, null);
        }

        @SuppressLint("ResourceType")
        void setDetails(final ExamReservation res) {
            if (expandableLayout.isExpanded()) {
                iconExpand.setImageResource(R.drawable.ic_expand_less_black_24dp);
            } else {
                iconExpand.setImageResource(R.drawable.ic_expand_more_black_24dp);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            String infos = activity.getResources().getString(R.string.description_reservation, res.getNote());
            if (!infos.endsWith(".")) infos = infos + ".";
            txtName.setText(res.getExamSubject());
            txtTeacher.setText(activity.getResources().getString(R.string.teacher_reservation, res.getTeacher()));
            txtDate.setText(activity.getResources().getString(R.string.date_exam, res.getExamDate().format(formatter)));
            txtReservationPeriod.setText(activity.getResources().getString(R.string.reservation_period, res.getStartDate().format(formatter), res.getEndDate().format(formatter)));
            txtDate.setText(activity.getResources().getString(R.string.date_exam, res.getExamDate().format(formatter)));
            txtChannel.setText(activity.getResources().getString(R.string.channel_reservation, res.getChannel()));
            if (res.getYearCourse() != null) {
                txtYear.setVisibility(View.VISIBLE);
                txtYear.setText(activity.getResources().getString(R.string.accademic_year_pay, res.getYearCourse()));
            } else txtYear.setVisibility(View.GONE);
            if (res.getNote() == null || res.getNote().trim().isEmpty())
                txtInfo.setVisibility(View.GONE);
            else {
                txtInfo.setText(infos);
            }

            HashMap<String, String> attendingModesHashMap = new HashMap<>();
            if (res.getAttendingModesList() == null || res.getAttendingModesList().length() == 0)
                chooseAttendingModeSpinner.setVisibility(View.GONE);
            else {
                chooseAttendingModeSpinner.setVisibility(View.VISIBLE);

                JSONArray attendingModesJsonArray = res.getAttendingModesList();

                ArrayList<String> attendingModesArrayList = new ArrayList<>();

                try {
                    for (int i = 0; i < attendingModesJsonArray.length(); i++) {
                        JSONObject scelta = attendingModesJsonArray.getJSONObject(i);

                        if(scelta != null) {
                            String attendingModeTypeDescription;
                            try {
                                attendingModeTypeDescription = scelta.getString("descrizioneTipoEsame");

                                attendingModesArrayList.add(attendingModeTypeDescription);
                            } catch(JSONException ex) {
                                continue;
                            }

                            try {
                                attendingModesHashMap.put(attendingModeTypeDescription, scelta.getString("tipoEsame"));
                            } catch(JSONException ex) {
                                attendingModesArrayList.remove(attendingModeTypeDescription);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this.activity, android.R.layout.simple_spinner_item, attendingModesArrayList);
                adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                chooseAttendingModeSpinner.setAdapter(adapterSpinner);
            }

            final HashMap<String, String> finalAttendingModesHashMap = new HashMap<>(attendingModesHashMap);
            chooseAttendingModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String attendingModeTypeDescription = (String) parent.getItemAtPosition(position);

                    res.setAttendingModeType(finalAttendingModesHashMap.get(attendingModeTypeDescription));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            if (existActiveReservations(res)) {
                setPlaceButtonEnabled(false, true);
            } else setPlaceButtonEnabled(ClientHelper.canPlaceReservation(res), false);

            placeButton.setOnClickListener(v -> new Thread(() -> {
                if (ral.placeReservation(res))
                    activity.runOnUiThread(() -> setPlaceButtonEnabled(false, true));
                else activity.runOnUiThread(() -> setPlaceButtonEnabled(true, false));
            }).start());
        }

        @Override
        public void onExpansionUpdate(float expansionFraction, int state) {
            if (state == ExpandableLayout.State.EXPANDING) {
                rv.smoothScrollToPosition(getAdapterPosition());
            }
        }
    }
}
