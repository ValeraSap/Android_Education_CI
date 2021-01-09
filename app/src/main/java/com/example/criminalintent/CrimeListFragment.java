package com.example.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.nio.channels.NonWritableChannelException;
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
	private Callbacks mCallbacks;

	public interface Callbacks {
		void onCrimeSelected(Crime crime);
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		mCallbacks=(Callbacks) context;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		/*if(savedInstanceState!=null)
		{
			mSubtitleVisible=savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
		}*/
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
				mSubtitleVisible = !mSubtitleVisible;
				updateSubtitle();
				getActivity().invalidateOptionsMenu(); //???
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks=null;
	}

	private void addCrime()
	{
		//mListEmptyTextView.setVisibility(View.GONE);
		Crime crime = new Crime();
		CrimeLab.getInstance(getActivity()).addCrime(crime);
		updateUI(); //писок сразу перезагружается (для планшетов)
		mCallbacks.onCrimeSelected(crime);
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
	public void updateUI()
	{
		CrimeLab crimeLab=CrimeLab.getInstance(getActivity());
		List<Crime> crimes=crimeLab.getCrimes();

		if(mAdapter==null)
		{
			mAdapter=new CrimeAdapter(crimes);
			mCrimeRecyclerView.setAdapter(mAdapter);
		}
		else
		{
			//todo does it works
			CrimeDiffUtilCallback crimeDiffUtilCallback=
					new CrimeDiffUtilCallback(mAdapter.getCrimes(),crimes);
			DiffUtil.DiffResult crimeDiffResult=DiffUtil.calculateDiff(crimeDiffUtilCallback);
			mAdapter.setCrimes(crimes);
			crimeDiffResult.dispatchUpdatesTo(mAdapter);
		}


		//mAdapter.notifyDataSetChanged();


		if(!mAdapter.mCrimes.isEmpty())
			mListEmptyTextView.setVisibility(View.GONE);
		else
			mListEmptyTextView.setVisibility(View.VISIBLE);

		updateSubtitle();

	}
	class CrimeRequirePoliceHolder extends CrimeHolder

	{
		private Button mCallPoliceButton;
		private static final String POLICE_NUMBER="102";

		public CrimeRequirePoliceHolder(LayoutInflater inflater, ViewGroup parent) {
			super (inflater.inflate(R.layout.list_item_crime_require_police,parent,false));

			mTitleTextView=(TextView) itemView.findViewById(R.id.item_crime_title);
			mDataTextView=(TextView) itemView.findViewById(R.id.item_crime_date);
			mSolvedImageView=(ImageView) itemView.findViewById(R.id.item_crime_solved);
			itemView.setOnClickListener(this); //////??
			mCallPoliceButton=(Button)itemView.findViewById(R.id.call_police);
			mCallPoliceButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View view) {
					Uri number=Uri.parse("tel:"+POLICE_NUMBER);
					Intent i=new Intent(Intent.ACTION_DIAL,number);
					startActivity(i);
				}
			});

		}

	}
	class CrimeNotRequirePoliceHolder extends CrimeHolder
	{
		public CrimeNotRequirePoliceHolder(LayoutInflater inflater, ViewGroup parent)
		{
			super (inflater.inflate(R.layout.list_item_crime,parent,false));

			mTitleTextView=(TextView) itemView.findViewById(R.id.item_crime_title);
			mDataTextView=(TextView) itemView.findViewById(R.id.item_crime_date);
			mSolvedImageView=(ImageView) itemView.findViewById(R.id.item_crime_solved);
			itemView.setOnClickListener(this);
		}
	}

	class CrimeHolder extends RecyclerView.ViewHolder
		implements View.OnClickListener
	{
		protected TextView mTitleTextView;
		protected TextView mDataTextView;
		protected ImageView mSolvedImageView;
		protected Crime mCrime;

		public CrimeHolder(@NonNull View itemView) {
			super(itemView);
		}


		/*public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
			super (inflater.inflate(R.layout.list_item_crime,parent,false));

			mTitleTextView=(TextView) itemView.findViewById(R.id.item_crime_title);
			mDataTextView=(TextView) itemView.findViewById(R.id.item_crime_date);
			mSolvedImageView=(ImageView) itemView.findViewById(R.id.item_crime_solved);
			itemView.setOnClickListener(this); //////??

		}*/
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

			mCallbacks.onCrimeSelected(mCrime);
		}
	}
	class CrimeAdapter extends  RecyclerView.Adapter
	{



		private List<Crime> mCrimes;
		private final int NOT_REQUIRE_POLICE=0;
		private final int REQUIRE_POLICE=1;

		public CrimeAdapter(List<Crime> crimes) {
			mCrimes = crimes;
		}

		@NonNull
		@Override
		public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());

			if(viewType==NOT_REQUIRE_POLICE)
				return new CrimeNotRequirePoliceHolder(inflater,parent); //в завис от viewtype new RequirePoliceCrimeHolder
			else return new CrimeRequirePoliceHolder(inflater,parent);
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
			if(mCrimes.get(position).isRequiresPolice())
				return REQUIRE_POLICE;
			else return NOT_REQUIRE_POLICE;

		}
		public List<Crime> getCrimes() {
			return mCrimes;
		}
		public void  setCrimes(List<Crime> crimes) {
			mCrimes=crimes;
		}


	}

	public class CrimeDiffUtilCallback extends DiffUtil.Callback {

		private  final List<Crime> mOldList;
		private  final List<Crime> mNewList;

		public CrimeDiffUtilCallback(List<Crime> oldCrimes,List<Crime> newCrimes)
		{
			mOldList=oldCrimes;
			mNewList=newCrimes;
		}

		@Override
		public int getOldListSize() {
			return mOldList.size();
		}

		@Override
		public int getNewListSize() {
			return mNewList.size();
		}

		@Override
		public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
			Crime oldCrime=mOldList.get(oldItemPosition);
			Crime newCrime=mNewList.get(newItemPosition);
			return oldCrime.getID()==newCrime.getID();
		}

		@Override
		public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
			Crime oldCrime=mOldList.get(oldItemPosition);
			Crime newCrime=mNewList.get(newItemPosition);
			return (oldCrime.getTitle()==newCrime.getTitle() &&
					oldCrime.getDate()==newCrime.getDate() &&
					oldCrime.isSolved()==newCrime.isSolved() &&
					oldCrime.isRequiresPolice()==newCrime.isRequiresPolice());

		}
	}
}
