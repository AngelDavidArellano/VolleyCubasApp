package com.volleycubas.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = "Nueva Notificación";
        String body = "Tienes un mensaje.";
        String sound = "whistle_sound"; // Valor predeterminado

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // Verificar si la notificación contiene un sonido personalizado
        if (remoteMessage.getData().containsKey("sonido")) {
            sound = remoteMessage.getData().get("sonido");
        }

        mostrarNotificacion(title, body, sound);
    }

    private void mostrarNotificacion(String title, String messageBody, String sound) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Obtener el sonido personalizado desde /res/raw/
        //Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/raw/whistle_sound");
        Uri soundUri = Uri.parse(String.valueOf(R.raw.whistle_sound));

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "custom_channel_id")
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
        // Crear un nuevo canal de notificación con el sonido personalizado
        NotificationChannel channel = new NotificationChannel(
                "custom_channel_id",
                "Canal Personalizado",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(soundUri, audioAttributes);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(0, notificationBuilder.build());
    }

}