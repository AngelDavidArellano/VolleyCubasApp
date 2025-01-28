package com.volleycubas.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class EjercicioDetailsActivity extends AppCompatActivity {

    private TextView tvExerciseTitle, tvExerciseCreator, tvExerciseType, tvExerciseDescription;
    private ImageView ivExerciseImage, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio_details);

        // Enlazar vistas
        tvExerciseTitle = findViewById(R.id.tvExerciseTitle);
        tvExerciseCreator = findViewById(R.id.tvExerciseCreator);
        tvExerciseType = findViewById(R.id.tvExerciseType);
        tvExerciseDescription = findViewById(R.id.tvExerciseDescription);
        ivExerciseImage = findViewById(R.id.ivExerciseImage);
        backButton = findViewById(R.id.back_button);

        // Configurar el botón de retroceso
        backButton.setOnClickListener(v -> onBackPressed());

        // Obtener los datos del Intent
        String titulo = getIntent().getStringExtra("titulo");
        String creador = getIntent().getStringExtra("creador");
        String tipo = getIntent().getStringExtra("tipo");
        String descripcion = getIntent().getStringExtra("descripcion");
        String urlImagen = getIntent().getStringExtra("url_imagen");

        // Configurar los datos en la vista
        tvExerciseTitle.setText(titulo);
        tvExerciseCreator.setText("Creador/a: " + creador);
        tvExerciseType.setText("Tipo: " + tipo);
        tvExerciseDescription.setText(descripcion);

        Log.d("URL ejercicio:", urlImagen != null ? urlImagen : "URL vacía");

        // Cargar la imagen con Glide
        if (urlImagen != null && !urlImagen.isEmpty()) {
            Glide.with(this)
                    .load(urlImagen)
                    .placeholder(R.drawable.ic_image_placeholder) // Imagen de carga por defecto
                    .into(ivExerciseImage);
        } else {
            ivExerciseImage.setImageResource(R.drawable.ic_image_placeholder); // Imagen por defecto
        }
    }
}
