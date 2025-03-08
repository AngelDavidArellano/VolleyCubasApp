package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EntrenamientosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntrenamientosAdapter adapter;
    private FirebaseFirestore db;
    private String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamientos);

        teamId = getIntent().getStringExtra("teamId");

        recyclerView = findViewById(R.id.rvEntrenamientos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnCreateLineup).setOnClickListener(v -> {
            // Navegar a la actividad para crear entrenamientos
            Intent intent = new Intent(this, CreateEntrenamientoActivity.class);
            intent.putExtra("teamId", teamId);
            startActivity(intent);
        });

        findViewById(R.id.btnUseCode).setOnClickListener(v -> {
            // Navegar a la actividad para usar un código
            showUseCodeDialog();
        });

        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());

        fetchEntrenamientos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEntrenamientos();
    }

    private void fetchEntrenamientos() {
        // Paso 1: Recuperar el documento del equipo y su array de entrenamientos
        db.collection("equipos").document(teamId)
                .get()
                .addOnSuccessListener(teamDoc -> {
                    if (teamDoc.exists()) {
                        // Obtener el array de IDs de entrenamientos
                        List<Object> entrenamientoIds = (List<Object>) teamDoc.get("entrenamientos");

                        if (entrenamientoIds == null || entrenamientoIds.isEmpty()) {
                            Toast.makeText(this, "No hay entrenamientos asociados a este equipo.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Convertir todos los IDs a String
                        List<String> entrenamientoIdsAsString = new ArrayList<>();
                        for (Object id : entrenamientoIds) {
                            entrenamientoIdsAsString.add(String.valueOf(id));
                        }

                        // Log para verificar los IDs recuperados
                        Log.d("EntrenamientosActivity", "Entrenamientos asociados: " + entrenamientoIdsAsString);

                        // Paso 2: Consultar la colección "entrenamientos" con los IDs
                        db.collection("entrenamientos")
                                .whereIn("__name__", entrenamientoIdsAsString) // Filtrar por el ID del documento
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<Entrenamiento> entrenamientos = new ArrayList<>();
                                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                        // Obtener los valores del documento
                                        String id = doc.getId();
                                        String creador = doc.getString("creador");
                                        String titulo = doc.getString("titulo");
                                        String tipo = doc.getString("tipo");
                                        String descripcion = doc.getString("descripcion");
                                        String fechaCreacion = doc.getString("fechaCreacion");
                                        List<String> ejercicios = (List<String>) doc.get("ejercicios");

                                        // Crear un objeto Entrenamiento
                                        Entrenamiento entrenamiento = new Entrenamiento(
                                                id,
                                                creador,
                                                titulo,
                                                tipo,
                                                descripcion,
                                                fechaCreacion,
                                                ejercicios
                                        );

                                        entrenamientos.add(entrenamiento);
                                    }

                                    // Configurar el adaptador con los entrenamientos filtrados
                                    adapter = new EntrenamientosAdapter(entrenamientos, entrenamiento -> {
                                        // Acción al seleccionar un entrenamiento
                                        Toast.makeText(this, "Entrenamiento pulsado: " + entrenamiento.getTitulo(), Toast.LENGTH_SHORT).show();
                                    }, teamId);

                                    recyclerView.setAdapter(adapter);
                                })
                                .addOnFailureListener(e -> {
                                    // Manejar el error de la consulta a "entrenamientos"
                                    Toast.makeText(this, "Error al cargar entrenamientos.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No se encontró el equipo.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Manejar el error al consultar el documento del equipo
                    Toast.makeText(this, "Error al cargar los datos del equipo.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showUseCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usar Código de Entrenamiento");

        final EditText input = new EditText(this);
        input.setHint("Introduce el código");
        builder.setView(input);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                addTrainingByCode(code);
            } else {
                Toast.makeText(this, "El código no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addTrainingByCode(String code) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Verificar si el código existe como documento en la colección "entrenamientos"
        db.collection("entrenamientos").document(code)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // El documento existe, añadir el código al array "entrenamientos" del equipo
                        db.collection("equipos").document(teamId)
                                .update("entrenamientos", FieldValue.arrayUnion(code))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Entrenamiento añadido con éxito", Toast.LENGTH_SHORT).show();
                                    fetchEntrenamientos();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al añadir el entrenamiento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "El código no es válido. Entrenamiento no encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar el código: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
