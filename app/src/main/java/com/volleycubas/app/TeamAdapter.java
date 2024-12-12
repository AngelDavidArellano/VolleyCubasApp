package com.volleycubas.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<Team> teams;
    private Context context;
    private OnTeamClickListener listener;

    // Constructor con el listener
    public TeamAdapter(List<Team> teams, Context context, OnTeamClickListener listener) {
        this.teams = teams;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);

        // Set team name and players count
        holder.teamName.setText(team.getNombre());
        holder.teamPlayersCount.setText(team.getNumero_jugadores() + " jugadores");

        // Load image using Glide
        if (team.getUrl_imagen() != null && !team.getUrl_imagen().isEmpty()) {
            Glide.with(context)
                    .load(team.getUrl_imagen()) // URL de la imagen
                    .override(500, 500) // Limitar tamaño de renderizado
                    .placeholder(R.drawable.ic_image_placeholder) // Placeholder mientras carga
                    .error(R.drawable.ic_image_placeholder) // Imagen de error
                    .into(holder.teamImage); // Cargar en el ImageView
        } else {
            holder.teamImage.setImageResource(R.drawable.ic_image_placeholder); // Imagen por defecto
        }

        // Set click listener for the team item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTeamClick(team.getId()); // Envía el ID del equipo al listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    // ViewHolder para manejar los elementos
    static class TeamViewHolder extends RecyclerView.ViewHolder {
        ImageView teamImage;
        TextView teamName, teamPlayersCount;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            teamImage = itemView.findViewById(R.id.teamImage);
            teamName = itemView.findViewById(R.id.teamName);
            teamPlayersCount = itemView.findViewById(R.id.teamPlayersCount);
        }
    }

    // Interfaz para manejar los clics en un equipo
    public interface OnTeamClickListener {
        void onTeamClick(String teamId);
    }
}
