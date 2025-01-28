package com.volleycubas.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.volleycubas.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
        String local = (String) match.get("equipo_local");
        String hora = (String) match.get("hora");
        String ubicacion = (String) match.get("localizacion");
        String pista = (String) match.get("pista");

        if (rival != null && rival.equals("DESCANSA")) {
            holder.tvNextGame.setText(local + " (" + rival + ")");
        } else {
            holder.tvNextGame.setText((local != null && rival != null) ? local + "  vs  " + rival : "Rival desconocido");
        }

        String fecha_hora_partido = (fecha != null && hora != null) ? fecha + " " + hora : null;

        noMatchPosibility(holder, fecha_hora_partido);

        // Comprobar si cualquier dato es "Aplazado", "APLAZADO" o "aplazado"
        if (esAplazado(fecha) || esAplazado(hora) || esAplazado(ubicacion) || esAplazado(pista)) {
            holder.tvFecha.setText("APLAZADO");

            // Ocultar los datos de hora y ubicación
            holder.tvTime.setVisibility(View.GONE);
            holder.ivTime.setVisibility(View.GONE);
            holder.ivPlace.setVisibility(View.GONE);
            holder.tvPlace.setVisibility(View.GONE);
        } else {
            // Restablecer los valores si no está aplazado
            holder.tvFecha.setText(fecha != null ? fecha : "Fecha desconocida");
            holder.tvTime.setText(hora != null ? hora : "Hora desconocida");
            holder.tvPlace.setText(ubicacion != null ? ubicacion + " - " + pista : "Ubicación desconocida");

            // Asegurar que los iconos sean visibles
            holder.ivTime.setVisibility(View.VISIBLE);
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.ivPlace.setVisibility(View.VISIBLE);
            holder.tvPlace.setVisibility(View.VISIBLE);

            noMatchPosibility(holder, fecha_hora_partido);
        }


        // Actualizar el estado del partido
        updateMatchStatus(holder, fecha_hora_partido);
    }
    private boolean esAplazado(String texto) {
        return texto != null && texto.equalsIgnoreCase("Aplazado");
    }


    @Override
    public int getItemCount() {
        return matches.size();
    }

    private void updateMatchStatus(GameViewHolder holder, String matchDateTime) {
        try {
            // Crear un objeto Date con la fecha y hora del partido
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date matchDateObj = sdf.parse(matchDateTime);
            Date currentDate = new Date();
            long oneHourAndHalf = 90 * 60 * 1000; // 1 hora y media en milisegundos

            if (matchDateObj == null || matchDateObj.after(currentDate)) {
                // Partido en el futuro o con fecha/hora nula
                holder.statusView.setBackgroundResource(R.drawable.status_indicator_gray);
            } else if (matchDateObj.getTime() <= currentDate.getTime() &&
                    currentDate.getTime() <= matchDateObj.getTime() + oneHourAndHalf) {
                // Partido en curso (entre la hora de inicio y 1.5 horas después)
                holder.statusView.setBackgroundResource(R.drawable.status_indicator_green);

                // Animación de agrandar y reducir tamaño
                animateStatusIndicator(holder.statusView);
            } else {
                // Partido ya finalizado (pasó más de 1.5 horas desde el inicio)
                holder.statusView.setBackgroundResource(R.drawable.status_indicator_red);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error o formato incorrecto
            holder.statusView.setBackgroundResource(R.drawable.status_indicator_gray);
        }
    }

    private void animateStatusIndicator(View statusView) {
        // Animación de escala
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(statusView, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(statusView, "scaleY", 1f, 1.1f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(statusView, "scaleX", 1.1f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(statusView, "scaleY", 1.1f, 1f);

        // Configurar duración de las animaciones
        scaleUpX.setDuration(300);  // Duración de aumento
        scaleUpY.setDuration(300);
        scaleDownX.setDuration(300); // Duración de reducción
        scaleDownY.setDuration(300);

        // Establecer repetición infinita para los animadores individuales
        scaleUpX.setRepeatCount(ValueAnimator.INFINITE);
        scaleUpY.setRepeatCount(ValueAnimator.INFINITE);
        scaleDownX.setRepeatCount(ValueAnimator.INFINITE);
        scaleDownY.setRepeatCount(ValueAnimator.INFINITE);

        // Configurar que la animación se invierta
        scaleUpX.setRepeatMode(ValueAnimator.REVERSE);
        scaleUpY.setRepeatMode(ValueAnimator.REVERSE);
        scaleDownX.setRepeatMode(ValueAnimator.REVERSE);
        scaleDownY.setRepeatMode(ValueAnimator.REVERSE);

        // Crear un AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleUpX).with(scaleUpY);  // Primero aumentar
        animatorSet.play(scaleDownX).with(scaleDownY).after(300); // Después reducir

        // Establecer interpolador para animaciones suaves
        animatorSet.setInterpolator(new LinearInterpolator());

        // Iniciar la animación
        animatorSet.start();
    }

    private void noMatchPosibility(GameViewHolder holder, String matchDateTime) {
        // Evitar NullPointerException en logs
        Log.d("Fecha y hora", matchDateTime != null ? matchDateTime : "NULL");

        // Restablecer visibilidad antes de ocultar
        holder.ivTime.setVisibility(View.VISIBLE);
        holder.tvTime.setVisibility(View.VISIBLE);
        holder.ivPlace.setVisibility(View.VISIBLE);
        holder.tvPlace.setVisibility(View.VISIBLE);
        holder.ivFecha.setVisibility(View.VISIBLE);
        holder.tvFecha.setVisibility(View.VISIBLE);

        // Si la fecha y hora son "null null", ocultar los datos
        if (matchDateTime == null || matchDateTime.equals("null null")) {
            holder.ivTime.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);
            holder.ivPlace.setVisibility(View.GONE);
            holder.tvPlace.setVisibility(View.GONE);
            holder.ivFecha.setVisibility(View.GONE);
            holder.tvFecha.setVisibility(View.GONE);
        }
    }



    static class GameViewHolder extends RecyclerView.ViewHolder {

        TextView tvFecha, tvNextGame, tvTime, tvPlace;
        ImageView ivFecha, ivTime, ivPlace;
        View statusView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tv_next_match_date);
            tvNextGame = itemView.findViewById(R.id.tv_next_match);
            tvTime = itemView.findViewById(R.id.tv_next_match_time);
            tvPlace = itemView.findViewById(R.id.tv_next_match_location);

            ivFecha = itemView.findViewById(R.id.iv_calendar);
            ivPlace = itemView.findViewById(R.id.iv_location);
            ivTime = itemView.findViewById(R.id.iv_clock);

            statusView = itemView.findViewById(R.id.iv_status_indicator);
        }
    }
}
