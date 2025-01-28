package com.volleycubas.app;

import android.content.Intent;
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
    private ImageView backButton, shareButton;

    private static final String TAG = "EntrenamientoDetails";

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
        shareButton = findViewById(R.id.share_button);
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

        // Logs para verificar datos recibidos
        Log.d(TAG, "Entrenamiento ID: " + id);
        Log.d(TAG, "Título: " + titulo);
        Log.d(TAG, "Creador: " + creador);
        Log.d(TAG, "Fecha Creación: " + fechaCreacion);
        Log.d(TAG, "Tipo: " + tipo);
        Log.d(TAG, "Descripción: " + descripcion);
        Log.d(TAG, "Ejercicios IDs: " + ejerciciosIds);

        // Configurar los datos en la vista
        tvTrainingTitle.setText(titulo);
        tvTrainingCreator.setText("Creador/a: " + creador);
        tvTrainingDate.setText("Fecha: " + fechaCreacion);
        tvTrainingType.setText("Tipo: " + tipo);
        tvTrainingDescription.setText(descripcion);

        // Cargar la lista de ejercicios
        fetchEjercicios(ejerciciosIds);

        shareButton.setOnClickListener(v -> {
            // Texto predeterminado que incluirá el teamId
            String mensaje = "\uD83C\uDF89 ¡Añade mi entrenamiento '" + titulo + "' a tu equipo en VolleyCubasApp! \uD83C\uDFD0\n\uD83D\uDC49 Código de entrenamiento: \uD83D\uDD25" + id + " \uD83D\uDCAA\uD83C\uDFFC\n";

            // Crear un Intent para compartir
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, mensaje);

            // Iniciar el selector de aplicaciones para compartir
            startActivity(Intent.createChooser(intent, "Compartir código del entrenamiento"));
        });
    }

    private void fetchEjercicios(List<String> ejerciciosIds) {
        if (ejerciciosIds == null || ejerciciosIds.isEmpty()) {
            Log.w(TAG, "No hay ejercicios asociados.");
            Toast.makeText(this, "No hay ejercicios asociados", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Consultando ejercicios con IDs: " + ejerciciosIds);

        // Consultar documentos por el ID del documento (usando "__name__")
        db.collection("ejercicios")
                .whereIn("__name__", ejerciciosIds) // Buscar por el ID del documento
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Consulta Firestore exitosa. Documentos obtenidos: " + queryDocumentSnapshots.size());
                    ejerciciosList.clear(); // Limpiar la lista antes de agregar nuevos datos
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d(TAG, "Procesando documento con ID: " + doc.getId());
                        Ejercicio ejercicio = new Ejercicio(
                                doc.getId(), // ID del documento
                                doc.getString("creador"),
                                doc.getString("titulo"),
                                doc.getString("tipo"),
                                doc.getString("url_imagen"),
                                doc.getString("descripcion"),
                                doc.getLong("timestamp")
                        );
                        Log.d(TAG, "Ejercicio añadido: " + ejercicio.getTitulo());
                        ejerciciosList.add(ejercicio);
                    }

                    if (ejerciciosList.isEmpty()) {
                        Log.w(TAG, "No se encontraron ejercicios con los IDs proporcionados.");
                        Toast.makeText(this, "No se encontraron ejercicios asociados", Toast.LENGTH_SHORT).show();
                    }

                    // Configurar el adaptador con los ejercicios cargados
                    adapter = new EjerciciosAdapter(ejerciciosList, ejercicio -> {
                        Log.d(TAG, "Ejercicio seleccionado: " + ejercicio.getTitulo());
                        Toast.makeText(this, "Ejercicio seleccionado: " + ejercicio.getTitulo(), Toast.LENGTH_SHORT).show();
                    });
                    rvExercises.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar Firestore: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar los ejercicios", Toast.LENGTH_SHORT).show();
                });
    }
}
