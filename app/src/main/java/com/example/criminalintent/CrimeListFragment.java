package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;

import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {

	private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
	private  boolean mSubtitleVisible;

	/****
	 Класс RecyclerView взаимодействует с адаптером, когда требуется создать новый объект ViewHolder
	 или связать существующий объект ViewHolder с объектом Crime.
	 Он обращается за помощью к адаптеру, вызывая его метод.
	 Сам виджет RecyclerView ничего не знает об объекте Crime, но адаптер располагает полной информацией о Crime.
	 */
	RecyclerView mCrimeRecyclerView;
	private CrimeAdapter mAdapter;
	private FloatingActionButton mAddCrimeButton;
	private TextView mListEmptyTextView;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(savedInstanceState!=null)
		{
			mSubtitleVisible=savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
		mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
		mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); //LinearLayoutManager размещает элементы в вертикальном списке
		mListEmptyTextView=(TextView)view.findViewById(R.id.empty_list_textview);

		mAddCrimeButton=(FloatingActionButton)view.findViewById(R.id.add_crime_button);
		mAddCrimeButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				addCrime();
			}
		});



		updateUI();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateUI();
	}


	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		//Вызов заполняет экземпляр Menu командами, определенными в файле.
		inflater.inflate(R.menu.fragment_crime_list,menu);

		MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
		if (mSubtitleVisible) {
			subtitleItem.setTitle(R.string.hide_subtitle);
		} else {
			subtitleItem.setTitle(R.string.show_subtitle);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
	}


	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId())
		{
			case R.id.new_crime:
				addCrime();
				return  true; //верните true; тем самым вы сообщаете, что дальнейшая обработка не нужна
			case R.id.show_subtitle:
				updateSubtitle();
				mSubtitleVisible = !mSubtitleVisible;
				getActivity().invalidateOptionsMenu();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void addCrime()
	{
		//mListEmptyTextView.setVisibility(View.GONE);
		Crime crime = new Crime();
		CrimeLab.getInstance(getActivity()).addCrime(crime);
		Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getID());
		startActivity(intent);
	}

	private void updateSubtitle()
	{
		CrimeLab crimeLab =CrimeLab.getInstance(getActivity());
		int crimeCount = crimeLab.getCrimes().size();
		String subtitle = getResources()
				.getQuantityString(R.plurals.subtitle_plural,crimeCount,crimeCount);
		// getString(R.string.subtitle_format, crimeCount); ///значения, подставляемые на место заполнителей в строковом ресурсе
		if (!mSubtitleVisible) {
			subtitle = null;
		}
		AppCompatActivity activity = (AppCompatActivity) getActivity(); //активность, являющаяся хостом для CrimeListFragment, преобразуется в AppCompatActivity
		//чтобы панель инструментов была доступной для приложения
		activity.getSupportActionBar().setSubtitle(subtitle);

	}
	private void updateUI()
	{
		CrimeLab crimeLab=CrimeLab.getInstance(getActivity());
		List<Crime> crimes=crimeLab.getCrimes();

		if(mAdapter==null)
		{
			mAdapter=new CrimeAdapter(crimes);
			mCrimeRecyclerView.setAdapter(mAdapter);
		}
		else
			mAdapter.setCrimes(crimes);
			mAdapter.notifyDataSetChanged();
		//TODO mAdapter.notifyItemChanged(int)

		if(!mAdapter.mCrimes.isEmpty())
			mListEmptyTextView.setVisibility(View.GONE);
		else
			mListEmptyTextView.setVisibility(View.VISIBLE);

		updateSubtitle();

	}

	class CrimeHolder extends RecyclerView.ViewHolder
		implements View.OnClickListener
	{
		private TextView mTitleTextView;
		private TextView mDataTextView;
		private ImageView mSolvedImageView;
		private Crime mCrime;



		public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
			super (inflater.inflate(R.layout.list_item_crime,parent,false));

			mTitleTextView=(TextView) itemView.findViewById(R.id.item_crime_title);
			mDataTextView=(TextView) itemView.findViewById(R.id.item_crime_date);
			mSolvedImageView=(ImageView) itemView.findViewById(R.id.item_crime_solved);
			itemView.setOnClickListener(this); //////??

		}
		public void bind (Crime crime)
		{
			mCrime=crime;
			mTitleTextView.setText(mCrime.getTitle());
			//mDataTextView.setText(mCrime.getDate().toString());
			//new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm" )
			mDataTextView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(mCrime.getDate())
					+" at " +DateFormat.getTimeInstance(DateFormat.SHORT).format(mCrime.getDate()));

			mSolvedImageView.setVisibility(mCrime.isSolved()?View.VISIBLE:View.GONE);
		}

		@Override
		public void onClick(View v) {

			//TODO пометить, что этазапись изменилась
			Intent intent = CrimePagerActivity.newIntent(getActivity(),mCrime.getID());
			startActivity(intent);
		}
	}
	class CrimeAdapter extends  RecyclerView.Adapter
	{

		private List<Crime> mCrimes;

		public CrimeAdapter(List<Crime> crimes) {
			mCrimes = crimes;
		}

		@NonNull
		@Override
		public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());

			//todo добавить логику, которая будет возвращать разные объекты ViewHolder
			// в зависимости от нового значения viewType, возвращаемого методом getItemViewType(int)

			return new CrimeHolder(inflater,parent); //в завис от viewtype new RequirePoliceCrimeHolder
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

			Crime crime = mCrimes.get(position);
			((CrimeHolder) holder).bind(crime);

		}


		@Override
		public int getItemCount() {
			return mCrimes.size();
		}


		@Override
		public int getItemViewType(int position) {
			//TODO какое представление следует загружать в CrimeAdapter
			return super.getItemViewType(position);
		}

		public void  setCrimes(List<Crime> crimes) {
			mCrimes=crimes;
		}


	}
}
