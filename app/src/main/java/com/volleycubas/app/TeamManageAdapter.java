package com.volleycubas.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class TeamManageAdapter extends RecyclerView.Adapter<TeamManageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> teamNames;
    private ArrayList<String> teamCodes;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String trainerId;


    public TeamManageAdapter(Context context, ArrayList<String> teamNames, ArrayList<String> teamCodes, String trainerID) {
        this.context = context;
        this.teamNames = teamNames;
        this.teamCodes = teamCodes;

        trainerId = trainerID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_team_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String teamName = teamNames.get(position);
        String teamCode = teamCodes.get(position);

        holder.tvTeamName.setText(teamName);

        // Configura el botón de eliminar
        holder.btnDeleteTeam.setOnClickListener(v -> {
            // Confirmación de eliminación
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Eliminar equipo")
                    .setMessage("¿Estás seguro de que deseas eliminar el equipo " + teamName + "?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Elimina de Firebase
                        db.collection("entrenadores").document(trainerId)
                                .update("equipos", FieldValue.arrayRemove(teamCode))
                                .addOnSuccessListener(aVoid -> {
                                    // Eliminar de las listas locales
                                    int removedPosition = holder.getAdapterPosition();
                                    teamNames.remove(removedPosition);
                                    teamCodes.remove(removedPosition);

                                    // Notificar al adaptador que el elemento fue eliminado
                                    notifyItemRemoved(removedPosition);
                                    notifyItemRangeChanged(removedPosition, getItemCount());

                                    MainActivity.isLoadingInitialized = false;
                                    Toast.makeText(context, "Equipo '" + teamName + "' eliminado correctamente", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar jugador: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return teamNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName;
        ImageView btnDeleteTeam;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            btnDeleteTeam = itemView.findViewById(R.id.btnDeleteTeam);
        }
    }
}
