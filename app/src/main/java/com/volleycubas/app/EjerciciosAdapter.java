package com.volleycubas.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class EjerciciosAdapter extends RecyclerView.Adapter<EjerciciosAdapter.EjercicioViewHolder> {

    private List<Ejercicio> ejercicios;
    private OnItemClickListener listener;

    // Interfaz para manejar clics en los ejercicios
    public interface OnItemClickListener {
        void onItemClick(Ejercicio ejercicio);
    }

    // Constructor del adaptador
    public EjerciciosAdapter(List<Ejercicio> ejercicios, OnItemClickListener listener) {
        this.ejercicios = ejercicios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicios, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        Ejercicio ejercicio = ejercicios.get(position);

        // Configurar los valores de cada elemento
        holder.name.setText(ejercicio.getTitulo());
        holder.creator.setText("Creador/a: " + ejercicio.getCreador());
        holder.type.setText("Tipo: " + ejercicio.getTipo());

        // Manejo de imágenes
        File directory = new File(holder.itemView.getContext().getFilesDir(), "img/exercises_images");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File localFile = new File(directory, ejercicio.getId() + "_exercise.png");

        if (localFile.exists() && !isImageOutdated(localFile, ejercicio.getTimestamp())) {
            Log.d("EjerciciosAdapter", "Imagen local está actualizada: " + localFile.getAbsolutePath());
            ejercicio.setUrlImagen(localFile.getAbsolutePath()); // Actualizar la URL localmente
        } else if (ejercicio.getUrlImagen() != null && !ejercicio.getUrlImagen().isEmpty()) {
            Log.d("EjerciciosAdapter", "Descargando imagen desde URL: " + ejercicio.getUrlImagen());
            downloadImage(ejercicio.getUrlImagen(), localFile, ejercicio);
        } else {
            Log.w("EjerciciosAdapter", "El ejercicio no tiene URL de imagen válida.");
        }



        // Manejar el clic en el ítem
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EjercicioDetailsActivity.class);
            intent.putExtra("titulo", ejercicio.getTitulo());
            intent.putExtra("creador", ejercicio.getCreador());
            intent.putExtra("tipo", ejercicio.getTipo());
            intent.putExtra("descripcion", ejercicio.getDescripcion());
            intent.putExtra("url_imagen", ejercicio.getUrlImagen());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    private void downloadImage(String url, File localFile, Ejercicio ejercicio) {
        new Thread(() -> {
            try {
                java.net.URL imageUrl = new java.net.URL(url);
                Bitmap bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());

                // Guardar imagen localmente
                saveExerciseImageLocally(bitmap, localFile, ejercicio.getTimestamp());

                // Actualizar la URL local en el objeto ejercicio
                ejercicio.setUrlImagen(localFile.getAbsolutePath());

                Log.d("EjerciciosAdapter", "Imagen descargada y guardada: " + localFile.getAbsolutePath());
            } catch (Exception e) {
                Log.e("EjerciciosAdapter", "Error al descargar la imagen desde URL: " + url, e);
            }
        }).start();
    }

    private void saveExerciseImageLocally(Bitmap bitmap, File file, long timestamp) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            File timestampFile = new File(file.getParent(), file.getName().replace("_exercise.png", "_timestamp.txt"));
            try (FileOutputStream tsFos = new FileOutputStream(timestampFile)) {
                tsFos.write(String.valueOf(timestamp).getBytes());
            }
        } catch (IOException e) {
            Log.e("EjerciciosAdapter", "Error guardando imagen del ejercicio localmente.", e);
        }
    }

    private boolean isImageOutdated(File file, long serverTimestamp) {
        File timestampFile = new File(file.getParent(), file.getName().replace("_exercise.png", "_timestamp.txt"));
        if (timestampFile.exists()) {
            try {
                long localTimestamp = Long.parseLong(new String(java.nio.file.Files.readAllBytes(timestampFile.toPath())));
                return serverTimestamp > localTimestamp;
            } catch (IOException | NumberFormatException e) {
                Log.e("EjerciciosAdapter", "Error leyendo timestamp local.", e);
            }
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    // Clase ViewHolder para manejar las vistas de cada ítem
    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        TextView name, creator, type;
        ImageView image;

        public EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvEjerciciosName);
            creator = itemView.findViewById(R.id.tvEjerciciosCreator);
            type = itemView.findViewById(R.id.tvEjerciciosType);
            //image = itemView.findViewById(R.id.ivEjercicioImage);
        }
    }
}
