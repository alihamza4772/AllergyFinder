package net.devx1.allergyfinder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.devx1.allergyfinder.model.Allergic;

import java.util.ArrayList;
import java.util.List;

import net.devx1.allergyfinder.db.AllergicContract.AllergyTable;
import net.devx1.allergyfinder.db.AllergicContract.HistoryTable;
import net.devx1.allergyfinder.model.History;


public class DbOperations {

	private static SQLiteDatabase initWrite(Context ctx) {
		AllergicDbHelper helper = new AllergicDbHelper(ctx);
		return helper.getWritableDatabase();
	}

	private static SQLiteDatabase initRead(Context ctx) {
		AllergicDbHelper helper = new AllergicDbHelper(ctx);
		return helper.getReadableDatabase();
	}

	public static long insert(Context ctx, String allergic) {
		ContentValues cv = new ContentValues();
		cv.put(AllergyTable.COLUMN_ALLERGIC, allergic);
		return initWrite(ctx).insert(AllergyTable.TABLE_NAME, null, cv);
	}

	public static List<Allergic> retrieve(Context ctx) {
		String[] projection = {
			AllergyTable.COLUMN_ALLERGIC
		};

		Cursor cursor =
			initRead(ctx).query(
				AllergyTable.TABLE_NAME,
				projection,
				null,
				null,
				null,
				null,
				null
			);

		List<Allergic> allergies = new ArrayList<>();
		while (cursor.moveToNext()) {
			allergies.add(new Allergic(cursor.getString(cursor.getColumnIndexOrThrow(AllergyTable.COLUMN_ALLERGIC))));
		}

		return allergies;
	}

	public static List<String> retrieveText(Context ctx) {
		List<Allergic> allergics = retrieve(ctx);
		List<String> allergies = new ArrayList<>();
		for (Allergic allergic : allergics) {
			allergies.add(allergic.getAllergicTo());
		}
		return allergies;
	}

	public static int delete(Context ctx, String allergy) {
		String whereClause = AllergyTable.COLUMN_ALLERGIC + " = ? ";
		String[] whereArgs = {allergy};

		return initWrite(ctx).delete(
			AllergyTable.TABLE_NAME, whereClause, whereArgs
		);
	}

	public static long history(Context ctx, String path, String status, String allergies) {
		ContentValues cv = new ContentValues();
		cv.put(HistoryTable.COLUMN_PATH, path);
		cv.put(HistoryTable.COLUMN_STATUS, status);
		cv.put(HistoryTable.COLUMN_ALLERGIES, allergies);
		return initWrite(ctx).insert(HistoryTable.TABLE_NAME, null, cv);
	}

	public static List<History> retrieveHistory(Context ctx) {
		String[] projection = {
			HistoryTable.COLUMN_PATH,
			HistoryTable.COLUMN_STATUS,
			HistoryTable.COLUMN_ALLERGIES
		};

		Cursor cursor =
			initRead(ctx).query(
				HistoryTable.TABLE_NAME,
				projection,
				null,
				null,
				null,
				null,
				null
			);

		List<History> history = new ArrayList<>();
		while (cursor.moveToNext()) {
			history.add(new History(
				cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_PATH)),
				cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_STATUS)),
				cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_ALLERGIES)
				)));
		}

		return history;
	}

	public static List<History> retrieveAllergicHistory(Context ctx) {
		return retrieveCustomHistory(ctx, "allergic");
	}

	public static List<History> retrieveSafeHistory(Context ctx) {
		return retrieveCustomHistory(ctx, "safe");
	}

	private static List<History> retrieveCustomHistory(Context ctx, String status) {
		String[] projection = {
			HistoryTable.COLUMN_PATH,
			HistoryTable.COLUMN_STATUS,
			HistoryTable.COLUMN_ALLERGIES
		};

		String selection = HistoryTable.COLUMN_STATUS + " = ?";
		String[] selectionArgs = {status};


		Cursor cursor =
			initRead(ctx).query(
				HistoryTable.TABLE_NAME,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				null
			);

		List<History> history = new ArrayList<>();
		while (cursor.moveToNext()) {
			history.add(new History(
				cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_PATH)),
				cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_STATUS)),
				cursor.getString(cursor.getColumnIndex(HistoryTable.COLUMN_ALLERGIES)
				)));
		}

		return history;
	}

}
