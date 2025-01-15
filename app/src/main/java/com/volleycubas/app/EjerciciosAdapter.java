package com.volleycubas.app;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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

        // Cargar la imagen con Glide (si la URL está disponible)
        /*if (ejercicio.getUrlImagen() != null && !ejercicio.getUrlImagen().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(ejercicio.getUrlImagen())
                    .placeholder(R.drawable.ic_image_placeholder) // Imagen por defecto
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_image_placeholder); // Imagen por defecto
        }*/

        // Manejar el clic en el ítem
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EjercicioDetailsActivity.class);
            intent.putExtra("titulo", ejercicio.getTitulo());
            intent.putExtra("creador", ejercicio.getCreador());
            intent.putExtra("tipo", ejercicio.getTipo());
            intent.putExtra("descripcion", ejercicio.getDescripcion());
            intent.putExtra("urlImagen", ejercicio.getUrlImagen());
            holder.itemView.getContext().startActivity(intent);
        });
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
