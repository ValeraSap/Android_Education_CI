package com.example.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.criminalintent.database.CrimeBaseHelper;
import com.example.criminalintent.database.CrimeCursorWrapper;
import com.example.criminalintent.database.CrimeDbSchema;
import com.example.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

	private static CrimeLab sCrimeLab;
	private Context mContext;
	private SQLiteDatabase mDatabase;


	public static CrimeLab getInstance(Context context){
		if(sCrimeLab == null)
			sCrimeLab = new CrimeLab(context);
		return sCrimeLab;
	}

	private CrimeLab(Context context){
		mContext=context.getApplicationContext(); // Он будет использоваться CrimeLab в главе 16
		mDatabase=new CrimeBaseHelper(mContext)
				.getWritableDatabase();

	}

	public void addCrime(Crime c) {
		ContentValues values=getContentValues(c);
		mDatabase.insert(CrimeTable.NAME,null,values);
	}

	public void deleteCrime(Crime c) {
		String uuidString=c.getID().toString();
		mDatabase.delete(CrimeTable.NAME,
				CrimeTable.Cols.UUID+" = ?",
				new String[]{uuidString});
	}

	public File getPhotoFile(Crime c)
	{
		File filesDir=mContext.getFilesDir();
		return new File(filesDir, c.getPhotoFilename());
	}

	public Crime getCrime(UUID uuid) {

		CrimeCursorWrapper cursor = queryCrimes(
				CrimeTable.Cols.UUID+ " = ?",
				new String[]{uuid.toString()});

		try{
			if(cursor.getCount()==0)
			{
				return null;
			}
			cursor.moveToFirst();
			return cursor.getCrime();
		} finally {
			cursor.close();
		}


	}

	public void updateCrime(Crime crime) {
		String uuidString=crime.getID().toString();
		ContentValues values=getContentValues(crime);

		mDatabase.update(CrimeTable.NAME,values,
				CrimeTable.Cols.UUID+" = ?",
				new String[]{uuidString});
	}

	public List<Crime> getCrimes()
	{
		List<Crime> crimes=new ArrayList<>();

		CrimeCursorWrapper cursor=queryCrimes(null,null);

		try{
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				crimes.add(cursor.getCrime());
				cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}
		return crimes;
	}

	private ContentValues getContentValues(Crime crime) {
		ContentValues values=new ContentValues();
		values.put(CrimeTable.Cols.UUID,crime.getID().toString());
		values.put(CrimeTable.Cols.TITLE,crime.getTitle());
		values.put(CrimeTable.Cols.DATE,crime.getDate().getTime());
		values.put(CrimeTable.Cols.SOLVED,crime.isSolved()?1:0);
		values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
		return values;
	}

	private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
		Cursor cursor=mDatabase.query(
				CrimeTable.NAME,
				null, ///null - все столбцы
				whereClause,
				whereArgs,
				null,
				null,
				null
		);
		return new CrimeCursorWrapper(cursor);
	}

}
