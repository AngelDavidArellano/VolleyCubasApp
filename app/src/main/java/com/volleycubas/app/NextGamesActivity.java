package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NextGamesActivity extends AppCompatActivity {

    private RecyclerView rvGames;
    private ImageView btnBack;
    private GamesAdapter gamesAdapter;
    private List<Map<String, Object>> allMatches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_games);

        rvGames = findViewById(R.id.nextGamesRecyclerView);
        gamesAdapter = new GamesAdapter(allMatches);
        rvGames.setLayoutManager(new LinearLayoutManager(this));
        rvGames.setAdapter(gamesAdapter);

        btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> finish());

        loadRelevantMatches();
    }

    private void loadRelevantMatches() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Consulta a la colección "horarios"
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

                        if (closestWeekend != null) {
                            Map<String, Object> weekendData = closestWeekend.getData();
                            if (weekendData != null) {
                                List<Map<String, Object>> matches = extractMatchesFromWeekend(weekendData);
                                Log.d("NextGamesActivity", "Partidos extraídos: " + matches.size());

                                if (matches != null && !matches.isEmpty()) {
                                    // Ordenar los partidos por fecha y hora, dejando los nulos al final
                                    Log.d("NextGamesActivity", "Ordenando partidos...");
                                    matches.sort((match1, match2) -> {
                                        try {
                                            // Obtener las fechas y horas de ambos partidos
                                            String fecha1 = (String) match1.get("fecha");
                                            String hora1 = (String) match1.get("hora");
                                            String fecha2 = (String) match2.get("fecha");
                                            String hora2 = (String) match2.get("hora");

                                            // Manejo explícito de casos nulos: si fecha o hora es nula, colócalo al final
                                            if (fecha1 == null && fecha2 == null) {
                                                return 0; // Ambos nulos, no cambia el orden
                                            } else if (fecha1 == null) {
                                                return 1; // Partido 1 es nulo, va después
                                            } else if (fecha2 == null) {
                                                return -1; // Partido 2 es nulo, va después
                                            }

                                            if (hora1 == null && hora2 == null) {
                                                return 0; // Ambos nulos, no cambia el orden
                                            } else if (hora1 == null) {
                                                return 1; // Partido 1 es nulo, va después
                                            } else if (hora2 == null) {
                                                return -1; // Partido 2 es nulo, va después
                                            }

                                            // Concatenar fecha y hora para comparación
                                            String dateTime1 = fecha1 + " " + hora1;
                                            String dateTime2 = fecha2 + " " + hora2;

                                            // Parsear las fechas y comparar
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                            Date matchDate1 = sdf.parse(dateTime1);
                                            Date matchDate2 = sdf.parse(dateTime2);

                                            if (matchDate1 != null && matchDate2 != null) {
                                                return matchDate1.compareTo(matchDate2); // Comparar fechas válidas
                                            } else if (matchDate1 == null) {
                                                return 1; // Partido 1 es nulo, va después
                                            } else {
                                                return -1; // Partido 2 es nulo, va después
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.e("NextGamesActivity", "Error al ordenar partidos", e);
                                            return 0; // En caso de error, no cambiar el orden
                                        }
                                    });
                                    Log.d("NextGamesActivity", "Partidos ordenados");

                                    allMatches.clear();
                                    allMatches.addAll(matches);
                                    gamesAdapter.notifyDataSetChanged();  // Actualizar la UI con los partidos ordenados
                                } else {
                                    showNoMatchMessage();  // Mostrar mensaje si no hay partidos
                                    Log.d("NextGamesActivity", "No se encontraron partidos para el fin de semana.");
                                }
                            }
                        } else {
                            showNoMatchMessage();  // Mostrar mensaje si no se encuentra ningún fin de semana relevante
                            Log.d("NextGamesActivity", "No se encontró ningún fin de semana relevante.");
                        }
                    } else {
                        showNoMatchMessage();  // Mostrar mensaje si la colección está vacía
                        Log.d("NextGamesActivity", "No se encontraron documentos en la colección 'horarios'.");
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    showNoMatchMessage();  // Mostrar mensaje en caso de error
                    Log.e("NextGamesActivity", "Error en la consulta de Firestore", e);
                });
    }



    private List<Map<String, Object>> extractMatchesFromWeekend(Map<String, Object> weekendData) {
        List<Map<String, Object>> matches = new ArrayList<>();
        Object partidosObj = weekendData.get("partidos");

        if (partidosObj instanceof Map) { // Verificar si "partidos" es un mapa
            Map<String, Object> partidosMap = (Map<String, Object>) partidosObj;

            // Recorrer el mapa "partidos"
            for (Map.Entry<String, Object> entry : partidosMap.entrySet()) {
                Object partidoObj = entry.getValue();
                if (partidoObj instanceof List) {
                    List<Map<String, Object>> partidoList = (List<Map<String, Object>>) partidoObj;

                    // Agregar los partidos a la lista
                    matches.addAll(partidoList);
                }
            }
        }

        return matches;
    }

    private void showNoMatchMessage() {
        // Mostrar un mensaje en la UI indicando que no hay partidos
        Log.d("NextGamesActivity", "No hay partidos disponibles para el fin de semana relevante.");
    }
}
