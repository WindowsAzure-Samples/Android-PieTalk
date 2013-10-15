package com.msdpe.pietalk.activities;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msdpe.pietalk.Constants;
import com.msdpe.pietalk.R;
import com.msdpe.pietalk.TestSettingsActivity;
import com.msdpe.pietalk.adapters.PiesArrayAdapter;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.datamodels.Pie;
import com.msdpe.pietalk.util.PieTalkAlert;
import com.msdpe.pietalk.util.PieTalkLogger;
import com.msdpe.pietalk.util.PieTalkResponse;

public class PiesListActivity extends BaseActivity {
	
	private final String TAG = "PiesListActivity";
	private ListView mLvPies;
	private PiesArrayAdapter mAdapter;	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);		
		super.onCreate(savedInstanceState, true);
		setContentView(R.layout.activity_pies_list);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mLvPies = (ListView) findViewById(R.id.lvPies);
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
		mPullToRefreshAttacher.addRefreshableView(mLvPies, new OnRefreshListener() {			
			@Override
			public void onRefreshStarted(View arg0) {
				// TODO Auto-generated method stub
				mPieTalkService.getPies();
				
			}
		});
	
//		mAdapter = new ArrayAdapter<String>(this,
//    	        android.R.layout.simple_list_item_1, mPieTalkService.getLocalPieUsernames());
//		mAdapter = new ArrayAdapter<String>(this,
//    	        R.layout.list_row_pie, R.id.text1, mPieTalkService.getLocalPieUsernames());		
		
		//mAdapter = new PiesArrayAdapter(this,  mPieTalkService.getLocalPieUsernames());
		mAdapter = new PiesArrayAdapter(this,  mPieTalkService.getLocalPies());
		mLvPies.setAdapter(mAdapter);
			
		mLvPies.setOnItemClickListener(pieClickListener);
		mLvPies.setOnItemLongClickListener(pieLongClickListener);
	}
	
	private OnItemClickListener pieClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
		}		
	};
	
	private OnItemLongClickListener pieLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			final Pie pie = mPieTalkService.getLocalPies().get(position);
			if (pie.getType().equals("FriendRequest")) {
				//Friend and update the pie
				mPieTalkService.acceptFriendRequest(pie, new ApiOperationCallback<PieTalkResponse>() {					
					@Override
					public void onCompleted(PieTalkResponse response, Exception ex,
							ServiceFilterResponse serviceFilterResponse) {
						PieTalkLogger.i(TAG, "Response received");
						if (ex != null || response.Error != null) {
																	
							//Display error					
							if (ex != null)
								PieTalkAlert.showSimpleErrorDialog(mActivity, ex.getCause().getMessage());
							else 
								Toast.makeText(mActivity, response.Error, Toast.LENGTH_SHORT).show();
						} else {
							mAdapter.remove(pie);
							mPieTalkService.getFriends();
						}
					}
				});
			} else if (pie.getType().equals("Message")) {
				if (pie.getHasUserSeen()) {
					//Do nothing, they should double tap to reply
				} else {
					//Show them the PIE!
				}
			}
			return false;
		}
		
	};
	

	
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		//filter.addAction(Constants.BROADCAST_PIES_UPDATED);
		filter.addAction(Constants.BROADCAST_PIES_UPDATED);
		registerReceiver(receiver, filter);
		super.onResume();	
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			mAdapter.clear();			
			//for (String item : mPieTalkService.getLocalPieUsernames()) {
			for (Pie pie : mPieTalkService.getLocalPies()) {
				mAdapter.add(pie);
			}		
			PieTalkLogger.i(TAG, "Refresh complete");
			mPullToRefreshAttacher.setRefreshComplete();
			mPullToRefreshAttacher.setRefreshing(false);
			
		}
	};

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);	
		ActionBar bar = getActionBar();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pies_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
			return true;		
		case R.id.menuSettings:
			
			Intent intent = new Intent(mActivity, TestSettingsActivity.class);
			startActivity(intent);
			finish();
			//mPieTalkService.getPies();
			//mPullToRefreshAttacher.setRefreshing(true);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}

}
