package com.example.welfarewave;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HelpCenterListActivity extends BaseActivity {

    private RecyclerView rvHelpCenters;
    private TextView tvEmpty;
    private HelpCenterAdapter adapter;
    private List<HelpCenter> centerList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbarHelpCenters);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvHelpCenters = findViewById(R.id.rvHelpCenters);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvHelpCenters.setLayoutManager(new LinearLayoutManager(this));
        centerList = new ArrayList<>();
        adapter = new HelpCenterAdapter(this, centerList);
        rvHelpCenters.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchCenters();
    }

    private void fetchCenters() {
        db.collection("help_centers")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    centerList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HelpCenter center = document.toObject(HelpCenter.class);
                        center.setId(document.getId());
                        centerList.add(center);
                    }
                    adapter.notifyDataSetChanged();

                    if (centerList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvHelpCenters.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvHelpCenters.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load help centers.", Toast.LENGTH_SHORT).show();
                });
    }
}
