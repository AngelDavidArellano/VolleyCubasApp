package com.volleycubas.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreateEntrenamientoActivity extends AppCompatActivity {

    private EditText etTrainingTitle, etTrainingDate, etTrainingType, etTrainingDescription;
    private RecyclerView rvExercises;
    private Button btnSaveTraining;
    private ImageView backButton, btnAddExercise;

    private FirebaseFirestore db;
    private String teamId, entrenador;

    private List<String> selectedExercises = new ArrayList<>();
    private List<String> selectedExercisesId = new ArrayList<>();
    private ExercisesListAdapter exercisesListAdapter; // Adaptador del RecyclerView principal


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entrenamiento);

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener el teamId desde el Intent
        teamId = getIntent().getStringExtra("teamId");

        // Enlazar vistas
        etTrainingTitle = findViewById(R.id.etTrainingTitle);
        etTrainingDate = findViewById(R.id.etTrainingDate);
        etTrainingType = findViewById(R.id.etTrainingType);
        etTrainingDescription = findViewById(R.id.etTrainingDescription);
        rvExercises = findViewById(R.id.rvExercises);
        btnSaveTraining = findViewById(R.id.btnSaveTraining);
        backButton = findViewById(R.id.back_button);
        btnAddExercise = findViewById(R.id.btnAddExercise);

        // Configurar RecyclerView principal
        exercisesListAdapter = new ExercisesListAdapter(selectedExercises);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setAdapter(exercisesListAdapter);

        // Configurar botón de retroceso
        backButton.setOnClickListener(v -> onBackPressed());

        // Configurar botón para guardar el entrenamiento
        btnSaveTraining.setOnClickListener(v -> saveTraining());

        btnAddExercise.setOnClickListener(v -> showExerciseDialog());

        obtainTrainerName();
    }

    private void obtainTrainerName (){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtener el nombre del creador desde la colección "entrenadores"
        db.collection("entrenadores").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        entrenador = documentSnapshot.getString("nombre");
                    } else {
                        Toast.makeText(this, "Usuario no encontrado en entrenadores.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos del creador: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void saveTraining() {
        // Validar que los campos no estén vacíos
        String title = etTrainingTitle.getText().toString().trim();
        String date = etTrainingDate.getText().toString().trim();
        String type = etTrainingType.getText().toString().trim();
        String description = etTrainingDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date)
                || TextUtils.isEmpty(type) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generar un ID único de 9 dígitos para el entrenamiento
        String trainingId = generateRandomId();

        // Crear el objeto del entrenamiento
        Map<String, Object> entrenamiento = new HashMap<>();
        entrenamiento.put("ID", trainingId);
        entrenamiento.put("creador", entrenador);
        entrenamiento.put("titulo", title);
        entrenamiento.put("tipo", type);
        entrenamiento.put("descripcion", description);
        entrenamiento.put("fechaCreacion", date);
        entrenamiento.put("ejercicios", selectedExercisesId);

        // Guardar el entrenamiento en Firestore
        db.collection("entrenamientos").document(trainingId)
                .set(entrenamiento)
                .addOnSuccessListener(aVoid -> {
                    // Añadir el ID del entrenamiento al array "entrenamientos" del equipo
                    addTrainingToTeam(trainingId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar el entrenamiento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addTrainingToTeam(String trainingId) {
        db.collection("equipos").document(teamId)
                .update("entrenamientos", FieldValue.arrayUnion(trainingId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Entrenamiento guardado correctamente.", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar la actividad
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar el equipo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String generateRandomId() {
        Random random = new Random();
        return String.valueOf(100000000 + random.nextInt(900000000)); // Genera un número de 9 dígitos
    }

    private void showExerciseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_exercise, null);

        EditText etSearchExercise = dialogView.findViewById(R.id.etSearchExercise);
        RecyclerView rvExerciseGrid = dialogView.findViewById(R.id.rvExerciseGrid);
        rvExerciseGrid.setLayoutManager(new GridLayoutManager(this, 1)); // 1 columnas para el grid

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        List<Ejercicio> ejercicios = new ArrayList<>();
        ExerciseDialogAdapter adapter = new ExerciseDialogAdapter(ejercicios, ejercicio -> {
            // Añadir el ejercicio seleccionado
            selectedExercisesId.add(ejercicio.getId()); // Agrega el id al RecyclerView principal
            selectedExercises.add(ejercicio.getTitulo()); // Agrega el titulo al RecyclerView principal
            exercisesListAdapter.notifyDataSetChanged(); // Notificar al adaptador principal
            dialog.dismiss(); // Cerrar el diálogo
        });

        rvExerciseGrid.setAdapter(adapter);

        // Escuchar cambios en la barra de búsqueda
        etSearchExercise.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrar ejercicios
                filterExercises(s.toString(), ejercicios, adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Cargar ejercicios desde Firestore
        loadExercises(ejercicios, adapter);

        dialog.show();
    }

    private void loadExercises(List<Ejercicio> ejercicios, ExerciseDialogAdapter adapter) {
        db.collection("ejercicios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ejercicios.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Ejercicio ejercicio = new Ejercicio(
                                doc.getId(),
                                doc.getString("creador"),
                                doc.getString("titulo"),
                                doc.getString("tipo"),
                                doc.getString("urlImagen"),
                                doc.getString("descripcion"),
                                doc.getLong("timestamp")
                        );
                        ejercicios.add(ejercicio);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar ejercicios", Toast.LENGTH_SHORT).show());
    }

    private void filterExercises(String query, List<Ejercicio> ejercicios, ExerciseDialogAdapter adapter) {
        List<Ejercicio> filteredList = new ArrayList<>();
        for (Ejercicio ejercicio : ejercicios) {
            if (ejercicio.getTitulo().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(ejercicio);
            }
        }
        adapter.notifyDataSetChanged();
    }

}
