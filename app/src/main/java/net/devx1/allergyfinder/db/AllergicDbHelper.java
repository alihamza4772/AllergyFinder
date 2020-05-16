package net.devx1.allergyfinder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import net.devx1.allergyfinder.db.AllergicContract.AllergyTable;
import net.devx1.allergyfinder.db.AllergicContract.HistoryTable;

import androidx.annotation.Nullable;

public class AllergicDbHelper extends SQLiteOpenHelper {
    private final static int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "AllergyRec.db";

    private static final String CREATE_TABLE_ALLERGIC =
            "CREATE TABLE " + AllergyTable.TABLE_NAME + " (" +
                    AllergyTable._ID + " INTEGER PRIMARY KEY," +
                    AllergyTable.COLUMN_ALLERGIC + " TEXT UNIQUE)";

    private final String DROP_TABLE_ALLERGIC =
            "DROP TABLE IF EXISTS " + AllergyTable.TABLE_NAME;

	private static final String CREATE_TABLE_HISTORY =
		"CREATE TABLE " + HistoryTable.TABLE_NAME + " (" +
			HistoryTable._ID + " INTEGER PRIMARY KEY," +
			HistoryTable.COLUMN_PATH + " TEXT UNIQUE," +
			HistoryTable.COLUMN_STATUS + " VARCHAR(10)," +
			HistoryTable.COLUMN_ALLERGIES + " TEXT)";


	private final String DROP_TABLE_HISTORY =
		"DROP TABLE IF EXISTS " + HistoryTable.TABLE_NAME;

    AllergicDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ALLERGIC);
        db.execSQL(CREATE_TABLE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_ALLERGIC);
        db.execSQL(DROP_TABLE_HISTORY);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (Build.VERSION.SDK_INT >= 28){
            db.disableWriteAheadLogging();
        }
    }
}
