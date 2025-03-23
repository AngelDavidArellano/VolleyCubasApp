package com.volleycubas.app;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class ClasificacionAdapter extends RecyclerView.Adapter<ClasificacionAdapter.ViewHolder> {

    private List<Clasificacion> listaClasificacion;

    public ClasificacionAdapter(List<Clasificacion> listaClasificacion) {
        this.listaClasificacion = listaClasificacion;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clasificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Clasificacion equipo = listaClasificacion.get(position);

        holder.txtPosicion.setText(String.valueOf(position + 1));

        String nombreFormateado = formatearNombreEquipo(equipo.getEquipo());

        // Establecer el texto en el TextView
        holder.txtEquipo.setText(nombreFormateado);

        holder.txtPJ.setText(String.valueOf(equipo.getJugados()));
        holder.txtSetsFavor.setText(String.valueOf(equipo.getSetsAFavor()));
        holder.txtSetsContra.setText(String.valueOf(equipo.getSetsEnContra()));
        holder.txtGanados.setText(String.valueOf(equipo.getGanados()));
        holder.txtPerdidos.setText(String.valueOf(equipo.getPerdidos()));

        // Diferencia de puntos (Puntos a favor - Puntos en contra)
        int puntosDiferencia = equipo.getPuntosAFavor() - equipo.getPuntosEnContra();

        // Obtén los colores desde resources
        int colorVerde = ContextCompat.getColor(holder.itemView.getContext(), R.color.light_green);
        int colorRojo = ContextCompat.getColor(holder.itemView.getContext(), R.color.light_red);

        // Aplica el color según la diferencia de puntos
        if (puntosDiferencia >= 0) {
            holder.txtPtsDiferencia.setTextColor(colorVerde);
        } else {
            holder.txtPtsDiferencia.setTextColor(colorRojo);
        }

        // Establece el texto con formato seguro
        holder.txtPtsDiferencia.setText(String.valueOf(puntosDiferencia));

        int colorDorado = ContextCompat.getColor(holder.itemView.getContext(), R.color.gold);
        int colorFondo = ContextCompat.getColor(holder.itemView.getContext(), R.color.bg);

        if (nombreFormateado.toLowerCase().contains("cubas")){
            holder.clubIndicator.setBackgroundTintList(ColorStateList.valueOf(colorDorado));
        } else {
            holder.clubIndicator.setBackgroundTintList(ColorStateList.valueOf(colorFondo));
        }

        if (position == 0){
            holder.crownIcon.setVisibility(View.VISIBLE);
        } else {
            holder.crownIcon.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return listaClasificacion.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPosicion, txtEquipo, txtPJ, txtSetsFavor, txtSetsContra, txtGanados, txtPerdidos, txtPtsDiferencia;
        View clubIndicator;
        ImageView crownIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPosicion = itemView.findViewById(R.id.txtPosicion);
            txtEquipo = itemView.findViewById(R.id.txtEquipo);
            txtPJ = itemView.findViewById(R.id.txtPJ);
            txtSetsFavor = itemView.findViewById(R.id.txtSetsFavor);
            txtSetsContra = itemView.findViewById(R.id.txtSetsContra);
            txtGanados = itemView.findViewById(R.id.txtGanados);
            txtPerdidos = itemView.findViewById(R.id.txtPerdidos);
            txtPtsDiferencia = itemView.findViewById(R.id.txtPtsDiferencia);
            clubIndicator = itemView.findViewById(R.id.clubIndicator);
            crownIcon = itemView.findViewById(R.id.crownIcon);
        }
    }

    public static String formatearNombreEquipo(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return nombre;
        }

        // Lista de palabras que deben permanecer en mayúsculas
        List<String> palabrasEnMayusculas = Arrays.asList("CDE", "IES", "CV", "CD");

        String[] palabras = nombre.toLowerCase().split("\\s+");
        StringBuilder nombreFormateado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                // Si la palabra está en la lista, mantenerla en mayúsculas
                if (palabrasEnMayusculas.contains(palabra.toUpperCase())) {
                    nombreFormateado.append(palabra.toUpperCase());
                } else {
                    // Primera letra en mayúscula, el resto en minúscula
                    nombreFormateado.append(Character.toUpperCase(palabra.charAt(0)))
                            .append(palabra.substring(1));
                }
                nombreFormateado.append(" ");
            }
        }

        return nombreFormateado.toString().trim(); // Eliminar espacios extra
    }


}
