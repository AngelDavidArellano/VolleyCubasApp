package com.volleycubas.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayersFragment extends Fragment {

    private static final String ARG_TEAM_ID = "teamId";
    private String teamId;

    private View loadingOverlay;
    private ImageView loadingIndicator;

    private RecyclerView playersRecyclerView;
    private PlayerAdapter playerAdapter;
    private List<Jugador> jugadoresList = new ArrayList<>();
    private List<Jugador> filteredJugadoresList = new ArrayList<>();

    private EditText searchEditText;

    public static PlayersFragment newInstance(String teamId) {
        PlayersFragment fragment = new PlayersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEAM_ID, teamId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            teamId = getArguments().getString(ARG_TEAM_ID);
        }
    }

    @Override
    public void onResume() {
        loadPlayers();
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_players, container, false);

        // Configurar RecyclerView
        playersRecyclerView = view.findViewById(R.id.playersRecyclerView);
        playerAdapter = new PlayerAdapter(jugadoresList, jugador -> {
            // Abrir PlayerDetailsActivity al hacer clic en un jugador
            Intent intent = new Intent(getContext(), PlayerDetailsActivity.class);
            intent.putExtra("jugador", (Parcelable) jugador);
            intent.putExtra("teamId", teamId);
            startActivity(intent);
        });

        playersRecyclerView.setAdapter(playerAdapter);
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadingOverlay = view.findViewById(R.id.loadingOverlay_players);
        loadingIndicator = view.findViewById(R.id.loadingGif_players);

        // Configurar barra de búsqueda
        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se requiere implementación
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlayers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se requiere implementación
            }
        });

        // Botón para añadir jugador
        view.findViewById(R.id.addPlayerButton).setOnClickListener(v -> showAddPlayerDialog());

        // Cargar jugadores
        loadPlayers();

        return view;
    }

    private void showLoadingIndicator() {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingOverlay.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void loadPlayers() {
        showLoadingIndicator();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (teamId == null) {
            Toast.makeText(getContext(), "Error: Team ID no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("equipos").document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    hideLoadingIndicator();
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> jugadoresFirebase = (List<Map<String, Object>>) documentSnapshot.get("jugadores");
                        if (jugadoresFirebase != null) {
                            jugadoresList.clear();

                            for (Map<String, Object> jugadorData : jugadoresFirebase) {
                                String id = (String) jugadorData.get("id");
                                String nombre = (String) jugadorData.get("nombre");
                                String posicion = (String) jugadorData.get("posicion");
                                Long numero = (Long) jugadorData.get("numero");
                                String notas = (String) jugadorData.get("notas");

                                Jugador jugador = new Jugador(
                                        id,
                                        nombre != null ? nombre : "Sin nombre",
                                        posicion != null ? posicion : "Sin posición",
                                        numero != null ? numero.intValue() : 0,
                                        notas != null ? notas : "No hay notas"
                                );

                                jugadoresList.add(jugador);
                            }

                            // Ordenar jugadores por número ascendente
                            jugadoresList.sort((j1, j2) -> Integer.compare(j1.getNumero(), j2.getNumero()));

                            // Inicialmente, mostrar todos los jugadores ordenados
                            filteredJugadoresList.clear();
                            filteredJugadoresList.addAll(jugadoresList);
                            playerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "No hay jugadores en este equipo.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "El equipo no existe.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    Toast.makeText(getContext(), "Error al cargar jugadores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void filterPlayers(String query) {
        filteredJugadoresList.clear();

        if (query.isEmpty()) {
            filteredJugadoresList.addAll(jugadoresList);
        } else {
            String normalizedQuery = normalizeString(query);
            for (Jugador jugador : jugadoresList) {
                String normalizedNombre = normalizeString(jugador.getNombre());
                if (normalizedNombre.contains(normalizedQuery)) {
                    filteredJugadoresList.add(jugador);
                }
            }
        }

        playerAdapter.notifyDataSetChanged();
    }

    private String normalizeString(String input) {
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // Elimina marcas diacríticas (tildes)
                .toLowerCase(); // Convierte a minúsculas
    }

    private void showAddPlayerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_player, null);

        EditText playerNameEditText = dialogView.findViewById(R.id.playerNameEditText);
        EditText playerPositionEditText = dialogView.findViewById(R.id.playerPositionEditText);
        EditText playerNumberEditText = dialogView.findViewById(R.id.playerNumberEditText);
        EditText playerNotesEditText = dialogView.findViewById(R.id.playerNotesEditText);
        Button addPlayerButton = dialogView.findViewById(R.id.addPlayerButtonDialog);

        addPlayerButton.setOnClickListener(v -> {
            String id = UUID.randomUUID().toString();

            String nombre = playerNameEditText.getText().toString().trim();
            String posicion = playerPositionEditText.getText().toString().trim();
            String numeroStr = playerNumberEditText.getText().toString().trim();
            String notas = playerNotesEditText.getText().toString().trim();

            if (nombre.isEmpty() || posicion.isEmpty() || numeroStr.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            int numero = Integer.parseInt(numeroStr);

            Jugador newJugador = new Jugador(id, nombre, posicion, numero, notas);

            jugadoresList.add(newJugador);

            // Ordenar la lista por número ascendente
            jugadoresList.sort((j1, j2) -> Integer.compare(j1.getNumero(), j2.getNumero()));

            playerAdapter.notifyDataSetChanged();

            savePlayerToFirestore(newJugador);
            dialog.dismiss();
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void savePlayerToFirestore(Jugador jugador) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Añadir el jugador al array de jugadores
        db.collection("equipos").document(teamId)
                .update("jugadores", FieldValue.arrayUnion(jugador.toMap()))
                .addOnSuccessListener(aVoid -> {
                    // Incrementar el número de jugadores
                    incrementPlayerCount(db);
                    Toast.makeText(getContext(), "Jugador añadido con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al añadir jugador: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Método para incrementar el número de jugadores
    private void incrementPlayerCount(FirebaseFirestore db) {
        db.collection("equipos").document(teamId)
                .update("numero_jugadores", FieldValue.increment(1)) // Incrementa en 1
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Número de jugadores actualizado", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al actualizar número de jugadores: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}