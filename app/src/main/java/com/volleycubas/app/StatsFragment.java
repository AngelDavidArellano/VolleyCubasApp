package com.volleycubas.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import java.io.File;


public class StatsFragment extends Fragment {

    private static final String ARG_TEAM_ID = "teamId";
    private String teamId;

    private ImageView settingsBtn;
    private TextView tvVictories, tvSetsWon, tvAttendancePercentage, tvPointsScored, tvMaxStreak, tvMaxStreak_data;
    private TextView tvVictories_amount, tvSetsWon_amount, tvPointsScored_amount;
    private ProgressBar progressVictories, progressSetsWon, progressAttendance, progressPointsScored;

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
        tvVictories_amount = view.findViewById(R.id.tvVictories_amount);
        tvSetsWon = view.findViewById(R.id.tvSetsWon);
        tvSetsWon_amount = view.findViewById(R.id.tvSetsWon_amount);
        tvAttendancePercentage = view.findViewById(R.id.tvAttendancePercentage);
        tvPointsScored = view.findViewById(R.id.tvPointsScored);
        tvPointsScored_amount = view.findViewById(R.id.tvPointsScored_amount);
        tvMaxStreak = view.findViewById(R.id.tvMaxStreak);
        tvMaxStreak_data = view.findViewById(R.id.tvMaxStreak_data);

        progressVictories = view.findViewById(R.id.progressVictories);
        progressSetsWon = view.findViewById(R.id.progressSetsWon);
        progressAttendance = view.findViewById(R.id.progressAttendance);
        progressPointsScored = view.findViewById(R.id.progressPointsScored);

        settingsBtn = view.findViewById(R.id.ivSettings);

