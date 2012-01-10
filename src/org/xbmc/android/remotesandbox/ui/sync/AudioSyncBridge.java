/*
 *      Copyright (C) 2005-2015 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.remotesandbox.ui.sync;

import org.xbmc.android.jsonrpc.service.AudioSyncService;
import org.xbmc.android.remotesandbox.R;
import org.xbmc.android.remotesandbox.ui.base.ReloadableActionBarActivity;
import org.xbmc.android.util.google.DetachableResultReceiver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Manages audio sync in the UI.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class AudioSyncBridge implements AbstractSyncBridge, DetachableResultReceiver.Receiver {
	
	private final static String TAG = AudioSyncBridge.class.getSimpleName();
	
	private ReloadableActionBarActivity mActivity;
	
	@Override
	public void sync(ReloadableActionBarActivity activity, Handler handler) {
		
		final long start = System.currentTimeMillis();
		final DetachableResultReceiver receiver = new DetachableResultReceiver(handler);
		receiver.setReceiver(this);
		
		Toast.makeText(activity, R.string.toast_syncing_all, Toast.LENGTH_SHORT).show();
		final Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, AudioSyncService.class);
		intent.putExtra(AudioSyncService.EXTRA_STATUS_RECEIVER, receiver);
		activity.setSyncing(true);
		activity.startService(intent);
		Log.d(TAG, "Triggered audio sync in " + (System.currentTimeMillis() - start ) + "ms.");
		
		mActivity = activity;
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		final long start = System.currentTimeMillis();
		final ReloadableActionBarActivity activity = mActivity;
		final boolean syncing;
		switch (resultCode) {
			case AudioSyncService.STATUS_RUNNING: {
				syncing = true;
				break;
			}
			case AudioSyncService.STATUS_FINISHED: {
				syncing = false;
				Toast.makeText(activity, R.string.toast_synced_all, Toast.LENGTH_SHORT).show();
				break;
			}
			case AudioSyncService.STATUS_ERROR: {
				// Error happened down in SyncService, show as toast.
				syncing = false;
				final String errorText = activity.getString(R.string.toast_sync_error, resultData.getString(Intent.EXTRA_TEXT));
				Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show();
				break;
			}
			default:
				syncing = false;
				break;
		}
		Log.d(TAG, "Audio refresh callback processed in " + (System.currentTimeMillis() - start) + "ms.");
		mActivity.setSyncing(syncing);
	}
}