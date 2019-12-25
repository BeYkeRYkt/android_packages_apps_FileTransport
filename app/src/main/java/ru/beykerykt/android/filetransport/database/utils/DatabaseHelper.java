package ru.beykerykt.android.filetransport.database.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SERVER = "server";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_HOST = "host";
    public static final String COLUMN_PORT = "port";
    public static final String COLUMN_ANONYMOUS = "anonymous";
    public static final String COLUMN_LOGIN = "login";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ACTIVE_MODE = "activeMode";

    private static final String DATABASE_CREATE = "create table " + TABLE_SERVER + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null,"
            + COLUMN_HOST + " text not null,"
            + COLUMN_PORT + " int not null,"
            + COLUMN_ANONYMOUS + " int not null,"
            + COLUMN_LOGIN + " text null,"
            + COLUMN_PASSWORD + " text null,"
            + COLUMN_ACTIVE_MODE + " int not null );";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVER);
        onCreate(db);
    }
}