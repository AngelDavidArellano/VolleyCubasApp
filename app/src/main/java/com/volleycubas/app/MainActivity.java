package com.volleycubas.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private TextView welcomeText, announcementTitle, announcementContent;
    private View loadingOverlay;
    private ImageView loadingIndicator;
    private RecyclerView teamsRecyclerView;
    private TeamAdapter teamAdapter;
    private List<Team> teamList = new ArrayList<>();

    public static boolean isDataLoaded = false;
    private boolean isLoadingInitialized = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Inicializar vistas
        welcomeText = findViewById(R.id.welcomeText);
        announcementTitle = findViewById(R.id.announcementTitle);
        announcementContent = findViewById(R.id.announcementContent);
        teamsRecyclerView = findViewById(R.id.teamsRecyclerView);

        loadingOverlay = findViewById(R.id.loadingOverlay_main);
        loadingIndicator = findViewById(R.id.loadingGif_main);
        setupManualRotation();

        // Configurar RecyclerView y Adapter
        teamAdapter = new TeamAdapter(teamList, this, teamId -> {
            Intent intent = new Intent(MainActivity.this, TeamMenuActivity.class);
            intent.putExtra("teamId", teamId);
            startActivity(intent);
        });
        teamsRecyclerView.setAdapter(teamAdapter);
        teamsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener el usuario actual y cargar datos
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            cargarMensajeBienvenida(currentUser.getUid());
            if (!isDataLoaded && !isLoadingInitialized) { // Evitar múltiples cargas
                cargarEquiposDesdeFirestore(currentUser.getUid());
                isDataLoaded = true;
                isLoadingInitialized = true;
            }
        }

        cargarAnuncioPrincipal();
        isDataLoaded = true;

        // Botón para crear o unirse a un equipo
        findViewById(R.id.fabAddTeam).setOnClickListener(v -> mostrarOpcionesEquipo());

        CardView cardGenerateTeams = findViewById(R.id.card_crear_equipos);
        cardGenerateTeams.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GenerateTeamsActivity.class);
            intent.putParcelableArrayListExtra("jugadores", new ArrayList<>());
            intent.putExtra("teamId", 000000);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDataLoaded = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !isLoadingInitialized || !isDataLoaded) {
            cargarEquiposDesdeFirestore(currentUser.getUid());
            isLoadingInitialized = true;
        }
    }


    private void showLoadingIndicator() {
        Log.d("LOADING ANIMATION","Animación empezada");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        if (loadingIndicator != null) {
            loadingIndicator.clearAnimation(); // Detener animación explícitamente
        }
        Log.d("LOADING ANIMATION","Animación finalizada");
        loadingOverlay.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void setupManualRotation() {
        // Configurar la animación
        RotateAnimation rotate = new RotateAnimation(
                0f, 360f, // Rotación de 0 a 360 grados
                Animation.RELATIVE_TO_SELF, 0.5f, // Punto de pivote en el centro
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(2000); // Duración de la rotación en milisegundos
        rotate.setRepeatCount(Animation.INFINITE); // Repetir indefinidamente

        // Iniciar la animación
        loadingIndicator.startAnimation(rotate);
    }

    private void cargarMensajeBienvenida(String entrenadorId) {
        firestore.collection("entrenadores").document(entrenadorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        welcomeText.setText("Bienvenido " + (nombre != null ? nombre : "") + "!");
                    } else {
                        welcomeText.setText("Bienvenid@!");
                    }
                })
                .addOnFailureListener(e -> {
                    welcomeText.setText("Error al cargar nombre.");
                    Toast.makeText(this, "Error al obtener el nombre: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarAnuncioPrincipal() {
        firestore.collection("anuncios").document("anuncio_principal")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String titulo = documentSnapshot.getString("titulo");
                        String contenido = documentSnapshot.getString("contenido");
                        announcementTitle.setText(titulo);
                        announcementContent.setText(contenido);
                    } else {
                        announcementTitle.setText("¿Tienes todo listo?");
                        announcementContent.setText("Recuerda preparar el partido del fin de semana");
                    }
                })
                .addOnFailureListener(e -> {
                    welcomeText.setText("Error al cargar nombre.");
                    Toast.makeText(this, "Error al obtener el nombre: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarEquiposDesdeFirestore(String trainerId) {
        showLoadingIndicator();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("entrenadores").document(trainerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Object> equipos = (List<Object>) documentSnapshot.get("equipos");
                        if (equipos != null) {
                            teamList.clear(); // Limpia la lista para evitar duplicados
                            List<String> teamIds = new ArrayList<>();
                            Map<String, Integer> teamIndexMap = new HashMap<>();
                            for (int i = 0; i < equipos.size(); i++) {
                                String teamId = String.valueOf(equipos.get(i));
                                teamIds.add(teamId);
                                teamIndexMap.put(teamId, i); // Asocia el ID al índice original
                            }

                            Map<String, Team> teamMap = new HashMap<>();
                            for (String teamId : teamIds) {
                                db.collection("equipos").document(teamId)
                                        .get()
                                        .addOnSuccessListener(teamSnapshot -> {
                                            if (teamSnapshot.exists()) {
                                                Team team = teamSnapshot.toObject(Team.class);
                                                if (team != null) {
                                                    teamMap.put(teamId, team);
                                                }
                                            }

                                            // Una vez se han procesado todos los IDs, organiza el orden
                                            if (teamMap.size() == teamIds.size()) {
                                                teamList.clear();
                                                for (String id : teamIds) {
                                                    if (teamMap.containsKey(id)) {
                                                        teamList.add(teamMap.get(id));
                                                    }
                                                }
                                                teamAdapter.notifyDataSetChanged();
                                            }

                                            hideLoadingIndicator();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("MainActivity", "Error al cargar equipo: " + e.getMessage());
                                            hideLoadingIndicator();
                                        });
                            }
                        } else {
                            hideLoadingIndicator();
                        }
                    } else {
                        hideLoadingIndicator(); // Si no existe el documento del entrenador
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    Log.e("MainActivity", "Error al cargar equipos desde Firestore: " + e.getMessage());
                });
    }


    private void mostrarOpcionesEquipo() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_team, null);

        TextView crearEquipoOpcion = bottomSheetView.findViewById(R.id.createTeamOption);
        TextView unirEquipoOpcion = bottomSheetView.findViewById(R.id.joinTeamOption);

        crearEquipoOpcion.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(MainActivity.this, CreateTeamActivity.class));
        });

        unirEquipoOpcion.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            mostrarDialogoUnirseEquipo();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void mostrarDialogoUnirseEquipo() {
        BottomSheetDialog joinTeamDialog = new BottomSheetDialog(this);
        View joinTeamView = getLayoutInflater().inflate(R.layout.dialog_join_team, null);

        EditText codeEditText = joinTeamView.findViewById(R.id.codeEditText);
        Button joinButton = joinTeamView.findViewById(R.id.joinTeamButton);

        joinButton.setOnClickListener(v -> {
            String code = codeEditText.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un código.", Toast.LENGTH_SHORT).show();
                return;
            }
            unirseEquipoPorCodigo(code);
            joinTeamDialog.dismiss();
        });

        joinTeamDialog.setContentView(joinTeamView);
        joinTeamDialog.show();
    }

    private void unirseEquipoPorCodigo(String codigo) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        firestore.collection("equipos")
                .whereEqualTo("id", codigo)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String equipoId = querySnapshot.getDocuments().get(0).getId();

                        firestore.collection("entrenadores").document(currentUser.getUid())
                                .update("equipos", FieldValue.arrayUnion(equipoId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Te has unido al equipo con éxito.", Toast.LENGTH_SHORT).show();
                                    cargarEquiposDesdeFirestore(currentUser.getUid());
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error al unirte al equipo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Código de equipo inválido.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al buscar equipo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
