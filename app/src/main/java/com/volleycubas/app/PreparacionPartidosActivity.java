package com.volleycubas.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PreparacionPartidosActivity extends AppCompatActivity {

    private ArrayList<Jugador> jugadoresList = new ArrayList<>();
    private String teamId;
    private GridLayout preparacionContainer;
    private View cardView, editorView;
    private SetPreparacionAdapter setAdapter;
    private FirebaseFirestore db;
    private List<Map<String, List<String>>> listaSets = new ArrayList<>();
    private List<PreparacionPartido> listaPreparaciones = new ArrayList<>();
    private PreparacionPartido preparacionActual;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparacion_partidos);

        preparacionContainer = findViewById(R.id.preparacion_container);
        db = FirebaseFirestore.getInstance();

        cardView = LayoutInflater.from(this).inflate(R.layout.preparacion_card, preparacionContainer, false);
        editorView = LayoutInflater.from(this).inflate(R.layout.preparacion_editor, null);

        jugadoresList = getIntent().getParcelableArrayListExtra("jugadores");
        teamId = getIntent().getStringExtra("teamId");

        ImageView addPrepButton = findViewById(R.id.addPrepButton);
        addPrepButton.setOnClickListener(v -> agregarNuevaPreparacion());

        cargarPreparaciones();
    }

    private void cargarPreparaciones() {
        db.collection("equipos").document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listaPreparaciones.clear();
                        List<Map<String, Object>> preparaciones = (List<Map<String, Object>>) documentSnapshot.get("preparacion_partidos");
                        for (Map<String, Object> map : preparaciones) {
                            PreparacionPartido preparacion = new PreparacionPartido(
                                    (String) map.get("id"),
                                    (String) map.get("titulo"),
                                    (String) map.get("fecha"),
                                    (Map<String, List<String>>) map.get("sets"),
                                    (String) map.get("notas")
                            );
                            listaPreparaciones.add(preparacion);
                        }
                        actualizarUI();
                    }
                });
    }

    private void actualizarUI() {
        preparacionContainer.removeAllViews(); // Limpia las vistas anteriores

        for (PreparacionPartido preparacion : listaPreparaciones) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.preparacion_card, preparacionContainer, false);

            TextView tituloView = cardView.findViewById(R.id.preparacion_titulo);
            TextView fechaView = cardView.findViewById(R.id.preparacion_fecha);
            TextView notasView = cardView.findViewById(R.id.preparacion_notas);

            tituloView.setText(preparacion.getTitulo());
            fechaView.setText(preparacion.getFecha());

            String notas = preparacion.getNotas();
            if (notas.length() > 50) {
                notas = notas.substring(0, 50) + "...";
            }
            notasView.setText(notas);

            cardView.setOnClickListener(v -> abrirEditor(preparacion));
            preparacionContainer.addView(cardView);
        }
    }

    private void agregarNuevoSet() {
        Map<String, List<String>> nuevoSet = new HashMap<>();
        nuevoSet.put("Nuevo Set", new ArrayList<>());
        listaSets.add(nuevoSet);
        setAdapter.notifyDataSetChanged();
    }

    private void abrirEditor(PreparacionPartido preparacion) {
        if (editorView.getParent() != null) {
            ((ViewGroup) editorView.getParent()).removeView(editorView);
        }
        preparacionActual = preparacion;

        EditText editTitulo = editorView.findViewById(R.id.edit_preparacion_titulo);
        EditText editFecha = editorView.findViewById(R.id.edit_preparacion_fecha);
        EditText editNotas = editorView.findViewById(R.id.edit_preparacion_notas);
        ImageView addSetButton = editorView.findViewById(R.id.iv_addSets);
        RecyclerView rvSets = editorView.findViewById(R.id.rv_preparacion_sets);
        rvSets.setLayoutManager(new LinearLayoutManager(this));

        addSetButton.setOnClickListener(v -> agregarNuevoSet());

        listaSets = new ArrayList<>();
        preparacion.getSets().forEach((key, value) -> {
            Map<String, List<String>> modifiableMap = new HashMap<>();
            modifiableMap.put(key, new ArrayList<>(value));
            listaSets.add(modifiableMap);
        });

        List<String> jugadoresNombres = new ArrayList<>();

        for (Jugador jugador : jugadoresList) {
            jugadoresNombres.add(jugador.getNombre());
        }


        setAdapter = new SetPreparacionAdapter(this, listaSets, jugadoresNombres);
        rvSets.setAdapter(setAdapter);

        editTitulo.setText(preparacion.getTitulo());
        editFecha.setText(preparacion.getFecha());
        editNotas.setText(preparacion.getNotas());

        new AlertDialog.Builder(this)
                .setView(editorView)
                .setPositiveButton("GUARDAR", (dialog, which) -> actualizarPreparacion())
                .setNegativeButton("CANCELAR", null)
                .setNeutralButton("ELIMINAR", (dialog, which) -> eliminarPreparacion())
                .show();
    }

    private void agregarNuevaPreparacion() {
        if (editorView.getParent() != null) {
            ((ViewGroup) editorView.getParent()).removeView(editorView);
        }

        preparacionActual = new PreparacionPartido(
                UUID.randomUUID().toString(),
                "",
                "",
                new HashMap<>(),
                ""
        );

        EditText editTitulo = editorView.findViewById(R.id.edit_preparacion_titulo);
        EditText editFecha = editorView.findViewById(R.id.edit_preparacion_fecha);
        EditText editNotas = editorView.findViewById(R.id.edit_preparacion_notas);
        ImageView addSetButton = editorView.findViewById(R.id.iv_addSets);
        RecyclerView rvSets = editorView.findViewById(R.id.rv_preparacion_sets);
        rvSets.setLayoutManager(new LinearLayoutManager(this));

        listaSets = new ArrayList<>();
        List<String> jugadoresNombres = new ArrayList<>();
        for (Jugador jugador : jugadoresList) {
            jugadoresNombres.add(jugador.getNombre());
        }

        setAdapter = new SetPreparacionAdapter(this, listaSets, jugadoresNombres);
        rvSets.setAdapter(setAdapter);

        editTitulo.setText("");
        editFecha.setText("");
        editNotas.setText("");

        new AlertDialog.Builder(this)
                .setView(editorView)
                .setPositiveButton("GUARDAR", (dialog, which) -> guardarNuevaPreparacion())
                .setNegativeButton("CANCELAR", null)
                .show();
    }

    private void actualizarPreparacion() {
        if (editorView.getParent() != null) {
            ((ViewGroup) editorView.getParent()).removeView(editorView);
        }

        preparacionActual.setTitulo(((EditText) editorView.findViewById(R.id.edit_preparacion_titulo)).getText().toString());
        preparacionActual.setFecha(((EditText) editorView.findViewById(R.id.edit_preparacion_fecha)).getText().toString());
        preparacionActual.setNotas(((EditText) editorView.findViewById(R.id.edit_preparacion_notas)).getText().toString());

        Log.d("ActualizarPreparacion", "Preparación actual antes de actualizar sets: " + preparacionActual.toString());

        Map<String, List<String>> setsActualizados = new LinkedHashMap<>(); // ✅ Mantiene el orden

        for (Map<String, List<String>> set : listaSets) {
            for (Map.Entry<String, List<String>> entry : set.entrySet()) {
                String setKey = entry.getKey().equals("Nuevo Set") ? "Set " + (setsActualizados.size() + 1) : entry.getKey();

                setsActualizados.put(setKey, new ArrayList<>(entry.getValue())); // ✅ Se mantiene el orden de inserción

                Log.d("ActualizarPreparacion", "Guardando set: " + setKey + " -> " + entry.getValue());
            }
        }

        preparacionActual.setSets(setsActualizados);
        Log.d("ActualizarPreparacion", "Sets finales antes de guardar: " + setsActualizados);


        for (PreparacionPartido prep : listaPreparaciones) {
            Log.d("Lista preps", "Preparación en lista: " + prep.toString());
        }

        db.collection("equipos").document(teamId)
                .update("preparacion_partidos", listaPreparaciones)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ActualizarPreparacion", "Actualización en Firebase exitosa.");
                    actualizarUI();
                })
                .addOnFailureListener(e -> Log.e("ActualizarPreparacion", "Error al actualizar Firebase", e));
    }

    private void guardarNuevaPreparacion() {
        preparacionActual.setTitulo(((EditText) editorView.findViewById(R.id.edit_preparacion_titulo)).getText().toString());
        preparacionActual.setFecha(((EditText) editorView.findViewById(R.id.edit_preparacion_fecha)).getText().toString());
        preparacionActual.setNotas(((EditText) editorView.findViewById(R.id.edit_preparacion_notas)).getText().toString());

        Log.d("GuardarNuevaPreparacion", "Preparación nueva antes de actualizar sets: " + preparacionActual.toString());

        Map<String, List<String>> setsActualizados = new LinkedHashMap<>(); // ✅ Mantiene el orden

        for (Map<String, List<String>> set : listaSets) {
            for (Map.Entry<String, List<String>> entry : set.entrySet()) {
                String setKey = entry.getKey().equals("Nuevo Set") ? "Set " + (setsActualizados.size() + 1) : entry.getKey();

                setsActualizados.put(setKey, new ArrayList<>(entry.getValue())); // ✅ Se mantiene el orden de inserción

                Log.d("ActualizarPreparacion", "Guardando set: " + setKey + " -> " + entry.getValue());
            }
        }

        preparacionActual.setSets(setsActualizados);
        Log.d("ActualizarPreparacion", "Sets finales antes de guardar: " + setsActualizados);


        listaPreparaciones.add(preparacionActual);
        db.collection("equipos").document(teamId)
                .update("preparacion_partidos", listaPreparaciones)
                .addOnSuccessListener(aVoid -> {
                    Log.d("GuardarNuevaPreparacion", "Preparación guardada en Firebase correctamente.");
                    actualizarUI();
                })
                .addOnFailureListener(e -> Log.e("GuardarNuevaPreparacion", "Error al guardar en Firebase", e));
    }

    private void eliminarPreparacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Preparación")
                .setMessage("¿Estás seguro de que deseas eliminar esta preparación?")
                .setPositiveButton("ELIMINAR", (dialog, which) -> {
                    listaPreparaciones.remove(preparacionActual);

                    db.collection("equipos").document(teamId)
                            .update("preparacion_partidos", listaPreparaciones)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("EliminarPreparacion", "Preparación eliminada en Firebase correctamente.");
                                actualizarUI();
                            })
                            .addOnFailureListener(e -> Log.e("EliminarPreparacion", "Error al eliminar en Firebase", e));
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }
}