package net.devx1.allergyfinder.db;

import android.provider.BaseColumns;

public class AllergicContract {
	private AllergicContract() {
	}

	public class UsersTable implements BaseColumns {
		public static final String TABLE_NAME = "Users";
		public static final String COLUMN_USERNAME = "username";
		public static final String COLUMN_PASSWORD = "password";
		public static final String COLUMN_IMAGE = "image";
	}

	public class AllergyTable implements BaseColumns {
		public static final String TABLE_NAME = "Allergy";
		public static final String COLUMN_USERNAME = "username";
		public static final String COLUMN_ALLERGIC = "allergic";
	}

	public class HistoryTable implements BaseColumns {
		public static final String TABLE_NAME = "History";
		public static final String COLUMN_USERNAME = "username";
		public static final String COLUMN_PATH = "path";
		public static final String COLUMN_STATUS = "status";
		public static final String COLUMN_ALLERGIES = "allergies";
	}
}
