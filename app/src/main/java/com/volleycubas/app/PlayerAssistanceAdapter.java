package com.volleycubas.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlayerAssistanceAdapter extends RecyclerView.Adapter<PlayerAssistanceAdapter.PlayerAssistanceViewHolder> {

    private final List<Jugador> jugadoresList;

    public PlayerAssistanceAdapter(List<Jugador> jugadoresList) {
        this.jugadoresList = jugadoresList;
    }

    @NonNull
    @Override
    public PlayerAssistanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assistance_player, parent, false);
        return new PlayerAssistanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerAssistanceViewHolder holder, int position) {
        Jugador jugador = jugadoresList.get(position);
        holder.tvPlayerName.setText(jugador.getNombre());

        // Configurar estado inicial del RadioGroup
        if (jugador.isAsistencia() != null) {
            if (jugador.isAsistencia()) {
                holder.radioPresent.setChecked(true);
            } else {
                holder.radioAbsent.setChecked(true);
            }
        } else {
            holder.radioGroup.clearCheck();
        }

        // Listener para manejar selección de asistencia
        holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == holder.radioPresent.getId()) {
                jugador.setAsistencia(true);
            } else if (checkedId == holder.radioAbsent.getId()) {
                jugador.setAsistencia(false);
            }
        });
    }


    @Override
    public int getItemCount() {
        return jugadoresList.size();
    }

    static class PlayerAssistanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlayerName;
        RadioGroup radioGroup;
        RadioButton radioPresent, radioAbsent;

        PlayerAssistanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            radioGroup = itemView.findViewById(R.id.radio_group);
            radioPresent = itemView.findViewById(R.id.radio_present);
            radioAbsent = itemView.findViewById(R.id.radio_absent);
        }
    }
}
