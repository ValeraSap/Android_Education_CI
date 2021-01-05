package com.example.criminalintent;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment; //библиотека поддержки
import androidx.fragment.app.FragmentManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {

	private static final String ARG_CRIME_ID = "crime_id";

	private static final String DIALOG_DATE = "DialogDate";
	private static final String DIALOG_TIME = "DialogTime";

	private static final int REQUEST_DATE = 0;
	public static final int REQUEST_TIME =1;
	private static final int REQUEST_CONTACT = 2;
	private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 3;


	private Crime mCrime;
	private EditText mTitleField;
	private Button mDateButton;
	private CheckBox mSolved;
	private Button mDeleteButton;
	private Button mReportButton;
	private Button mSuspectButton;
	private Button mCallSuspectButton;

	private String mSuspectId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
		mCrime=CrimeLab.getInstance(getActivity()).getCrime(crimeId);

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/**********
		 *
		 * 	 @param 1 идентификатор ресурса макета
		 * 	 @param 2 определяет родителя представления, что обычно необходимо для правильной настройки виджета.
		 * 	 @param 3 указывает, нужно ли включать заполненное представление в родителя.
		 * 	         Мы передаем false, потому что представление будет добавлено в коде активности.
		 *
		 **********/

		View view=inflater.inflate(R.layout.fragment_crime, container,false);

		///Подключение виджетов фрагменту

		mTitleField=(EditText) view.findViewById(R.id.crime_title);
		mTitleField.setText(mCrime.getTitle());
				mTitleField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				mCrime.setTitle(charSequence.toString());

			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});

		mDateButton=(Button) view.findViewById(R.id.crime_date);
		updateDate();
		mDateButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				FragmentManager fragmentManager = getFragmentManager();
				DatePickerFragment dialog = DatePickerFragment
						.newInstance(mCrime.getDate());

				dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				dialog.show(fragmentManager, DIALOG_DATE);
			}
		});

		mSolved=(CheckBox) view.findViewById(R.id.crime_solved);
		mSolved.setChecked(mCrime.isSolved());
				mSolved.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mCrime.setSolved(b);
			}
		});

		mDeleteButton = (Button) view.findViewById(R.id.delete_crime_button);
		mDeleteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view) {

				CrimeLab crimeLab=CrimeLab.getInstance(getActivity());
				crimeLab.deleteCrime(mCrime);
				getActivity().finish();
			}
		});

		mReportButton=(Button)view.findViewById(R.id.crime_report);
		mReportButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				ShareCompat.IntentBuilder i=ShareCompat.IntentBuilder.from(getActivity());
				i.setType("text/plain");
				i.setText(getCrimeReport());
				i.setSubject(getString(R.string.crime_report_subject));
				i.setChooserTitle(getString(R.string.send_report));
				i.createChooserIntent();
				i.startChooser();
			}
		});
		//Мы еще воспользуемся интентом pickContact, поэтому он размещается за пределами слушателя OnClickListener кнопки mSuspectButton.
		final Intent pickContact=new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		mSuspectButton=(Button)view.findViewById(R.id.crime_suspect);
		mSuspectButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				startActivityForResult(pickContact,REQUEST_CONTACT);
			}
		});

		if(mCrime.getSuspect()!=null)
			mSuspectButton.setText(mCrime.getSuspect());

		/*********************************
		 * Защита от отсутствия контактных приложений
		 *******************************/
		PackageManager packageManager=getActivity().getPackageManager();
		if(packageManager.resolveActivity(pickContact,
				PackageManager.MATCH_DEFAULT_ONLY)==null) {
			mSuspectButton.setEnabled(false);
			mCallSuspectButton.setEnabled(false);
		}


	/*	final Intent pickNumber=new Intent(Intent.ACTION_PICK,
				Uri.parse(ContactsContract.CommonDataKinds.Phone._ID));*/
		mCallSuspectButton=(Button) view.findViewById(R.id.crime_call_suspect);
		mCallSuspectButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				if(mCrime.getSuspect()==null)
					return;

				getPermissionToReadUserContacts();



				//1 получить номер телефона
				String[] projection				= new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}; //столбец
				//String selection				= ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?"; ///правило //ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",    //ничего не находит
				String selection				= ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + "GrandJigas" +"%'";
				String[] selectionParameters 	= new String[]{mCrime.getSuspect()};

				// todo fix query
				Cursor c= getActivity().getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						projection,
						selection,
						null,
						null
						);
				try{
					if(c.getCount()==0)
						return;
					c.moveToFirst();

					//Toast.makeText(getActivity(),c.getString(0),Toast.LENGTH_SHORT).show();

					//2 позвонить на номер
					Uri number=Uri.parse("tel:"+c.getString(0));
					Intent i = new Intent(Intent.ACTION_DIAL,number);
					startActivity(i);

				} finally {
					c.close();
				}

			}
		});

		return view;
	}

	private void getPermissionToReadUserContacts() {
		if (ContextCompat.checkSelfPermission(getActivity(),
				Manifest.permission.READ_CONTACTS)
				!= PackageManager.PERMISSION_GRANTED) {
			// Check if the user has been asked about this permission already and denied
			// it. If so, we want to give more explanation about why the permission is needed.
			if (shouldShowRequestPermissionRationale(
					Manifest.permission.READ_CONTACTS)) {
				// Show our own UI to explain to the user why we need to read the contacts
				// before actually requesting the permission and showing the default UI
			}

			// Fire off an async request to actually get the permission
			// This will show the standard permission request dialog UI
			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
					READ_CONTACTS_PERMISSIONS_REQUEST);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		CrimeLab.getInstance(getActivity()).updateCrime(mCrime);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if(requestCode==REQUEST_DATE )
		{
			Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
			FragmentManager fragmentManager = getFragmentManager();
			TimePickerFragment timeDialog = TimePickerFragment.newInstance(date);
			timeDialog.setTargetFragment(this, REQUEST_TIME);
			timeDialog.show(fragmentManager,DIALOG_TIME);
		}
		else if(requestCode==REQUEST_TIME)
		{
			Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
			mCrime.setDate(date);
			updateDate();
		}
		else if(requestCode==REQUEST_CONTACT && data!=null)
		{
			Uri contactUri = data.getData();
			String[] queryFields = new String[] {
					ContactsContract.Contacts.DISPLAY_NAME
			};

			Cursor c = getActivity().getContentResolver()
					.query(contactUri,queryFields,null,null,null);
			try {
				if(c.getCount()==0)
					return;
				c.moveToFirst();
				String suspect=c.getString(0);
				mCrime.setSuspect(suspect);
				mSuspectButton.setText(suspect);
			} finally {
				c.close();
			}
		}

	}

	private void updateDate() {
		mDateButton.setText(mCrime.getDate().toString());
	}

	private String getCrimeReport() {
		String solvedString = null;
		if(mCrime.isSolved()) {
			solvedString=getString(R.string.crime_report_solved);
		} else {
			solvedString=getString(R.string.crime_report_unsolved);
		}
		String dateFormat = "EEE, MMM dd";
		String dateString = DateFormat.format(dateFormat,mCrime.getDate()).toString();

		String suspect=mCrime.getSuspect();
		if(suspect==null)
			suspect=getString(R.string.crime_report_no_suspect);
		else
			suspect=getString(R.string.crime_report_suspect,suspect);

		String report = getString(R.string.crime_report,
				mCrime.getTitle(),dateString,solvedString,suspect);

		return report;

	}

	public static CrimeFragment newInstance(UUID crimeId){
		Bundle args = new Bundle();
		args.putSerializable(ARG_CRIME_ID, crimeId);
		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		return fragment;
	}


}
