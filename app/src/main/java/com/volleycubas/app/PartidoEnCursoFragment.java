package com.volleycubas.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class PartidoEnCursoFragment extends Fragment {

    private static final String ARG_TEAM_ID = "teamId";
    private static final String ARG_PARTIDO = "partido";

    private String teamId;
    private String teamName;
    private Partido partido;

    private TextView nameTeamA;
    private TextView nameTeamB;

    private TextView scoreTeamA;
    private TextView scoreTeamB;

    private TextView setsScoreTeamA;
    private TextView setsScoreTeamB;
    private EditText notas;

    private int pointsTeamA = 0;
    private int pointsTeamB = 0;
    private int setsTeamA = 0;
    private int setsTeamB = 0;

    public static PartidoEnCursoFragment newInstance(String teamId, Partido partido) {
        PartidoEnCursoFragment fragment = new PartidoEnCursoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEAM_ID, teamId);
        args.putSerializable(ARG_PARTIDO, partido);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            teamId = getArguments().getString(ARG_TEAM_ID);
            partido = (Partido) getArguments().getSerializable(ARG_PARTIDO);
        }

        Button buttonFinishSet = view.findViewById(R.id.btn_finish_set);
        Button buttonFinishMatch = view.findViewById(R.id.btn_finish_match);

        nameTeamA = view.findViewById(R.id.team_a_name);
        nameTeamB = view.findViewById(R.id.team_b_name);

        setsScoreTeamA = view.findViewById(R.id.set_team_a);
        setsScoreTeamB = view.findViewById(R.id.set_team_b);

        scoreTeamA = view.findViewById(R.id.score_team_a);
        scoreTeamB = view.findViewById(R.id.score_team_b);

        notas = view.findViewById(R.id.tv_notas);

        if (teamId != null) {
            cargarNombreEquipo();
            cargarPartidoDesdeFirestore();
        }

        // Configurar clic en el marcador del equipo A
        scoreTeamA.setOnClickListener(v -> {
            pointsTeamA++;
            partido.agregarPuntoAFavor(); // Añadir punto a favor
            scoreTeamA.setText(String.format("%02d", pointsTeamA));
            verificarCondicionesDeSet();
            guardarPartido(); // Guardar el estado actualizado
        });

        // Configurar clic en el marcador del equipo B
        scoreTeamB.setOnClickListener(v -> {
            pointsTeamB++;
            partido.agregarPuntoEnContra(); // Añadir punto en contra
            scoreTeamB.setText(String.format("%02d", pointsTeamB));
            verificarCondicionesDeSet();
            guardarPartido(); // Guardar el estado actualizado
        });

        buttonFinishMatch.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Terminar Partido")
                    .setMessage("¿Estás seguro de que quieres terminar el partido? Esto moverá el partido al historial.")
                    .setPositiveButton("Terminar", (dialog, which) -> finalizarPartido())
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        buttonFinishSet.setOnClickListener(v -> {
            // Determinar quién tiene más puntos actualmente
            boolean equipoAGanador = pointsTeamA > pointsTeamB;
            String equipoGanador = equipoAGanador ? teamName : partido.getRival();

            // Mostrar el AlertDialog
            new AlertDialog.Builder(requireContext())
                    .setTitle("Terminar Set")
                    .setMessage("¿Quieres guardar el set a favor de " + equipoGanador + "?")
                    .setPositiveButton("Guardar", (dialog, which) -> finalizarSet(equipoAGanador))
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        notas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesitas implementar esto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (partido != null) {
                    partido.setNotas(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                guardarPartido();
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_partido_en_curso, container, false);
    }

    private void verificarCondicionesDeSet() {
        if ((pointsTeamA >= 25 || pointsTeamB >= 25) &&
                Math.abs(pointsTeamA - pointsTeamB) >= 2) {

            boolean equipoAGanador = pointsTeamA > pointsTeamB;
            String equipoGanador = equipoAGanador ? teamName : partido.getRival();

            // Mostrar diálogo de confirmación
            new AlertDialog.Builder(requireContext())
                    .setTitle("Set finalizado")
                    .setMessage("¿Quieres guardar el set a favor de " + equipoGanador + "?")
                    .setPositiveButton("Guardar", (dialog, which) -> finalizarSet(equipoAGanador))
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }


    private void finalizarSet(boolean equipoAGanador) {
        // Sumar el set al equipo correspondiente
        if (equipoAGanador) {
            setsTeamA++;
            setsScoreTeamA.setText(String.valueOf(setsTeamA));
            partido.setSetsAFavor(setsTeamA); // Actualizar en el objeto Partido
        } else {
            setsTeamB++;
            setsScoreTeamB.setText(String.valueOf(setsTeamB));
            partido.setSetsEnContra(setsTeamB); // Actualizar en el objeto Partido
        }

        // Añadir "X" a la secuencia de puntos
        partido.finalizarSet();

        // Reiniciar los puntos para el nuevo set
        reiniciarPuntos();

        // Guardar el estado actualizado en Firestore
        guardarPartido();
    }

    private void finalizarPartido() {
        if (teamId == null) {
            Log.e("PartidoEnCursoFragment", "El teamId es nulo. No se puede finalizar el partido.");
            return;
        }

        // Establecer la fecha actual
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaActual = dateFormat.format(new Date());
        partido.setFecha(fechaActual);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Agregar el partido a historial_partidos y eliminar partidoEnCurso
        db.collection("equipos")
                .document(teamId)
                .update(
                        "historial_partidos", FieldValue.arrayUnion(partido.toMap()), // Agregar a historial
                        "partidoEnCurso", FieldValue.delete() // Eliminar completamente partidoEnCurso
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("PartidoEnCursoFragment", "Partido finalizado y movido a historial_partidos.");
                    navigateToStartMatch(); // Volver al inicio
                })
                .addOnFailureListener(e -> {
                    Log.e("PartidoEnCursoFragment", "Error al finalizar partido: " + e.getMessage());
                });
    }


    private void navigateToStartMatch() {
        StartMatchFragment startMatchFragment = StartMatchFragment.newInstance(teamId);
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, startMatchFragment); // Cambia 'R.id.fragment_container' al ID de tu contenedor de fragmentos
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }


    private void reiniciarPuntos() {
        pointsTeamA = 0;
        pointsTeamB = 0;
        scoreTeamA.setText(String.format("%02d", pointsTeamA));
        scoreTeamB.setText(String.format("%02d", pointsTeamB));
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
                        if (teamName != null) {
                            nameTeamA.setText(teamName);
                        } else {
                            Log.w("PartidoEnCursoFragment", "El nombre del equipo no se encontró en Firestore.");
                        }
                    } else {
                        Log.e("PartidoEnCursoFragment", "No se encontró el documento del equipo en Firestore.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PartidoEnCursoFragment", "Error al cargar el nombre del equipo: " + e.getMessage());
                });
    }

    private void cargarPartidoDesdeFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos")
                .document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Cargar los datos del partido en curso
                        Map<String, Object> partidoEnCursoMap = (Map<String, Object>) documentSnapshot.get("partidoEnCurso");
                        if (partidoEnCursoMap != null) {
                            partido = Partido.fromMap(partidoEnCursoMap);

                            // Reflejar datos del partido
                            nameTeamB.setText(partido.getRival());

                            // Obtener la secuencia de puntos del partido
                            String puntosSecuencia = partido.getPuntosSecuencia();

                            // Calcular puntos del set actual
                            int lastXIndex = puntosSecuencia.lastIndexOf('X');
                            String setActual = lastXIndex == -1 ? puntosSecuencia : puntosSecuencia.substring(lastXIndex + 1);

                            pointsTeamA = countOccurrences(setActual, 'A');
                            pointsTeamB = countOccurrences(setActual, 'B');

                            // Actualizar los sets ganados
                            setsTeamA = partido.getSetsAFavor();
                            setsTeamB = partido.getSetsEnContra();

                            // Reflejar datos en la interfaz
                            scoreTeamA.setText(String.format("%02d", pointsTeamA));
                            scoreTeamB.setText(String.format("%02d", pointsTeamB));
                            setsScoreTeamA.setText(String.valueOf(setsTeamA));
                            setsScoreTeamB.setText(String.valueOf(setsTeamB));

                        } else {
                            Log.w("PartidoEnCursoFragment", "No se encontró el campo 'partidoEnCurso' en Firestore.");
                        }
                    } else {
                        Log.e("PartidoEnCursoFragment", "No se encontró el documento del equipo en Firestore.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PartidoEnCursoFragment", "Error al cargar partido desde Firestore: " + e.getMessage());
                });
    }

    private void guardarPartido() {
        if (teamId == null) {
            Log.e("PartidoEnCursoFragment", "El teamId es nulo. No se puede guardar el partido.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos")
                .document(teamId)
                .update("partidoEnCurso", partido.toMap()) // Actualizar el campo `partidoEnCurso`
                .addOnSuccessListener(aVoid -> {
                    Log.d("PartidoEnCursoFragment", "Partido actualizado correctamente.");
                })
                .addOnFailureListener(e -> {
                    Log.e("PartidoEnCursoFragment", "Error al guardar partido: " + e.getMessage());
                });
    }

    private int countOccurrences(String text, char character) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == character) {
                count++;
            }
        }
        return count;
    }
}
