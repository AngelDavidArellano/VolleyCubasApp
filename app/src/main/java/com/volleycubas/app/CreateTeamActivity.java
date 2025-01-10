package com.volleycubas.app;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CreateTeamActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText teamNameEditText;
    private Button uploadImageButton, createTeamButton;
    private ImageView teamImageView, backButton;
    private TextView teamNameTextView, teamPlayersCountTextView;

    private ProgressBar loadingBar;
    private TextView loadingText;
    private ImageView loadingGif;
    private View loadingOverlay;

    private Uri imageUri;
    private String uploadedImageUrl;

    private FirebaseStorage storage;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        // Inicializar vistas
        teamNameEditText = findViewById(R.id.teamNameEditText);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        createTeamButton = findViewById(R.id.createTeamButton);
        teamImageView = findViewById(R.id.teamImage);
        teamNameTextView = findViewById(R.id.teamName);
        teamPlayersCountTextView = findViewById(R.id.teamPlayersCount);
        backButton = findViewById(R.id.back_button);
        loadingBar = findViewById(R.id.loadingBar_create_team);
        loadingText = findViewById(R.id.progress_text);
        loadingGif = findViewById(R.id.loadingGif_create_team);
        loadingOverlay = findViewById(R.id.loadingOverlay_historial);

        // Inicializar Firebase
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Vista previa
        teamNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                teamNameTextView.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Botón para subir imagen
        uploadImageButton.setOnClickListener(v -> selectImage());

        // Botón para crear equipo
        createTeamButton.setOnClickListener(v -> createTeam());

        // Configurar botón de volver atrás
        backButton.setOnClickListener(v -> finish());

        setupManualRotation();
    }

    private void showLoadingIndicator() {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingGif.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);

        backButton.setEnabled(false);
        uploadImageButton.setEnabled(false);
        createTeamButton.setEnabled(false);
    }

    private void hideLoadingIndicator() {
        loadingOverlay.setVisibility(View.GONE);
        loadingGif.setVisibility(View.GONE);
        loadingBar.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);

        backButton.setEnabled(true);
        uploadImageButton.setEnabled(true);
        createTeamButton.setEnabled(true);
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
        loadingGif.startAnimation(rotate);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // Redimensionar la imagen seleccionada
                Bitmap resizedBitmap = resizeBitmap(bitmap, 1000, 1000);
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Recortar la imagen al cuadrado
                int width = originalBitmap.getWidth();
                int height = originalBitmap.getHeight();
                int newDimension = Math.min(width, height); // Selecciona el lado más corto
                int xOffset = (width - newDimension) / 2;
                int yOffset = (height - newDimension) / 2;

                Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, newDimension, newDimension);
                teamImageView.setImageBitmap(croppedBitmap);

                // Guardar la imagen redimensionada para la subida
                imageUri = getImageUriFromBitmap(resizedBitmap);

            } catch (IOException e) {
                Log.e("CreateTeamActivity", "Error al cargar imagen", e);
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
            // Opcional: establecer una imagen por defecto
            teamImageView.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    // Método para redimensionar la imagen seleccionada
    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap; // No necesita redimensionar
        }

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    // Método para obtener un Uri de la imagen redimensionada
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "ResizedImage", null);
        return Uri.parse(path);
    }

    private Bitmap resizeAndCompressImage(Uri imageUri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Leer dimensiones iniciales
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        // Recortar la imagen al cuadrado
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        int newDimension = Math.min(width, height); // Selecciona el lado más corto
        int xOffset = (width - newDimension) / 2;
        int yOffset = (height - newDimension) / 2;

        Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, newDimension, newDimension);

        // Redimensionar la imagen recortada
        options.inJustDecodeBounds = false;
        int maxWidth = 1024;
        int maxHeight = 1024;

        int originalWidth = croppedBitmap.getWidth();
        int originalHeight = croppedBitmap.getHeight();

        int inSampleSize = 1;

        if (originalHeight > maxHeight || originalWidth > maxWidth) {
            final int halfHeight = originalHeight / 2;
            final int halfWidth = originalWidth / 2;

            while ((halfHeight / inSampleSize) > maxHeight && (halfWidth / inSampleSize) > maxWidth) {
                inSampleSize *= 2;
            }
        }

        // Escalar la imagen al tamaño deseado
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, maxWidth, maxHeight, true);

        // Comprimir la imagen si supera 1 MB
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100; // Comenzar con máxima calidad
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        while (baos.toByteArray().length > 1_000_000 && quality > 5) {
            baos.reset();
            quality -= 5; // Reducir calidad en 5%
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        return resizedBitmap;
    }

    private void uploadImageToFirebase(String teamId, byte[] imageData, OnCompleteListener<Uri> onCompleteListener) {
        if (imageData == null) {
            TaskCompletionSource<Uri> taskCompletionSource = new TaskCompletionSource<>();
            taskCompletionSource.setResult(null);
            taskCompletionSource.getTask().addOnCompleteListener(onCompleteListener);
            return;
        }

        StorageReference storageRef = storage.getReference().child("teams/" + teamId + "/team_image.jpg");

        UploadTask uploadTask = storageRef.putBytes(imageData);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(onCompleteListener);
    }

    private void createTeam() {
        String teamName = teamNameEditText.getText().toString().trim();

        if (teamName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un nombre para el equipo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Por favor, selecciona una imagen para el equipo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generar código único de seis caracteres (100000 a 999999)
        int uniqueCode = 100000 + (int) (Math.random() * 900000);
        String teamCode = String.valueOf(uniqueCode);

        // Obtener el ID del entrenador autenticado
        String trainerId = getTrainerId();

        if (trainerId == null) {
            Toast.makeText(this, "Error: No se pudo obtener el ID del entrenador", Toast.LENGTH_SHORT).show();
            return;
        }

        String teamId = teamCode; // ID del equipo basado en el código

        // Mostrar el indicador de carga
        showLoadingIndicator();

        new Thread(() -> {
            try {
                // Etapa 1: Recortar y redimensionar la imagen
                runOnUiThread(() -> updateProgress(0, "Recortando y redimensionando la imagen..."));
                Bitmap processedBitmap = resizeAndCompressImage(imageUri);

                // Preparar los bytes de la imagen para subir
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                runOnUiThread(() -> updateProgress(33, "Preparando para subir la imagen..."));

                // Etapa 2: Subir la imagen
                uploadImageToFirebase(teamId, imageData, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        uploadedImageUrl = task.getResult().toString();
                    } else {
                        uploadedImageUrl = null; // Usar una imagen por defecto si falla
                    }

                    runOnUiThread(() -> updateProgress(66, "Guardando los datos del equipo..."));

                    // Etapa 3: Guardar datos del equipo en Firestore
                    Team team = new Team();
                    team.setId(teamId);
                    team.setNombre(teamName);
                    team.setUrl_imagen(uploadedImageUrl);
                    team.setMejor_racha(0);
                    team.setPuntos_a_favor(0);
                    team.setPuntos_totales(0);
                    team.setSets_a_favor(0);
                    team.setSets_totales(0);
                    team.setPartidos_jugados(0);
                    team.setPartidos_ganados(0);
                    team.setRival_fecha_mejor_racha("No hay registros");

                    firestore.collection("equipos").document(teamId)
                            .set(team)
                            .addOnSuccessListener(aVoid -> {
                                firestore.collection("entrenadores").document(trainerId)
                                        .update("equipos", FieldValue.arrayUnion(teamCode))
                                        .addOnSuccessListener(aVoid1 -> {
                                            runOnUiThread(() -> {
                                                updateProgress(100, "¡Equipo creado con éxito!");
                                                Toast.makeText(this, "Equipo creado con éxito", Toast.LENGTH_SHORT).show();
                                                MainActivity.isDataLoaded = false;
                                                finish();
                                            });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("CreateTeamActivity", "Error al agregar equipo al entrenador", e);
                                            runOnUiThread(() -> {
                                                hideLoadingIndicator();
                                                Toast.makeText(this, "Error al agregar el equipo al entrenador", Toast.LENGTH_SHORT).show();
                                            });
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("CreateTeamActivity", "Error al crear equipo", e);
                                runOnUiThread(() -> {
                                    hideLoadingIndicator();
                                    Toast.makeText(this, "Error al crear el equipo", Toast.LENGTH_SHORT).show();
                                });
                            });
                    });

            } catch (IOException e) {
                Log.e("CreateTeamActivity", "Error al procesar la imagen", e);
                runOnUiThread(() -> {
                    hideLoadingIndicator();
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateProgress(int progress, String message) {
        int currentProgress = loadingBar.getProgress();

        // Animación suave del progreso
        ValueAnimator animator = ValueAnimator.ofInt(currentProgress, progress);
        animator.setDuration(500); // Duración de la animación
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            loadingBar.setProgress(animatedValue);
        });
        animator.start();

        // Actualizar mensaje de progreso
        loadingText.setText(message);
    }


    private String getTrainerId() {
        // Devuelve el UID del usuario autenticado actualmente
        return FirebaseAuth.getInstance().getUid();
    }

}
