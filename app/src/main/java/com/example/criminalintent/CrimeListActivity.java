package com.example.criminalintent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;

public class CrimeListActivity extends SingleFragmentActivity
implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks{


	@Override
	protected Fragment createFragment() {
		return new CrimeListFragment();
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}

	@Override
	public void onCrimeSelected(Crime crime) {
		if(findViewById(R.id.detail_fragment_container)==null)
		{
			Intent intent=CrimePagerActivity.newIntent(this,crime.getID());
			startActivity(intent);
		}
		else {
			Fragment newDetail=CrimeFragment.newInstance(crime.getID());
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.detail_fragment_container,newDetail)
					.commit();

		}
	}

	/***
	 * Интерфейс CrimeFragment.Callbacks должен быть реализован
	 * во всех активностях, выполняющих функции хоста для CrimeFragment.
	 */
	@Override
	public void onCrimeUpdated(Crime crime) {
		CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragment_container);
		listFragment.updateUI();
	}
}