package com.volleycubas.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GenerateTeamsActivity extends AppCompatActivity {

    private TextView tvTeamCount;
    private RecyclerView rvPlayers, rvTeams;
    private int teamCount = 2;

    private List<String> players = new ArrayList<>();
    private List<List<String>> generatedTeams;

    private String teamId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_teams);

        // Inicializar vistas
        tvTeamCount = findViewById(R.id.tvTeamCount);
        rvPlayers = findViewById(R.id.rvPlayers);
        rvTeams = findViewById(R.id.rvTeams);

        ImageView btnDecreaseTeams = findViewById(R.id.btnDecreaseTeams);
        ImageView btnIncreaseTeams = findViewById(R.id.btnIncreaseTeams);
        Button btnGenerateTeams = findViewById(R.id.btnSaveAssistance);

        // Obtener el ID del equipo desde los extras del Intent
        teamId = getIntent().getStringExtra("teamId");

        // Obtener jugadores desde el Intent

        List<Jugador> jugadoresList = getIntent().getParcelableArrayListExtra("jugadores");
        if (jugadoresList != null) {
            Log.d("GenerateTeamsActivity", "Jugadores recibidos: " + jugadoresList);
            for (Jugador jugador : jugadoresList) {
                players.add(jugador.getNombre());
            }
        }

        // Configurar RecyclerView con la lista de jugadores
        Log.d("GenerateTeamsActivity", "Lista de jugadores para RecyclerView: " + players);

        PlayerAdapter playerAdapter = new PlayerAdapter(players);
        rvPlayers.setLayoutManager(new LinearLayoutManager(this));
        rvPlayers.setAdapter(playerAdapter);

        // Configurar RecyclerView de equipos
        rvTeams.setLayoutManager(new GridLayoutManager(this, 2));

        btnDecreaseTeams.setOnClickListener(v -> {
            if (teamCount > 2) {
                teamCount--;
                tvTeamCount.setText(String.valueOf(teamCount));
                updateButtonColors(btnDecreaseTeams, btnIncreaseTeams);
            }
        });

        btnIncreaseTeams.setOnClickListener(v -> {
            if (teamCount < players.size()) {
                teamCount++;
                tvTeamCount.setText(String.valueOf(teamCount));
                updateButtonColors(btnDecreaseTeams, btnIncreaseTeams);
            }
        });

        // Botón para generar equipos
        btnGenerateTeams.setOnClickListener(v -> generateTeams());

        ImageView btnAddPlayer = findViewById(R.id.btn_addPlayer);
        btnAddPlayer.setOnClickListener(v -> showAddPlayerBottomSheet());

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void updateButtonColors(ImageView btnDecrease, ImageView btnIncrease) {
        if (teamCount <= 2) {
            btnDecrease.setColorFilter(Color.GRAY); // Botón de menos en gris
        } else {
            btnDecrease.setColorFilter(Color.WHITE); // Botón de menos en blanco
        }

        if (teamCount >= players.size()) {
            btnIncrease.setColorFilter(Color.GRAY); // Botón de más en gris
        } else {
            btnIncrease.setColorFilter(Color.WHITE); // Botón de más en blanco
        }
    }

    private void loadPlayersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos")
                .document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> jugadoresList = (List<Map<String, Object>>) documentSnapshot.get("jugadores");
                        if (jugadoresList != null) {
                            players.clear(); // Limpia la lista para evitar duplicados
                            for (Map<String, Object> jugador : jugadoresList) {
                                String nombre = (String) jugador.get("nombre");
                                if (nombre != null && !nombre.isEmpty()) {
                                    players.add(nombre);
                                }
                            }
                        }

                        // Configurar el adaptador
                        PlayerAdapter playerAdapter = new PlayerAdapter(players);
                        rvPlayers.setLayoutManager(new LinearLayoutManager(this));
                        rvPlayers.setAdapter(playerAdapter);

                        // Notificar cambios
                        playerAdapter.notifyDataSetChanged();
                    } else {
                        players.clear();
                        rvPlayers.setAdapter(new PlayerAdapter(players));
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    players.clear();
                    rvPlayers.setAdapter(new PlayerAdapter(players));
                });
    }

    private void showAddPlayerBottomSheet() {
        // Crear el BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.layout_add_player_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Referencias a las vistas del BottomSheet
        EditText etPlayerName = bottomSheetView.findViewById(R.id.etPlayerName);
        Button btnSavePlayer = bottomSheetView.findViewById(R.id.btnSavePlayer);

        // Manejar el botón de guardar
        btnSavePlayer.setOnClickListener(v -> {
            String playerName = etPlayerName.getText().toString().trim();
            if (!playerName.isEmpty()) {
                // Añadir el jugador a la lista
                players.add(playerName);
                rvPlayers.getAdapter().notifyItemInserted(players.size() - 1);

                // Cerrar el BottomSheet
                bottomSheetDialog.dismiss();
            } else {
                etPlayerName.setError("El nombre no puede estar vacío");
            }
        });

        // Mostrar el BottomSheet
        bottomSheetDialog.show();
    }


    private void generateTeams() {
        // Limpiar equipos anteriores
        generatedTeams = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            generatedTeams.add(new ArrayList<>());
        }

        // Mezclar jugadores
        List<String> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        // Distribuir jugadores en equipos
        for (int i = 0; i < shuffledPlayers.size(); i++) {
            int teamIndex = i % teamCount;
            generatedTeams.get(teamIndex).add(shuffledPlayers.get(i));
        }

        // Registrar los equipos creados
        for (int i = 0; i < generatedTeams.size(); i++) {
            Log.d("GenerateTeamsActivity", "Equipo " + (i + 1) + ": " + generatedTeams.get(i));
        }

        // Actualizar RecyclerView de equipos
        TeamAdapter teamAdapter = new TeamAdapter(generatedTeams);
        rvTeams.setAdapter(teamAdapter);

        teamAdapter.notifyDataSetChanged();
    }

    // Adaptador para jugadores
    private static class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

        private final List<String> players;

        PlayerAdapter(List<String> players) {
            this.players = players;
        }

        @NonNull
        @Override
        public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_randomizer, parent, false);
            return new PlayerViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
            String playerName = players.get(position);
            holder.tvPlayerName.setText(playerName);

            holder.ivRemoveButton.setOnClickListener(v -> {
                players.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, players.size());
            });
        }

        @Override
        public int getItemCount() {
            return players.size();
        }

        static class PlayerViewHolder extends RecyclerView.ViewHolder {

            TextView tvPlayerName;
            ImageView ivRemoveButton;

            PlayerViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
                ivRemoveButton = itemView.findViewById(R.id.Iv_remove_button);
            }
        }
    }

    // Adaptador para equipos
    private static class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

        private final List<List<String>> teams;

        TeamAdapter(List<List<String>> teams) {
            this.teams = teams;
        }

        @NonNull
        @Override
        public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_randomizer, parent, false);
            return new TeamViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
            // Título del equipo
            holder.tvTeamNumber.setText("Equipo " + (position + 1));

            // Lista de jugadores del equipo
            StringBuilder teamContent = new StringBuilder();
            for (String player : teams.get(position)) {
                teamContent.append(player).append("\n");
            }
            holder.tvTeamPlayers.setText(teamContent.toString().trim());
        }



        @Override
        public int getItemCount() {
            return teams.size();
        }

        static class TeamViewHolder extends RecyclerView.ViewHolder {

            TextView tvTeamPlayers;
            TextView tvTeamNumber;

            TeamViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTeamPlayers = itemView.findViewById(R.id.tvTeamPlayers);
                tvTeamNumber = itemView.findViewById(R.id.tvTeamTitle);
            }
        }
    }
}