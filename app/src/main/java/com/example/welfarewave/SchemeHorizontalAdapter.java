package com.example.welfarewave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SchemeHorizontalAdapter extends RecyclerView.Adapter<SchemeHorizontalAdapter.VH> {

    public interface OnSchemeClickListener {
        void onSchemeClick(@NonNull Scheme scheme);
    }

    private final List<Scheme> schemes;
    private final OnSchemeClickListener listener;

    public SchemeHorizontalAdapter(@NonNull List<Scheme> schemes, @NonNull OnSchemeClickListener listener) {
        this.schemes = schemes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scheme_card_horizontal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Scheme scheme = schemes.get(position);
        holder.tvTitle.setText(safe(scheme.getTitle()));

        // For the "latest" carousel, show a short benefit summary if available.
        String metaRaw = scheme.getBenefits();
        if (metaRaw == null || metaRaw.trim().isEmpty()) {
            metaRaw = scheme.getBeneficiaryType() == null ? "" : ("For: " + scheme.getBeneficiaryType());
        }
        final String meta = metaRaw;
        holder.tvMeta.setText(meta == null ? "" : meta);

        if ("ml".equals(LocaleHelper.getLanguage(holder.itemView.getContext()))) {
            if (scheme.getTranslatedTitle() != null) {
                holder.tvTitle.setText(scheme.getTranslatedTitle());
            } else {
                TranslationManager.getInstance().translateText(scheme.getTitle(),
                        new TranslationManager.OnTranslationCompleteListener() {
                            @Override
                            public void onTranslationSuccess(String translatedText) {
                                scheme.setTranslatedTitle(translatedText);
                                if (holder.getAdapterPosition() == position) {
                                    holder.tvTitle.setText(translatedText);
                                }
                            }

                            @Override
                            public void onTranslationFailure(Exception e) {
                            }
                        });
            }

            if (scheme.getTranslatedBenefits() != null) {
                holder.tvMeta.setText(scheme.getTranslatedBenefits());
            } else {
                TranslationManager.getInstance().translateText(meta,
                        new TranslationManager.OnTranslationCompleteListener() {
                            @Override
                            public void onTranslationSuccess(String translatedText) {
                                scheme.setTranslatedBenefits(translatedText);
                                if (holder.getAdapterPosition() == position) {
                                    holder.tvMeta.setText(translatedText);
                                }
                            }

                            @Override
                            public void onTranslationFailure(Exception e) {
                            }
                        });
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onSchemeClick(scheme));
    }

    @Override
    public int getItemCount() {
        return schemes.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvMeta;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSchemeTitle);
            tvMeta = itemView.findViewById(R.id.tvSchemeMeta);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