        // Cargar datos del equipo
        loadTeamStats();

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), TeamSettingsActivity.class);
            intent.putExtra("teamId", teamId);
            startActivity(intent);
        });

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
                        int setsAFavor = documentSnapshot.contains("sets_a_favor") ?
                                documentSnapshot.getLong("sets_a_favor").intValue() : 0;
                        int setsTotales = documentSnapshot.contains("sets_totales") ?
                                documentSnapshot.getLong("sets_totales").intValue() : 0;
                        int puntosAFavor = documentSnapshot.contains("puntos_a_favor") ?
                                documentSnapshot.getLong("puntos_a_favor").intValue() : 0;
                        int puntosTotales = documentSnapshot.contains("puntos_totales") ?
                                documentSnapshot.getLong("puntos_totales").intValue() : 0;
                        int mejorRacha = documentSnapshot.contains("mejor_racha") ?
                                documentSnapshot.getLong("mejor_racha").intValue() : 0;
                        String mejorRachaData = documentSnapshot.contains("rival_fecha_mejor_racha") ?
                                documentSnapshot.getString("rival_fecha_mejor_racha") : "";
                        int partidosJugados = documentSnapshot.contains("partidos_jugados") ?
                                documentSnapshot.getLong("partidos_jugados").intValue() : 0;
                        int partidosGanados = documentSnapshot.contains("partidos_ganados") ?
                                documentSnapshot.getLong("partidos_ganados").intValue() : 0;

                        // Datos del card
                        String nombre = documentSnapshot.contains("nombre") ? documentSnapshot.getString("nombre") : "";
                        String capitan = documentSnapshot.contains("capitan") ? documentSnapshot.getString("capitan") : "";
                        String liga = documentSnapshot.contains("liga") ? documentSnapshot.getString("liga") : "";
                        int numeroJugadores = documentSnapshot.contains("numero_jugadores") ?
                                documentSnapshot.getLong("numero_jugadores").intValue() : 0;
                        String temporadaCreacion = documentSnapshot.contains("temporada_creacion") ?
                                documentSnapshot.getString("temporada_creacion") : "";
                        String urlImagen = documentSnapshot.contains("url_imagen") ? documentSnapshot.getString("url_imagen") : "";

                        String entrenador = "Sin entrenador";
                        if (documentSnapshot.contains("entrenadores")) {
                            List<String> entrenadores = (List<String>) documentSnapshot.get("entrenadores");
                            if (entrenadores != null && !entrenadores.isEmpty()) {
                                entrenador = String.join(", ", entrenadores); // Combinar los nombres
                            }
                        }

                        // Actualizar estadísticas
                        tvSetsWon.setText(String.valueOf(setsAFavor));
                        tvSetsWon_amount.setText("de " + setsTotales + " jugados");
                        progressSetsWon.setProgress(setsTotales == 0 ? 0 : (setsAFavor * 100) / setsTotales);

                        tvPointsScored.setText(String.valueOf(puntosAFavor));
                        tvPointsScored_amount.setText("de " + puntosTotales + " jugados");
                        progressPointsScored.setProgress(puntosTotales == 0 ? 0 : (puntosAFavor * 100) / puntosTotales);

                        tvMaxStreak.setText(String.valueOf(mejorRacha));
                        tvMaxStreak_data.setText(mejorRachaData);

                        tvVictories.setText(String.valueOf(partidosGanados));
                        tvVictories_amount.setText("de " + partidosJugados + " partidos");
                        progressVictories.setProgress(partidosJugados == 0 ? 0 : (partidosGanados * 100) / partidosJugados);

                        updateTeamCard(nombre, capitan, liga, numeroJugadores, entrenador, temporadaCreacion, urlImagen, documentSnapshot.contains("timestamp") ? documentSnapshot.getLong("timestamp") : 0L);
                    } else {
                        Log.e("StatsFragment", "No se encontró el documento del equipo.");
                    }

                })
                .addOnFailureListener(e -> Log.e("StatsFragment", "Error al cargar datos del equipo.", e));

        db.collection("registros").document(teamId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    double porcentajeAcumulado = documentSnapshot.contains("porcentaje_acumulado") ?
                            documentSnapshot.getDouble("porcentaje_acumulado") : 0.0;
                    tvAttendancePercentage.setText(String.format("%.1f", porcentajeAcumulado));
                    progressAttendance.setProgress((int) porcentajeAcumulado);
                }).addOnFailureListener(e -> Log.e("StatsFragment", "Error al cargar porcentaje de asistencias del equipo.", e));
    }


    private void updateTeamCard(String nombre, String capitan, String liga, int numeroJugadores, String entrenador, String temporadaCreacion, String urlImagen, long timestamp) {
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

        File directory = new File(requireContext().getFilesDir(), "img/team_images");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File localFile = new File(directory, teamId + "_team.png");

        if (localFile.exists() && !isImageOutdated(localFile, timestamp)) {
            Log.d("StatsFragment", "Cargando imagen del equipo desde almacenamiento local.");
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            ivTeamImage.setImageBitmap(bitmap);
        } else if (urlImagen != null && !urlImagen.isEmpty()) {
            Log.d("StatsFragment", "Descargando imagen del equipo desde URL: " + urlImagen);
            Glide.with(this)
                    .asBitmap()
                    .load(urlImagen)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            Log.d("StatsFragment", "Imagen del equipo descargada con éxito.");
                            ivTeamImage.setImageBitmap(resource);
                            saveTeamImageLocally(resource, localFile, timestamp);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            // No hacer nada
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Log.e("StatsFragment", "Error al descargar la imagen del equipo con Glide.");
                        }
                    });
        } else {
            Log.w("StatsFragment", "URL de imagen de equipo nulo o vacío. Usando imagen predeterminada.");
            ivTeamImage.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    private void saveTeamImageLocally(Bitmap bitmap, File file, long timestamp) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            File timestampFile = new File(file.getParent(), teamId + "_timestamp.txt");
            try (FileOutputStream tsFos = new FileOutputStream(timestampFile)) {
                tsFos.write(String.valueOf(timestamp).getBytes());
            }
        } catch (IOException e) {
            Log.e("StatsFragment", "Error guardando imagen del equipo localmente.", e);
        }
    }

    private boolean isImageOutdated(File file, long serverTimestamp) {
        File timestampFile = new File(file.getParent(), teamId + "_timestamp.txt");
        if (timestampFile.exists()) {
            try {
                long localTimestamp = Long.parseLong(new String(java.nio.file.Files.readAllBytes(timestampFile.toPath())));
                return serverTimestamp > localTimestamp;
            } catch (IOException | NumberFormatException e) {
                Log.e("StatsFragment", "Error leyendo timestamp local.", e);
            }
        }
        return true;
    }

}
