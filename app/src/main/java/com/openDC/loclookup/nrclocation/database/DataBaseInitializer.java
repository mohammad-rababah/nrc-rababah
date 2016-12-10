package rababah.mohamm.nrc.nrclocation.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by hp on 09/12/2016.
 */

public class DataBaseInitializer extends SQLiteOpenHelper {

    private static final String TABLE_CACHE = "TABLE_CACHE";
    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String AGE = "AGE";
    private static final String GENDER = "GENDER";
    private static final String DATABASE_NAME = "NRC";
    private static final String CREATE_TABLE_CACHE = "CREATE TABLE"
            + TABLE_CACHE + "(" + ID + " INTEGER PRIMARY KEY,"
            + NAME + " TEXT," + AGE + " TEXT,"
            + GENDER + " TEXT" + ")";

    public DataBaseInitializer(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_CACHE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
