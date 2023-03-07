package net.sapienzastudents.matypist.openstud.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.sapienzastudents.matypist.openstud.R;

import org.threeten.bp.format.DateTimeFormatter;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.models.PaymentDescription;
import matypist.openstud.driver.core.models.Tax;

public class TaxAdapter extends RecyclerView.Adapter<TaxAdapter.TaxHolder> {

    private List<Tax> taxes;
    private Context context;
    private int mode;

    public TaxAdapter(Context context, List<Tax> taxes, int mode) {
        this.taxes = taxes;
        this.context = context;
        this.mode = mode;
    }

    @NonNull
    @Override
    public TaxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row_tax, parent, false);
        TaxHolder holder = new TaxHolder(view);
        holder.setMode(mode);
        holder.setContext(context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TaxHolder holder, int position) {
        Tax tax = taxes.get(position);
        holder.setDetails(tax);
    }

    @Override
    public int getItemCount() {
        return taxes.size();
    }

    public enum Mode {
        PAID(0), UNPAID(1);
        private final int value;

        Mode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static class TaxHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.codeTax)
        TextView txtCode;
        @BindView(R.id.codeCourse)
        TextView txtCourse;
        @BindView(R.id.mainDescription)
        TextView txtPaymentDescription;
        @BindView(R.id.academicYear)
        TextView txtAcademicYear;
        @BindView(R.id.payment_amount)
        TextView txtPaymentAmount;
        @BindView(R.id.paymentDate)
        TextView txtPaymentDate;
        private int mode;
        private Context context;

        TaxHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setMode(int mode) {
            this.mode = mode;
        }

        private void setContext(Context context) {
            this.context = context;
        }

        void setDetails(Tax tax) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            txtCourse.setText(context.getString(R.string.course_code_pay, tax.getCodeCourse()));
            txtAcademicYear.setText(context.getString(R.string.accademic_year_pay, String.valueOf(tax.getAcademicYear())));
            if (tax.getAmount() % 1 != 0)
                txtPaymentAmount.setText(context.getString(R.string.payment_amount, decimalFormat.format(tax.getAmount())));
            else
                txtPaymentAmount.setText(context.getString(R.string.payment_amount, String.valueOf((int) tax.getAmount())));
            if (mode == Mode.UNPAID.getValue()) {
                txtCode.setText(context.getString(R.string.payment_number, tax.getCode()));
                txtCode.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.big_text_tax));
                txtCode.setTypeface(null, Typeface.BOLD);
                txtPaymentDescription.setVisibility(View.GONE);
                txtPaymentDate.setVisibility(View.GONE);
                txtPaymentDescription.setVisibility(View.GONE);
            } else {
                txtCode.setText(context.getString(R.string.payment_number_ext, tax.getCode()));
                txtPaymentDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.big_text_tax));
                txtPaymentDescription.setTypeface(null, Typeface.BOLD);
                txtPaymentDate.setText(context.getString(R.string.payment_date, tax.getPaymentDate().format(formatter)));
                List<PaymentDescription> list = tax.getPaymentDescriptionList();
                if (!list.isEmpty())
                    txtPaymentDescription.setText(tax.getPaymentDescriptionList().get(0).getDescription());
            }
        }
    }
}
