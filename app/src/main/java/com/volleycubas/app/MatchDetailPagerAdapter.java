package com.volleycubas.app;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

public class MatchDetailPagerAdapter extends RecyclerView.Adapter<MatchDetailPagerAdapter.ViewHolder> {

    private final List<String> setDetails;
    private final String rival;
    private final String teamName;

    public MatchDetailPagerAdapter(List<String> setDetails, String rival, String teamName) {
        this.setDetails = setDetails;
        this.rival = rival;
        this.teamName = teamName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(setDetails.get(position), rival, teamName);
    }

    @Override
    public int getItemCount() {
        return setDetails.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView ganador_set;
        private final FlexboxLayout colorBarLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ganador_set = itemView.findViewById(R.id.ganador_set);
            colorBarLayout = itemView.findViewById(R.id.color_bar_layout);
        }

        public void bind(String detail, String rival, String teamName) {
            int countA = 0;
            int countB = 0;

            if (detail != null && !detail.isEmpty()) {
                int[] counts = countCharacters(detail);
                countA = counts[0];
                countB = counts[1];
            }

            // Determinar el ganador del set
            String ganador = (countA > countB) ? teamName : rival;

            // Configurar vistas
            ganador_set.setText(ganador + " (" + countA + " - " + countB + ")");

            // Crear la barra de colores
            populateColorBar(detail);

            Log.d("MatchDetailPagerAdapter", "Set: " + detail + ", Ganador: " + ganador + ", Puntos A: " + countA + ", Puntos B: " + countB);
        }

        private void populateColorBar(String detail) {
            // Limpiar la barra de colores antes de llenarla
            colorBarLayout.removeAllViews();

            // Crear una vista por cada carácter
            for (char c : detail.toCharArray()) {
                View colorView = new View(colorBarLayout.getContext());
                float[] hsv_blue = {220, 0.69f, 0.80f};
                float[] hsv_red = {0, 0.68f, 0.90f};

                int color = (c == 'A') ? Color.HSVToColor(hsv_blue) :Color.HSVToColor(hsv_red); // Define colores
                colorView.setBackgroundColor(color);

                // Establecer el tamaño de cada vista
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(50, 50); // Tamaño de cada bloque
                params.setMargins(4, 4, 4, 4); // Margen entre bloques
                colorView.setLayoutParams(params);

                // Añadir la vista a la barra de colores
                colorBarLayout.addView(colorView);
            }
        }

        private int[] countCharacters(String detail) {
            int countA = 0;
            int countB = 0;
            for (char c : detail.toCharArray()) {
                if (c == 'A') countA++;
                else if (c == 'B') countB++;
            }
            return new int[]{countA, countB};
        }
    }

}
