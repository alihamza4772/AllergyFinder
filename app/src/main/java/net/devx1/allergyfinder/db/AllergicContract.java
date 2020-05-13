package net.devx1.allergyfinder.db;

import android.provider.BaseColumns;

public class AllergicContract {
    private AllergicContract(){}

    public class AllergyTable implements BaseColumns{
        public static final String TABLE_NAME = "Allergy";
        public static final String COLUMN_ALLERGIC = "allergic";
    }
}
