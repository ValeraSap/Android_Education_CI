package com.example.criminalintent;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment; //библиотека поддержки
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {

	private static final String ARG_CRIME_ID = "crime_id";

	private static final String DIALOG_DATE = "DialogDate";
	private static final String DIALOG_TIME = "DialogTime";
	private static final String DIALOG_PHOTO= "DialogPhoto";

	private final String DATE_PATTERN= "E dd MMM yyyy 'at' HH:mm";

	private static final int REQUEST_DATE = 0;
	public static final int REQUEST_TIME =1;
	private static final int REQUEST_CONTACT = 2;
	private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 3;
	private static final int REQUEST_PHOTO=4;


	private Crime mCrime;
	private File mPhotoFile;
	private EditText mTitleField;
	private Button mDateButton;
	private CheckBox mSolved;
	private Button mDeleteButton;
	private Button mReportButton;
	private Button mSuspectButton;
	private Button mCallSuspectButton;
	private ImageButton mPhotoButton;
	private ImageView mPhotoView;
	private CheckBox mPoliceRequire;

	private Callbacks mCallbacks;

	public interface Callbacks {
		void onCrimeUpdated(Crime crime);
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mCallbacks = (Callbacks) context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
		mCrime=CrimeLab.getInstance(getActivity()).getCrime(crimeId);
		mPhotoFile=CrimeLab.getInstance(getActivity()).getPhotoFile(mCrime);
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
				updateCrime();

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
		mSolved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mCrime.setSolved(b);
				updateCrime();
			}
		});
		mPoliceRequire=(CheckBox) view.findViewById(R.id.crime_require_police_checkbox);
		mPoliceRequire.setChecked(mCrime.isRequiresPolice());
		mPoliceRequire.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				mCrime.setRequiresPolice(b);
				updateCrime();
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

				getPermissionToReadUserContacts(); //todo fix permission

				String num=getPhoneNumber(mCrime.getSuspect(),getActivity());
				//2 позвонить на номер
					Uri number=Uri.parse("tel:"+num);
					Intent i = new Intent(Intent.ACTION_DIAL,number);
					startActivity(i);


			}
		});


		mPhotoButton=(ImageButton)view.findViewById(R.id.crime_camera);

		final Intent captureImage=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		boolean canTakePhoto = mPhotoFile !=null &&
				captureImage.resolveActivity(packageManager)!=null;
		mPhotoButton.setEnabled(canTakePhoto);

		mPhotoButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{

				Uri uri= FileProvider.getUriForFile(getActivity(),
						"com.example.criminalintent.fileprovider",
						mPhotoFile);
				captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
				List<ResolveInfo> cameraActivities = getActivity()
						.getPackageManager().queryIntentActivities(captureImage,
								PackageManager.MATCH_DEFAULT_ONLY);

				for(ResolveInfo activity : cameraActivities) {
					getActivity().grantUriPermission(activity.activityInfo.packageName,
							uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				}
				startActivityForResult(captureImage,REQUEST_PHOTO);
			}
		});
		mPhotoView=(ImageView)view.findViewById(R.id.crime_photo);
		updatePhotoView();
		mPhotoView.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view)
			{
				PhotoFragment pf=PhotoFragment.newInstance(mPhotoFile);
				FragmentManager fm=getFragmentManager();
				pf.show(fm,DIALOG_PHOTO);
			}
		});

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	public String getPhoneNumber(String name, Context context){
		String ret = null;
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name + "%'";
		String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection, selection, null, null);
		try {
			if (c.moveToFirst()) {
				ret = c.getString(0);
			}
		} finally {
			c.close();
		}

		if (ret == null)
			ret = "Unsaved";
		return ret;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
			updateCrime();//порядок следования????
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
				updateCrime();
				mSuspectButton.setText(suspect);
			} finally {
				c.close();
			}
		} else if(requestCode==REQUEST_PHOTO) {
			///////////////Отобрали разрешение записывать в наш каталог файлы
			Uri uri=FileProvider.getUriForFile(getActivity(),
					"com.example.criminalintent.fileprovider",
					mPhotoFile);
			getActivity().revokeUriPermission(uri,
					Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			updateCrime();
			updatePhotoView();
		}

	}
	private void updateCrime() { ////???
		CrimeLab.getInstance(getActivity()).updateCrime(mCrime);
		mCallbacks.onCrimeUpdated(mCrime);
	}

	private void updateDate() {
		Locale currentLocale = getResources().getConfiguration().locale;

		SimpleDateFormat formatter= new SimpleDateFormat(DATE_PATTERN, currentLocale );
		mDateButton.setText(formatter.format(mCrime.getDate()));
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

	private void updatePhotoView() {
		if(mPhotoFile == null || !mPhotoFile.exists())
		{
			mPhotoView.setImageDrawable(null);
			mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
		} else {
		
			mPhotoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),
							mPhotoView.getWidth(),mPhotoView.getHeight()
								);
					mPhotoView.setImageBitmap(bitmap);
					mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));

				}
			});


		}
	}

	public static CrimeFragment newInstance(UUID crimeId){
		Bundle args = new Bundle();
		args.putSerializable(ARG_CRIME_ID, crimeId);
		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		return fragment;
	}


}
