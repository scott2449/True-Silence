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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class EnforceSilenceService extends Service {

	private ScheduledExecutorService timer;
	
	public final int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.i("tsilence", "enforce service started");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Integer timerPref = Integer.parseInt(prefs.getString("refreshRate", "600"));
		timer = Executors.newScheduledThreadPool(2);
		timer.scheduleAtFixedRate(new Runnable() {
			public void run() {
				Intent intent = new Intent(getBaseContext(), SilenceService.class);
                intent.putExtra("fromEnforce", true);
                getBaseContext().startService(intent);
			}
		}, timerPref, timerPref, TimeUnit.SECONDS);
        return START_STICKY;
	}

	@Override
	public final void onDestroy() {
		Log.i("tsilence", "enforce service destroyed");
		timer.shutdown();
		super.onDestroy();
	}

	@Override
	public final IBinder onBind(Intent intent) {
		return null;
	}

}
