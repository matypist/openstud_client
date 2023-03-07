package net.sapienzastudents.matypist.openstud.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.sapienzastudents.matypist.openstud.R;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import matypist.openstud.driver.core.models.Exam;

public class DropdownExamAdapter extends ArrayAdapter<Exam> {
    private List<Exam> exams;
    private List<Exam> all;
    private Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            LinkedList<Exam> tempList = new LinkedList<>();
            if (constraint != null && all != null) {
                int length = all.size();
                int i = 0;
                while (i < length) {
                    Exam item = all.get(i);
                    String title = item.getDescription().toLowerCase().trim();
                    String filterText = constraint.toString().toLowerCase().trim();
                    if (title.startsWith(filterText)) tempList.addFirst(item);
                    else if (title.contains(filterText)) tempList.addLast(item);
                    i++;
                }
                filterResults.values = tempList;
                filterResults.count = tempList.size();
            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {
            exams.clear();
            if (results.values != null) exams.addAll((Collection<? extends Exam>) results.values);
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };


    public DropdownExamAdapter(Context context, List<Exam> examsDoable) {
        super(context, 0, examsDoable);
        exams = examsDoable;
        all = new LinkedList<>(exams);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.item_dropdown, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.itemView = convertView.findViewById(R.id.textView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Exam item = getItem(position);
        if (item != null) {
            viewHolder.itemView.setText(item.getDescription());
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return myFilter;
    }

    private static class ViewHolder {
        private TextView itemView;
    }

}
