package com.volleycubas.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.volleycubas.app.R.id;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeamMenuActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    private String teamId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_menu);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager = findViewById(R.id.viewPager);

        teamId = getIntent().getStringExtra("teamId");

        if (teamId != null) {
            setupViewPager(teamId); // Configura el ViewPager con el teamId
            setupBottomNavigation(); // Sincroniza la navegación
            loadTeamData(teamId);    // Carga los datos del equipo
        } else {
            Toast.makeText(this, "Error: ID del equipo no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTeamData(String teamId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("equipos").document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Team team = documentSnapshot.toObject(Team.class);
                        if (team != null) {
                            setTitle(team.getNombre()); // Mostrar el nombre del equipo en la barra de título
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar los datos del equipo", Toast.LENGTH_SHORT).show();
                });
    }

    public void updateFragmentToMatchInProgress(Partido partido) {
        AdapterViewPager adapter = (AdapterViewPager) viewPager.getAdapter();
        if (adapter != null) {
            // Actualiza el estado en el adaptador
            adapter.setMatchInProgress(true, partido);

            // Reemplaza dinámicamente el fragmento en el ViewPager
            Fragment partidoEnCursoFragment = PartidoEnCursoFragment.newInstance(teamId, partido);
            adapter.replaceFragment(2, partidoEnCursoFragment); // 2 es la posición del fragmento del partido
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(2, true); // Cambia al nuevo fragmento dinámicamente
        }
    }


    private void setupViewPager(String teamId) {
        AdapterViewPager adapter = new AdapterViewPager(this, teamId);
        viewPager.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos").document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("partidoEnCurso")) {
                        Map<String, Object> partidoData = (Map<String, Object>) documentSnapshot.get("partidoEnCurso");
                        Partido partido = Partido.fromMap(partidoData);
                        adapter.setMatchInProgress(true, partido);
                    } else {
                        adapter.setMatchInProgress(false, null);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al cargar el estado del partido", e));
    }


    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_players) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.navigation_history) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.navigation_start_match) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.navigation_lineup) {
                viewPager.setCurrentItem(3);
            } else if (itemId == R.id.navigation_preparation) {
                viewPager.setCurrentItem(4);
            }
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_players);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_history);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_start_match);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_lineup);
                        break;
                    case 4:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_preparation);
                        break;
                }
            }
        });

        // Acción para el botón flotante
        findViewById(R.id.fabCenterButton).setOnClickListener(v -> {
            viewPager.setCurrentItem(2);
            bottomNavigationView.setSelectedItemId(R.id.navigation_start_match);
        });
    }

    public void resetToStartMatch() {
        if (viewPager != null && viewPager.getAdapter() instanceof AdapterViewPager) {
            AdapterViewPager adapter = (AdapterViewPager) viewPager.getAdapter();

            // Reinicia el estado de partido en curso
            adapter.setMatchInProgress(false, null);

            // Notifica cambios al adaptador
            adapter.notifyDataSetChanged();

            // Vuelve al fragmento inicial
            viewPager.setCurrentItem(1, false);
        }
    }
}
