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

package com.panic.services;

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
import android.os.IBinder;
//import android.util.Log;
import android.widget.RemoteViews;

import com.panic.R;
import com.panic.widgets.SilenceWidget;

public class SilenceService extends Service {
    //TODO Have status icon reappear on reboot.. must write a boot broadcast listener
	//TODO Try and automatically turn off true silence when a third party interferes with streams
	//TODO Provide an activity so that the user can see an icon even if they don't understand widgets
	final static private String PREFS_NAME = "SilencePrefs";

	@Override
	public void onStart(Intent intent, int startId) {
		
		boolean isSilent = getSilenceState(this);
		
		int[] streamIds = {AudioManager.STREAM_VOICE_CALL, 
							AudioManager.STREAM_SYSTEM, 
							AudioManager.STREAM_RING, 
							AudioManager.STREAM_MUSIC,
							AudioManager.STREAM_ALARM,
							AudioManager.STREAM_NOTIFICATION
							};
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, SilenceWidget.class));
		
		RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.silence_widget_layout);
		setSilenceState(this, views, !isSilent);
		appWidgetManager.updateAppWidget(widgetIds, views);
		
		AudioManager am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
		for(int id : streamIds){
			if(!isSilent){
				setStreamPref(this, id, am.getStreamVolume(id));
			 	am.setStreamVolume(id,0,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			 	//Notification On
			 	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				Notification notif = new Notification(R.drawable.sound_muted, "True Silence", System.currentTimeMillis());
				notif.flags |= Notification.FLAG_ONGOING_EVENT;
				PendingIntent pi = PendingIntent.getService(this, 0, new Intent(this,SilenceService.class), PendingIntent.FLAG_UPDATE_CURRENT);
				notif.setLatestEventInfo(this, "True Silence", "All volume levels are 0, click here to restore levels", pi);
				nm.notify(1, notif);
			}else{
				if(getStreamPref(this, id) != 0) //Fixes bug w/ linked streams
					am.setStreamVolume(id,getStreamPref(this, id),AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				//Notification Off
				NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				nm.cancel(1);
			}
		}
		
		stopSelf();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void setStreamPref(Context context, int streamId, int streamVol){
		SharedPreferences.Editor prefEdit = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		prefEdit.putInt(new Integer(streamId).toString(), streamVol);
		prefEdit.commit();
	}
	
	private int getStreamPref(Context context, int streamId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(new Integer(streamId).toString(),7);
    }
	
	private static void setSilenceState(Context context, RemoteViews views, boolean isSilent) {
        SharedPreferences.Editor prefEdit = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        prefEdit.putBoolean("silent", isSilent);
        prefEdit.commit();
        setSilenceIcon(context, views);
    }
	
	private static boolean getSilenceState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("silent",false);
    }
    
	public static void setSilenceIcon(Context context, RemoteViews views){
		if(getSilenceState(context)){
			views.setImageViewResource(R.id.ImageView01, R.drawable.sound_off_icon);
	    }else{
	    	views.setImageViewResource(R.id.ImageView01, R.drawable.sound_icon);
	    }
	}
	
}