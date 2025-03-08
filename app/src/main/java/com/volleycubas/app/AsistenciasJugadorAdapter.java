package com.volleycubas.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AsistenciasJugadorAdapter extends RecyclerView.Adapter<AsistenciasJugadorAdapter.AsistenciaViewHolder> {
    private List<Asistencia> asistenciasList;

    public AsistenciasJugadorAdapter(List<Asistencia> asistenciasList) {
        this.asistenciasList = asistenciasList;
    }

    @NonNull
    @Override
    public AsistenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asistencias_jugador, parent, false);
        return new AsistenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AsistenciaViewHolder holder, int position) {
        Asistencia asistencia = asistenciasList.get(position);

        // Obtener y formatear la fecha de "dd/MM/yyyy" a "dd/MM"
        String fecha = asistencia.getFecha();
        fecha = fecha.replace("-", "/");
        if (fecha.contains("/")) {
            String[] partes = fecha.split("/");
            if (partes.length == 3) {
                fecha = partes[0] + "/" + partes[1]; // Solo día y mes
            }
        }

        // Obtener el tipo, asegurando que si es "Entrenamiento" se muestre como "Entreno"
        String tipo = asistencia.getTipo().equalsIgnoreCase("Entrenamiento") ? "Entreno" : asistencia.getTipo();

        // Establecer los valores en el ViewHolder
        holder.tvFecha.setText(fecha);
        holder.tvTipo.setText(tipo);

        if (asistencia.getAsistencia() != null && asistencia.getAsistencia()) {
            holder.ivAsistencia.setImageResource(R.drawable.ic_check);
            holder.ivAsistencia.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.green));
        } else {
            holder.ivAsistencia.setImageResource(R.drawable.ic_cross);
            holder.ivAsistencia.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return asistenciasList.size();
    }

    public static class AsistenciaViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvTipo;
        ImageView ivAsistencia;

        public AsistenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            ivAsistencia = itemView.findViewById(R.id.ivAsistencia);
        }
    }
}
