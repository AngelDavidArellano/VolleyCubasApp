package com.volleycubas.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EntrenamientosAdapter extends RecyclerView.Adapter<EntrenamientosAdapter.EntrenamientoViewHolder> {

    private List<Entrenamiento> entrenamientos;
    private OnItemClickListener listener;
    private String teamId;


    public interface OnItemClickListener {
        void onItemClick(Entrenamiento entrenamiento);
    }

    public EntrenamientosAdapter(List<Entrenamiento> entrenamientos, OnItemClickListener listener, String teamId) {
        this.entrenamientos = entrenamientos;
        this.listener = listener;
        this.teamId = teamId;
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

        holder.itemView.setOnLongClickListener(v -> {
            mostrarDialogoConfirmacion(holder.itemView.getContext(), position, entrenamiento.getId());
            return true;
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

    private void mostrarDialogoConfirmacion(Context context, int position, String entrenamientoId) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar entrenamiento")
                .setMessage("¿Quieres eliminar este entrenamiento del equipo?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarEntrenamientoDelEquipo(position, entrenamientoId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Método para eliminar solo el ID del array de entrenamientos en Firestore
    private void eliminarEntrenamientoDelEquipo(int position, String entrenamientoId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos").document(teamId)
                .update("entrenamientos", FieldValue.arrayRemove(entrenamientoId))
                .addOnSuccessListener(aVoid -> {
                    entrenamientos.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrenamientosAdapter", "Error al eliminar entrenamiento del equipo: " + e.getMessage());
                });
    }
}
