package com.volleycubas.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateEjercicioActivity extends AppCompatActivity {

    private EditText etExerciseTitle, etExerciseType, etExerciseDescription;
    private ImageView ivExerciseImage, addImage, backButton;
    private Button btnSaveExercise;
    private String entrenador;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ejercicio);

        // Inicializar Firebase Firestore y Storage
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Enlazar vistas
        etExerciseTitle = findViewById(R.id.etExerciseTitle);
        etExerciseType = findViewById(R.id.etExerciseType);
        etExerciseDescription = findViewById(R.id.etExerciseDescription);
        ivExerciseImage = findViewById(R.id.ivExerciseImage);
        addImage = findViewById(R.id.addImage);
        btnSaveExercise = findViewById(R.id.btnSaveExercise);
        backButton = findViewById(R.id.back_button);

        // Configurar botón de retroceso
        backButton.setOnClickListener(v -> onBackPressed());

        // Configurar botón para añadir imagen
        addImage.setOnClickListener(v -> selectImage());

        // Configurar botón para guardar ejercicio
        btnSaveExercise.setOnClickListener(v -> saveExercise());

        obtainTrainerName();
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this).load(selectedImageUri).into(ivExerciseImage);
                }
            });

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveExercise() {
        btnSaveExercise.setEnabled(false); // Deshabilitar botón mientras se guarda

        // Validar que los campos no estén vacíos
        String title = etExerciseTitle.getText().toString().trim();
        String type = etExerciseType.getText().toString().trim();
        String description = etExerciseDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(type) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            btnSaveExercise.setEnabled(true); // Habilitar botón
            return;
        }

        // Generar un ID único de 9 dígitos para el ejercicio
        String exerciseId = generateRandomId();

        if (selectedImageUri != null) {
            // Subir la imagen a Firebase Storage
            StorageReference imageRef = storageRef.child("exercises/" + exerciseId + "/exercise_image.jpg");
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Guardar el ejercicio en Firestore con la URL de la imagen
                        saveExerciseToFirestore(exerciseId, title, type, description, uri.toString());
                        btnSaveExercise.setEnabled(true); // Habilitar botón
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSaveExercise.setEnabled(true); // Habilitar botón
                    });
        } else {
            // Guardar el ejercicio en Firestore sin imagen
            saveExerciseToFirestore(exerciseId, title, type, description, null);
            btnSaveExercise.setEnabled(true); // Habilitar botón
        }
    }


    private void saveExerciseToFirestore(String exerciseId, String title, String type, String description, String imageUrl) {
        Map<String, Object> ejercicio = new HashMap<>();
        ejercicio.put("ID", exerciseId);
        ejercicio.put("titulo", title);
        ejercicio.put("creador", entrenador);
        ejercicio.put("tipo", type);
        ejercicio.put("descripcion", description);
        ejercicio.put("url_imagen", imageUrl);
        ejercicio.put("timestamp", System.currentTimeMillis());

        db.collection("ejercicios").document(exerciseId)
                .set(ejercicio)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ejercicio guardado correctamente.", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar la actividad
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar el ejercicio: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String generateRandomId() {
        Random random = new Random();
        return String.valueOf(100000000 + random.nextInt(900000000)); // Genera un número de 9 dígitos
    }

    private void obtainTrainerName (){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtener el nombre del creador desde la colección "entrenadores"
        db.collection("entrenadores").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        entrenador = documentSnapshot.getString("nombre");
                    } else {
                        Toast.makeText(this, "Usuario no encontrado en entrenadores.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos del creador: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }

}
