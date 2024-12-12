package com.volleycubas.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrainingFragment extends Fragment {

    private static final String ARG_TEAM_ID = "teamId";
    private String teamId;
    private TextView tvNextMatchTeam, tvNextMatchDate, tvNextMatchTime, tvNextMatchLocation, tvDaysRemaining;

    private List<Jugador> jugadoresList = new ArrayList<>();


    public static TrainingFragment newInstance(String teamId) {
        TrainingFragment fragment = new TrainingFragment();
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
        View view = inflater.inflate(R.layout.fragment_training, container, false);

        // Inicializar vistas
        tvNextMatchTeam = view.findViewById(R.id.tv_next_match_team);
        tvNextMatchDate = view.findViewById(R.id.tv_next_match_date);
        tvNextMatchTime = view.findViewById(R.id.tv_next_match_time);
        tvNextMatchLocation = view.findViewById(R.id.tv_next_match_location);
        tvDaysRemaining = view.findViewById(R.id.tv_days_remaining);

        // Cargar el próximo partido
        loadNextMatch(teamId);
        loadPlayersFromFirestore(teamId);

        //Sub pantallas
        CardView cardGenerateTeams = view.findViewById(R.id.cardGenerateTeams);
        cardGenerateTeams.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), GenerateTeamsActivity.class);
            intent.putParcelableArrayListExtra("jugadores", new ArrayList<>(jugadoresList));
            intent.putExtra("teamId", teamId);
            Log.d("TrainingFragment", "Jugadores enviados: " + jugadoresList.toString());
            startActivity(intent);
        });

        CardView cardLineups = view.findViewById(R.id.cardLineups);
        cardLineups.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), LineupsActivity.class);
            intent.putExtra("teamId", teamId);
            startActivity(intent);
        });

        CardView cardAssistance = view.findViewById(R.id.cardAssistance);
        cardAssistance.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AssistanceActivity.class);
            intent.putParcelableArrayListExtra("jugadores", new ArrayList<>(jugadoresList));
            intent.putExtra("teamId", teamId);
            Log.d("TrainingFragment", "Jugadores enviados: " + jugadoresList.toString());
            startActivity(intent);
        });


        return view;
    }

    private void loadNextMatch(String teamId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Consulta ordenada por el campo "inicio" de los fines de semana
        db.collection("horarios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot closestWeekend = null;
                        long closestDifference = Long.MAX_VALUE;

                        // Iterar por los documentos para encontrar el más cercano
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Map<String, Object> weekendData = doc.getData();
                            if (weekendData != null) {
                                // Obtener la fecha "inicio" (viernes) del fin de semana
                                String inicio = (String) ((Map<String, Object>) weekendData.get("fin_de_semana")).get("inicio");
                                if (inicio != null) {
                                    try {
                                        // Convertir la fecha a formato Date
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        Date weekendStart = sdf.parse(inicio);
                                        Date today = new Date();

                                        // Calcular la diferencia de días entre hoy y el inicio del fin de semana
                                        long diff = Math.abs(weekendStart.getTime() - today.getTime());

                                        // Buscar el fin de semana más cercano
                                        if (diff < closestDifference) {
                                            closestDifference = diff;
                                            closestWeekend = doc;
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        // Usar el documento más cercano si existe
                        if (closestWeekend != null) {
                            Map<String, Object> weekendData = closestWeekend.getData();
                            if (weekendData != null) {
                                // Extraer el partido correspondiente al equipo
                                Map<String, Object> match = extractMatchFromWeekend(weekendData, teamId);
                                if (match != null) {
                                    updateUIWithMatch(match);
                                } else {
                                    showNoMatchMessage();
                                }
                            }
                        } else {
                            showNoMatchMessage();
                        }
                    } else {
                        showNoMatchMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    showNoMatchMessage();
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMatchFromWeekend(Map<String, Object> weekendData, String teamId) {
        Object partidosObj = weekendData.get("partidos");

        if (partidosObj instanceof Map) {
            // Si es un mapa, recorrer cada clave (ID de equipo)
            Map<String, List<Map<String, Object>>> partidos = (Map<String, List<Map<String, Object>>>) partidosObj;
            if (partidos.containsKey(teamId)) {
                List<Map<String, Object>> listaPartidos = partidos.get(teamId);
                // Usar el primer partido de la lista como próximo partido
                if (listaPartidos != null && !listaPartidos.isEmpty()) {
                    return listaPartidos.get(0);
                }
            }
        }

        return null; // Devuelve null si no se encuentra el partido
    }

    private void updateUIWithMatch(Map<String, Object> match) {
        // Actualizar las vistas con los datos del partido
        String rival = (String) match.get("rival");
        String fecha = (String) match.get("fecha");
        String hora = (String) match.get("hora");
        String localizacion = (String) match.get("localizacion");
        String pista = (String) match.get("pista");

        tvNextMatchTeam.setText(rival != null ? rival : "Sin rival");
        tvNextMatchDate.setText(fecha != null ? fecha : "--/--/----");
        tvNextMatchTime.setText(hora != null ? hora : "--:--");
        tvNextMatchLocation.setText(localizacion != null ? localizacion + (pista != null ? " - " + pista : "") : "Localización no disponible");

        // Calcular días y horas restantes
        if (fecha != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date matchDate;

                if (hora != null) {
                    matchDate = sdf.parse(fecha + " " + hora);
                } else {
                    // Si no hay hora, solo usar la fecha
                    sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    matchDate = sdf.parse(fecha);
                }

                if (matchDate != null) {
                    long diffMillis = matchDate.getTime() - System.currentTimeMillis();

                    if (diffMillis > 0) {
                        long daysRemaining = diffMillis / (1000 * 60 * 60 * 24);
                        long hoursRemaining = (diffMillis / (1000 * 60 * 60)) % 24;

                        if (daysRemaining > 0) {
                            if (hoursRemaining > 0) {
                                tvDaysRemaining.setText("Quedan " + daysRemaining + " días y " + hoursRemaining + " horas");
                            } else {
                                tvDaysRemaining.setText("Quedan " + daysRemaining + " días");
                            }
                        } else {
                            tvDaysRemaining.setText("Quedan " + hoursRemaining + " horas");
                        }
                    } else {
                        // Si el partido ya pasó
                        tvDaysRemaining.setText("Partido finalizado");
                        tvDaysRemaining.setBackgroundTintList(getResources().getColorStateList(R.color.red, getContext().getTheme()));
                    }
                } else {
                    tvDaysRemaining.setText("Fecha no válida");
                }
            } catch (ParseException e) {
                Log.e("LineupFragment", "Error parsing date", e);
                tvDaysRemaining.setText("Fecha inválida");
            }
        } else {
            tvDaysRemaining.setText("No disponible");
        }
    }

    private void loadPlayersFromFirestore(String teamId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos")
                .document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> jugadoresFirestore = (List<Map<String, Object>>) documentSnapshot.get("jugadores");
                        if (jugadoresFirestore != null) {
                            jugadoresList.clear();
                            for (Map<String, Object> jugadorMap : jugadoresFirestore) {
                                String id = (String) jugadorMap.get("id");
                                String nombre = (String) jugadorMap.get("nombre");
                                String posicion = (String) jugadorMap.get("posicion");
                                Long numero = (Long) jugadorMap.get("numero");
                                jugadoresList.add(new Jugador(id, nombre, Math.toIntExact(numero), posicion));
                            }
                        }
                    } else {
                        jugadoresList.clear();
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    jugadoresList.clear();
                });
    }

    private void showNoMatchMessage() {
        tvNextMatchTeam.setText("Sin próximos partidos");
        tvNextMatchDate.setText("");
        tvNextMatchTime.setText("");
        tvNextMatchLocation.setText("");
        tvDaysRemaining.setText("");
    }

}
