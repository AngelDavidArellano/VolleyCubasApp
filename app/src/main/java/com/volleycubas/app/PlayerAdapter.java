package com.volleycubas.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private List<Jugador> jugadoresList;
    private OnPlayerClickListener listener;

    // Constructor
    public PlayerAdapter(List<Jugador> jugadoresList, OnPlayerClickListener listener) {
        this.jugadoresList = jugadoresList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Jugador jugador = jugadoresList.get(position);

        // Configurar los datos del jugador en la vista
        holder.nameTextView.setText(jugador.getNombre());
        holder.positionTextView.setText(jugador.getNumero() + " - " + jugador.getPosicion());

        // Configurar el clic
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayerClick(jugador);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jugadoresList.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView positionTextView;
        ImageView arrowIcon;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.playerName);
            positionTextView = itemView.findViewById(R.id.playerPosition_Number);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }

    // Interfaz para manejar clics en los jugadores
    public interface OnPlayerClickListener {
        void onPlayerClick(Jugador jugador);
    }
}
