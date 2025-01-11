package com.volleycubas.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private String trainerID, nombreApellidosEntrenador, email, profilePhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance(); // Inicializar FirebaseAuth

        trainerID = getIntent().getStringExtra("trainerID");
        nombreApellidosEntrenador = getIntent().getStringExtra("nombreCompleto");
        email = getIntent().getStringExtra("email");
        profilePhotoUrl = getIntent().getStringExtra("profilePhotoUrl");

        // Configurar vistas
        ImageView profilePicture = findViewById(R.id.profile_picture);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvInstagram = findViewById(R.id.tvInstagram);
        Button logout = findViewById(R.id.btn_logout);
        Switch switchDarkMode = findViewById(R.id.switch_dark_mode);

        // Cargar foto de perfil con Glide
        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
            Glide.with(this).load(profilePhotoUrl).placeholder(R.drawable.ic_profile).into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.ic_profile);
        }

        // Mostrar nombre y correo
        tvName.setText(nombreApellidosEntrenador);
        tvEmail.setText(email);

        // Listener para el botón de cerrar sesión
        logout.setOnClickListener(v -> {
            // Cerrar la sesión
            mAuth.signOut();

            // Redirigir al LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
}
