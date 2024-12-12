package com.volleycubas.app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class StartMatchFragment extends Fragment {

    private static final String ARG_TEAM_ID = "teamId";
    private String teamId;
    private EditText etRival, etFase;
    private RadioGroup radioGroupSets;
    private Button btnStartMatch;


    public static StartMatchFragment newInstance(String teamId) {
        StartMatchFragment fragment = new StartMatchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEAM_ID, teamId);
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_match, container, false);

        // Obtener el teamId desde argumentos
        if (getArguments() != null) {
            teamId = getArguments().getString(ARG_TEAM_ID);
        }

        // Inicializar vistas
        etRival = view.findViewById(R.id.etRival);
        etFase = view.findViewById(R.id.etFase);
        radioGroupSets = view.findViewById(R.id.radioGroupSets);
        btnStartMatch = view.findViewById(R.id.btnStartMatch);

        // Configurar botón "Comenzar Partido"
        btnStartMatch.setOnClickListener(v -> startMatch());

        return view;
    }

    private void startMatch() {
        String rival = etRival.getText().toString().trim();
        String fase = etFase.getText().toString().trim();
        int selectedSetOption = radioGroupSets.getCheckedRadioButtonId();

        // Validar entradas
        if (rival.isEmpty() || fase.isEmpty() || selectedSetOption == -1) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int sets = (selectedSetOption == R.id.rbBestOf3) ? 3 : 5;

        // Crear un nuevo partido
        String partidoId = UUID.randomUUID().toString();
        Partido partido = new Partido(partidoId, rival, fase, sets);

        // Guardar en Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos").document(teamId)
                .update("partidoEnCurso", partido.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Partido creado", Toast.LENGTH_SHORT).show();
                    // Notificar a la actividad
                    if (getActivity() instanceof TeamMenuActivity) {
                        ((TeamMenuActivity) getActivity()).updateFragmentToMatchInProgress(partido);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar partido: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
