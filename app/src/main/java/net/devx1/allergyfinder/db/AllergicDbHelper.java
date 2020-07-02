package net.devx1.allergyfinder.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.LocaleDisplayNames;
import android.os.Build;
import android.util.Log;

import net.devx1.allergyfinder.db.AllergicContract.UsersTable;
import net.devx1.allergyfinder.db.AllergicContract.AllergyTable;
import net.devx1.allergyfinder.db.AllergicContract.HistoryTable;
import net.devx1.allergyfinder.model.History;

import androidx.annotation.Nullable;

public class AllergicDbHelper extends SQLiteOpenHelper {
	private final static int DATABASE_VERSION = 7;
	private static final String DATABASE_NAME = "AllergyRec.db";
//db
	private static final String CREATE_TABLE_USERS =
		"CREATE TABLE " + UsersTable.TABLE_NAME + " (" +
			UsersTable._ID + " INTEGER PRIMARY KEY," +
			UsersTable.COLUMN_USERNAME + " TEXT UNIQUE," +
			UsersTable.COLUMN_PASSWORD + " TEXT," +
			UsersTable.COLUMN_IMAGE + " TEXT)";

	private final String DROP_TABLE_USERS =
		"DROP TABLE IF EXISTS " + UsersTable.TABLE_NAME;

	private static final String CREATE_TABLE_ALLERGIC =
		"CREATE TABLE " + AllergyTable.TABLE_NAME + " (" +
			AllergyTable._ID + " INTEGER," +
			AllergyTable.COLUMN_USERNAME + " TEXT," +
			AllergyTable.COLUMN_ALLERGIC + " TEXT," +
			" FOREIGN KEY(" + AllergyTable.COLUMN_USERNAME + ") REFERENCES " +
			UsersTable.TABLE_NAME + "(" + UsersTable.COLUMN_USERNAME + "), " +
			"PRIMARY KEY(" +
			AllergyTable.COLUMN_USERNAME + ", " +
			AllergyTable.COLUMN_ALLERGIC + "));";

	private final String DROP_TABLE_ALLERGIC =
		"DROP TABLE IF EXISTS " + AllergyTable.TABLE_NAME;

	private static final String CREATE_TABLE_HISTORY =
		"CREATE TABLE " + HistoryTable.TABLE_NAME + " (" +
			HistoryTable._ID + " INTEGER PRIMARY KEY," +
			HistoryTable.COLUMN_USERNAME + " TEXT," +
			HistoryTable.COLUMN_PATH + " TEXT UNIQUE," +
			HistoryTable.COLUMN_STATUS + " VARCHAR(10)," +
			HistoryTable.COLUMN_ALLERGIES + " TEXT," +
			" FOREIGN KEY(" + HistoryTable.COLUMN_USERNAME + ") REFERENCES " +
			UsersTable.TABLE_NAME + "(" + UsersTable.COLUMN_USERNAME + "));";


	private final String DROP_TABLE_HISTORY =
		"DROP TABLE IF EXISTS " + HistoryTable.TABLE_NAME;

	AllergicDbHelper(@Nullable Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CREATE_TABLE_USERS);
			db.execSQL(CREATE_TABLE_ALLERGIC);
			db.execSQL(CREATE_TABLE_HISTORY);
		} catch (SQLException e) {
			Log.d("SQLEXCEP", e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DROP_TABLE_HISTORY);
		db.execSQL(DROP_TABLE_ALLERGIC);
		db.execSQL(DROP_TABLE_USERS);
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (Build.VERSION.SDK_INT >= 28) {
			db.disableWriteAheadLogging();
		}
	}
}
