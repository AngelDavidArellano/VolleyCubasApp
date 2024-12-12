package com.volleycubas.app;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private List<Partido> historialPartidos;
    private OnPartidoClickListener onPartidoClickListener;
    private String teamId;

    public interface OnPartidoClickListener {
        void onPartidoClick(Partido partido);
    }

    public HistorialAdapter(List<Partido> historialPartidos, OnPartidoClickListener onPartidoClickListener, String teamId) {
        this.historialPartidos = historialPartidos;
        this.onPartidoClickListener = onPartidoClickListener;
        this.teamId = teamId;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        Partido partido = historialPartidos.get(position);
        holder.bind(partido);

        holder.itemView.setOnClickListener(v -> MatchDetailsActivity.start(v.getContext(), partido, teamId));
    }


    @Override
    public int getItemCount() {
        return historialPartidos.size();
    }

    class HistorialViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRival_Resultado, tvFecha;
        private View color_line;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRival_Resultado = itemView.findViewById(R.id.tv_rival_resultado);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            color_line = itemView.findViewById(R.id.color_line);
        }

        public void bind(Partido partido) {
            String resultado = "(" + partido.getSetsAFavor() + " - " + partido.getSetsEnContra() + ")";
            tvRival_Resultado.setText(partido.getRival() + " " + resultado);
            tvFecha.setText(partido.getFecha());

            if (partido.getSetsAFavor() > partido.getSetsEnContra()) {
                color_line.setBackgroundResource(R.color.blue);
            } else {
                color_line.setBackgroundResource(R.color.red);
            }

            itemView.setOnClickListener(v -> onPartidoClickListener.onPartidoClick(partido));
        }

    }
}
