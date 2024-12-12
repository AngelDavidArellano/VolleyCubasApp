package com.volleycubas.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

public class AssistanceAdapter extends RecyclerView.Adapter<AssistanceAdapter.ViewHolder> {
    private final ArrayList<Map<String, Object>> assistanceList;
    private final Context context;
    private final String teamId;


    public AssistanceAdapter(Context context, ArrayList<Map<String, Object>> assistanceList, String teamId) {
        this.context = context;
        this.assistanceList = assistanceList;
        this.teamId = teamId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assistance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> assistanceData = assistanceList.get(position);

        String type = (String) assistanceData.get("tipo");
        String fecha = (String) assistanceData.get("fecha");
        String extraInfo = (String) assistanceData.get("extraInfo");
        ArrayList<Map<String, Object>> jugadores = (ArrayList<Map<String, Object>>) assistanceData.get("jugadores");

        // Configuración del título
        if (extraInfo != null && !extraInfo.isEmpty()) {
            assert type != null;
            if (type.equals("Partido")){
                holder.tvTitle.setText(type + " (" + extraInfo + ")");
            } else if (type.equals("Otro")) {
                holder.tvTitle.setText(extraInfo);
            }
        } else {
            holder.tvTitle.setText(type);
        }

        // Contar asistentes y ausentes
        int asistentes = 0;
        int ausentes = 0;
        for (Map<String, Object> jugador : jugadores) {
            Boolean asistencia = (Boolean) jugador.get("asistencia");
            if (asistencia != null && asistencia) {
                asistentes++;
            } else {
                ausentes++;
            }
        }

        // Mostrar resumen
        holder.tvSummary.setText(asistentes + " asistentes - " + ausentes + " ausentes");

        // Configurar clic en el ítem
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddAssistanceActivity.class);

            // Crear lista de Jugador desde la lista de HashMap
            ArrayList<Jugador> jugadoresList = new ArrayList<>();
            if (jugadores != null) {
                for (Map<String, Object> jugadorMap : jugadores) {
                    Boolean asistencia = jugadorMap.get("asistencia") != null ? (Boolean) jugadorMap.get("asistencia") : false;
                    Jugador jugador = new Jugador(
                            (String) jugadorMap.get("id"),
                            (String) jugadorMap.get("nombre"),
                            asistencia
                    );
                    jugadoresList.add(jugador);
                }
            }

            Log.d("AssistanceAdapter", "Evento pulsado: " +
                    "\nFecha: " + fecha +
                    "\nTipo: " + type +
                    "\nExtraInfo: " + extraInfo +
                    "\nJugadores: " + jugadores+
                    "\nJugadores List: " + jugadoresList
            );

            if (jugadoresList != null) {
                for (Jugador jugador : jugadoresList) {
                    Log.d("AssistanceAdapter", "Jugador cargado: " +
                            "\nID: " + jugador.getId() +
                            "\nNombre: " + jugador.getNombre() +
                            "\nAsistencia: " + jugador.isAsistencia());
                }
            } else {
                Log.e("AssistanceAdapter", "La lista de jugadores está vacía o no se recibió correctamente.");
            }

            intent.putExtra("selectedDate", (String) assistanceData.get("fecha"));
            intent.putExtra("teamId", teamId);
            intent.putExtra("jugadores", jugadoresList);
            intent.putExtra("tipo", type);
            intent.putExtra("extraInfo", extraInfo);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return assistanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSummary = itemView.findViewById(R.id.tvSummary);
        }
    }
}
