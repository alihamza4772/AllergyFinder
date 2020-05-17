package net.devx1.allergyfinder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import net.devx1.allergyfinder.model.Allergic;

import java.util.ArrayList;
import java.util.List;

import net.devx1.allergyfinder.db.AllergicContract.UsersTable;
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

	public static long insertUser(Context ctx, String username, String password) {
		ContentValues cv = new ContentValues();
		cv.put(UsersTable.COLUMN_USERNAME, username);
		cv.put(UsersTable.COLUMN_PASSWORD, password);
		cv.put(UsersTable.COLUMN_IMAGE, "null");
		return initWrite(ctx).insert(UsersTable.TABLE_NAME, null, cv);
	}

	public static boolean isUserExists(Context ctx, String username, String password) {
		String[] projection = {
			UsersTable.COLUMN_USERNAME
		};

		String selection =
			UsersTable.COLUMN_USERNAME + " = ? AND " + UsersTable.COLUMN_PASSWORD + " = ?";
		String[] selectionArgs = {username, password};

		Cursor cursor =
			initRead(ctx).query(
				UsersTable.TABLE_NAME,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				null
			);
		int size = cursor.getCount();
		cursor.close();
		return size > 0;
	}

	public static String getUserPicture(Context context, String username){
		String[] projection = {
			UsersTable.COLUMN_IMAGE
		};

		String selection =
			UsersTable.COLUMN_USERNAME + " = ?";
		String[] selectionArgs = {username};

		Cursor cursor =
			initRead(context).query(
				UsersTable.TABLE_NAME,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				null
			);
		cursor.moveToNext();
		String path = cursor.getString(0);
		cursor.close();
		return path;
	}

	public static long insertAllergy(Context ctx, String username, String allergic) {
		ContentValues cv = new ContentValues();
		cv.put(AllergyTable.COLUMN_USERNAME, username);
		cv.put(AllergyTable.COLUMN_ALLERGIC, allergic);
		return initWrite(ctx).insert(AllergyTable.TABLE_NAME, null, cv);
	}

	public static List<Allergic> retrieveAllergies(Context ctx, String username) {
		String[] projection = {
			AllergyTable.COLUMN_ALLERGIC
		};

		String selection =
			AllergyTable.COLUMN_USERNAME + " = ?";
		String[] selectionArgs = {username};

		Cursor cursor =
			initRead(ctx).query(
				AllergyTable.TABLE_NAME,
				projection,
				selection,
				selectionArgs,
				null,
				null,
				null
			);

		List<Allergic> allergies = new ArrayList<>();
		Toast.makeText(ctx, "" + cursor.getCount(), Toast.LENGTH_SHORT).show();
		while (cursor.moveToNext()) {
			allergies.add(new Allergic(cursor.getString(cursor.getColumnIndexOrThrow(AllergyTable.COLUMN_ALLERGIC))));
		}
		cursor.close();
		return allergies;
	}

	public static List<String> retrieveText(Context ctx, String username) {
		List<Allergic> allergics = retrieveAllergies(ctx, username);
		List<String> allergies = new ArrayList<>();
		for (Allergic allergic : allergics) {
			allergies.add(allergic.getAllergicTo());
		}
		return allergies;
	}

	public static int deleteAllergy(Context ctx, String allergy) {
		String whereClause = AllergyTable.COLUMN_ALLERGIC + " = ? ";
		String[] whereArgs = {allergy};

		return initWrite(ctx).delete(
			AllergyTable.TABLE_NAME, whereClause, whereArgs
		);
	}

	public static long insertHistory(Context ctx, String user, String path, String status,
	                                 String allergies) {
		ContentValues cv = new ContentValues();
		cv.put(HistoryTable.COLUMN_USERNAME, user);
		cv.put(HistoryTable.COLUMN_PATH, path);
		cv.put(HistoryTable.COLUMN_STATUS, status);
		cv.put(HistoryTable.COLUMN_ALLERGIES, allergies);
		return initWrite(ctx).insert(HistoryTable.TABLE_NAME, null, cv);
	}

	public static List<History> retrieveHistory(Context ctx, String username) {
		String[] projection = {
			HistoryTable.COLUMN_PATH,
			HistoryTable.COLUMN_STATUS,
			HistoryTable.COLUMN_ALLERGIES
		};

		String selection = HistoryTable.COLUMN_USERNAME + " = ?";
		String[] selectionArgs = {username};

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

		cursor.close();

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

		cursor.close();
		return history;
	}

}
