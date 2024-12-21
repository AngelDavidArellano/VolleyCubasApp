package com.volleycubas.app;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeamSettingsActivity extends AppCompatActivity {

    private String teamId;
    private Button exportTeamDataButton;
    private FirebaseFirestore db;
    private boolean isWorkbookSaved = false;


    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Hilo único para sincronizar tareas

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown(); // Liberar el hilo al cerrar la actividad
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_settings);

        db = FirebaseFirestore.getInstance();
        teamId = getIntent().getStringExtra("teamId");

        if (teamId == null || teamId.isEmpty()) {
            finish();
            return;
        }

        // Verificar permisos en tiempo de ejecución
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        exportTeamDataButton = findViewById(R.id.export_team_data_button);
        exportTeamDataButton.setOnClickListener(v -> exportTeamAndAsistances());
    }

    private void exportTeamAndAsistances() {
        executorService.execute(() -> {
            try {
                InputStream fis = getAssets().open("plantilla_ejemplo_datos.xlsm");
                Workbook workbook = WorkbookFactory.create(fis);
                CellStyle borderedCenteredStyle = createBorderedCenteredStyle(workbook);
                CellStyle streakStyle = createStreakStyle(workbook);

                db.collection("equipos").document(teamId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            Map<String, Object> teamData = documentSnapshot.getData();
                            if (documentSnapshot.exists()) {
                                if (teamData != null) {
                                    fillJugadoresSheet(workbook.getSheet("Jugadores"), teamData, borderedCenteredStyle);
                                    fillHistorialSheet(workbook.getSheet("Historial partidos"), teamData, borderedCenteredStyle);
                                }
                            }

                            db.collection("registros").document(teamId).get()
                                    .addOnSuccessListener(asistenciasSnapshot -> {
                                        if (asistenciasSnapshot.exists()) {
                                            Map<String, Object> registrosData = asistenciasSnapshot.getData();
                                            if (registrosData != null) {
                                                List<String> fechas = (List<String>) registrosData.get("fechas");
                                                Double porcentaje_acumulado = (Double) registrosData.get("porcentaje_acumulado");
                                                if (fechas != null && !fechas.isEmpty()) {
                                                    List<Map<String, Object>> asistenciasList = new ArrayList<>();
                                                    int[] pendingTasks = {fechas.size()};

                                                    for (String fecha : fechas) {
                                                        String fechaReemplazada = fecha.replace("/", "-");
                                                        db.collection("registros").document(teamId).collection(fechaReemplazada)
                                                                .get()
                                                                .addOnSuccessListener(subcollectionSnapshot -> {
                                                                    for (QueryDocumentSnapshot subdoc : subcollectionSnapshot) {
                                                                        Map<String, Object> subdocData = subdoc.getData();
                                                                        if (subdocData != null) {
                                                                            Map<String, Object> asistencia = new HashMap<>();
                                                                            asistencia.put("fecha", subdocData.getOrDefault("fecha", fechaReemplazada));
                                                                            asistencia.put("jugadores", subdocData.get("jugadores"));
                                                                            asistencia.put("porcentaje_dia", subdocData.getOrDefault("porcentaje_dia", 0));

                                                                            // Concatenar "tipo" y "extraInfo" si existe
                                                                            String tipo = (String) subdocData.getOrDefault("tipo", "Desconocido");
                                                                            String extraInfo = (String) subdocData.get("extraInfo");
                                                                            if (extraInfo != null && !extraInfo.isEmpty()) {
                                                                                tipo += " - " + extraInfo;
                                                                            }
                                                                            asistencia.put("tipo", tipo);

                                                                            asistenciasList.add(asistencia);
                                                                        }
                                                                    }

                                                                    synchronized (pendingTasks) {
                                                                        pendingTasks[0]--;
                                                                        if (pendingTasks[0] == 0) {
                                                                            Sheet asistenciaSheet = workbook.getSheet("Asistencia");
                                                                            fillAsistenciasSheetFromList(asistenciaSheet, asistenciasList, borderedCenteredStyle);
                                                                            fillEstadisticasSheet(workbook.getSheet("Estadisticas"), teamData, porcentaje_acumulado, borderedCenteredStyle, streakStyle);
                                                                            saveWorkbookToFileOnce(workbook);
                                                                        }
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.e("EXPORT_ASISTANCE", "Error al obtener subcolección de asistencia", e);
                                                                    synchronized (pendingTasks) {
                                                                        pendingTasks[0]--;
                                                                        if (pendingTasks[0] == 0) {
                                                                            saveWorkbookToFileOnce(workbook);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                } else {
                                                    saveWorkbookToFileOnce(workbook);
                                                }
                                            } else {
                                                saveWorkbookToFileOnce(workbook);
                                            }
                                        } else {
                                            saveWorkbookToFileOnce(workbook);
                                        }
                                    });
                        });
            } catch (Exception e) {
                Log.e("EXPORT_ERROR", "Error al procesar la plantilla", e);
                runOnUiThread(() -> Toast.makeText(this, "Error al procesar la plantilla: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private synchronized void saveWorkbookToFileOnce(Workbook workbook) {
        if (isWorkbookSaved) {
            Log.d("SAVE_SKIP", "El Workbook ya fue guardado. Ignorando nueva solicitud.");
            return; // Salir si ya se guardó
        }

        try {
            File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "VolleyCubas");
            if (!exportDir.exists()) {
                boolean dirCreated = exportDir.mkdirs();
                Log.d("Excel", "Directorio creado: " + dirCreated);
            }
            File exportFile = new File(exportDir, "team_data_and_asistencias_" + teamId + ".xlsm");
            Log.d("Excel", "Guardando archivo en: " + exportFile.getAbsolutePath());

            try (FileOutputStream fos = new FileOutputStream(exportFile)) {
                workbook.write(fos);
            }

            Log.d("Excel", "Archivo guardado correctamente.");
            isWorkbookSaved = true; // Actualizamos el flag
            runOnUiThread(() -> Toast.makeText(this, "Datos exportados en: " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Log.e("SAVE_ERROR", "Error al guardar el archivo", e);
            runOnUiThread(() -> Toast.makeText(this, "Error al guardar el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } finally {
            try {
                workbook.close(); // Cerramos el Workbook
            } catch (Exception e) {
                Log.e("WORKBOOK_CLOSE_ERROR", "Error al cerrar el Workbook", e);
            }
        }
    }
    private void fillJugadoresSheet(Sheet sheet, Map<String, Object> teamData, CellStyle style) {
        List<Map<String, Object>> jugadores = (List<Map<String, Object>>) teamData.get("jugadores");
        if (jugadores != null) {
            int startRow = 8;
            for (Map<String, Object> jugador : jugadores) {
                Row row = sheet.getRow(startRow++);
                if (row == null) row = sheet.createRow(startRow - 1);
                applyDataToMergedCell(row, 0, jugador.get("numero").toString(), style);
                applyDataToMergedCell(row, 2, jugador.get("nombre").toString(), style);
                applyDataToMergedCell(row, 4, jugador.get("posicion").toString(), style);
                applyDataToMergedCell(row, 6, jugador.get("numeroMVPs").toString(), style);
            }
        }
    }

    private void fillHistorialSheet(Sheet sheet, Map<String, Object> teamData, CellStyle style) {
        List<Map<String, Object>> partidos = (List<Map<String, Object>>) teamData.get("historial_partidos");
        if (partidos != null) {
            int startRow = 8;
            for (Map<String, Object> partido : partidos) {
                Row row = sheet.getRow(startRow++);
                if (row == null) row = sheet.createRow(startRow - 1);

                applyDataToMergedCell(row, 0, partido.get("puntosPorSet").toString(), style);
                applyDataToMergedCell(row, 1, partido.get("fecha").toString(), style);
                applyDataToMergedCell(row, 2, partido.get("setsAFavor") + " - " + partido.get("setsEnContra"), style);
                applyDataToMergedCell(row, 3, partido.get("rival").toString(), style);
                applyDataToMergedCell(row, 5, partido.get("fase").toString(), style);
                applyDataToMergedCell(row, 11, "▶", style);
            }
        }
    }

    private void fillEstadisticasSheet(Sheet sheet, Map<String, Object> teamData, Double porcentaje_acumulado, CellStyle style, CellStyle streakStyle) {
        // Mejor racha
        applyDataToMergedCell(sheet.getRow(10), 8, teamData.get("mejor_racha").toString(), streakStyle); // Mejor racha (I11)

        // Partidos ganados
        applyDataToMergedCell(sheet.getRow(12), 1, teamData.get("partidos_ganados").toString(), style); // Partidos ganados (B13)

        // Puntos a favor
        applyDataToMergedCell(sheet.getRow(25), 1, teamData.get("puntos_a_favor").toString(), style); // Puntos a favor (B26)

        // Partidos jugados
        applyDataToMergedCell(sheet.getRow(14), 1, teamData.get("partidos_jugados").toString(), style); // Partidos jugados (B15)

        // Sets a favor
        applyDataToMergedCell(sheet.getRow(12), 5, teamData.get("sets_a_favor").toString(), style); // Sets a favor (F13)

        // Sets totales
        applyDataToMergedCell(sheet.getRow(14), 5, teamData.get("sets_totales").toString(), style); // Sets totales (F15)

        // Puntos totales
        applyDataToMergedCell(sheet.getRow(27), 1, teamData.get("puntos_totales").toString(), style); // Puntos totales (B28)

        // Porcentaje asistencia
        applyDataToMergedCell(sheet.getRow(25), 5, String.valueOf(porcentaje_acumulado), style); // Porcentaje asistencia (F26)

        // Crear un estilo para el jugador con más MVPs
        CellStyle mvpStyle = sheet.getWorkbook().createCellStyle();
        Font mvpFont = sheet.getWorkbook().createFont();
        mvpFont.setFontName("Arial Black");
        mvpFont.setFontHeightInPoints((short) 36);
        mvpStyle.setFont(mvpFont);
        mvpStyle.setWrapText(true); // Ajustar texto
        mvpStyle.setAlignment(HorizontalAlignment.CENTER);
        mvpStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle strakeStyle = sheet.getWorkbook().createCellStyle();
        Font strakeFont = sheet.getWorkbook().createFont();
        strakeFont.setFontName("Calibri");
        strakeFont.setFontHeightInPoints((short) 12);
        strakeStyle.setFont(strakeFont);
        strakeStyle.setWrapText(true); // Ajustar texto
        strakeStyle.setAlignment(HorizontalAlignment.CENTER);
        strakeStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Rival y fecha mejor racha
        applyDataToMergedCell(sheet.getRow(15), 8, teamData.get("rival_fecha_mejor_racha").toString(), strakeStyle); // Rival y fecha (I16)

        // Jugador con más numeroMVPs
        List<Map<String, Object>> jugadores = (List<Map<String, Object>>) teamData.get("jugadores");
        if (jugadores != null && !jugadores.isEmpty()) {
            Map<String, Object> topJugador = null;
            int maxMVPs = 0;

            for (Map<String, Object> jugador : jugadores) {
                int numeroMVPs = Integer.parseInt(jugador.get("numeroMVPs").toString());
                if (numeroMVPs > maxMVPs) {
                    maxMVPs = numeroMVPs;
                    topJugador = jugador;
                }
            }

            if (topJugador != null) {
                Row row = sheet.getRow(22);
                if (row == null) row = sheet.createRow(22);
                Cell cell = row.getCell(8);
                if (cell == null) cell = row.createCell(8);
                cell.setCellValue(topJugador.get("nombre").toString());
                cell.setCellStyle(mvpStyle); // Aplicar el estilo personalizado
            }
        }
    }

    private void fillAsistenciasSheetFromList(Sheet sheet, List<Map<String, Object>> asistenciasList, CellStyle style) {
        Log.d("Excel", "Entrando al método fillAsistenciasSheetFromList.");
        if (asistenciasList != null) {
            int startRow = 8;
            for (int i = 0; i < asistenciasList.size(); i++) {
                Row row = sheet.getRow(startRow + i);
                if (row == null) row = sheet.createRow(startRow + i);

                Map<String, Object> asistencia = asistenciasList.get(i);
                List<Map<String, Object>> jugadores = (List<Map<String, Object>>) asistencia.get("jugadores");

                // Combinar nombres de jugadores y asistencia
                StringBuilder nombresYAsistencias = new StringBuilder();
                if (jugadores != null) {
                    for (Map<String, Object> jugador : jugadores) {
                        String nombre = (String) jugador.get("nombre");
                        String asistenciaJugador = String.valueOf(jugador.get("asistencia"));
                        nombresYAsistencias.append(nombre).append(" - ").append(asistenciaJugador).append(", ");
                    }
                    if (nombresYAsistencias.length() > 2) {
                        nombresYAsistencias.setLength(nombresYAsistencias.length() - 2); // Eliminar última coma
                    }
                }

                applyDataToMergedCell(row, 0, nombresYAsistencias.toString(), style); // Columna A
                applyDataToMergedCell(row, 1, asistencia.get("fecha").toString(), style); // Columna B
                applyDataToMergedCell(row, 2, asistencia.get("tipo").toString(), style); // Columna C

                // Conteo de asistencias y ausencias
                long asistenciasCount = jugadores != null ? jugadores.stream()
                        .filter(jugador -> Boolean.TRUE.equals(jugador.get("asistencia"))).count() : 0;
                long ausenciasCount = jugadores != null ? jugadores.stream()
                        .filter(jugador -> Boolean.FALSE.equals(jugador.get("asistencia")) || jugador.get("asistencia") == null).count() : 0;

                applyDataToMergedCell(row, 5, String.valueOf(asistenciasCount), style); // Columna D
                applyDataToMergedCell(row, 7, String.valueOf(ausenciasCount), style); // Columna E
                applyDataToMergedCell(row, 9, asistencia.get("porcentaje_dia").toString(), style); // Columna F
                applyDataToMergedCell(row, 11, "▶", style); // Columna G
            }
        }
    }

    private CellStyle createBorderedCenteredStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createStreakStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setFontName("Arial Black");
        font.setFontHeightInPoints((short) 48);
        CellStyle streakStyle = workbook.createCellStyle();
        streakStyle.setFont(font);
        streakStyle.setAlignment(HorizontalAlignment.RIGHT);
        return streakStyle;
    }

    private void applyDataToMergedCell(Row row, int columnIndex, String value, CellStyle style) {
        if (row == null) {
            Log.e("Excel", "Row is null. Cannot apply data.");
            return; // No hacemos nada si la fila no existe
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) cell = row.createCell(columnIndex);
        try {
            cell.setCellValue(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            cell.setCellValue(value);
        }
        cell.setCellStyle(style);
    }
}
