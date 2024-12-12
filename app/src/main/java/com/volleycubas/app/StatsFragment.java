package com.volleycubas.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class StatsFragment extends Fragment {

    private static final String ARG_TEAM_ID = "teamId";
    private String teamId;

    private TextView tvVictories, tvSetsWon, tvAttendancePercentage, tvPointsScored, tvMaxStreak;
    private ProgressBar progressVictories, progressSetsWon, progressAttendance, progressPointsScored, progressMaxStreak;

    public static StatsFragment newInstance(String teamId) {
        StatsFragment fragment = new StatsFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Inicializar vistas
        tvVictories = view.findViewById(R.id.tvVictories);
        tvSetsWon = view.findViewById(R.id.tvSetsWon);
        tvAttendancePercentage = view.findViewById(R.id.tvAttendancePercentage);
        tvPointsScored = view.findViewById(R.id.tvPointsScored);
        tvMaxStreak = view.findViewById(R.id.tvMaxStreak);

        progressVictories = view.findViewById(R.id.progressVictories);
        progressSetsWon = view.findViewById(R.id.progressSetsWon);
        progressAttendance = view.findViewById(R.id.progressAttendance);
        progressPointsScored = view.findViewById(R.id.progressPointsScored);
        progressMaxStreak = view.findViewById(R.id.progressMaxStreak);

        // Cargar datos del equipo
        loadTeamStats();

        return view;
    }

    private void loadTeamStats() {
        if (teamId == null) {
            Log.e("StatsFragment", "El teamId es nulo.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos").document(teamId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Estadísticas
                        int setsAFavor = documentSnapshot.getLong("sets_a_favor").intValue();
                        int setsTotales = documentSnapshot.getLong("sets_totales").intValue();
                        int puntosAFavor = documentSnapshot.getLong("puntos_a_favor").intValue();
                        int puntosTotales = documentSnapshot.getLong("puntos_totales").intValue();
                        int mejorRacha = documentSnapshot.getLong("mejor_racha").intValue();
                        int partidosJugados = documentSnapshot.getLong("partidos_jugados").intValue();
                        int partidosGanados = documentSnapshot.getLong("partidos_ganados").intValue();

                        // Datos del card
                        String nombre = documentSnapshot.getString("nombre");
                        String capitan = documentSnapshot.getString("capitan");
                        String liga = documentSnapshot.getString("liga");
                        int numeroJugadores = documentSnapshot.getLong("numero_jugadores").intValue();
                        String temporadaCreacion = documentSnapshot.getString("temporada_creacion");
                        String urlImagen = documentSnapshot.getString("url_imagen");

                        String entrenador = "Sin entrenador";
                        if (documentSnapshot.contains("entrenadores")) {
                            List<String> entrenadores = (List<String>) documentSnapshot.get("entrenadores");
                            if (entrenadores != null && !entrenadores.isEmpty()) {
                                entrenador = String.join(", ", entrenadores); // Combinar los nombres
                            }
                        }

                        // Actualizar estadísticas
                        tvSetsWon.setText(String.format("%d/%d", setsAFavor, setsTotales));
                        progressSetsWon.setProgress(setsTotales == 0 ? 0 : (setsAFavor * 100) / setsTotales);

                        tvPointsScored.setText(String.format("%d/%d", puntosAFavor, puntosTotales));
                        progressPointsScored.setProgress(puntosTotales == 0 ? 0 : (puntosAFavor * 100) / puntosTotales);

                        tvMaxStreak.setText(String.valueOf(mejorRacha));
                        progressMaxStreak.setProgress(100);

                        tvVictories.setText(String.format("%d/%d", partidosGanados, partidosJugados));
                        progressVictories.setProgress(partidosJugados == 0 ? 0 : (partidosGanados * 100) / partidosJugados);

                        tvAttendancePercentage.setText("95%");
                        progressAttendance.setProgress(95);

                        updateTeamCard(nombre, capitan, liga, numeroJugadores, entrenador, temporadaCreacion, urlImagen);
                    } else {
                        Log.e("StatsFragment", "No se encontró el documento del equipo.");
                    }
                })
                .addOnFailureListener(e -> Log.e("StatsFragment", "Error al cargar datos del equipo.", e));
    }

    private void updateTeamCard(String nombre, String capitan, String liga, int numeroJugadores, String entrenador, String temporadaCreacion, String urlImagen) {
        Log.d("StatsFragment", "Intentando cargar team_card...");
        View teamCard = getView().findViewById(R.id.team_card);

        if (teamCard == null) {
            Log.e("StatsFragment", "team_card es null. Asegúrate de que está incluido en fragment_stats.xml.");
            return;
        }

        TextView tvTeamName = teamCard.findViewById(R.id.teamName);
        if (tvTeamName == null) {
            Log.e("StatsFragment", "tvTeamName no encontrado en layout_team_card.xml.");
            return;
        }

        TextView tvSeason = teamCard.findViewById(R.id.teamSeason);
        TextView tvLeague = teamCard.findViewById(R.id.teamLeague);
        TextView tvPlayersCount = teamCard.findViewById(R.id.teamPlayersCount);
        TextView tvCaptain = teamCard.findViewById(R.id.teamCaptain);
        TextView tvCoach = teamCard.findViewById(R.id.teamCoach);
        ImageView ivTeamImage = teamCard.findViewById(R.id.teamImage);

        tvTeamName.setText(nombre != null ? nombre : "Nombre no disponible");
        tvSeason.setText(temporadaCreacion != null ? "Creado en la temporada " + temporadaCreacion : "Temporada no disponible");
        tvLeague.setText(liga != null ? liga : "Sin liga");
        tvPlayersCount.setText(String.valueOf(numeroJugadores));
        tvCaptain.setText(capitan != null ? capitan : "Sin capitán");
        tvCoach.setText(entrenador != null ? entrenador : "Sin entrenador");

        if (urlImagen != null && !urlImagen.isEmpty()) {
            Glide.with(this)
                    .load(urlImagen)
                    .placeholder(R.drawable.ic_image_placeholder) // Imagen de carga
                    .error(R.drawable.ic_image_placeholder) // Imagen de error
                    .into(ivTeamImage);
        } else {
            ivTeamImage.setImageResource(R.drawable.ic_image_placeholder); // Imagen por defecto
        }
    }

}
