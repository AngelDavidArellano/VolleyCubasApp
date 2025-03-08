package com.volleycubas.app;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LineupAdapter extends RecyclerView.Adapter<LineupAdapter.LineupViewHolder> {
    private List<Alineacion> alineacionesList;
    private OnLineupClickListener listener;
    private String teamId;

    // Interfaz para clic en alineación
    public interface OnLineupClickListener {
        void onLineupClick(Alineacion alineacion);
    }

    // Constructor
    public LineupAdapter(List<Alineacion> alineacionesList, OnLineupClickListener listener, String teamId) {
        this.alineacionesList = alineacionesList;
        this.listener = listener;
        this.teamId = teamId;
    }

    public void setOnItemClickListener(OnLineupClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LineupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lineup, parent, false);
        return new LineupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LineupViewHolder holder, int position) {
        Alineacion alineacion = alineacionesList.get(position);
        holder.tvName.setText(alineacion.getNombre());
        holder.tvCreator.setText("Creador/a: " + alineacion.getCreador());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLineupClick(alineacion);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            mostrarDialogoConfirmacion(holder.itemView.getContext(), position, alineacion.getId());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return alineacionesList.size();
    }

    public static class LineupViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCreator;

        public LineupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvLineupName);
            tvCreator = itemView.findViewById(R.id.tvLineupCreator);
        }
    }

    private void mostrarDialogoConfirmacion(Context context, int position, String alineacionId) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar alineación")
                .setMessage("¿Quieres eliminar esta alineación del equipo?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarAlineacionDelEquipo(position, alineacionId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Método para eliminar solo el ID del array de alineaciones en Firestore
    private void eliminarAlineacionDelEquipo(int position, String alineacionId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("equipos").document(teamId)
                .update("alineaciones", FieldValue.arrayRemove(alineacionId))
                .addOnSuccessListener(aVoid -> {
                    alineacionesList.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Log.e("LineupAdapter", "Error al eliminar alineación del equipo: " + e.getMessage());
                });
    }
}
