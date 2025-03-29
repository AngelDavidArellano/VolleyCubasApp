package com.volleycubas.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    public static boolean isLoadingInitialized = false;

    private int clickCount = 0;
    private long lastClickTime = 0;
    private static final int CLICK_THRESHOLD = 7;
    private static final long CLICK_RESET_TIME = 1000; // Tiempo límite para considerar clicks consecutivos (ms)

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private TextView welcomeText, announcementTitle, announcementContent;
    private View loadingOverlay;
    private ImageView loadingIndicator, profilePhoto;
    private RecyclerView teamsRecyclerView;
    private TeamAdapter teamAdapter;
    private List<Team> teamList = new ArrayList<>();
    private List<String> teamListNames = new ArrayList<>();
    private List<String> teamListCodes = new ArrayList<>();

    private String trainerID, nombreEntrenador, apellidosEntrenador, email, profilePhotoUrl;

    public static boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trainerID = FirebaseAuth.getInstance().getUid();

        actualizarTokenFCM(trainerID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Inicializar vistas
        welcomeText = findViewById(R.id.welcomeText);
        announcementTitle = findViewById(R.id.announcementTitle);
        announcementContent = findViewById(R.id.announcementContent);
        teamsRecyclerView = findViewById(R.id.teamsRecyclerView);

        loadingOverlay = findViewById(R.id.loadingOverlay_main);
        loadingIndicator = findViewById(R.id.loadingGif_main);
        setupManualRotation();

        // Configurar RecyclerView y Adapter
        teamAdapter = new TeamAdapter(teamList, this, teamId -> {
            Intent intent = new Intent(MainActivity.this, TeamMenuActivity.class);
            intent.putExtra("teamId", teamId);
            startActivity(intent);
        });
        teamsRecyclerView.setAdapter(teamAdapter);
        teamsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener el usuario actual y cargar datos
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            cargarMensajeBienvenida(currentUser.getUid());
            if (!isDataLoaded && !isLoadingInitialized) { // Evitar múltiples cargas
                cargarEquiposDesdeFirestore(currentUser.getUid());
                isDataLoaded = true;
                isLoadingInitialized = true;
            }
        }

        cargarAnuncioPrincipal();
        getprofilePhotoUrl();
        isDataLoaded = true;

        // Botón para crear o unirse a un equipo
        findViewById(R.id.fabAddTeam).setOnClickListener(v -> mostrarOpcionesEquipo());

        CardView cardGenerateTeams = findViewById(R.id.card_crear_equipos);
        cardGenerateTeams.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GenerateTeamsActivity.class);
            intent.putParcelableArrayListExtra("jugadores", new ArrayList<>());
            intent.putExtra("teamId", 000000);
            startActivity(intent);
        });

        CardView cardHorarios = findViewById(R.id.card_horarios);
        cardHorarios.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NextGamesActivity.class);
            startActivity(intent);
        });

        CardView cardClasificaciones = findViewById(R.id.card_clasificaciones);
        cardClasificaciones.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ClasificacionesActivity.class);
            startActivity(intent);
        });

        profilePhoto = findViewById(R.id.profilePhoto);

        loadProfileImage();

        profilePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("trainerID", trainerID);
            intent.putExtra("profilePhotoUrl", profilePhotoUrl);
            intent.putExtra("nombreCompleto", nombreEntrenador + " " + apellidosEntrenador);
            intent.putExtra("email", email);
            intent.putStringArrayListExtra("listaEquiposNombres", new ArrayList<>(teamListNames));
            intent.putStringArrayListExtra("listaEquiposCodigos", new ArrayList<>(teamListCodes));
            startActivity(intent);
        });

        ImageView logoCubasVoley = findViewById(R.id.logoCubasVoley);
        logoCubasVoley.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_RESET_TIME) {
                clickCount++;
                if (clickCount >= CLICK_THRESHOLD) {
                    // Acción al alcanzar el umbral
                    clickCount = 0; // Resetear contador
                    Intent intent = new Intent(MainActivity.this, HiddenActivity.class); // Reemplaza con tu actividad
                    startActivity(intent);
                }
            } else {
                clickCount = 1; // Reinicia el contador si el tiempo es demasiado largo
            }
            lastClickTime = currentTime;
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDataLoaded = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && (!isLoadingInitialized || !isDataLoaded)) {
            cargarEquiposDesdeFirestore(currentUser.getUid());
            loadProfileImage();
            isLoadingInitialized = true;
        }
    }


    private void showLoadingIndicator() {
        Log.d("LOADING ANIMATION","Animación empezada");
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        if (loadingIndicator != null) {
            loadingIndicator.clearAnimation(); // Detener animación explícitamente
        }
        Log.d("LOADING ANIMATION","Animación finalizada");
        loadingOverlay.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void setupManualRotation() {
        // Configurar la animación
        RotateAnimation rotate = new RotateAnimation(
                0f, 360f, // Rotación de 0 a 360 grados
                Animation.RELATIVE_TO_SELF, 0.5f, // Punto de pivote en el centro
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(2000); // Duración de la rotación en milisegundos
        rotate.setRepeatCount(Animation.INFINITE); // Repetir indefinidamente

        // Iniciar la animación
        loadingIndicator.startAnimation(rotate);
    }

    private void cargarMensajeBienvenida(String entrenadorId) {
        firestore.collection("entrenadores").document(entrenadorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        nombreEntrenador = documentSnapshot.getString("nombre");
                        apellidosEntrenador = documentSnapshot.getString("apellidos");
                        email = documentSnapshot.getString("email");
                        welcomeText.setText("Bienvenido " + (nombreEntrenador != null ? nombreEntrenador : "") + "!");
                    } else {
                        welcomeText.setText("Bienvenid@!");
                    }
                })
                .addOnFailureListener(e -> {
                    welcomeText.setText("Error al cargar nombre.");
                    Toast.makeText(this, "Error al obtener el nombre: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarAnuncioPrincipal() {
        firestore.collection("anuncios").document("anuncio_principal")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String titulo = documentSnapshot.getString("titulo");
                        String contenido = documentSnapshot.getString("contenido");

                        if (contenido.contains("AQUÍ") && titulo.toLowerCase().contains("actualización")) {
                            SpannableString spannable = new SpannableString(contenido);

                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/u/1/folders/1KfZiETWGpEoFdh4CMOLVslPOrYNz5Nde"));
                                    widget.getContext().startActivity(intent);
                                }

                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    super.updateDrawState(ds);
                                    ds.setUnderlineText(true); // Subrayado opcional
                                }
                            };

                            int startIndex = contenido.indexOf("AQUÍ");
                            if (startIndex != -1) {
                                spannable.setSpan(clickableSpan, startIndex, startIndex + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            announcementContent.setText(spannable);
                            announcementContent.setMovementMethod(LinkMovementMethod.getInstance());
                        } else {
                            // Mostrar texto plano sin hipervínculo
                            announcementContent.setText(contenido);
                        }
                        announcementTitle.setText(titulo);

                    } else {
                        announcementTitle.setText("¿Tienes todo listo?");
                        announcementContent.setText("Recuerda preparar el partido del fin de semana");
                    }
                })
                .addOnFailureListener(e -> {
                    welcomeText.setText("Error al cargar nombre.");
                    Toast.makeText(this, "Error al obtener el nombre: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getprofilePhotoUrl() {
        firestore.collection("entrenadores").document(trainerID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        profilePhotoUrl = documentSnapshot.getString("profilePhotoUrl");

                    } else {
                        Log.e("Error de autenticación", "No se encuentra el usuario en la BD");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Error de autenticación", "No se encuentra el usuario en la BD");
                });
    }

    private void cargarEquiposDesdeFirestore(String trainerId) {
        showLoadingIndicator();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("entrenadores").document(trainerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Object> equipos = (List<Object>) documentSnapshot.get("equipos");
                        if (equipos != null) {
                            teamList.clear(); // Limpia la lista para evitar duplicados
                            List<String> teamIds = new ArrayList<>();
                            Map<String, Integer> teamIndexMap = new HashMap<>();
                            for (int i = 0; i < equipos.size(); i++) {
                                String teamId = String.valueOf(equipos.get(i));
                                teamIds.add(teamId);
                                teamIndexMap.put(teamId, i); // Asocia el ID al índice original
                            }

                            Map<String, Team> teamMap = new HashMap<>();
                            for (String teamId : teamIds) {
                                db.collection("equipos").document(teamId)
                                        .get()
                                        .addOnSuccessListener(teamSnapshot -> {
                                            if (teamSnapshot.exists()) {
                                                Team team = teamSnapshot.toObject(Team.class);
                                                if (team != null) {
                                                    teamMap.put(teamId, team);
                                                }
                                            }

                                            // Una vez se han procesado todos los IDs, organiza el orden
                                            if (teamMap.size() == teamIds.size()) {
                                                teamList.clear();
                                                teamListNames.clear();
                                                teamListCodes.clear();
                                                for (String id : teamIds) {
                                                    if (teamMap.containsKey(id)) {
                                                        teamList.add(teamMap.get(id));
                                                        teamListCodes.add(teamMap.get(id).getId());
                                                        teamListNames.add(teamMap.get(id).getNombre());
                                                    }
                                                }
                                                loadTeamImages(teamList);
                                                teamAdapter.notifyDataSetChanged();
                                            }

                                            hideLoadingIndicator();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("MainActivity", "Error al cargar equipo: " + e.getMessage());
                                            hideLoadingIndicator();
                                        });
                            }
                        } else {
                            hideLoadingIndicator();
                        }
                    } else {
                        hideLoadingIndicator(); // Si no existe el documento del entrenador
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingIndicator();
                    Log.e("MainActivity", "Error al cargar equipos desde Firestore: " + e.getMessage());
                });
    }

    private void mostrarOpcionesEquipo() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_team, null);

        TextView crearEquipoOpcion = bottomSheetView.findViewById(R.id.createTeamOption);
        TextView unirEquipoOpcion = bottomSheetView.findViewById(R.id.joinTeamOption);

        crearEquipoOpcion.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(MainActivity.this, CreateTeamActivity.class));
        });

        unirEquipoOpcion.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            mostrarDialogoUnirseEquipo();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void mostrarDialogoUnirseEquipo() {
        BottomSheetDialog joinTeamDialog = new BottomSheetDialog(this);
        View joinTeamView = getLayoutInflater().inflate(R.layout.dialog_join_team, null);

        EditText codeEditText = joinTeamView.findViewById(R.id.codeEditText);
        Button joinButton = joinTeamView.findViewById(R.id.joinTeamButton);

        joinButton.setOnClickListener(v -> {
            String code = codeEditText.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un código.", Toast.LENGTH_SHORT).show();
                return;
            }
            unirseEquipoPorCodigo(code);
            joinTeamDialog.dismiss();
        });

        joinTeamDialog.setContentView(joinTeamView);
        joinTeamDialog.show();
    }

    private void unirseEquipoPorCodigo(String codigo) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        firestore.collection("equipos")
                .whereEqualTo("id", codigo)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String equipoId = querySnapshot.getDocuments().get(0).getId();

                        firestore.collection("entrenadores").document(currentUser.getUid())
                                .update("equipos", FieldValue.arrayUnion(equipoId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Te has unido al equipo con éxito.", Toast.LENGTH_SHORT).show();
                                    cargarEquiposDesdeFirestore(currentUser.getUid());
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error al unirte al equipo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Código de equipo inválido.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al buscar equipo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadProfileImage() {
        File directory = new File(getFilesDir(), "img/profile_photos");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File localFile = new File(directory, trainerID + "_profile.png");

        if (localFile.exists() && !isImageOutdated(localFile)) {
            // Cargar desde almacenamiento local
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            profilePhoto.setImageBitmap(bitmap);
        } else {
            // Descargar desde Firebase
            firestore.collection("entrenadores").document(trainerID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            profilePhotoUrl = documentSnapshot.getString("profilePhotoUrl");
                            long serverTimestamp = documentSnapshot.getLong("timestamp");

                            if (profilePhotoUrl != null) {
                                Glide.with(this)
                                        .asBitmap()
                                        .load(profilePhotoUrl)
                                        .placeholder(R.drawable.ic_loading)
                                        .error(R.drawable.ic_profile)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                profilePhoto.setImageBitmap(resource);
                                                saveImageLocally(resource, localFile, serverTimestamp);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("MainActivity", "Error cargando imagen de perfil", e));
        }
    }

    private void saveImageLocally(Bitmap bitmap, File file, long timestamp) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Cambiar el formato a PNG para preservar transparencia
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            // Guardar el timestamp asociado
            File timestampFile = new File(file.getParent(), trainerID + "_profile_timestamp.txt");
            try (FileOutputStream tsFos = new FileOutputStream(timestampFile)) {
                tsFos.write(String.valueOf(timestamp).getBytes());
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Error guardando imagen localmente", e);
        }
    }

    private boolean isImageOutdated(File file) {
        File timestampFile = new File(file.getParent(), trainerID + "_profile_timestamp.txt");
        if (timestampFile.exists()) {
            try {
                long localTimestamp = Long.parseLong(new String(java.nio.file.Files.readAllBytes(timestampFile.toPath())));
                firestore.collection("entrenadores").document(trainerID)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            long serverTimestamp = documentSnapshot.getLong("timestamp");
                            if (serverTimestamp > localTimestamp) {
                                file.delete();
                                timestampFile.delete();
                            }
                        });
                return false;
            } catch (IOException | NumberFormatException e) {
                Log.e("MainActivity", "Error leyendo timestamp local", e);
            }
        }
        return true;
    }

    private void loadTeamImages(List<Team> teams) {
        File directory = new File(getFilesDir(), "img/team_images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (Team team : teams) {
            String teamId = team.getId();
            String imageUrl = team.getUrl_imagen();
            File localFile = new File(directory, teamId + "_team.png");

            if (localFile.exists() && !isImageOutdated(localFile, team.getTimestamp())) {
                Log.d("MainActivity", "Imagen del equipo " + teamId + " cargada desde almacenamiento local");
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                saveTeamImageLocally(resource, localFile, team.getTimestamp());
                            }

                            @Override
                            public void onLoadCleared(Drawable placeholder) {
                                // No hacer nada
                            }
                        });
            }
        }
    }

    private void saveTeamImageLocally(Bitmap bitmap, File file, long timestamp) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            File timestampFile = new File(file.getParent(), file.getName().replace("_team.png", "_timestamp.txt"));
            try (FileOutputStream tsFos = new FileOutputStream(timestampFile)) {
                tsFos.write(String.valueOf(timestamp).getBytes());
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Error guardando imagen del equipo localmente", e);
        }
    }

    private boolean isImageOutdated(File file, long serverTimestamp) {
        File timestampFile = new File(file.getParent(), file.getName().replace("_team.png", "_timestamp.txt"));
        if (timestampFile.exists()) {
            try {
                long localTimestamp = Long.parseLong(new String(java.nio.file.Files.readAllBytes(timestampFile.toPath())));
                return serverTimestamp > localTimestamp;
            } catch (IOException | NumberFormatException e) {
                Log.e("MainActivity", "Error leyendo timestamp local", e);
            }
        }
        return true;
    }

    private void actualizarTokenFCM(String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "No se pudo obtener el token", task.getException());
                        return;
                    }

                    String newToken = task.getResult();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("tokens").document(userId)
                            .set(Collections.singletonMap("token", newToken))
                            .addOnSuccessListener(aVoid -> Log.d("FCM", "Token actualizado en Firestore"))
                            .addOnFailureListener(e -> Log.w("FCM", "Error al actualizar token", e));
                });
    }
}
