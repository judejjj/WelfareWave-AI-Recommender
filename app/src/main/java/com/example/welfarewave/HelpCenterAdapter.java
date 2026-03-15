package com.example.welfarewave;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HelpCenterAdapter extends RecyclerView.Adapter<HelpCenterAdapter.ViewHolder> {

    private final Context context;
    private final List<HelpCenter> centerList;

    public HelpCenterAdapter(Context context, List<HelpCenter> centerList) {
        this.context = context;
        this.centerList = centerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_help_center, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelpCenter center = centerList.get(position);
        holder.tvName.setText(center.getName());
        holder.tvLocation.setText(center.getLocation());

        holder.btnCall.setOnClickListener(v -> {
            String phone = center.getPhone();
            if (phone == null || phone.trim().isEmpty()) {
                Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone.trim()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return centerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation;
        MaterialButton btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCenterName);
            tvLocation = itemView.findViewById(R.id.tvCenterLocation);
            btnCall = itemView.findViewById(R.id.btnCallCenter);
        }
    }
}
