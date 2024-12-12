package com.volleycubas.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class PlayerDetailsActivity extends AppCompatActivity {

    private EditText etPlayerName, etPlayerNumber, etPlayerPosition, etPlayerNotes;
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

        // Configurar botón de volver atrás
        btnBack.setOnClickListener(v -> finish());

        // Configurar botón de editar
        btnEditPlayer.setOnClickListener(v -> updatePlayer());

        // Configurar botón de eliminar
        btnDeletePlayer.setOnClickListener(v -> deletePlayer());
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
}
