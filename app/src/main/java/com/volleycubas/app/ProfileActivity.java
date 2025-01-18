package com.volleycubas.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private String trainerID, nombreApellidosEntrenador, email, profilePhotoUrl;
    private ArrayList<String> teamListNames, teamListCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance(); // Inicializar FirebaseAuth

        trainerID = getIntent().getStringExtra("trainerID");
        nombreApellidosEntrenador = getIntent().getStringExtra("nombreCompleto");
        email = getIntent().getStringExtra("email");
        profilePhotoUrl = getIntent().getStringExtra("profilePhotoUrl");

        teamListNames = getIntent().getStringArrayListExtra("listaEquiposNombres");
        teamListCodes = getIntent().getStringArrayListExtra("listaEquiposCodigos");

        if (teamListNames != null && teamListCodes != null) {
            int i = 0;
            for (String name : teamListNames) {
                Log.d("Equipo recibido", name + " (" + teamListCodes.get(i) + ")");
                i++;
            }
        }

        // Configurar vistas
        ImageView profilePicture = findViewById(R.id.profile_picture);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvManageTeams = findViewById(R.id.tvManageTeams);
        TextView tvInstagram = findViewById(R.id.tvInstagram);
        Button logout = findViewById(R.id.btn_logout);
        Switch switchDarkMode = findViewById(R.id.switch_dark_mode);

        // Cargar foto de perfil con Glide
        loadProfileImage(profilePicture);

        // Mostrar nombre y correo
        tvName.setText(nombreApellidosEntrenador);
        tvEmail.setText(email);

        // Listener para el botón de cerrar sesión
        logout.setOnClickListener(v -> {
            // Cerrar la sesión
            mAuth.signOut();
            MainActivity.isDataLoaded = false;
            MainActivity.isLoadingInitialized = false;

            // Redirigir al LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        tvManageTeams.setOnClickListener(v -> {
            if (teamListNames != null && teamListCodes != null) {
                // Crear un AlertDialog con RecyclerView
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Gestionar equipos");

                // Crear una vista para el RecyclerView
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_manage_teams, null);
                RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewTeams);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));

                // Configurar el adaptador personalizado
                TeamManageAdapter adapter = new TeamManageAdapter(this, teamListNames, teamListCodes, trainerID);
                recyclerView.setAdapter(adapter);

                builder.setView(dialogView);
                builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());

                builder.create().show();
            } else {
                // Mostrar mensaje si no hay equipos
                new androidx.appcompat.app.AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Sin equipos")
                        .setMessage("No hay equipos asociados a este entrenador.")
                        .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });



        // Listener para el texto de Instagram
        tvInstagram.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/voleibolcubas/?hl=es"));
            startActivity(intent);
        });

        // Listener para el switch de modo oscuro
        /*switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Cambiar el tema de la aplicación (modo oscuro/claro)
            if (isChecked) {
                // Activar modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                // Desactivar modo oscuro
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });*/

        // Botón de regreso
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // Finalizar actividad y regresar
            finish();
        });
    }

    private void loadProfileImage(ImageView profilePicture) {
        File directory = new File(getFilesDir(), "img/profile_photos");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File localFile = new File(directory, trainerID + "_profile.png");

        Log.d("ProfileActivity", "Directorio de almacenamiento: " + directory.getAbsolutePath());
        Log.d("ProfileActivity", "Archivo local esperado: " + localFile.getAbsolutePath());

        if (localFile.exists() && !isImageOutdated(localFile)) {
            // Cargar desde almacenamiento local
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            if (bitmap != null) {
                Log.d("ProfileActivity", "Imagen cargada desde almacenamiento local");
                profilePicture.setImageBitmap(bitmap);
            } else {
                Log.e("ProfileActivity", "Error al decodificar la imagen desde almacenamiento local");
            }
        } else if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
            Log.d("ProfileActivity", "Descargando imagen desde URL: " + profilePhotoUrl);
            // Descargar desde Firebase
            Glide.with(this)
                    .asBitmap()
                    .load(profilePhotoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            Log.d("ProfileActivity", "Imagen descargada con éxito desde Firebase");
                            profilePicture.setImageBitmap(resource);
                            saveImageLocally(resource, localFile);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            Log.d("ProfileActivity", "Glide: recurso limpiado");
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Log.e("ProfileActivity", "Error al descargar la imagen con Glide");
                        }
                    });
        } else {
            Log.w("ProfileActivity", "URL de imagen de perfil nulo o vacío. Usando imagen predeterminada");
            profilePicture.setImageResource(R.drawable.ic_profile);
        }
    }

    private void saveImageLocally(Bitmap bitmap, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            // Guardar el timestamp asociado
            File timestampFile = new File(file.getParent(), trainerID + "_profile_timestamp.txt");
            try (FileOutputStream tsFos = new FileOutputStream(timestampFile)) {
                long currentTimestamp = System.currentTimeMillis();
                tsFos.write(String.valueOf(currentTimestamp).getBytes());
            }
        } catch (IOException e) {
            Log.e("ProfileActivity", "Error guardando imagen localmente", e);
        }
    }

    private boolean isImageOutdated(File file) {
        File timestampFile = new File(file.getParent(), trainerID + "_profile_timestamp.txt");
        if (timestampFile.exists()) {
            try {
                long localTimestamp = Long.parseLong(new String(java.nio.file.Files.readAllBytes(timestampFile.toPath())));
                long serverTimestamp = System.currentTimeMillis(); // Simular obtención desde Firebase
                return serverTimestamp > localTimestamp;
            } catch (IOException | NumberFormatException e) {
                Log.e("ProfileActivity", "Error leyendo timestamp local", e);
            }
        }
        return true;
    }
}
