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

package com.panic.silence.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.panic.R;
import com.panic.silence.services.SilenceService;

public class SilenceWidget extends AppWidgetProvider {
	
	@Override
	public final void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.i("tsilence", "widget");
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, SilenceService.class);
            intent.putExtra("fromWidget", true);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.silence_widget_layout);
            setSilenceIcon(context, views);
            views.setOnClickPendingIntent(R.id.silence_layout, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	//logic duplicated from ApplicationState because of visibility issues.
	final void setSilenceIcon(Context context, RemoteViews views){
		SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getString(R.string.prefName), Context.MODE_PRIVATE);
		if(prefs.getBoolean("silent",true)){
			views.setImageViewResource(R.id.ImageView01, R.drawable.sound_icon);
	    }else{
	    	views.setImageViewResource(R.id.ImageView01, R.drawable.sound_off_icon);
	    }
	}
	
}
