package com.volleycubas.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClasificacionesActivity extends AppCompatActivity {

    private Spinner spinnerLigas;
    private RecyclerView recyclerClasificacion;
    private ClasificacionAdapter adapter;
    private TextView tvLeagueName;
    private ImageView loadingGifMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clasificaciones);

        loadingGifMain = findViewById(R.id.loadingGif_main);

        spinnerLigas = findViewById(R.id.spinnerLigas);
        recyclerClasificacion = findViewById(R.id.recyclerClasificacion);
        tvLeagueName = findViewById(R.id.tvLeagueName);
        tvLeagueName.setVisibility(View.GONE);

        recyclerClasificacion.setLayoutManager(new LinearLayoutManager(this));

        cargarLigas();
        iniciarRotacion();

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void iniciarRotacion() {
        ObjectAnimator rotacion = ObjectAnimator.ofFloat(loadingGifMain, "rotation", 0f, 360f);
        rotacion.setDuration(100); // 15 segundos
        rotacion.setRepeatCount(ValueAnimator.INFINITE); // Repite la animación infinitamente
        rotacion.setInterpolator(new android.view.animation.LinearInterpolator()); // Rotación suave
        rotacion.start();
    }

    private void cargarLigas() {
        RetrofitClient.getApiService().getLigas().enqueue(new Callback<List<Liga>>() {
            @Override
            public void onResponse(Call<List<Liga>> call, Response<List<Liga>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Liga> listaLigas = response.body();
                    ArrayAdapter<Liga> adapter = new ArrayAdapter<>(
                            ClasificacionesActivity.this,
                            R.layout.spinner_selected_item,
                            listaLigas
                    );
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerLigas.setAdapter(adapter);
                } else {
                    Toast.makeText(ClasificacionesActivity.this, "Error al obtener las ligas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Liga>> call, Throwable t) {
                Toast.makeText(ClasificacionesActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });

        spinnerLigas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Liga ligaSeleccionada = (Liga) parent.getItemAtPosition(position);
                obtenerClasificacion(ligaSeleccionada.getGrupoId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void obtenerClasificacion(String grupoId) {
        RetrofitClient.getApiService().getClasificacion(grupoId).enqueue(new Callback<List<Clasificacion>>() {
            @Override
            public void onResponse(Call<List<Clasificacion>> call, Response<List<Clasificacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new ClasificacionAdapter(response.body());

                    Clasificacion primeraClasificacion = response.body().get(0);
                    tvLeagueName.setVisibility(View.VISIBLE);
                    tvLeagueName.setText(primeraClasificacion.getLigaNombre());

                    recyclerClasificacion.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Clasificacion>> call, Throwable t) {
                Toast.makeText(ClasificacionesActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
