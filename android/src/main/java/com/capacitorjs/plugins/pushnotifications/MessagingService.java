package com.capacitorjs.plugins.pushnotifications;

// import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationManager;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
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

import android.os.Bundle;

import java.util.Objects;

public class MessagingService extends FirebaseMessagingService {
    private final AcknowledgeService acknowledgeService = new AcknowledgeService();

    public void handleIntent(Intent intent) {
        Log.i("MessagingService", "intent received");
        super.handleIntent(intent);

				Bundle bundle = intent.getExtras();
				if (bundle != null && bundle.containsKey("criticalalert")) {
          String s1 = bundle.getString("criticalalert");
          Log.i("MessagingService bundle", s1);

					if (Objects.equals(s1, "1")) {
						Log.i("MessagingService bundle", bundle.getString("criticalalert"));

						var audioManager = (AudioManager) getSystemService(ContextWrapper.AUDIO_SERVICE);
						if(audioManager != null) {
							int originalRingMode = audioManager.getRingerMode();
              //boolean volumeFixed = audioManager.isVolumeFixed();
							int originalNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
							int maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

							Log.i("MessagingService", "originalRingMode " + originalRingMode + " " + originalNotificationVolume + " " + maxNotificationVolume);
              var notificationManager = (NotificationManager) getSystemService(ContextWrapper.NOTIFICATION_SERVICE);
              int isDndModeEnabled = 0;
              if (notificationManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                  isDndModeEnabled = notificationManager.getCurrentInterruptionFilter();
                }
                if (isDndModeEnabled != NotificationManager.INTERRUPTION_FILTER_ALL && originalRingMode == AudioManager.RINGER_MODE_SILENT && originalNotificationVolume != 0) {
                  originalRingMode = AudioManager.RINGER_MODE_NORMAL;
                }
              }
              Log.i("MessagingService", "isDndModeEnabled "+isDndModeEnabled);
              // When DND mode is enabled, we get ringerMode as silent even though actual ringer mode is Normal
							//          int isDndModeEnabled = NotificationManagerCompat.from(myContext).getCurrentInterruptionFilter();
							//          if (isDndModeEnabled != NotificationManager.INTERRUPTION_FILTER_ALL && originalRingMode == AudioManager.RINGER_MODE_SILENT && originalNotificationVolume != 0) {
							//            originalRingMode = AudioManager.RINGER_MODE_NORMAL;
							//          }
							Log.i("MessagingService", "originalNotificationVolume "+originalNotificationVolume+" maxNotificationVolume "+maxNotificationVolume);
              // ringToneVolume != null ? (int) Math.ceil(maxNotificationVolume * ringToneVolume) : originalNotificationVolume;

              try {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
              } catch (Exception e) {
                Log.i("MessagingService", "RINGER_MODE_NORMAL not set");
              }
              int originalRingMode1 = audioManager.getRingerMode();
              Log.i("MessagingService", "RINGER_MODE_NORMAL "+originalRingMode1);

              try {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxNotificationVolume, 0);
              } catch (Exception e) {
                Log.i("MessagingService", "maxNotificationVolume not set");
              }

              int sv1 = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
              Log.i("MessagingService", "sv1 "+sv1);
							// Resetting the original ring mode, volume and dnd mode
              int finalOriginalRingMode = originalRingMode;
              int finalIsDndModeEnabled = isDndModeEnabled;
              new Handler(Looper.getMainLooper()).postDelayed(() -> {
                  try {
                    audioManager.setRingerMode(finalOriginalRingMode);
                  } catch (Exception e) {
                    Log.i("MessagingService", "finalOriginalRingMode not set");
                  }

                  try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                      notificationManager.setInterruptionFilter(finalIsDndModeEnabled);
                    }
                  } catch (Exception e) {
                    Log.i("MessagingService", "setInterruptionFilter fehler");
                  }

                  try {
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalNotificationVolume, 0);
                  } catch (Exception e) {
                    Log.i("MessagingService", "originalNotificationVolume not set");
                  }
                  int sv2 = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                  Log.i("MessagingService", "sv2 "+sv2);
								}, getSoundFileDuration(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)));
						}
					}
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
