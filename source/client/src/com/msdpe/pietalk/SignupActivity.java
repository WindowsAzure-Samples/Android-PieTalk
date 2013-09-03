package com.msdpe.pietalk;


import java.util.Calendar;

import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.msdpe.pietalk.util.TextValidator;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {
	private final String TAG = "SignupActivity";
	private TextView mLblDisclaimer;
	private EditText mTxtBirthday;
	private EditText mTxtEmail;
	private EditText mTxtPassword;
	private Button mBtnSignup;
	private boolean mDateIsInFuture = false;
	private ProgressBar mProgressSignup;
	private Calendar mSelectedDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		mLblDisclaimer = (TextView) findViewById(R.id.lblDisclaimer);
		mLblDisclaimer.setText(Html.fromHtml(getResources().getString(R.string.sign_up_disclaimer)));
		mLblDisclaimer.setMovementMethod(LinkMovementMethod.getInstance());
		
		mTxtBirthday = (EditText) findViewById(R.id.txtBirthday);
		mTxtBirthday.setClickable(true);
		mTxtBirthday.setOnClickListener(birthdayListener);
		
		mBtnSignup = (Button) findViewById(R.id.btnSignup);		
		mTxtEmail = (EditText) findViewById(R.id.txtEmail);
		mTxtPassword = (EditText) findViewById(R.id.txtPassword);
		mProgressSignup = (ProgressBar) findViewById(R.id.progressSignup);
		
		mTxtEmail.addTextChangedListener(new TextValidator(mTxtEmail) {			
			@Override
			public void validate(TextView textView, String text) {
				checkValid();
			}
		});
		mTxtPassword.addTextChangedListener(new TextValidator(mTxtPassword) {			
			@Override
			public void validate(TextView textView, String text) {
				checkValid();
				
			}
		});
		
		mBtnSignup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isValid()) {
					mBtnSignup.setVisibility(View.GONE);
					mProgressSignup.setVisibility(View.VISIBLE);
					
					mPieTalkService.registerUser(mTxtPassword.getText().toString(), 
						mTxtBirthday.getText().toString(), mTxtEmail.getText().toString(), new ApiJsonOperationCallback() {								
							@Override
							public void onCompleted(JsonElement arg0, Exception exc,
									ServiceFilterResponse arg2) {
								Log.i(TAG, arg2.toString());
								Log.i(TAG, arg0.toString());
								if (exc != null) {
									Log.e(TAG, "Error: " + exc.getMessage());
								}
								mBtnSignup.setVisibility(View.VISIBLE);
								mProgressSignup.setVisibility(View.GONE);
							}
						});					
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signup, menu);
		return true;
	}
	
	private OnClickListener birthdayListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			DialogFragment newFragment = new DatePickerFragment();
		    newFragment.show(getFragmentManager(), "datePicker");	 
		}
	};
	
	public static class DatePickerFragment extends DialogFragment
		    { //implements DatePickerDialog.OnDateSetListener {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), (SignupActivity) getActivity(), year, month, day);
		}
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		//TODO: consider formatting for region (i.e. MMDDYYYY vs DDMMYYYY)
		mTxtBirthday.setText(month + "/" + day + "/" + year);
		
		mSelectedDate = Calendar.getInstance();
		mSelectedDate.set(year, month, day);
		
		if (mSelectedDate.after(Calendar.getInstance())) {
			Toast.makeText(getApplicationContext(), R.string.born_in_future, Toast.LENGTH_LONG).show();
			mTxtBirthday.setError("Date must be in the past");
			mDateIsInFuture = true;
		} else
			mTxtBirthday.setError(null);
			mDateIsInFuture = false;
		
		checkValid();
	}
	
	
	private void checkValid() {
		if (this.isValid()) {
			mBtnSignup.setBackgroundResource(R.drawable.sign_up_button_style);
			//mBtnSignup.setVisibility(View.GONE);
			//mProgressSignup.setVisibility(View.VISIBLE);
		} else {
			mBtnSignup.setBackgroundResource(R.drawable.second_sign_up_button_style);
			//mBtnSignup.setVisibility(View.VISIBLE);
			//mProgressSignup.setVisibility(View.GONE);
		}
	}
	
	private boolean isValid() {
		if (mDateIsInFuture || mTxtBirthday.getText().toString().equals(""))
			return false;
		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mTxtEmail.getText().toString()).matches()) {
			return false;
		}
		if (mTxtPassword.getText().toString().equals(""))
			return false;
		
		return true;
	}

}
