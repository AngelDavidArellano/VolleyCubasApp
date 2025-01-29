package com.volleycubas.app;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class SetPreparacionAdapter extends RecyclerView.Adapter<SetPreparacionAdapter.SetViewHolder> {

    private List<Map<String, List<String>>> listaSets;
    private List<String> jugadoresNombres;
    private Context context;

    public SetPreparacionAdapter(Context context, List<Map<String, List<String>>> listaSets, List<String> jugadoresNombres) {
        this.context = context;
        this.listaSets = listaSets;
        this.jugadoresNombres = jugadoresNombres;
    }


    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_set_prep, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        Map<String, List<String>> setData = listaSets.get(position);
        String setKey = setData.keySet().iterator().next();
        List<String> jugadores = setData.get(setKey);

        // Asegurar que la lista tenga exactamente 6 elementos
        while (jugadores.size() < 6) {
            jugadores.add("+");
        }

        holder.setTitle.setText("Set " + (position + 1));

        for (int i = 0; i < 6; i++) {
            if (jugadores.size() > i) {
                holder.jugadorViews[i].setText(jugadores.get(i));
                holder.jugadorViews[i].setBackgroundColor(00000000);
            } else {
                holder.jugadorViews[i].setText("+");
                holder.jugadorViews[i].setBackgroundColor(Color.parseColor("#FFAAAAAA"));
            }
            if (holder.jugadorViews[i].getText().equals("+")){
                holder.jugadorViews[i].setBackgroundColor(Color.parseColor("#FFAAAAAA"));
            } else {
                holder.jugadorViews[i].setBackgroundColor(00000000);
            }
            final int index = i;
            holder.jugadorViews[i].setOnClickListener(v -> mostrarDialogoSeleccion(holder.jugadorViews[index], jugadores, index));
        }
    }

    private void mostrarDialogoSeleccion(TextView jugadorView, List<String> jugadores, int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Selecciona un jugador");

        String[] jugadoresArray = jugadoresNombres.toArray(new String[0]);
        builder.setItems(jugadoresArray, (dialog, which) -> {
            jugadores.set(index, jugadoresNombres.get(which));
            jugadorView.setText(jugadoresNombres.get(which));
            notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return listaSets.size();
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView setTitle;
        TextView[] jugadorViews = new TextView[6];

        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            setTitle = itemView.findViewById(R.id.tvPrimerSet);
            jugadorViews[0] = itemView.findViewById(R.id.jugador1);
            jugadorViews[1] = itemView.findViewById(R.id.jugador2);
            jugadorViews[2] = itemView.findViewById(R.id.jugador3);
            jugadorViews[3] = itemView.findViewById(R.id.jugador4);
            jugadorViews[4] = itemView.findViewById(R.id.jugador5);
            jugadorViews[5] = itemView.findViewById(R.id.jugador6);
        }
    }
}
