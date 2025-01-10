package com.volleycubas.app;

import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RotationPagerAdapter extends RecyclerView.Adapter<RotationPagerAdapter.ViewHolder> {

    private List<Map<String, Map<String, Double>>> rotations;
    private Context context;

    public RotationPagerAdapter(Context context, List<Map<String, Map<String, Double>>> rotations) {
        this.context = context;
        this.rotations = rotations;
    }

    public List<Map<String, Map<String, Double>>> getRotations() {
        return rotations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.field_view_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Map<String, Double>> rotation = rotations.get(position);

        holder.clearPlayerPositions();
        holder.updateRotation(rotation);

        for (Map.Entry<String, Map<String, Double>> entry : rotation.entrySet()) {
            String playerId = entry.getKey();
            View playerView = holder.fieldView.findViewById(holder.getPlayerViewId(playerId));
            if (playerView != null) {
                holder.setupDraggablePlayer(playerView, playerId, rotation);
            }
        }
    }

    @Override
    public int getItemCount() {
        return rotations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout fieldView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fieldView = itemView.findViewById(R.id.fieldView);
        }

        private int getPlayerViewId(String playerId) {
            return itemView.getContext().getResources().getIdentifier(playerId, "id", itemView.getContext().getPackageName());
        }

        public void updateRotation(Map<String, Map<String, Double>> rotation) {
            fieldView.post(() -> {
                for (Map.Entry<String, Map<String, Double>> entry : rotation.entrySet()) {
                    String playerId = entry.getKey().trim();
                    Map<String, Double> position = entry.getValue();
                    updatePlayerPosition(playerId, position);
                }
            });
        }

        public void clearPlayerPositions() {
            for (int i = 0; i < fieldView.getChildCount(); i++) {
                View child = fieldView.getChildAt(i);
                if (child instanceof View) {
                    child.setX(0);
                    child.setY(0);
                }
            }
        }

        private void updatePlayerPosition(String playerId, Map<String, Double> position) {
            if (position == null) return;

            double x = position.get("x") instanceof Number ? ((Number) position.get("x")).doubleValue() : 0.0;
            double y = position.get("y") instanceof Number ? ((Number) position.get("y")).doubleValue() : 0.0;

            View playerView = fieldView.findViewById(getPlayerViewId(playerId));

            if (playerView != null) {
                fieldView.post(() -> {
                    float fieldWidth = fieldView.getWidth();
                    float fieldHeight = fieldView.getHeight();

                    if (fieldWidth > 0 && fieldHeight > 0) {
                        float translatedX = (float) (x / 10.0 * fieldWidth);
                        float translatedY = (float) (y / 10.0 * fieldHeight);

                        playerView.setX(translatedX - (playerView.getWidth() / 2));
                        playerView.setY(translatedY - (playerView.getHeight() / 2));
                    }
                });
            } else {
                Log.e("PlayerPosition", "No se encontró la vista para el jugador: " + playerId);
            }
        }

        public void setupDraggablePlayer(View player, String playerId, Map<String, Map<String, Double>> rotation) {
            if (player == null) return;

            player.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(null, shadowBuilder, v, 0);
                    v.setTag(playerId);
                    return true;
                }
                return false;
            });

            fieldView.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        View draggedView = (View) event.getLocalState();
                        if (draggedView != null) {
                            String draggedPlayerId = (String) draggedView.getTag();

                            if (draggedPlayerId != null) {
                                float newX = event.getX() - draggedView.getWidth() / 2;
                                float newY = event.getY() - draggedView.getHeight() / 2;

                                newX = Math.max(0, Math.min(newX, fieldView.getWidth() - draggedView.getWidth()));
                                newY = Math.max(0, Math.min(newY, fieldView.getHeight() - draggedView.getHeight()));

                                draggedView.setX(newX);
                                draggedView.setY(newY);

                                Map<String, Double> newPosition = new HashMap<>();
                                newPosition.put("x", (double) (((newX + draggedView.getWidth() / 2) / fieldView.getWidth()) * 10));
                                newPosition.put("y", (double) (((newY + draggedView.getHeight() / 2) / fieldView.getHeight()) * 10));

                                rotation.put(draggedPlayerId, newPosition);
                            }
                        }
                        return true;

                    default:
                        return true;
                }
            });
        }
    }
}
