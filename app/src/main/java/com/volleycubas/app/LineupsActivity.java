package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LineupsActivity extends AppCompatActivity {
    private RecyclerView rvLineups;
    private LineupAdapter adapter;
    private List<Alineacion> alineacionesList = new ArrayList<>();
    private String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineups);

        // Obtener ID del equipo
        teamId = getIntent().getStringExtra("teamId");

        // Inicializar RecyclerView
        rvLineups = findViewById(R.id.rvLineups);
        rvLineups.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LineupAdapter(alineacionesList, alineacion -> {
            Intent intent = new Intent(this, LineupDetailActivity.class);
            intent.putExtra("lineupId", alineacion.getId());
            startActivity(intent);
        });

        rvLineups.setAdapter(adapter);

        // Cargar alineaciones
        loadLineupsFromFirebase();

        ImageView btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> finish());

        Button btnCreateLineup = findViewById(R.id.btnCreateLineup);
        btnCreateLineup.setOnClickListener(v -> showCreateLineupDialog());

        Button btnUseCode = findViewById(R.id.btnUseCode);
        btnUseCode.setOnClickListener(v -> showUseCodeDialog());
    }

    private void showLoadingIndicator() {
        findViewById(R.id.loadingOverlay_players).setVisibility(View.VISIBLE);
        findViewById(R.id.loadingGif_players).setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        findViewById(R.id.loadingOverlay_players).setVisibility(View.GONE);
        findViewById(R.id.loadingGif_players).setVisibility(View.GONE);
    }

    private void showCreateLineupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear Alineación");

        final EditText input = new EditText(this);
        input.setHint("Nombre de la alineación");
        builder.setView(input);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String lineupName = input.getText().toString().trim();
            if (!lineupName.isEmpty()) {
                // Obtener el ID del usuario autenticado
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Obtener el nombre del creador desde la colección "entrenadores"
                db.collection("entrenadores").document(userId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String creador = documentSnapshot.getString("nombre");
                                if (creador != null) {
                                    createLineupInFirebase(lineupName, creador); // Pasar el nombre del creador
                                } else {
                                    Toast.makeText(this, "Nombre del creador no disponible.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Usuario no encontrado en entrenadores.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos del creador: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void createLineupInFirebase(String nombre, String creador) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Crear un ID aleatorio de 5 dígitos
        String alineacionId = String.valueOf(10000 + new Random().nextInt(90000));

        // Crear la primera rotación como un mapa con jugadores y sus posiciones
        Map<String, Map<String, Double>> primeraRotacion = new HashMap<>();
        primeraRotacion.put("player1", createPlayerPosition(7.5, 6.0));
        primeraRotacion.put("player2", createPlayerPosition(7.5, 2.5));
        primeraRotacion.put("player3", createPlayerPosition(4.5, 2.5));
        primeraRotacion.put("player4", createPlayerPosition(1.5, 2.5));
        primeraRotacion.put("player5", createPlayerPosition(1.5, 6.0));
        primeraRotacion.put("player6", createPlayerPosition(4.5, 6.0));

        // Crear el mapa de rotaciones
        Map<String, Map<String, Map<String, Double>>> rotaciones = new HashMap<>();
        rotaciones.put("0", primeraRotacion);

        // Crear los datos de alineación
        Map<String, Object> alineacionData = new HashMap<>();
        alineacionData.put("id", alineacionId);
        alineacionData.put("nombre", nombre);
        alineacionData.put("creador", creador);
        alineacionData.put("rotaciones", rotaciones); // Mapa de rotaciones

        // Guardar en Firestore
        db.collection("alineaciones").document(alineacionId)
                .set(alineacionData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Alineación creada con éxito", Toast.LENGTH_SHORT).show();
                    addLineupToTeam(alineacionId); // Añadir alineación al equipo
                    loadLineupsFromFirebase();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al crear alineación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showUseCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usar Código de Alineación");

        final EditText input = new EditText(this);
        input.setHint("Introduce el código");
        builder.setView(input);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                addLineupByCode(code);
            } else {
                Toast.makeText(this, "El código no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addLineupByCode(String code) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Verificar si el código existe como documento en la colección "alineaciones"
        db.collection("alineaciones").document(code)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // El documento existe, añadir el código al array "alineaciones" del equipo
                        db.collection("equipos").document(teamId)
                                .update("alineaciones", FieldValue.arrayUnion(code))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Alineación añadida con éxito", Toast.LENGTH_SHORT).show();
                                    loadLineupsFromFirebase(); // Recargar alineaciones
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al añadir la alineación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "El código no es válido. Alineación no encontrada.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar el código: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Double> createPlayerPosition(double x, double y) {
        Map<String, Double> position = new HashMap<>();
        position.put("x", x);
        position.put("y", y);
        return position;
    }

    private void addLineupToTeam(String lineupId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Añadir el ID de la alineación al array de alineaciones del equipo
        db.collection("equipos").document(teamId)
                .update("alineaciones", FieldValue.arrayUnion(lineupId))
                .addOnSuccessListener(aVoid -> Log.d("LineupsActivity", "Alineación añadida al equipo"))
                .addOnFailureListener(e -> Log.e("LineupsActivity", "Error al añadir alineación al equipo", e));
    }

    private void loadLineupsFromFirebase() {
        showLoadingIndicator();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtener los IDs de alineaciones desde el equipo
        db.collection("equipos").document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    hideLoadingIndicator();

                    if (documentSnapshot.exists()) {
                        List<String> alineacionesIds = (List<String>) documentSnapshot.get("alineaciones");

                        if (alineacionesIds != null && !alineacionesIds.isEmpty()) {
                            alineacionesList.clear(); // Limpiar lista antes de agregar nuevas alineaciones
                            for (String alineacionId : alineacionesIds) {
                                // Descargar cada alineación por ID
                                db.collection("alineaciones").document(alineacionId)
                                        .get()
                                        .addOnSuccessListener(alineacionDoc -> {
                                            if (alineacionDoc.exists()) {
                                                // Obtener los datos de la alineación
                                                String id = alineacionDoc.getString("id");
                                                String nombre = alineacionDoc.getString("nombre");
                                                String creador = alineacionDoc.getString("creador");

                                                // Procesar rotaciones
                                                Map<String, Object> rotacionesMap = (Map<String, Object>) alineacionDoc.get("rotaciones");
                                                List<Map<String, Object>> rotacionesList = new ArrayList<>();
                                                if (rotacionesMap != null) {
                                                    rotacionesMap.values().forEach(rotation -> {
                                                        if (rotation instanceof Map) {
                                                            rotacionesList.add((Map<String, Object>) rotation);
                                                        }
                                                    });
                                                }
                                                // Crear objeto Alineacion y agregarlo a la lista
                                                Alineacion alineacion = new Alineacion(id, nombre, creador, rotacionesList);
                                                alineacionesList.add(alineacion);

                                                // Notificar cambios al adaptador
                                                adapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("LineupsActivity", "Error al cargar alineación: " + e.getMessage()));
                            }
                        } else {
                            Log.d("LineupsActivity", "No hay alineaciones para este equipo.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    Log.e("LineupsActivity", "Error al cargar alineaciones del equipo: " + e.getMessage());
                });
    }
}
