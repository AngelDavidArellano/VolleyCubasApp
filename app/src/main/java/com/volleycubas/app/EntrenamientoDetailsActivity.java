package com.volleycubas.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EntrenamientoDetailsActivity extends AppCompatActivity {

    private TextView tvTrainingTitle, tvTrainingCreator, tvTrainingDate, tvTrainingType, tvTrainingDescription;
    private RecyclerView rvExercises;
    private FirebaseFirestore db;
    private List<Ejercicio> ejerciciosList = new ArrayList<>();
    private EjerciciosAdapter adapter;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento_details);

        // Enlazar vistas
        tvTrainingTitle = findViewById(R.id.tvTrainingTitle);
        tvTrainingCreator = findViewById(R.id.tvTrainingCreator);
        tvTrainingDate = findViewById(R.id.tvTrainingDate);
        tvTrainingType = findViewById(R.id.tvTrainingType);
        tvTrainingDescription = findViewById(R.id.tvTrainingDescription);
        rvExercises = findViewById(R.id.rvExercises);
        backButton = findViewById(R.id.back_button);

        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        // Configurar el botón de retroceso
        backButton.setOnClickListener(v -> onBackPressed());

        // Obtener los datos del Intent
        String id = getIntent().getStringExtra("id");
        String titulo = getIntent().getStringExtra("titulo");
        String creador = getIntent().getStringExtra("creador");
        String fechaCreacion = getIntent().getStringExtra("fechaCreacion");
        String tipo = getIntent().getStringExtra("tipo");
        String descripcion = getIntent().getStringExtra("descripcion");
        List<String> ejerciciosIds = getIntent().getStringArrayListExtra("ejercicios");

        // Configurar los datos en la vista
        tvTrainingTitle.setText(titulo);
        tvTrainingCreator.setText("Creador/a: " + creador);
        tvTrainingDate.setText("Fecha: " + fechaCreacion);
        tvTrainingType.setText("Tipo: " + tipo);
        tvTrainingDescription.setText(descripcion);

        Log.d("Ejercicios encontrados: ", ejerciciosIds.toString());

        // Cargar la lista de ejercicios
        fetchEjercicios(ejerciciosIds);
    }

    private void fetchEjercicios(List<String> ejerciciosIds) {
        if (ejerciciosIds == null || ejerciciosIds.isEmpty()) {
            Toast.makeText(this, "No hay ejercicios asociados", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String idEjercicio : ejerciciosIds) {
            db.collection("ejercicio").document(idEjercicio)
                    .get()
                    .addOnSuccessListener(teamSnapshot -> {
                        if (teamSnapshot.exists()) {
                            Ejercicio ejercicio = new Ejercicio(
                                    teamSnapshot.getId(),
                                    teamSnapshot.getString("creador"),
                                    teamSnapshot.getString("titulo"),
                                    teamSnapshot.getString("tipo"),
                                    teamSnapshot.getString("urlImagen"),
                                    teamSnapshot.getString("descripcion")
                            );
                            ejerciciosList.add(ejercicio);
                        }

                        // Configurar el adaptador
                        adapter = new EjerciciosAdapter(ejerciciosList, ejercicio -> {
                            // Acción al seleccionar un ejercicio
                            Toast.makeText(this, "Ejercicio seleccionado: " + ejercicio.getTitulo(), Toast.LENGTH_SHORT).show();
                        });
                        rvExercises.setAdapter(adapter);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar los ejercicios", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
