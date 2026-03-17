package com.example.welfarewave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.SchemeViewHolder> {

    public interface OnSchemeClickListener {
        void onSchemeClick(@NonNull Scheme scheme);
    }

    private final List<Scheme> schemes;
    private final OnSchemeClickListener listener;

    public SchemeAdapter(@NonNull List<Scheme> schemes, @NonNull OnSchemeClickListener listener) {
        this.schemes = schemes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SchemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scheme_card, parent, false);
        return new SchemeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SchemeViewHolder holder, int position) {
        Scheme scheme = schemes.get(position);
        holder.tvTitle.setText(safe(scheme.getTitle()));

        String meta = scheme.getBeneficiaryType() == null || scheme.getBeneficiaryType().trim().isEmpty()
                ? "For: General"
                : "For: " + scheme.getBeneficiaryType();
        holder.tvMeta.setText(meta);

        if ("ml".equals(LocaleHelper.getLanguage(holder.itemView.getContext()))) {
            // Apply cached translation if exists
            if (scheme.getTranslatedTitle() != null) {
                holder.tvTitle.setText(scheme.getTranslatedTitle());
            } else {
                TranslationManager.getInstance().translateText(scheme.getTitle(),
                        new TranslationManager.OnTranslationCompleteListener() {
                            @Override
                            public void onTranslationSuccess(String translatedText) {
                                scheme.setTranslatedTitle(translatedText);
                                // Make sure we are still binding the correct item in the recycled view
                                if (holder.getAdapterPosition() == position) {
                                    holder.tvTitle.setText(translatedText);
                                }
                            }

                            @Override
                            public void onTranslationFailure(Exception e) {
                            }
                        });
            }

            // Translate meta
            TranslationManager.getInstance().translateText(meta,
                    new TranslationManager.OnTranslationCompleteListener() {
                        @Override
                        public void onTranslationSuccess(String translatedText) {
                            if (holder.getAdapterPosition() == position) {
                                holder.tvMeta.setText(translatedText);
                            }
                        }

                        @Override
                        public void onTranslationFailure(Exception e) {
                        }
                    });
        }

        holder.itemView.setOnClickListener(v -> listener.onSchemeClick(scheme));
    }

    @Override
    public int getItemCount() {
        return schemes.size();
    }

    static class SchemeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvMeta;

        SchemeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSchemeTitle);
            tvMeta = itemView.findViewById(R.id.tvSchemeMeta);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
