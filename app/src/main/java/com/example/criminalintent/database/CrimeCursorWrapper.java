package com.example.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.criminalintent.Crime;
import com.example.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {
	public CrimeCursorWrapper(Cursor cursor) {
		super(cursor);
	}

	public Crime getCrime() {
		String uuidString=getString(getColumnIndex(CrimeTable.Cols.UUID));
		String title =getString(getColumnIndex(CrimeTable.Cols.TITLE));
		long date =getLong(getColumnIndex(CrimeTable.Cols.DATE));
		int issolved=	getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
		String suspect=getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
		int isPoliceRequired=getInt(getColumnIndex(CrimeTable.Cols.POLICE));

		Crime crime=new Crime(UUID.fromString(uuidString));
		crime.setTitle(title);
		crime.setDate(new Date(date));
		crime.setSolved(issolved!=0);
		crime.setSuspect(suspect);
		crime.setRequiresPolice(isPoliceRequired!=0);
		return crime;
	}
}
