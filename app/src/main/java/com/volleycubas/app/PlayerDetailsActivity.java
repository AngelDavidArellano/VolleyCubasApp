package com.volleycubas.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerDetailsActivity extends AppCompatActivity {

    private EditText etPlayerName, etPlayerNumber, etPlayerPosition, etPlayerNotes, etNumeroMVPs;
    private Button btnEditPlayer, btnDeletePlayer;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private String teamId;
    private Jugador jugador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);

        // Inicializar vistas
        etPlayerName = findViewById(R.id.etPlayerName);
        etPlayerNumber = findViewById(R.id.etPlayerNumber);
        etPlayerPosition = findViewById(R.id.etPlayerPosition);
        etPlayerNotes = findViewById(R.id.etPlayerNotes);
        etNumeroMVPs = findViewById(R.id.etNumeroMVPs);
        btnEditPlayer = findViewById(R.id.btnEditPlayer);
        btnDeletePlayer = findViewById(R.id.btnDeletePlayer);
        btnBack = findViewById(R.id.back_button);

        // Obtener datos del intent
        teamId = getIntent().getStringExtra("teamId");
        jugador = (Jugador) getIntent().getSerializableExtra("jugador");

        // Asegurarse de que los datos sean válidos
        if (jugador == null || teamId == null) {
            Toast.makeText(this, "Error al cargar detalles del jugador.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Llenar campos con los datos del jugador
        etPlayerName.setText(jugador.getNombre());
        etPlayerNumber.setText(String.valueOf(jugador.getNumero()));
        etPlayerPosition.setText(jugador.getPosicion());
        etPlayerNotes.setText(jugador.getNotas());
        etNumeroMVPs.setText(String.valueOf(jugador.getNumeroMVPs()));

        // Configurar botón de volver atrás
        btnBack.setOnClickListener(v -> finish());

        // Configurar botón de editar
        btnEditPlayer.setOnClickListener(v -> updatePlayer());

        // Configurar botón de eliminar
        btnDeletePlayer.setOnClickListener(v -> deletePlayer());


        cargarAsistenciasJugador();
    }

    private void updatePlayer() {
        jugador.setNombre(etPlayerName.getText().toString());
        jugador.setNumero(Integer.parseInt(etPlayerNumber.getText().toString()));
        jugador.setPosicion(etPlayerPosition.getText().toString());
        jugador.setNotas(etPlayerNotes.getText().toString());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("equipos").document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtiene la lista de jugadores del equipo
                        List<Map<String, Object>> jugadoresFirebase = (List<Map<String, Object>>) documentSnapshot.get("jugadores");

                        if (jugadoresFirebase != null) {
                            for (int i = 0; i < jugadoresFirebase.size(); i++) {
                                Map<String, Object> jugadorData = jugadoresFirebase.get(i);

                                // Busca el jugador por su ID
                                if (jugadorData.get("id").equals(jugador.getId())) {
                                    // Reemplaza los datos del jugador
                                    jugadoresFirebase.set(i, jugador.toMap());

                                    // Actualiza Firestore con la lista de jugadores modificada
                                    db.collection("equipos").document(teamId)
                                            .update("jugadores", jugadoresFirebase)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Jugador actualizado con éxito.", Toast.LENGTH_SHORT).show();
                                                finish(); // Cierra la actividad y regresa al fragmento
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Error al actualizar jugador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                    return;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar equipo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deletePlayer() {
        // Eliminar jugador de Firestore
        db.collection("equipos").document(teamId)
                .update("jugadores", FieldValue.arrayRemove(jugador.toMap()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Jugador eliminado con éxito.", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar actividad y volver al fragment
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar jugador: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cargarAsistenciasJugador() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, List<Asistencia>> asistenciasPorJugador = new HashMap<>();
        AtomicInteger totalFechas = new AtomicInteger();  // Contador de fechas totales a procesar
        int[] fechasProcesadas = {0}; // Contador de fechas ya procesadas

        Log.d("Asistencias", "🔄 Iniciando carga de asistencias para el jugador: " + jugador.getNombre());

        db.collection("registros").document(teamId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.d("Asistencias", "❌ No se encontraron registros para el equipo.");
                        return;
                    }

                    // Obtener la lista de fechas registradas
                    List<String> fechas = (List<String>) documentSnapshot.get("fechas");
                    if (fechas == null || fechas.isEmpty()) {
                        Log.d("Asistencias", "❌ No hay fechas de asistencia registradas.");
                        return;
                    }

                    Log.d("Asistencias", "📅 Fechas encontradas: " + fechas.toString());

                    totalFechas.set(fechas.size()); // Guardamos cuántas fechas hay

                    for (String fecha : fechas) {
                        String fechaFormateada = fecha.replace("/", "-");
                        Log.d("Asistencias", "🔎 Buscando asistencias en la fecha: " + fechaFormateada);

                        db.collection("registros").document(teamId)
                                .collection(fechaFormateada)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                        String tipo = (String) document.get("tipo");
                                        List<Map<String, Object>> jugadoresAsistencia =
                                                (List<Map<String, Object>>) document.get("jugadores");

                                        if (jugadoresAsistencia != null) {
                                            for (Map<String, Object> jugadorData : jugadoresAsistencia) {
                                                String jugadorId = (String) jugadorData.get("id");

                                                if (jugadorId.equals(jugador.getId())) {
                                                    Boolean asistencia = (Boolean) jugadorData.get("asistencia");
                                                    Asistencia registroAsistencia = new Asistencia(fechaFormateada, asistencia, tipo);

                                                    if (!asistenciasPorJugador.containsKey(jugadorId)) {
                                                        asistenciasPorJugador.put(jugadorId, new ArrayList<>());
                                                    }
                                                    asistenciasPorJugador.get(jugadorId).add(registroAsistencia);

                                                    Log.d("Asistencias", "✅ Asistencia añadida -> Fecha: " + fechaFormateada +
                                                            " | Asistió: " + (asistencia != null ? (asistencia ? "✅ Sí" : "❌ No") : "❌ No") +
                                                            " | Tipo: " + tipo);
                                                }
                                            }
                                        }
                                    }

                                    // Incrementamos el número de fechas procesadas
                                    fechasProcesadas[0]++;

                                    // Solo imprimimos cuando TODAS las fechas hayan sido procesadas
                                    if (fechasProcesadas[0] == totalFechas.get()) {
                                        if (asistenciasPorJugador.containsKey(jugador.getId())) {
                                            imprimirAsistenciasOrdenadas(asistenciasPorJugador.get(jugador.getId()));
                                        } else {
                                            Log.d("Asistencias", "⚠️ No se encontraron asistencias para el jugador " + jugador.getNombre());
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "⚠️ Error al obtener registros de la fecha: " + fechaFormateada, e);
                                    fechasProcesadas[0]++; // Asegurar que el contador siga avanzando
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "⚠️ Error al acceder a los registros del equipo", e));
    }


    private void imprimirAsistenciasOrdenadas(List<Asistencia> asistencias) {
        if (asistencias == null || asistencias.isEmpty()) {
            Log.d("Asistencias", "❌ No hay registros de asistencia para este jugador.");
            return;
        }

        // Ordenar por fecha (suponiendo formato "dd-MM-yyyy")
        asistencias.sort((a1, a2) -> a1.getFecha().compareTo(a2.getFecha()));

        Log.d("Asistencias", "-------------------------------------");
        Log.d("Asistencias", "📌 Asistencias de " + jugador.getNombre() + " (Ordenadas por fecha)");

        for (Asistencia asistencia : asistencias) {
            boolean asistenciaConfirmada = asistencia.getAsistencia() != null ? asistencia.getAsistencia() : false;
            Log.d("Asistencias", "📅 Fecha: " + asistencia.getFecha() +
                    " | Asistió: " + (asistenciaConfirmada ? "✅ Sí" : "❌ No") +
                    " | Tipo: " + asistencia.getTipo());
        }
        Log.d("Asistencias", "=====================================");
    }


}
