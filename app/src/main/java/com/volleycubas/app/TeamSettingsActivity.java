package com.volleycubas.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeamSettingsActivity extends AppCompatActivity {

    private static final String TAG = "TeamSettingsActivity";
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private String teamId;

    private Button changeTeamImageButton;
    private Button changeTeamNameButton;
    private Button changeLeagueButton;
    private Button changeCaptainButton;
    private Button changeCoachButton;
    private Button changeCreationSeasonButton;
    private Button exportTeamDataButton;
    private Button shareTeamCodeButton;

    private Team team = new Team();
    private ArrayList<String> nombresJugadores = new ArrayList<>();

    private FirebaseFirestore db;
    private boolean isWorkbookSaved = false;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        changeTeamImageButton = findViewById(R.id.change_team_image_button);
        changeTeamNameButton = findViewById(R.id.change_team_name_button);
        changeLeagueButton = findViewById(R.id.change_league_button);
        changeCaptainButton = findViewById(R.id.change_captain_button);
        changeCoachButton = findViewById(R.id.change_coach_button);
        changeCreationSeasonButton = findViewById(R.id.change_creation_season_button);
        exportTeamDataButton = findViewById(R.id.export_team_data_button);
        shareTeamCodeButton = findViewById(R.id.share_team_code_button);

        loadTeamData(teamId);


        changeTeamImageButton.setOnClickListener(v -> seleccionarImagen());

        changeTeamNameButton.setOnClickListener(v -> {
            showEditDialog("Cambiar Nombre del Equipo", team.getNombre(), newValue -> {
                team.setNombre(newValue);
                updateTeamData("nombre", newValue); // Actualiza en Firebase
            });
        });

        changeLeagueButton.setOnClickListener(v -> {
            showEditDialog("Cambiar Liga", team.getLiga(), newValue -> {
                team.setLiga(newValue);
                updateTeamData("liga", newValue); // Actualiza en Firebase
            });
        });

        changeCaptainButton.setOnClickListener(v -> {
            showPlayerSelectionDialog(nombresJugadores);
        });

        changeCoachButton.setOnClickListener(v -> {
            if (team.getEntrenadores() == null) {
                team.setEntrenadores(new ArrayList<>()); // Inicializar si está vacío
            }

            // Mostrar el diálogo para gestionar entrenadores
            showManageCoachesDialog();
        });

        changeCreationSeasonButton.setOnClickListener(v -> {
            showEditDialog("Cambiar Temporada de Creación", team.getTemporada_creacion(), newValue -> {
                team.setTemporada_creacion(newValue);
                updateTeamData("temporada_creacion", newValue); // Actualiza en Firebase
            });
        });

        exportTeamDataButton.setOnClickListener(v -> exportTeamAndAsistances());

        shareTeamCodeButton.setOnClickListener(v -> {
            // Texto predeterminado que incluirá el teamId
            String mensaje = "\uD83C\uDF89 ¡Únete a mi equipo en VolleyCubasApp! \uD83C\uDFD0\n\uD83D\uDC49 Código de equipo: ⚡️" + teamId + "⚡️\n";

            // Crear un Intent para compartir
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, mensaje);

            // Iniciar el selector de aplicaciones para compartir
            startActivity(Intent.createChooser(intent, "Compartir código del equipo"));
        });
    }

    private void loadTeamData(String teamId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference teamRef = db.collection("equipos").document(teamId);

        teamRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "Datos del equipo: " + document.getData());

                    // Crear el objeto Team con los datos descargados
                    team = document.toObject(Team.class);

                    // Obtener la lista de jugadores desde el documento
                    List<Map<String, Object>> jugadoresFirestore = (List<Map<String, Object>>) document.get("jugadores");
                    if (jugadoresFirestore != null) {
                        for (Map<String, Object> jugadorMap : jugadoresFirestore) {
                            String nombre = (String) jugadorMap.get("nombre");
                            if (nombre != null) {
                                nombresJugadores.add(nombre);
                            }
                        }
                    }

                    Log.d(TAG, "Nombres de jugadores cargados: " + nombresJugadores);
                } else {
                    Log.d(TAG, "No se encontró el documento");
                }
            } else {
                Log.e(TAG, "Error obteniendo documento", task.getException());
            }
        });
    }

    private void showEditDialog(String title, String currentValue, OnDataChangedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Crear un EditText para mostrar/modificar el valor actual
        final EditText input = new EditText(this);
        input.setText(currentValue);
        input.setSelection(currentValue.length()); // Coloca el cursor al final
        builder.setView(input);

        // Botón para confirmar los cambios
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            if (!newValue.isEmpty()) {
                listener.onDataChanged(newValue);
            } else {
                Toast.makeText(this, "El valor no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para cancelar
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        // Mostrar el diálogo
        builder.show();
    }

    private void showPlayerSelectionDialog(ArrayList<String> nombresJugadores) {
        if (nombresJugadores.isEmpty()) {
            Toast.makeText(this, "No hay jugadores disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] jugadoresArray = nombresJugadores.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Capitán");
        builder.setItems(jugadoresArray, (dialog, which) -> {
            String seleccionado = jugadoresArray[which];
            showConfirmationDialog("Cambiar Capitán", "¿Estás seguro de que deseas cambiar el capitán a " + seleccionado + "?", () -> {
                // Aquí puedes actualizar el capitán en Firebase o el objeto local
                updateTeamData("capitan", seleccionado);
                Toast.makeText(this, "El capitán ha sido cambiado a: " + seleccionado, Toast.LENGTH_SHORT).show();
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Confirmar", (dialog, which) -> onConfirm.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateTeamData(String field, Object newValue) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos").document(teamId)
                .update(field, newValue)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando los datos", e);
                    Toast.makeText(this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void showManageCoachesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Gestionar Entrenadores");

        // Crear una copia de la lista de entrenadores para manipular localmente
        List<String> entrenadoresTemp = new ArrayList<>(team.getEntrenadores());

        // Crear un layout dinámico para la lista de entrenadores
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Añadir entrenadores actuales con opción de eliminación
        for (int i = 0; i < entrenadoresTemp.size(); i++) {
            String entrenador = entrenadoresTemp.get(i);

            LinearLayout entrenadorLayout = new LinearLayout(this);
            entrenadorLayout.setOrientation(LinearLayout.HORIZONTAL);
            entrenadorLayout.setPadding(10, 10, 10, 10);

            TextView entrenadorText = new TextView(this);
            entrenadorText.setText(entrenador);
            entrenadorText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            entrenadorLayout.addView(entrenadorText);

            Button deleteButton = new Button(this);
            deleteButton.setText("-");
            deleteButton.setOnClickListener(v -> {
                entrenadoresTemp.remove(entrenador);
                layout.removeView(entrenadorLayout); // Eliminar el entrenador visualmente
                Toast.makeText(this, entrenador + " eliminado", Toast.LENGTH_SHORT).show();
            });
            entrenadorLayout.addView(deleteButton);

            layout.addView(entrenadorLayout);
        }

        // Botón para añadir un nuevo entrenador
        Button addButton = new Button(this);
        addButton.setText("+ Añadir Entrenador");
        addButton.setOnClickListener(v -> showAddCoachDialog(entrenadoresTemp, layout));
        layout.addView(addButton);

        // Mostrar el diálogo
        builder.setView(layout);
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            // Guardar los cambios en Firebase y actualizar el objeto `team`
            team.setEntrenadores(entrenadoresTemp);
            updateTeamData("entrenadores", entrenadoresTemp);
            Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showAddCoachDialog(List<String> entrenadoresTemp, LinearLayout layout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir Entrenador");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String nuevoEntrenador = input.getText().toString().trim();
            if (!nuevoEntrenador.isEmpty()) {
                entrenadoresTemp.add(nuevoEntrenador);

                // Añadir visualmente el nuevo entrenador
                LinearLayout entrenadorLayout = new LinearLayout(this);
                entrenadorLayout.setOrientation(LinearLayout.HORIZONTAL);
                entrenadorLayout.setPadding(10, 10, 10, 10);

                TextView entrenadorText = new TextView(this);
                entrenadorText.setText(nuevoEntrenador);
                entrenadorText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                entrenadorLayout.addView(entrenadorText);

                Button deleteButton = new Button(this);
                deleteButton.setText("-");
                deleteButton.setOnClickListener(v1 -> {
                    entrenadoresTemp.remove(nuevoEntrenador);
                    layout.removeView(entrenadorLayout);
                    Toast.makeText(this, nuevoEntrenador + " eliminado", Toast.LENGTH_SHORT).show();
                });
                entrenadorLayout.addView(deleteButton);

                layout.addView(entrenadorLayout, layout.getChildCount() - 1); // Añadir antes del botón +
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap resizedBitmap = resizeAndCompressImage(imageUri);

                // Convertir a byte array para subirla
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                // Subir la imagen redimensionada a Firebase
                uploadImageToFirebase(teamId, imageData, task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        updateTeamData("url_imagen", downloadUri.toString());
                        Toast.makeText(this, "Imagen actualizada correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                Log.e("TeamSettingsActivity", "Error al cargar imagen", e);
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap resizeAndCompressImage(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        // Recortar la imagen al cuadrado
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        int newDimension = Math.min(width, height);
        int xOffset = (width - newDimension) / 2;
        int yOffset = (height - newDimension) / 2;

        Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, newDimension, newDimension);

        // Redimensionar la imagen recortada
        return Bitmap.createScaledBitmap(croppedBitmap, 1000, 1000, true);
    }

    private void uploadImageToFirebase(String teamId, byte[] imageData, OnCompleteListener<Uri> onCompleteListener) {
        if (imageData == null) {
            TaskCompletionSource<Uri> taskCompletionSource = new TaskCompletionSource<>();
            taskCompletionSource.setResult(null);
            taskCompletionSource.getTask().addOnCompleteListener(onCompleteListener);
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("teams/" + teamId + "/team_image.jpg");

        UploadTask uploadTask = storageRef.putBytes(imageData);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(onCompleteListener);
    }


    private interface OnDataChangedListener {
        void onDataChanged(String newValue);
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
