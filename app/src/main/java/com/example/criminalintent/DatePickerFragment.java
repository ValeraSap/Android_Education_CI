package com.example.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {

	private static final String ARG_DATE = "date";

	public static final String EXTRA_DATE =
			"com.example.criminalintent.date";

	private DatePicker mDatePicker;
	private Button mDateButton;
	private  Calendar mCalendar;  ////

	public static DatePickerFragment newInstance(Date date)
	{
		Bundle args = new Bundle();
		args.putSerializable(ARG_DATE,date);

		DatePickerFragment fragment = new DatePickerFragment();
		fragment.setArguments(args);
		return fragment;

	}
/*	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		Date date =(Date) getArguments().getSerializable(ARG_DATE);

		mCalendar = Calendar.getInstance();
		mCalendar.setTime(date);
		int year =mCalendar.get(Calendar.YEAR);
		int month=mCalendar.get(Calendar.MONTH);
		int day = mCalendar.get(Calendar.DAY_OF_MONTH);

		View v = inflater.inflate(R.layout.dialog_date,container,false);

		mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_picker);
		mDatePicker.init(year, month, day, null);
		mDateButton  = (Button) v.findViewById(R.id.date_ok_button);
		mDateButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				okButtonClick();
				//getActivity().getSupportFragmentManager().beginTransaction().remove(DatePickerFragment.this).commit();
				getActivity().onBackPressed();
			}
		});
		return v;
	}*/


	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

		Date date =(Date) getArguments().getSerializable(ARG_DATE);

		//Calendar
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year =calendar.get(Calendar.YEAR);
		int month=calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		View v = LayoutInflater.from(getActivity())
				.inflate(R.layout.dialog_date,null);

		mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_picker);
		mDatePicker.init(year, month, day, null);

		return new AlertDialog.Builder(getActivity())
				//.setTitle(R.string.date_picker_title)
				.setView(v)
				.setPositiveButton(android.R.string.ok,  new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int year = mDatePicker.getYear();
						int month= mDatePicker.getMonth();
						int day= mDatePicker.getDayOfMonth();
						Date date=  new GregorianCalendar(year, month, day)
									.getTime();
						sendResult(Activity.RESULT_OK,date);
					}
				})

				.create();
	}


	private void okButtonClick() {
int year = mDatePicker.getYear();
int month= mDatePicker.getMonth();
	int day= mDatePicker.getDayOfMonth();
		Date date=  new GregorianCalendar(year, month, day)
				.getTime();
		sendResult(Activity.RESULT_OK, date);
	}


	private void sendResult(int resultCode, Date date)
	{
		if(getTargetFragment()==null)
			return;

		Intent intent = new Intent();
		intent.putExtra(EXTRA_DATE, date);

		getTargetFragment()
				.onActivityResult(getTargetRequestCode(), resultCode, intent);
	}

}
