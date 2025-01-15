package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EjerciciosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EjerciciosAdapter adapter;
    private FirebaseFirestore db;
    private List<Ejercicio> ejerciciosList = new ArrayList<>();
    private List<Ejercicio> filteredList = new ArrayList<>();
    private EditText searchEditText;
    private ImageView addExerciseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicios);

        recyclerView = findViewById(R.id.ejerciciosRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        addExerciseButton = findViewById(R.id.addPlayerButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        // Acción para agregar un nuevo ejercicio
        addExerciseButton.setOnClickListener(v -> {
            // Aquí puedes agregar un Intent para ir a CrearEjercicioActivity
            Toast.makeText(this, "Agregar ejercicio clicado", Toast.LENGTH_SHORT).show();
        });

        // Escuchar cambios en el buscador para filtrar ejercicios
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExercises(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchEjercicios();
    }

    // Cargar los ejercicios desde Firestore
    private void fetchEjercicios() {
        db.collection("ejercicios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ejerciciosList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Obtener los valores del documento
                        String id = doc.getId();
                        String creador = doc.getString("creador");
                        String titulo = doc.getString("titulo");
                        String tipo = doc.getString("tipo");
                        String urlImagen = doc.getString("urlImagen");
                        String descripcion = doc.getString("descripcion");

                        // Crear un objeto Ejercicio con los valores obtenidos
                        Ejercicio ejercicio = new Ejercicio(
                                id,
                                creador,
                                titulo,
                                tipo,
                                urlImagen,
                                descripcion
                        );

                        ejerciciosList.add(ejercicio);
                    }

                    // Configurar el adaptador con la lista de ejercicios
                    filteredList.addAll(ejerciciosList);
                    adapter = new EjerciciosAdapter(filteredList, ejercicio -> {
                        // Acción al seleccionar un ejercicio
                        Toast.makeText(this, "Ejercicio seleccionado: " + ejercicio.getTitulo(), Toast.LENGTH_SHORT).show();
                    });

                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // Manejar el error
                    Toast.makeText(this, "Error al cargar ejercicios", Toast.LENGTH_SHORT).show();
                });
    }

    // Filtrar los ejercicios según el texto ingresado en el buscador
    private void filterExercises(String query) {
        filteredList.clear();
        for (Ejercicio ejercicio : ejerciciosList) {
            if (ejercicio.getTitulo().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(ejercicio);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
