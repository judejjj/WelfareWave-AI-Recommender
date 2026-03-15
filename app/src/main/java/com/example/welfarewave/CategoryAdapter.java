package com.example.welfarewave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public static class CategoryItem {
        public final String name;
        public final String canonicalName;
        public final @DrawableRes int iconRes;

        public CategoryItem(@NonNull String name, @NonNull String canonicalName, @DrawableRes int iconRes) {
            this.name = name;
            this.canonicalName = canonicalName;
            this.iconRes = iconRes;
        }
    }

    public interface Listener {
        void onCategoryClick(@NonNull CategoryItem item);
    }

    private final List<CategoryItem> items;
    private final Listener listener;

    public CategoryAdapter(@NonNull List<CategoryItem> items, @NonNull Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CategoryItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.ivIcon.setImageResource(item.iconRes);
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvName;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}

