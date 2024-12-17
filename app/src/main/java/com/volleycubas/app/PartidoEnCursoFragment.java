package com.volleycubas.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PartidoEnCursoFragment extends Fragment {

    private static final String ARG_TEAM_ID = "teamId";
    private static final String ARG_PARTIDO = "partido";

    private String teamId, teamName;
    private Partido partido;

    private ImageView undoButton, posesionTeamA, posesionTeamB;

    private TextView nameTeamA, nameTeamB, scoreTeamA, scoreTeamB,
            timeoutButtonA, timeoutButtonB, setsScoreTeamA, setsScoreTeamB;

    private TextView player1, player1_name, player2, player2_name, player3, player3_name,
            player4, player4_name, player5, player5_name, player6, player6_name;

    private EditText notas;

    private int pointsTeamA = 0;
    private int pointsTeamB = 0;
    private int setsTeamA = 0;
    private int setsTeamB = 0;

    private int timeoutsTeamA = 0;
    private int timeoutsTeamB = 0;

    private boolean posesionSaque = true;

    private List<Jugador> jugadoresList = new ArrayList<>();

    private ArrayList<TextView> playerList = new ArrayList<>();
    private ArrayList<TextView> playerNameList = new ArrayList<>();
    private HashMap<TextView, Float[]> posicionesIniciales = new HashMap<>();

    private ArrayList<Accion> historialAcciones = new ArrayList<>();


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

        undoButton = view.findViewById(R.id.undo_button);

        posesionTeamA = view.findViewById(R.id.posesionTeamA);
        posesionTeamB = view.findViewById(R.id.posesionTeamB);

        timeoutButtonA = view.findViewById(R.id.timeout_button_team_a);
        timeoutButtonB = view.findViewById(R.id.timeout_button_team_b);

        nameTeamA = view.findViewById(R.id.team_a_name);
        nameTeamB = view.findViewById(R.id.team_b_name);

        setsScoreTeamA = view.findViewById(R.id.set_team_a);
        setsScoreTeamB = view.findViewById(R.id.set_team_b);

        scoreTeamA = view.findViewById(R.id.score_team_a);
        scoreTeamB = view.findViewById(R.id.score_team_b);

        player1 = view.findViewById(R.id.player1);
        player1_name = view.findViewById(R.id.player1_name);

        player2 = view.findViewById(R.id.player2);
        player2_name = view.findViewById(R.id.player2_name);

        player3 = view.findViewById(R.id.player3);
        player3_name = view.findViewById(R.id.player3_name);

        player4 = view.findViewById(R.id.player4);
        player4_name = view.findViewById(R.id.player4_name);

        player5 = view.findViewById(R.id.player5);
        player5_name = view.findViewById(R.id.player5_name);

        player6 = view.findViewById(R.id.player6);
        player6_name = view.findViewById(R.id.player6_name);

        llenarArrayListJugadoresView();
        guardarPosicionesIniciales();

        cargarHistorialDesdeSharedPreferences();
        cargarJugadoresDesdeSharedPreferences();

        notas = view.findViewById(R.id.tv_notas);

        if (teamId != null) {
            cargarNombreEquipoDesdeFirestore();
            cargarJugadoresDesdeFirestore();
            cargarPartidoDesdeFirestore();
        }

        // Configurar clic en el marcador del equipo A
        scoreTeamA.setOnClickListener(v -> {
            pointsTeamA++;
            if (!posesionSaque) {
                posesionSaque = true;
                historialAcciones.add(Accion.ROTACION);
                rotarJugadores();
            } else {
                historialAcciones.add(Accion.PUNTO_A);
            }
            actualizarIndicadorSaque();
            partido.agregarPuntoAFavor();
            scoreTeamA.setText(String.format("%02d", pointsTeamA));
            verificarCondicionesDeSet();
            guardarPartido();
            guardarHistorialEnSharedPreferences();
        });

        // Configurar clic en el marcador del equipo B
        scoreTeamB.setOnClickListener(v -> {
            historialAcciones.add(Accion.PUNTO_B);

            pointsTeamB++;
            posesionSaque = false;

            actualizarIndicadorSaque();
            partido.agregarPuntoEnContra();
            scoreTeamB.setText(String.format("%02d", pointsTeamB));
            verificarCondicionesDeSet();
            guardarPartido();
            guardarHistorialEnSharedPreferences();
        });

        timeoutButtonA.setOnClickListener(v -> {
            historialAcciones.add(Accion.TIMEOUT_A);

            timeoutsTeamA++;
            partido.agregarTimeoutAFavor(); // Usa el método encapsulado
            guardarPartido(); // Guardar cambios en Firestore
            actualizarTimeoutsUI();
            guardarHistorialEnSharedPreferences();
        });

        timeoutButtonB.setOnClickListener(v -> {
            historialAcciones.add(Accion.TIMEOUT_B);

            timeoutsTeamB++;
            partido.agregarTimeoutEnContra(); // Usa el método encapsulado
            guardarPartido(); // Guardar cambios en Firestore
            actualizarTimeoutsUI();
            guardarHistorialEnSharedPreferences();
        });

        buttonFinishMatch.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Terminar Partido")
                    .setMessage("¿Estás seguro de que quieres terminar el partido? Esto moverá el partido al historial.")
                    .setPositiveButton("Terminar", (dialog, which) -> {
                        finalizarPartido();
                        navigateToStartMatch();
                    })
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

        undoButton.setOnClickListener(v -> {
            if (!historialAcciones.isEmpty()) {
                Accion ultimaAccion = historialAcciones.remove(historialAcciones.size() - 1);

                switch (ultimaAccion) {
                    case PUNTO_A:
                        partido.eliminarUltimoPunto();
                        pointsTeamA--;
                        scoreTeamA.setText(String.format("%02d", pointsTeamA));
                        break;

                    case PUNTO_B:
                        partido.eliminarUltimoPunto();
                        pointsTeamB--;
                        scoreTeamB.setText(String.format("%02d", pointsTeamB));
                        break;

                    case ROTACION:
                        rotarJugadoresInvertdo();
                        pointsTeamA--;
                        partido.eliminarUltimoPunto();
                        posesionSaque = false;
                        scoreTeamA.setText(String.format("%02d", pointsTeamA));
                        break;

                    case TIMEOUT_A:
                        timeoutsTeamA--;
                        timeoutButtonA.setText(timeoutsTeamA);
                        partido.eliminarUltimoTimeout();
                        break;

                    case TIMEOUT_B:
                        timeoutsTeamB--;
                        timeoutButtonB.setText(timeoutsTeamB);
                        partido.eliminarUltimoTimeout();
                        break;
                }

                actualizarIndicadorSaque();
                guardarPartido();
                guardarHistorialEnSharedPreferences();
            } else {
                Log.d("Undo", "No hay acciones para deshacer");
            }
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

        player1.setOnLongClickListener(v -> {
            mostrarSeleccionJugador(player1, player1_name);
            return true;
        });

        player2.setOnLongClickListener(v -> {
            mostrarSeleccionJugador(player2, player2_name);
            return true;
        });

        player3.setOnLongClickListener(v -> {
            mostrarSeleccionJugador(player3, player3_name);
            return true;
        });

        player4.setOnLongClickListener(v -> {
            mostrarSeleccionJugador(player4, player4_name);
            return true;
        });

        player5.setOnLongClickListener(v -> {
            mostrarSeleccionJugador(player5, player5_name);
            return true;
        });

        player6.setOnLongClickListener(v -> {
            mostrarSeleccionJugador(player6, player6_name);
            return true;
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_partido_en_curso, container, false);
    }

    private void rotarJugadores() {
        float offsetX = 275f; // Ajusta el desplazamiento horizontal (positivo para derecha)
        float offsetX_null = 0f;  // Sin desplazamiento horizontal
        float offsetY = 275f;  // Ajusta el desplazamiento vertical
        float offsetY_null = 0f;  // Sin desplazamiento vertical

        // Simula movimiento con animación y actualiza el texto al finalizar
        animarYActualizar(playerList.get(0), playerList.get(5).getText().toString(),
                playerNameList.get(0), playerNameList.get(5).getText().toString(),
                -offsetX, offsetY_null, null);
        animarYActualizar(playerList.get(1), playerList.get(0).getText().toString(),
                playerNameList.get(1), playerNameList.get(0).getText().toString(),
                -offsetX, offsetY_null, null);
        animarYActualizar(playerList.get(2), playerList.get(1).getText().toString(),
                playerNameList.get(2), playerNameList.get(1).getText().toString(),
                offsetX_null, -offsetY, null);
        animarYActualizar(playerList.get(3), playerList.get(2).getText().toString(),
                playerNameList.get(3), playerNameList.get(2).getText().toString(),
                offsetX, offsetY_null, null);
        animarYActualizar(playerList.get(4), playerList.get(3).getText().toString(),
                playerNameList.get(4), playerNameList.get(3).getText().toString(),
                offsetX, offsetY_null, null);
        animarYActualizar(playerList.get(5), playerList.get(4).getText().toString(),
                playerNameList.get(5), playerNameList.get(4).getText().toString(),
                offsetX_null, offsetY, this::guardarJugadoresEnSharedPreferences);

        guardarJugadoresEnSharedPreferences();
    }

    private void rotarJugadoresInvertdo() {
        float offsetX = 275f; // Ajusta el desplazamiento horizontal (positivo para derecha)
        float offsetX_null = 0f;  // Sin desplazamiento horizontal
        float offsetY = 275f;  // Ajusta el desplazamiento vertical
        float offsetY_null = 0f;  // Sin desplazamiento vertical

        // Simula movimiento con animación y actualiza el texto al finalizar
        animarYActualizar(playerList.get(0), playerList.get(1).getText().toString(),
                playerNameList.get(0), playerNameList.get(1).getText().toString(),
                offsetX_null, -offsetY, null);
        animarYActualizar(playerList.get(1), playerList.get(2).getText().toString(),
                playerNameList.get(1), playerNameList.get(2).getText().toString(),
                offsetX, offsetY_null, null);
        animarYActualizar(playerList.get(2), playerList.get(3).getText().toString(),
                playerNameList.get(2), playerNameList.get(3).getText().toString(),
                offsetX, offsetY_null, null);
        animarYActualizar(playerList.get(3), playerList.get(4).getText().toString(),
                playerNameList.get(3), playerNameList.get(4).getText().toString(),
                offsetX_null, offsetY, null);
        animarYActualizar(playerList.get(4), playerList.get(5).getText().toString(),
                playerNameList.get(4), playerNameList.get(5).getText().toString(),
                -offsetX, offsetY_null, null);
        animarYActualizar(playerList.get(5), playerList.get(0).getText().toString(),
                playerNameList.get(5), playerNameList.get(0).getText().toString(),
                -offsetX, offsetY_null, this::guardarJugadoresEnSharedPreferences);

    }

    private void animarYActualizar(TextView textView, String nuevoTexto,
                                   TextView nameTextView, String nuevoNombre,
                                   float offsetX, float offsetY, Runnable onAnimationEnd) {
        textView.animate()
                .translationXBy(offsetX)
                .translationYBy(offsetY)
                .setDuration(300)
                .withEndAction(() -> {
                    textView.setText(nuevoTexto); // Actualizar número
                    textView.setTranslationX(0);
                    textView.setTranslationY(0);
                    if (onAnimationEnd != null) {
                        onAnimationEnd.run();
                    }
                })
                .start();

        nameTextView.animate()
                .translationXBy(offsetX)
                .translationYBy(offsetY)
                .setDuration(300)
                .withEndAction(() -> {
                    nameTextView.setText(nuevoNombre); // Actualizar nombre
                    nameTextView.setTranslationX(0);
                    nameTextView.setTranslationY(0);
                })
                .start();
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

    private void actualizarIndicadorSaque() {
        if (posesionSaque) {
            posesionTeamA.setColorFilter(getResources().getColor(R.color.white));
            posesionTeamB.setColorFilter(getResources().getColor(R.color.grey));
        } else {
            posesionTeamA.setColorFilter(getResources().getColor(R.color.grey));
            posesionTeamB.setColorFilter(getResources().getColor(R.color.white));
        }
    }

    private void mostrarSeleccionJugador(TextView numeroJugadorView, TextView nombreJugadorView) {
        if (jugadoresList.isEmpty()) {
            Log.d("mostrarSeleccionJugador", "No hay jugadores disponibles en la lista.");
            new AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("No hay jugadores disponibles en este equipo.")
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }

        // Crear una lista de nombres para mostrar en el Dialog
        String[] nombresJugadores = new String[jugadoresList.size()];
        for (int i = 0; i < jugadoresList.size(); i++) {
            nombresJugadores[i] = jugadoresList.get(i).getNumero() + " - " + jugadoresList.get(i).getNombre();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar Jugador")
                .setItems(nombresJugadores, (dialog, which) -> {
                    // Al seleccionar un jugador, actualizar los TextViews
                    Jugador jugadorSeleccionado = jugadoresList.get(which);
                    Log.d("mostrarSeleccionJugador", "Jugador seleccionado: " + jugadorSeleccionado.getNombre());
                    numeroJugadorView.setText(String.valueOf(jugadorSeleccionado.getNumero()));
                    nombreJugadorView.setText(jugadorSeleccionado.getNombre());

                    guardarJugadoresEnSharedPreferences();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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

        // Cálculo de estadísticas
        String puntosSecuencia = partido.getPuntosSecuencia();
        int puntosAFavor = countOccurrences(puntosSecuencia, 'A');
        int puntosEnContra = countOccurrences(puntosSecuencia, 'B');
        int mejorRacha = calcularMejorRacha(puntosSecuencia, 'A');
        int setsTotales = partido.getSets();
        int setsAFavor = partido.getSetsAFavor();
        int setsEnContra = partido.getSetsEnContra();
        boolean esVictoria = setsAFavor > setsEnContra;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Actualizar Firestore
        db.collection("equipos")
                .document(teamId)
                .update(
                        "historial_partidos", FieldValue.arrayUnion(partido.toMap()), // Agregar a historial
                        "partidoEnCurso", FieldValue.delete() // Eliminar partido en curso
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("PartidoEnCursoFragment", "Partido finalizado y movido a historial_partidos.");

                    // Actualizar estadísticas del equipo
                    db.collection("equipos")
                            .document(teamId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Obtener estadísticas actuales
                                    long partidosJugados = documentSnapshot.contains("partidos_jugados") ?
                                            documentSnapshot.getLong("partidos_jugados") : 0;
                                    long partidosGanados = documentSnapshot.contains("partidos_ganados") ?
                                            documentSnapshot.getLong("partidos_ganados") : 0;
                                    long puntosTotales = documentSnapshot.contains("puntos_totales") ?
                                            documentSnapshot.getLong("puntos_totales") : 0;
                                    long puntosAFavorActuales = documentSnapshot.contains("puntos_a_favor") ?
                                            documentSnapshot.getLong("puntos_a_favor") : 0;
                                    long setsTotalesActuales = documentSnapshot.contains("sets_totales") ?
                                            documentSnapshot.getLong("sets_totales") : 0;
                                    long setsAFavorActuales = documentSnapshot.contains("sets_a_favor") ?
                                            documentSnapshot.getLong("sets_a_favor") : 0;
                                    long mejorRachaActual = documentSnapshot.contains("mejor_racha") ?
                                            documentSnapshot.getLong("mejor_racha") : 0;

                                    // Actualizar estadísticas
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("partidos_jugados", partidosJugados + 1);
                                    updates.put("partidos_ganados", partidosGanados + (esVictoria ? 1 : 0));
                                    updates.put("puntos_totales", puntosTotales + puntosAFavor + puntosEnContra);
                                    updates.put("puntos_a_favor", puntosAFavorActuales + puntosAFavor);
                                    updates.put("sets_totales", setsTotalesActuales + setsTotales);
                                    updates.put("sets_a_favor", setsAFavorActuales + setsAFavor);

                                    if (mejorRacha > mejorRachaActual) {
                                        updates.put("mejor_racha", mejorRacha);
                                        updates.put("rival_fecha_mejor_racha", partido.getRival() + " - " + partido.getFecha());
                                    }

                                    // Guardar estadísticas actualizadas
                                    db.collection("equipos")
                                            .document(teamId)
                                            .update(updates)
                                            .addOnSuccessListener(aVoid2 -> Log.d("PartidoEnCursoFragment", "Estadísticas del equipo actualizadas correctamente."))
                                            .addOnFailureListener(e -> Log.e("PartidoEnCursoFragment", "Error al actualizar estadísticas: " + e.getMessage()));
                                }
                            })
                            .addOnFailureListener(e -> Log.e("PartidoEnCursoFragment", "Error al obtener documento del equipo: " + e.getMessage()));

                    // Navegar a pantalla de inicio de partidos
                    navigateToStartMatch();
                    eliminarHistorialEnSharedPreferences();
                    eliminarJugadoresEnSharedPreferences();
                })
                .addOnFailureListener(e -> Log.e("PartidoEnCursoFragment", "Error al finalizar partido: " + e.getMessage()));

    }

    private int calcularMejorRacha(String secuencia, char caracter) {
        int mejorRacha = 0;
        int rachaActual = 0;

        for (char c : secuencia.toCharArray()) {
            if (c == caracter) {
                rachaActual++;
                if (rachaActual > mejorRacha) {
                    mejorRacha = rachaActual;
                }
            } else {
                rachaActual = 0;
            }
        }

        return mejorRacha;
    }

    private void reiniciarPuntos() {
        pointsTeamA = 0;
        pointsTeamB = 0;

        timeoutsTeamA = 0;
        timeoutsTeamB = 0;

        scoreTeamA.setText(String.format("%02d", pointsTeamA));
        scoreTeamB.setText(String.format("%02d", pointsTeamB));

        actualizarTimeoutsUI();
    }

    private void actualizarTimeoutsUI() {
        timeoutButtonA.setText(String.valueOf(timeoutsTeamA));
        timeoutButtonB.setText(String.valueOf(timeoutsTeamB));
    }

    private void cargarNombreEquipoDesdeFirestore() {
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

    private void cargarJugadoresDesdeFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos")
                .document(teamId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> jugadoresFirestore = (List<Map<String, Object>>) documentSnapshot.get("jugadores");
                        if (jugadoresFirestore != null) {
                            jugadoresList.clear();
                            for (Map<String, Object> jugadorMap : jugadoresFirestore) {
                                String id = (String) jugadorMap.get("id");
                                String nombre = (String) jugadorMap.get("nombre");
                                String posicion = (String) jugadorMap.get("posicion");
                                Long numero = (Long) jugadorMap.get("numero");
                                jugadoresList.add(new Jugador(id, nombre, Math.toIntExact(numero), posicion));
                            }
                            Log.d("cargarJugadores", "Jugadores cargados: " + jugadoresList);
                        }
                    } else {
                        Log.w("cargarJugadores", "No se encontraron jugadores en Firestore.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("cargarJugadores", "Error al cargar jugadores: " + e.getMessage());
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

    private void navigateToStartMatch() {
        if (getActivity() instanceof TeamMenuActivity) {
            TeamMenuActivity activity = (TeamMenuActivity) getActivity();
            activity.resetToStartMatch();
        }
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

    private void llenarArrayListJugadoresView(){
        playerList.add(player1);
        playerList.add(player6);
        playerList.add(player5);
        playerList.add(player4);
        playerList.add(player3);
        playerList.add(player2);

        playerNameList.add(player1_name);
        playerNameList.add(player6_name);
        playerNameList.add(player5_name);
        playerNameList.add(player4_name);
        playerNameList.add(player3_name);
        playerNameList.add(player2_name);
    }

    private void guardarPosicionesIniciales() {
        for (TextView player : playerList) {
            posicionesIniciales.put(player, new Float[]{player.getX(), player.getY()});
        }
    }

    private void guardarHistorialEnSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences(teamId+"_historial_partido", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String historialJson = gson.toJson(historialAcciones);

        editor.putString(teamId + "_historial_partido", historialJson);
        editor.putBoolean(teamId + "_posesion_saque", posesionSaque);
        editor.apply();
    }

    private void cargarHistorialDesdeSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences(teamId+"_historial_partido", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String historialJson = prefs.getString(teamId + "_historial_partido", null);

        if (historialJson != null) {
            Type type = new TypeToken<ArrayList<Accion>>() {}.getType();
            historialAcciones = gson.fromJson(historialJson, type);
        }

        // Cargar el estado de posesionSaque
        posesionSaque = prefs.getBoolean(teamId + "_posesion_saque", true);
        actualizarIndicadorSaque();
    }

    private void eliminarHistorialEnSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences(teamId+"_historial_partido", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(teamId + "_historial_partido");
        editor.remove(teamId + "_posesion_saque");
        editor.apply();
    }

    private void guardarJugadoresEnSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("jugadores_partido", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(teamId + "_player1", player1.getText().toString());
        editor.putString(teamId + "_player1_name", player1_name.getText().toString());

        editor.putString(teamId + "_player2", player2.getText().toString());
        editor.putString(teamId + "_player2_name", player2_name.getText().toString());

        editor.putString(teamId + "_player3", player3.getText().toString());
        editor.putString(teamId + "_player3_name", player3_name.getText().toString());

        editor.putString(teamId + "_player4", player4.getText().toString());
        editor.putString(teamId + "_player4_name", player4_name.getText().toString());

        editor.putString(teamId + "_player5", player5.getText().toString());
        editor.putString(teamId + "_player5_name", player5_name.getText().toString());

        editor.putString(teamId + "_player6", player6.getText().toString());
        editor.putString(teamId + "_player6_name", player6_name.getText().toString());

        editor.apply();
    }

    private void cargarJugadoresDesdeSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("jugadores_partido", Context.MODE_PRIVATE);

        // Cargar valores si existen, de lo contrario, mantener los actuales
        player1.setText(prefs.getString(teamId + "_player1", player1.getText().toString()));
        player1_name.setText(prefs.getString(teamId + "_player1_name", player1_name.getText().toString()));

        player2.setText(prefs.getString(teamId + "_player2", player2.getText().toString()));
        player2_name.setText(prefs.getString(teamId + "_player2_name", player2_name.getText().toString()));

        player3.setText(prefs.getString(teamId + "_player3", player3.getText().toString()));
        player3_name.setText(prefs.getString(teamId + "_player3_name", player3_name.getText().toString()));

        player4.setText(prefs.getString(teamId + "_player4", player4.getText().toString()));
        player4_name.setText(prefs.getString(teamId + "_player4_name", player4_name.getText().toString()));

        player5.setText(prefs.getString(teamId + "_player5", player5.getText().toString()));
        player5_name.setText(prefs.getString(teamId + "_player5_name", player5_name.getText().toString()));

        player6.setText(prefs.getString(teamId + "_player6", player6.getText().toString()));
        player6_name.setText(prefs.getString(teamId + "_player6_name", player6_name.getText().toString()));
    }

    private void eliminarJugadoresEnSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("jugadores_partido", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(teamId + "_player1");
        editor.remove(teamId + "_player1_name");
        editor.remove(teamId + "_player2");
        editor.remove(teamId + "_player2_name");
        editor.remove(teamId + "_player3");
        editor.remove(teamId + "_player3_name");
        editor.remove(teamId + "_player4");
        editor.remove(teamId + "_player4_name");
        editor.remove(teamId + "_player5");
        editor.remove(teamId + "_player5_name");
        editor.remove(teamId + "_player6");
        editor.remove(teamId + "_player6_name");

        editor.apply();
    }

}