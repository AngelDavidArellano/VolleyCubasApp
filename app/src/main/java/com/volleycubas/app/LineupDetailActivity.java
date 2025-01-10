package com.volleycubas.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineupDetailActivity extends AppCompatActivity {

    private TextView tvLineupName, tvLineupCreator;
    private FirebaseFirestore db;
    private String lineupId;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RotationPagerAdapter rotationPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineup_detail);

        // Inicializar vistas
        tvLineupName = findViewById(R.id.tvLineupName);
        tvLineupCreator = findViewById(R.id.tvLineupCreator);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        ImageView backButton = findViewById(R.id.backButton);
        ImageView copyCodeButton = findViewById(R.id.copyCodeButton);

        findViewById(R.id.saveButton).setOnClickListener(v -> savePositionsToFirebase());

        // Obtener ID de la alineación desde el intent
        lineupId = getIntent().getStringExtra("lineupId");

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Cargar detalles de la alineación
        loadLineupDetails();

        copyCodeButton.setOnClickListener(v -> {
            if (lineupId != null && !lineupId.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Lineup ID", lineupId);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Código copiado al portapapeles: " + lineupId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Código no disponible.", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void savePositionsToFirebase() {
        List<Map<String, Map<String, Double>>> rotations = rotationPagerAdapter.getRotations();

        Map<String, Object> updatedRotations = new HashMap<>();
        for (int i = 0; i < rotations.size(); i++) {
            updatedRotations.put(String.valueOf(i), rotations.get(i));
            Log.d("New positions", String.valueOf(i) + ":" + rotations.get(i));
        }

        // Guardar las posiciones actualizadas en Firestore
        db.collection("alineaciones").document(lineupId)
                .update("rotaciones", updatedRotations)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Posiciones guardadas con éxito.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar posiciones: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadLineupDetails() {
        db.collection("alineaciones").document(lineupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String creador = documentSnapshot.getString("creador");

                        tvLineupName.setText(nombre != null ? nombre : "Sin nombre");
                        tvLineupCreator.setText("Creador/a: " + (creador != null ? creador : "Desconocido"));

                        Map<String, Map<String, Map<String, Double>>> rotaciones =
                                (Map<String, Map<String, Map<String, Double>>>) documentSnapshot.get("rotaciones");

                        if (rotaciones == null) {
                            rotaciones = new HashMap<>();
                        }

                        List<Map<String, Map<String, Double>>> rotationList = new ArrayList<>(rotaciones.values());

                        rotationPagerAdapter = new RotationPagerAdapter(this, rotationList);
                        viewPager.setAdapter(rotationPagerAdapter);

                        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                            tab.setText("Rotación " + (position + 1));
                        }).attach();
                    } else {
                        Toast.makeText(this, "Alineación no encontrada.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar alineación: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
