package com.volleycubas.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private static final String ARG_TEAM_ID = "teamId";
    private String teamId;

    private View loadingOverlay;
    private ImageView loadingIndicator;

    private RecyclerView recyclerView;
    private HistorialAdapter adapter;
    private EditText searchHistory;
    private List<Partido> historialPartidos = new ArrayList<>();
    private List<Partido> filteredPartidos = new ArrayList<>();


    public static HistoryFragment newInstance(String teamId) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEAM_ID, teamId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_historial);
        searchHistory = view.findViewById(R.id.search_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadingOverlay = view.findViewById(R.id.loadingOverlay_historial);
        loadingIndicator = view.findViewById(R.id.loadingGif_historial);

        if (getArguments() != null) {
            teamId = getArguments().getString(ARG_TEAM_ID);
            cargarHistorialPartidos();
        }

        // Configurar el filtro de búsqueda
        searchHistory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarHistorial(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    @Override
    public void onResume() {
        cargarHistorialPartidos();
        super.onResume();
    }

    private void showLoadingIndicator() {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingOverlay.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void cargarHistorialPartidos() {
        showLoadingIndicator();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos").document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    hideLoadingIndicator();

                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> partidosMap = (List<Map<String, Object>>) documentSnapshot.get("historial_partidos");
                        if (partidosMap != null) {
                            historialPartidos.clear();
                            for (Map<String, Object> map : partidosMap) {
                                Partido partido = Partido.fromMap(map);
                                if (partido != null) {
                                    historialPartidos.add(partido);
                                }
                            }

                            // Ordenar los partidos por fecha (más reciente primero)
                            historialPartidos.sort((p1, p2) -> {
                                try {
                                    // Formato esperado: "dd/MM/yyyy"
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date date1 = dateFormat.parse(p1.getFecha());
                                    Date date2 = dateFormat.parse(p2.getFecha());
                                    return date2.compareTo(date1); // Orden descendente
                                } catch (Exception e) {
                                    Log.e("HistorialFragment", "Error al parsear fechas: " + e.getMessage());
                                    return 0; // Si hay error, no cambiar el orden
                                }
                            });

                            // Actualizar lista filtrada y configurar el adaptador
                            filteredPartidos.clear();
                            filteredPartidos.addAll(historialPartidos);
                            adapter = new HistorialAdapter(filteredPartidos, partido -> {}, teamId);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    Log.e("HistorialFragment", "Error al cargar el historial: " + e.getMessage());
                });
    }

    private void filtrarHistorial(String query) {
        if (adapter == null) return; // Asegúrate de que el adaptador no sea nulo

        filteredPartidos.clear();
        String queryNormalizado = normalizarTexto(query);

        if (queryNormalizado.isEmpty()) {
            filteredPartidos.addAll(historialPartidos);
        } else {
            for (Partido partido : historialPartidos) {
                String resultado = "(" + partido.getSetsAFavor() + " - " + partido.getSetsEnContra() + ")";
                String rival = normalizarTexto(partido.getRival());
                resultado = normalizarTexto(resultado);
                String fecha = normalizarTexto(partido.getFecha());

                if (rival.contains(queryNormalizado) ||
                        resultado.contains(queryNormalizado) ||
                        fecha.contains(queryNormalizado)) {
                    filteredPartidos.add(partido);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }


    private String normalizarTexto(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // Eliminar marcas diacríticas (tildes)
                .toLowerCase();           // Convertir a minúsculas
    }
}
