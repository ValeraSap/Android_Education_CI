package com.example.criminalintent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {


	private static final String EXTRA_CRIME_ID =
			"com.example.criminalintent.crime_id";

	private ViewPager mViewPager;
	private List<Crime> mCrimes;

	private Button mFirstButton;
	private Button mLastButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crime_pager);

		UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

		mViewPager=(ViewPager) findViewById(R.id.crime_view_pager);
		mCrimes=CrimeLab.getInstance(this).getCrimes();

		mFirstButton=(Button) findViewById(R.id.first_crime);
		mLastButton=(Button) findViewById(R.id.last_crime);

		FragmentManager fm=getSupportFragmentManager();
		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
			@NonNull
			@Override
			public Fragment getItem(int position) {
				Crime crime=mCrimes.get(position);
				return CrimeFragment.newInstance(crime.getID());
			}

			@Override
			public int getCount() {
				return mCrimes.size();
			}
		});

		for (int i = 0; i < mCrimes.size(); i++) {
			if (mCrimes.get(i).getID().equals(crimeId)) {
				mViewPager.setCurrentItem(i);
				break;
			}
		}

		mFirstButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(0);
			}
		});

		mLastButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(mCrimes.size()-1);
			}
		});

		//TODO делать эту проверку при листании
		//вероятно придется переопределять свой адаптер

		if(mViewPager.getCurrentItem()==0)
			mFirstButton.setVisibility(View.INVISIBLE);
		else mFirstButton.setVisibility(View.VISIBLE);

		if(mViewPager.getCurrentItem()==mCrimes.size()-1)
			mLastButton.setVisibility(View.INVISIBLE);
		else mLastButton.setVisibility(View.VISIBLE);

	}


	public static Intent newIntent(Context context, UUID crimeId)
	{
		Intent intent = new Intent(context, CrimePagerActivity.class);
		intent.putExtra(EXTRA_CRIME_ID, crimeId);
		return intent;


	}

	/*class CrimePagerAdapter extends PagerAdapter
	{

		@Override
		public int getCount() {
			return 0;
		}

		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
			return false;
		}
	}*/
}