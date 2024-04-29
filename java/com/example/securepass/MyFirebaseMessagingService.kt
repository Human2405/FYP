package com.example.securepass

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService(){

  /*  override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("notification", "From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d("notification", "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        remoteMessage.notification?.body?.let { sendNotification(it) }
    }

   /* private void sendNotification(String from, String body){
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run(){
                //Toast.makeText(MyFirebaseMessagingService.this.getApplicationContext, )
            }
        }
    }*/
   private fun sendNotification(messageBody: String) {
       val intent = Intent(this, MainActivity::class.java)
       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
       val requestCode = 0
       val pendingIntent = PendingIntent.getActivity(
           this,
           requestCode,
           intent,
           PendingIntent.FLAG_IMMUTABLE,
       )

       val channelId = "SecurePass"
       val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
       val notificationBuilder = NotificationCompat.Builder(this, channelId)
           .setSmallIcon(R.mipmap.ic_launcher)
           .setContentTitle("SecurePass Notification")
           .setContentText(messageBody)
           .setAutoCancel(true)
           .setSound(defaultSoundUri)
           .setContentIntent(pendingIntent)

       val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

       // Since android Oreo notification channel is needed.
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           val channel = NotificationChannel(
               channelId,
               "Channel human readable title",
               NotificationManager.IMPORTANCE_DEFAULT,
           )
           notificationManager.createNotificationChannel(channel)
       }

       val notificationId = 0
       notificationManager.notify(notificationId, notificationBuilder.build())
   }*/
}

