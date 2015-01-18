package com.panic.silence.applications;

import com.panic.R;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class ApplicationState extends Application {

	private SharedPreferences.Editor prefEdit;
	private SharedPreferences prefs;

	@Override
	public final void onCreate() {
		super.onCreate();
		this.prefs = this.getApplicationContext().getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE);
		this.prefEdit = this.getApplicationContext().getSharedPreferences(getString(R.string.prefName), Context.MODE_PRIVATE).edit();
	}
	
	public final void setStreamPref(int streamId, int streamVol){
        prefEdit.putInt(Integer.toString(streamId), streamVol);
		prefEdit.commit();
	}
	
	public final int getStreamPref(int streamId) {
        return prefs.getInt(Integer.toString(streamId),7);
    }
	
	public final void setSilenceState(Context context, RemoteViews views, boolean isSilent) {
        prefEdit.putBoolean("silent", isSilent);
        prefEdit.commit();
        setSilenceIcon(views);
    }
	
	public final boolean getSilenceState() {
        return prefs.getBoolean("silent",true);
    }
    
	final void setSilenceIcon(RemoteViews views){
		if(getSilenceState()){
			views.setImageViewResource(R.id.ImageView01, R.drawable.sound_icon);
	    }else{
	    	views.setImageViewResource(R.id.ImageView01, R.drawable.sound_off_icon);
	    }
	}
	
}
