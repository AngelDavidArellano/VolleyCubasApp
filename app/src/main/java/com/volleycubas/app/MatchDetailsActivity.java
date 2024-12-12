package com.volleycubas.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_PARTIDO = "partido";
    private static final String EXTRA_ID = "teamId";

    private String teamId;
    private String teamName;
    private Partido partido;
    private TextView tvSetsTeamA, tvSetsTeamB, tvResultado, tvRivalName, tvLocalName, tvFecha, tvNotas;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private View color_line;


    public static void start(Context context, Partido partido, String teamId) {
        Intent intent = new Intent(context, MatchDetailsActivity.class);
        intent.putExtra("partido", partido);
        intent.putExtra("teamId", teamId);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);

        // Obtener los datos del partido
        partido = (Partido) getIntent().getSerializableExtra(EXTRA_PARTIDO);
        teamId = (String) getIntent().getSerializableExtra(EXTRA_ID);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        color_line = findViewById(R.id.color_line);

        // Inicializar vistas
        tvSetsTeamA = findViewById(R.id.tv_sets_score_teamA);
        tvSetsTeamB = findViewById(R.id.tv_sets_score_teamB);
        tvResultado = findViewById(R.id.tv_resultado);
        tvLocalName = findViewById(R.id.tv_teamA_name);
        tvRivalName = findViewById(R.id.tv_teamB_name);
        tvNotas = findViewById(R.id.tv_notas);
        tvFecha = findViewById(R.id.tv_fecha_fase);
        tabLayout = findViewById(R.id.tab_layout_sets);
        viewPager = findViewById(R.id.view_pager_sets);

        if (partido.getSetsAFavor() > partido.getSetsEnContra()) {
            color_line.setBackgroundResource(R.color.blue);
        } else {
            color_line.setBackgroundResource(R.color.red);
        }

        // Configurar vistas
        cargarNombreEquipo();
    }

    private void configurarDatosPartido() {
        if (partido == null) return;

        tvSetsTeamA.setText(String.valueOf(partido.getSetsAFavor()));
        tvSetsTeamB.setText(String.valueOf(partido.getSetsEnContra()));
        tvResultado.setText(partido.getSetsAFavor() > partido.getSetsEnContra() ? "Victoria" : "Derrota");
        tvLocalName.setText(teamName);
        tvRivalName.setText(partido.getRival());
        tvNotas.setText(partido.getNotas());
        tvFecha.setText(partido.getFase() + "  -  " + partido.getFecha());

        // Configurar tabs y ViewPager
        configurarViewPager();
    }

    private void cargarNombreEquipo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos")
                .document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Cargar el nombre del equipo propio directamente del campo "nombre"
                        teamName = documentSnapshot.getString("nombre");
                        Log.d("PartidoEnCursoFragment", "Nombre recuperado: " + teamName);
                        configurarDatosPartido();
                    } else {
                        Log.e("PartidoEnCursoFragment", "No se encontró el documento del equipo en Firestore.");
                        configurarDatosPartido();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PartidoEnCursoFragment", "Error al cargar el nombre del equipo: " + e.getMessage());
                });
    }

    private void configurarViewPager() {
        List<String> sets = obtenerSets();
        MatchDetailPagerAdapter adapter = new MatchDetailPagerAdapter(sets, partido.getRival(), teamName);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText("Set " + (position + 1))).attach();
    }

    private List<String> obtenerSets() {
        List<String> sets = new ArrayList<>();
        String[] secuencias = partido.getPuntosSecuencia().split("X");
        for (String secuencia : secuencias) {
            sets.add(secuencia.trim());
        }
        return sets;
    }
}
