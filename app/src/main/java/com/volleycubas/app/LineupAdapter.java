package com.volleycubas.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LineupAdapter extends RecyclerView.Adapter<LineupAdapter.LineupViewHolder> {
    private List<Alineacion> alineacionesList;
    private OnLineupClickListener listener;

    // Interfaz para clic en alineación
    public interface OnLineupClickListener {
        void onLineupClick(Alineacion alineacion);
    }

    // Constructor
    public LineupAdapter(List<Alineacion> alineacionesList, OnLineupClickListener listener) {
        this.alineacionesList = alineacionesList;
        this.listener = listener;
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
}
