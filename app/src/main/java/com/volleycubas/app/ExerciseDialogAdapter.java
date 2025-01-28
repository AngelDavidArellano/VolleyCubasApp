package com.volleycubas.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExerciseDialogAdapter extends RecyclerView.Adapter<ExerciseDialogAdapter.ExerciseViewHolder> {

    private List<Ejercicio> ejercicios;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Ejercicio ejercicio);
    }

    public ExerciseDialogAdapter(List<Ejercicio> ejercicios, OnItemClickListener listener) {
        this.ejercicios = ejercicios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Ejercicio ejercicio = ejercicios.get(position);
        holder.name.setText(ejercicio.getTitulo());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(ejercicio));
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvEjerciciosName);
        }
    }
}
