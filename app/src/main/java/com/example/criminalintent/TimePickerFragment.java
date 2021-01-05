package com.example.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.Inflater;

public class TimePickerFragment extends DialogFragment {

	private  static final String ARG_TIME = "time";
	public   static final String EXTRA_TIME =
			"com.example.criminalintent.time";

	private TimePicker mTimePicker;
	private  Calendar mCalendar;

	@RequiresApi(api = Build.VERSION_CODES.M)
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		View v = LayoutInflater.from(getActivity())
				.inflate(R.layout.dialog_time,null);

		Date date = (Date) getArguments().getSerializable(ARG_TIME);
		mCalendar = Calendar.getInstance();
		mCalendar.setTime(date);

		int hour = mCalendar.get(Calendar.HOUR);
		int minute = mCalendar.get(Calendar.MINUTE);

		mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_picker);
		mTimePicker.setHour(hour);
		mTimePicker.setMinute(minute);

		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.date_picker_title)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int year = mCalendar.get(Calendar.YEAR);
						int month= mCalendar.get(Calendar.MONTH);
						int day=mCalendar.get(Calendar.DAY_OF_MONTH);
						int hour=mTimePicker.getHour();
						int minute = mTimePicker.getMinute();
						Date date = new GregorianCalendar(year, month, day, hour,minute)
								.getTime();
						sendResult(Activity.RESULT_OK,date);
					}
				})
				.setView(v)
				.create();
	}

	private void sendResult(int resultCode, Date date)
	{
		if(getTargetFragment()==null)
			return;;
		Intent intent = new Intent();
		intent.putExtra(EXTRA_TIME,date);
		getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
	}


	public static TimePickerFragment newInstance(Date date)
	{
		Bundle arg =new Bundle();
		arg.putSerializable(ARG_TIME,date);

		TimePickerFragment tpf=new TimePickerFragment();
		tpf.setArguments(arg);
		return tpf;
	}
}
