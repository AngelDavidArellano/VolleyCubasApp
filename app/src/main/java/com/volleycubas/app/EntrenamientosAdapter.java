package com.volleycubas.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EntrenamientosAdapter extends RecyclerView.Adapter<EntrenamientosAdapter.EntrenamientoViewHolder> {

    private List<Entrenamiento> entrenamientos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Entrenamiento entrenamiento);
    }

    public EntrenamientosAdapter(List<Entrenamiento> entrenamientos, OnItemClickListener listener) {
        this.entrenamientos = entrenamientos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntrenamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrenamientos, parent, false);
        return new EntrenamientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrenamientoViewHolder holder, int position) {
        Entrenamiento entrenamiento = entrenamientos.get(position);
        holder.name.setText(entrenamiento.getTitulo());
        holder.creator.setText("Creador/a: " + entrenamiento.getCreador());
        holder.type.setText("Tipo: " + entrenamiento.getTipo());


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EntrenamientoDetailsActivity.class);
            intent.putExtra("id", entrenamiento.getId());
            intent.putExtra("titulo", entrenamiento.getTitulo());
            intent.putExtra("creador", entrenamiento.getCreador());
            intent.putExtra("fechaCreacion", entrenamiento.getFechaCreacion());
            intent.putExtra("tipo", entrenamiento.getTipo());
            intent.putExtra("descripcion", entrenamiento.getDescripcion());
            intent.putStringArrayListExtra("ejercicios", new ArrayList<>(entrenamiento.getEjercicios()));
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return entrenamientos.size();
    }

    static class EntrenamientoViewHolder extends RecyclerView.ViewHolder {
        TextView name, creator, type;

        public EntrenamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvEntrenamientoName);
            creator = itemView.findViewById(R.id.tvEntrenamientoCreator);
            type = itemView.findViewById(R.id.tvEntrenamientoType);
        }
    }
}
