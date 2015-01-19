/*
 * Copyright (c) 2007-2011 Scott Rahner, Panic Productions, a division N1 Concepts LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * http://www.opensource.org/licenses/mit-license.php
 */

package com.panic.silence.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.panic.R;
import com.panic.silence.applications.ApplicationState;
import com.panic.silence.widgets.SilenceWidget;

public class SilenceService extends Service {

    private final int[] streamIds = {//AudioManager.STREAM_VOICE_CALL,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_NOTIFICATION
    };

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("tsilence", "Staring Silence Service");

        //1. SETUP CONTEXT VARIABLES
        ApplicationState state = (ApplicationState) this.getApplication();
        boolean requestFromWidget = false;
        boolean requestFromEnforce = false;
        boolean isSilent = state.getSilenceState();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            requestFromWidget = extras.getBoolean("fromWidget", false);
            requestFromEnforce = extras.getBoolean("fromEnforce", false);
        }
        if (requestFromWidget) {
            Log.i("tsilence", "Toggling Silent State");
            isSilent = !isSilent;
        }

        //2. SET WIDGET STATE AND RESET THE INTENT
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, SilenceWidget.class));

        Intent reIntent = new Intent(this, SilenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, reIntent, 0);

        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.silence_widget_layout);

        state.setSilenceState(views, isSilent);
        views.setOnClickPendingIntent(R.id.silence_layout, pendingIntent);
        appWidgetManager.updateAppWidget(widgetIds, views);

        //3. SET AUDIO STREAMS
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        //check if silence refresh service should run
        Log.i("tsilence", "Silent: " + isSilent + ", From Enforce: " + requestFromEnforce);
        if (!isSilent && !requestFromEnforce) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean("silenceRefresh", false)) {
                startService(new Intent(this, EnforceSilenceService.class));
            }
        }

        //set stream volumes
        if (!isSilent) {
            for (int id : streamIds) {
                if (requestFromWidget) {
                    state.setStreamPref(id, am.getStreamVolume(id)); //save prefs only when service triggered by widget press
                }
                am.setStreamVolume(id, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
            //Notification On
            setNotification();
        } else {
            for (int id : streamIds) {
                if (state.getStreamPref(id) != 0) { //Fixes bug w/ linked streams
                    am.setStreamVolume(id, state.getStreamPref(id), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
                //Service off
                stopService(new Intent(this, EnforceSilenceService.class));
            }
            //Notification Off
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(1);
        }
        //kill this service
        stopSelf();
        return START_NOT_STICKY;
    }

    private void setNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent notifIntent = new Intent(this, SilenceService.class);
        notifIntent.putExtra("fromWidget", true);
        PendingIntent pi = PendingIntent.getService(this, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notif = new Notification.Builder(getBaseContext())
                .setContentTitle("True Silence")
                .setContentText(getBaseContext().getString(R.string.notification_text))
                .setSmallIcon(R.drawable.sound_muted)
                .setContentIntent(pi)
                .setOngoing(true)
                .getNotification();
        nm.notify(1, notif);
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return null;
    }

}