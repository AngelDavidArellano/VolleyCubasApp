package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class AssistanceActivity extends AppCompatActivity {
    private String selectedDate;
    private CalendarView calendarView;
    private ArrayList<Jugador> jugadoresList = new ArrayList<>();
    private String teamId;

    private RecyclerView rvAssistance;
    private TextView tvSelectedDate;
    private ImageView btnBack;
    private AssistanceAdapter assistanceAdapter;
    private ArrayList<Map<String, Object>> assistanceList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistance);

        // Inicializar Firebase y RecyclerView
        db = FirebaseFirestore.getInstance();
        rvAssistance = findViewById(R.id.rvAssistance);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnBack = findViewById(R.id.back_button);
        rvAssistance.setLayoutManager(new LinearLayoutManager(this));
        assistanceAdapter = new AssistanceAdapter(AssistanceActivity.this, assistanceList, teamId);
        rvAssistance.setAdapter(assistanceAdapter);

        // Recuperar datos del Intent
        jugadoresList = getIntent().getParcelableArrayListExtra("jugadores");
        teamId = getIntent().getStringExtra("teamId");

        if (jugadoresList != null) {
            Log.d("AssistanceActivity", "Jugadores recibidos: " + jugadoresList.toString());
        }

        ImageView btnAdd = findViewById(R.id.btn_add);

        // Configuración del CalendarView
        selectedDate = getCurrentDate();
        calendarView = findViewById(R.id.calendarView);
        calendarView.setMaxDate(System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000);

        // Formatear y mostrar la fecha actual en el TextView
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        Calendar calendar = Calendar.getInstance();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);

        String fechaFormateada = String.format("%d de %s", dayOfMonth, meses[month]);
        tvSelectedDate.setText(fechaFormateada);

        // Detectar selección de fechas
        calendarView.setOnDateChangeListener((view, year, month1, dayOfMonth1) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth1, month1 + 1, year);

            String fechaFormateada1 = String.format("%d de %s", dayOfMonth1, meses[month1]);
            tvSelectedDate.setText(fechaFormateada1);

            loadAssistanceForDate(selectedDate); // Cargar asistencias para la fecha seleccionada
        });

        // Cargar asistencias para la fecha actual
        loadAssistanceForDate(selectedDate);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(AssistanceActivity.this, AddAssistanceActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("teamId", teamId);
            intent.putExtra("jugadores", jugadoresList);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadAssistanceForDate(String date) {
        String formattedDate = date.replace("/", "-"); // Reemplazar "/" por "-" para la colección
        assistanceList.clear(); // Limpiar la lista antes de cargar nuevos datos

        db.collection("registros")
                .document(teamId)
                .collection(formattedDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        assistanceList.add(document.getData());
                    }
                    assistanceAdapter.notifyDataSetChanged(); // Actualizar el RecyclerView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar registros.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}
