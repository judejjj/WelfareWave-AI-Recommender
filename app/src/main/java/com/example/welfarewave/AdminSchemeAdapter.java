package com.example.welfarewave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminSchemeAdapter extends RecyclerView.Adapter<AdminSchemeAdapter.VH> {

    public interface Listener {
        void onEdit(@NonNull Scheme scheme);
        void onDelete(@NonNull Scheme scheme);
        void onOpen(@NonNull Scheme scheme);
    }

    private final List<Scheme> schemes;
    private final Listener listener;

    public AdminSchemeAdapter(@NonNull List<Scheme> schemes, @NonNull Listener listener) {
        this.schemes = schemes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_scheme_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Scheme scheme = schemes.get(position);
        holder.tvTitle.setText(safe(scheme.getTitle()));
        String meta = scheme.getBeneficiaryType() == null || scheme.getBeneficiaryType().trim().isEmpty()
                ? "For: Everyone"
                : "For: " + scheme.getBeneficiaryType();
        holder.tvMeta.setText(meta);

        holder.itemView.setOnClickListener(v -> listener.onOpen(scheme));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(scheme));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(scheme));
    }

    @Override
    public int getItemCount() {
        return schemes.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvMeta;
        ImageButton btnEdit;
        ImageButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSchemeTitle);
            tvMeta = itemView.findViewById(R.id.tvSchemeMeta);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

