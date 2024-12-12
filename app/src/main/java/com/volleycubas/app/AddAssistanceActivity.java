package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddAssistanceActivity extends AppCompatActivity {


    private ArrayList<Jugador> jugadoresList = new ArrayList<>();
    private String teamId;
    private String selectedDate;

    private TextView etDate;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_assistance);

        // Recuperar datos del Intent
        selectedDate = getIntent().getStringExtra("selectedDate");
        jugadoresList = getIntent().getParcelableArrayListExtra("jugadores");
        teamId = getIntent().getStringExtra("teamId");

        String tipo = getIntent().getStringExtra("tipo");
        String extraInfo = getIntent().getStringExtra("extraInfo");

        TextView tvDate = findViewById(R.id.etDate);
        if (tvDate != null) {
            tvDate.setText(selectedDate);
        }

        Spinner spinnerActivityType = findViewById(R.id.spinnerActivityType);
        EditText tvExtraInfo = findViewById(R.id.tvExtraInfo);
        ImageView btnDatePicker = findViewById(R.id.btnDatePicker);
        etDate = findViewById(R.id.etDate);
        btnBack = findViewById(R.id.back_button);

        // Configuración del Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                AddAssistanceActivity.this, R.array.activity_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityType.setAdapter(adapter);

        spinnerActivityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedActivity = parent.getItemAtPosition(position).toString();
                switch (selectedActivity) {
                    case "Partido":
                        tvExtraInfo.setHint("Introduce el rival");
                        tvExtraInfo.setVisibility(View.VISIBLE);
                        break;
                    case "Otro":
                        tvExtraInfo.setHint("Describe la actividad");
                        tvExtraInfo.setVisibility(View.VISIBLE);
                        break;
                    case "Entrenamiento":
                    default:
                        tvExtraInfo.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvExtraInfo.setVisibility(View.GONE);
            }
        });

        if (tipo != null) {
            // Seleccionar el tipo recibido
            int position = adapter.getPosition(tipo);
            spinnerActivityType.setSelection(position);
        }

        if (extraInfo != null) {
            // Mostrar información extra recibida
            tvExtraInfo.setText(extraInfo);
            tvExtraInfo.setVisibility(!extraInfo.isEmpty() ? View.VISIBLE : View.GONE);
        }

        // Configuración del RecyclerView con PlayerAssistanceAdapter
        RecyclerView rvPlayers = findViewById(R.id.rvPlayers);
        rvPlayers.setLayoutManager(new LinearLayoutManager(this));

        // Crear el adaptador con la lista de jugadores
        PlayerAssistanceAdapter playerAssistanceAdapter = new PlayerAssistanceAdapter(jugadoresList);
        rvPlayers.setAdapter(playerAssistanceAdapter);

        // Configurar botón guardar
        Button btnSaveAssistance = findViewById(R.id.btnSaveAssistance);
        btnSaveAssistance.setOnClickListener(v -> saveRecordToFirebase(spinnerActivityType, tvExtraInfo));

        if (jugadoresList != null) {
            for (Jugador jugador : jugadoresList) {
                Log.d("AddAssistanceActivity", "Jugador cargado: " +
                        "\nID: " + jugador.getId() +
                        "\nNombre: " + jugador.getNombre() +
                        "\nAsistencia: " + jugador.isAsistencia());
            }
        } else {
            Log.e("AddAssistanceActivity", "La lista de jugadores está vacía o no se recibió correctamente.");
        }
        btnDatePicker.setOnClickListener(v -> showDatePickerDialog());

        btnBack.setOnClickListener(v -> finish());
    }


    private void saveRecordToFirebase(Spinner spinnerActivityType, EditText tvExtraInfo) {
        String selectedActivity = spinnerActivityType.getSelectedItem().toString();
        String extraInfo = tvExtraInfo.getText().toString().trim();

        // Crear la lista de asistencia de jugadores
        List<Map<String, Object>> asistenciaJugadores = new ArrayList<>();
        for (Jugador jugador : jugadoresList) {
            Map<String, Object> jugadorMap = new HashMap<>();
            jugadorMap.put("id", jugador.getId());
            jugadorMap.put("nombre", jugador.getNombre());
            jugadorMap.put("asistencia", jugador.isAsistencia());
            asistenciaJugadores.add(jugadorMap);
        }

        // Crear el mapa del registro
        Map<String, Object> registroData = new HashMap<>();
        registroData.put("fecha", selectedDate);
        registroData.put("tipo", selectedActivity);
        if (!extraInfo.isEmpty()) {
            registroData.put("extraInfo", extraInfo);
        }
        registroData.put("jugadores", asistenciaJugadores);

        // Guardar en Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String selectedDateTitles = formatFirestoreDate(selectedDate);

        db.collection("registros")
                .document(teamId)
                .collection(selectedDateTitles) // Subcolección con la fecha
                .add(registroData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddAssistanceActivity.this, "Registro guardado correctamente.", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar la actividad
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddAssistanceActivity.this, "Error al guardar el registro.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private String formatFirestoreDate(String date) {
        return date.replace('/', '-');
    }

    private void showDatePickerDialog() {
        // Obtener la fecha actual para inicializar el DatePicker
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear y mostrar el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Formatear la fecha seleccionada
                    selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    etDate.setText(selectedDate); // Actualizar el EditText con la nueva fecha
                },
                year,
                month,
                day
        );

        // Mostrar el selector de fecha
        datePickerDialog.show();
    }
}