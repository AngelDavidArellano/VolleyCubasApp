package com.volleycubas.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.volleycubas.app.R;

import java.util.List;
import java.util.Map;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GameViewHolder> {

    private final List<Map<String, Object>> matches;

    public GamesAdapter(List<Map<String, Object>> matches) {
        this.matches = matches;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_next_games, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Map<String, Object> match = matches.get(position);
        String fecha = (String) match.get("fecha");
        String rival = (String) match.get("rival");

        holder.tvFecha.setText(fecha != null ? fecha : "Fecha desconocida");
        holder.tvNextGame.setText(rival != null ? "Rival: " + rival : "Rival desconocido");
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {

        TextView tvFecha, tvNextGame;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_next_match_date);
            tvNextGame = itemView.findViewById(R.id.tv_next_match);
        }
    }
}
