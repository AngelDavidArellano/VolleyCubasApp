package com.volleycubas.app;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class AdapterViewPager extends FragmentStateAdapter {
    private final String teamId;
    private boolean isMatchInProgress = false;
    private Partido partidoActual;

    public AdapterViewPager(@NonNull FragmentActivity fragmentActivity, String teamId) {
        super(fragmentActivity);
        this.teamId = teamId;

        // Verificar si hay partido en curso desde Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos").document(teamId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> partidoEnCursoMap = (Map<String, Object>) documentSnapshot.get("partidoEnCurso");
                        if (partidoEnCursoMap != null) {
                            Partido partido = Partido.fromMap(partidoEnCursoMap);
                            // Procesa el partido si existe
                        } else {
                            Log.d("AdapterViewPager", "No hay partido en curso. Campo 'partidoEnCurso' es nulo.");
                            // Lógica alternativa si no hay partido en curso
                        }
                    } else {
                        Log.e("AdapterViewPager", "El documento del equipo no existe.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdapterViewPager", "Error al cargar datos del equipo: " + e.getMessage());
                });

    }

    private final SparseArray<Fragment> fragmentList = new SparseArray<>();

    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (fragmentList.get(position) != null) {
            return fragmentList.get(position); // Devuelve el fragmento ya creado
        }

        switch (position) {
            case 0:
                fragment = PlayersFragment.newInstance(teamId);
                break;
            case 1:
                fragment = HistoryFragment.newInstance(teamId);
                break;
            case 2:
                fragment = isMatchInProgress
                        ? PartidoEnCursoFragment.newInstance(teamId, partidoActual)
                        : StartMatchFragment.newInstance(teamId);
                break;
            case 3:
                fragment = TrainingFragment.newInstance(teamId);
                break;
            case 4:
                fragment = StatsFragment.newInstance(teamId);
                break;
            default:
                throw new IllegalArgumentException("Posición inválida: " + position);
        }
        fragmentList.put(position, fragment);
        return fragment;
    }

    public void replaceFragment(int position, Fragment newFragment) {
        fragmentList.put(position, newFragment); // Reemplaza el fragmento dinámicamente
        notifyDataSetChanged(); // Notifica al ViewPager que los datos han cambiado
    }


    public void setMatchInProgress(boolean isMatchInProgress, Partido partido) {
        this.isMatchInProgress = isMatchInProgress;
        this.partidoActual = partido;

        if (!isMatchInProgress) {
            Fragment startMatchFragment = StartMatchFragment.newInstance(teamId);
            replaceFragment(2, startMatchFragment); // 2 es la posición del fragmento
        }

        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return 5; // Cantidad de fragmentos en el ViewPager
    }
}