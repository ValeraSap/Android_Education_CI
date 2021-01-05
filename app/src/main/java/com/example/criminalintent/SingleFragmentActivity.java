package com.example.criminalintent;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public abstract  class SingleFragmentActivity extends AppCompatActivity {

	protected  abstract Fragment createFragment();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		/********
		 * Мы вызываем getSupportFragmentManager(), потому что в приложении используется библиотека поддержки и класс AppCompatActivity.
		 * Если бы мы не использовали библиотеку поддержки, то вместо этого можно было бы субклассировать
		 * Activity и вызвать getFragmentManager().
		 */
		FragmentManager fragmentManager = getSupportFragmentManager(); //библиотека поддержки
		Fragment fragment=fragmentManager.findFragmentById(R.id.fragment_container); //ищем там, где он должен быть, т.е. в контейнере

		if(fragment==null)
		{
			fragment= createFragment();
			fragmentManager.beginTransaction()         ////////создает и возвращает экземпляр FragmentTransaction
					.add(R.id.fragment_container,fragment) // два параметра: идентификатор контейнерного представления и объект CrimeFragment (где будет лежать и что
					.commit();
		}
	}

}
