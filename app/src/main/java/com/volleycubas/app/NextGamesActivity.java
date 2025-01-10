package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NextGamesActivity extends AppCompatActivity {

    private RecyclerView rvGames;
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

        loadAllMatches();
    }

    private void loadAllMatches() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Consulta a la colección "horarios"
        db.collection("horarios")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        allMatches.clear();

                        // Iterar por los documentos
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Map<String, Object> weekendData = doc.getData();
                            if (weekendData != null) {
                                // Extraer los partidos del fin de semana
                                List<Map<String, Object>> matches = extractMatchesFromWeekend(weekendData);
                                if (matches != null && !matches.isEmpty()) {
                                    allMatches.addAll(matches); // Agregar partidos a la lista
                                }
                            }
                        }

                        // Actualizar la UI con todos los partidos encontrados
                        if (!allMatches.isEmpty()) {
                            gamesAdapter.notifyDataSetChanged(); // Actualizar el RecyclerView
                        } else {
                            showNoMatchMessage(); // Mostrar mensaje si no hay partidos
                        }
                    } else {
                        showNoMatchMessage(); // Mostrar mensaje si la colección está vacía
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    showNoMatchMessage(); // Mostrar mensaje en caso de error
                });
    }

    private List<Map<String, Object>> extractMatchesFromWeekend(Map<String, Object> weekendData) {
        List<Map<String, Object>> matches = new ArrayList<>();
        List<Map<String, Object>> partidos = (List<Map<String, Object>>) weekendData.get("partidos");

        if (partidos != null) {
            matches.addAll(partidos); // Agregar todos los partidos sin filtrar
        }
        return matches;
    }

    private void showNoMatchMessage() {
        // Mostrar un mensaje en la UI indicando que no hay partidos
        Log.d("NextGamesActivity", "No hay partidos disponibles para la fecha seleccionada.");
    }
}
