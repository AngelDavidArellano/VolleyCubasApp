package com.volleycubas.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClasificacionAdapter extends RecyclerView.Adapter<ClasificacionAdapter.ViewHolder> {

    private List<Clasificacion> listaClasificacion;

    public ClasificacionAdapter(List<Clasificacion> listaClasificacion) {
        this.listaClasificacion = listaClasificacion;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clasificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Clasificacion equipo = listaClasificacion.get(position);

        holder.txtPosicion.setText(String.valueOf(position + 1));
        holder.txtEquipo.setText(equipo.getEquipo());
        holder.txtPJ.setText(String.valueOf(equipo.getJugados()));
        holder.txtSetsFavor.setText(String.valueOf(equipo.getSetsAFavor()));
        holder.txtSetsContra.setText(String.valueOf(equipo.getSetsEnContra()));
        holder.txtGanados.setText(String.valueOf(equipo.getGanados()));
        holder.txtPerdidos.setText(String.valueOf(equipo.getPerdidos()));

        // Diferencia de puntos (Puntos a favor - Puntos en contra)
        int puntosDiferencia = equipo.getPuntosAFavor() - equipo.getPuntosEnContra();
        holder.txtPtsDiferencia.setText(String.valueOf(puntosDiferencia));
    }

    @Override
    public int getItemCount() {
        return listaClasificacion.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPosicion, txtEquipo, txtPJ, txtSetsFavor, txtSetsContra, txtGanados, txtPerdidos, txtPtsDiferencia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPosicion = itemView.findViewById(R.id.txtPosicion);
            txtEquipo = itemView.findViewById(R.id.txtEquipo);
            txtPJ = itemView.findViewById(R.id.txtPJ);
            txtSetsFavor = itemView.findViewById(R.id.txtSetsFavor);
            txtSetsContra = itemView.findViewById(R.id.txtSetsContra);
            txtGanados = itemView.findViewById(R.id.txtGanados);
            txtPerdidos = itemView.findViewById(R.id.txtPerdidos);
            txtPtsDiferencia = itemView.findViewById(R.id.txtPtsDiferencia);
        }
    }
}
