package com.capacitorjs.plugins.pushnotifications;

// import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.capacitorjs.plugins.pushnotifications.acknowledge.AcknowledgeService;

import android.net.Uri;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.Looper;
import android.media.MediaMetadataRetriever;



public class MessagingService extends FirebaseMessagingService {
    private final AcknowledgeService acknowledgeService = new AcknowledgeService();

    public void handleIntent(Intent intent) {
        Log.i("MessagingService", "intent received");
        super.handleIntent(intent);

        var audioManager = (AudioManager) getSystemService(ContextWrapper.AUDIO_SERVICE);
        if(audioManager != null) {
          int originalRingMode = audioManager.getRingerMode();
          int originalNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
          int maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

          Log.i("MessagingService", "originalRingMode " + originalRingMode + " " + originalNotificationVolume + " " + maxNotificationVolume);
          // When DND mode is enabled, we get ringerMode as silent even though actual ringer mode is Normal
          //          int isDndModeEnabled = NotificationManagerCompat.from(myContext).getCurrentInterruptionFilter();
          //          if (isDndModeEnabled != NotificationManager.INTERRUPTION_FILTER_ALL && originalRingMode == AudioManager.RINGER_MODE_SILENT && originalNotificationVolume != 0) {
          //            originalRingMode = AudioManager.RINGER_MODE_NORMAL;
          //          }
          Log.i("MessagingService", "originalNotificationVolume "+originalNotificationVolume);
          int newVolume = maxNotificationVolume; // ringToneVolume != null ? (int) Math.ceil(maxNotificationVolume * ringToneVolume) : originalNotificationVolume;

          audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
          audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newVolume, 0);

          // Resetting the original ring mode, volume and dnd mode
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
              audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
              audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalNotificationVolume, 0);
            }, getSoundFileDuration(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)));
        }

        this.acknowledgeService.initContent(this);
        this.acknowledgeService.newNotification(intent);
        Log.i("MessagingService", "intent exit");
    }

    public int getSoundFileDuration(Uri uri) {
      try {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationStr != null ? Integer.parseInt(durationStr) : 0;
      } catch (Exception ex) {
        return 5000;
      }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        PushNotificationsPlugin.sendRemoteMessage(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        PushNotificationsPlugin.onNewToken(s);
    }
}
