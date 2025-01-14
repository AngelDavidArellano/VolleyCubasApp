package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EntrenamientosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntrenamientosAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamientos);

        recyclerView = findViewById(R.id.rvEntrenamientos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnCreateLineup).setOnClickListener(v -> {
            // Navegar a la actividad para crear entrenamientos
            /*Intent intent = new Intent(this, CrearEntrenamientoActivity.class);
            startActivity(intent);*/
        });

        findViewById(R.id.btnUseCode).setOnClickListener(v -> {
            // Navegar a la actividad para usar un código
            /*Intent intent = new Intent(this, UsarCodigoActivity.class);
            startActivity(intent);*/
        });

        fetchEntrenamientos();
    }

    private void fetchEntrenamientos() {
        db.collection("entrenamientos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Entrenamiento> entrenamientos = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Entrenamiento entrenamiento = new Entrenamiento(
                                doc.getId(),
                                doc.getString("creador"),
                                doc.getString("titulo"),
                                doc.getString("tipo")
                        );
                        entrenamientos.add(entrenamiento);
                    }

                    adapter = new EntrenamientosAdapter(entrenamientos, entrenamiento -> {
                        // Navegar a DetalleEntrenamientoActivity
                        /*Intent intent = new Intent(this, DetalleEntrenamientoActivity.class);
                        intent.putExtra("entrenamiento_id", entrenamiento.getId());
                        startActivity(intent);*/
                        Toast.makeText(this, "Entrenamiento pulsado: "+ entrenamiento.getTitulo(), Toast.LENGTH_SHORT).show();
                    });

                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar entrenamientos", Toast.LENGTH_SHORT).show();
                });
    }
}
