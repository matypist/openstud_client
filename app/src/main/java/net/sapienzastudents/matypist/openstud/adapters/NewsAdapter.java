package net.sapienzastudents.matypist.openstud.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.helpers.RoundedTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import matypist.openstud.driver.core.models.News;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<News> news;
    private Context context;
    private View.OnClickListener ocl;

    public NewsAdapter(Context context, List<News> news, View.OnClickListener onClickListener) {
        this.news = news;
        this.context = context;
        this.ocl = onClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_news_large, parent, false);
            view.setOnClickListener(ocl);
            return new NewsHolderLarge(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_news_small, parent, false);
            view.setOnClickListener(ocl);
            return new NewsHolderSmall(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        News el = news.get(position);
        if (holder.getItemViewType() == 0) ((NewsHolderLarge) holder).setDetails(el);
        else ((NewsHolderSmall) holder).setDetails(el);
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NewsHolderSmall extends RecyclerView.ViewHolder {
        @BindView(R.id.nameNews)
        TextView txtName;
        @BindView(R.id.descriptionNews)
        TextView txtDescription;
        @BindView(R.id.image_news)
        ImageView imageView;

        NewsHolderSmall(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        void setDetails(News news) {
            txtName.setText(news.getTitle());
            txtDescription.setText(news.getDescription());
            if (news.getSmallImageUrl() != null && !news.getSmallImageUrl().trim().isEmpty())
                Picasso.get().load(news.getSmallImageUrl()).fit().centerCrop().transform(new RoundedTransformation(15, 0)).into(imageView);
            else if (news.getImageUrl() != null && !news.getImageUrl().trim().isEmpty())
                Picasso.get().load(news.getImageUrl()).fit().centerCrop().transform(new RoundedTransformation(15, 0)).into(imageView);
            else imageView.setVisibility(View.GONE);
            if (news.getDescription() == null || news.getDescription().isEmpty()) {
                txtName.setMaxLines(txtName.getMaxLines() + 1);
            }
        }
    }

    static class NewsHolderLarge extends RecyclerView.ViewHolder {
        @BindView(R.id.nameNews)
        TextView txtName;
        @BindView(R.id.descriptionNews)
        TextView txtDescription;
        @BindView(R.id.image_news)
        ImageView imageView;

        NewsHolderLarge(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        void setDetails(News news) {
            txtDescription.setVisibility(View.VISIBLE);
            txtName.setText(news.getTitle().trim());
            if (news.getDescription() == null || news.getDescription().trim().isEmpty())
                txtDescription.setVisibility(View.GONE);
            else txtDescription.setText(news.getDescription());
            if (news.getImageUrl() != null && !news.getImageUrl().trim().isEmpty())
                Picasso.get().load(news.getImageUrl()).fit().centerCrop().transform(new RoundedTransformation(15, 0)).into(imageView);
            else imageView.setVisibility(View.GONE);
        }
    }

}
