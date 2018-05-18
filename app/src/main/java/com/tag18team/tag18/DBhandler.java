package com.tag18team.tag18;

import android.content.ContentValues;
import android.content.Context;
import android.database.*;
import android.database.sqlite.*;
public class DBhandler extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "android.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_TABLE = "TAGS";
    // поля таблицы для хранения ФИО, Должности и Телефона (id формируется автоматически)
    public static final String COLUMN_ID = "TAG_ID";
    public static final String COLUMN_NAME = "TAG_NAME";
    public static final String COLUMN_DESC = "TAG_DESCRIPTION";
    public static final String COLUMN_FAV = "TAG_IS_FAVOURITE";
    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }
    // формируем запрос для создания базы данных
    private static final String DATABASE_CREATE = "create table "
            + DATABASE_TABLE + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_NAME
            + " text not null, " + COLUMN_DESC + " text not null, " + COLUMN_FAV + "text not null default 'false'"+");";
    public DBhandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //создаем таблицу
        db.execSQL(DATABASE_CREATE);
        //добавляем строку
        ContentValues initialValues = createContentValues("File", "Each file has this tag");
        db.insert(DATABASE_TABLE, null, initialValues);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
        onCreate(db);
    }
    /**
     * Создаёт новый тег. Если создан успешно - возвращается
     * номер строки rowId, иначе -1
     */
    public long createNewTable(String NAME, String DESC) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues initialValues = createContentValues(NAME, DESC);
        long row = db.insert(DATABASE_TABLE, null, initialValues);
        db.close();
        return row;
    }
    /**
     * Изменение строчки
     */
    public boolean updateTable(long rowId, String NAME, String DESC) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateValues = createContentValues(NAME, DESC);
        return db.update(DATABASE_TABLE, updateValues, COLUMN_ID + "=" + rowId,
                null) > 0;
    }
    /**
     * Удаление контакта
     */
    public void deleteTable(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, COLUMN_ID + "=" + rowId, null);
        db.close();
    }
    /**
     * Получение всех контактов
     */
    public Cursor getFullTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(DATABASE_TABLE, new String[]{COLUMN_ID,
                        COLUMN_NAME, COLUMN_DESC}, null,
                null, null, null, null);
    }
    /**
     * Получаем конкретный контакт
     */
    public Cursor getTable(long rowId) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.query(true, DATABASE_TABLE,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_DESC}, COLUMN_ID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    /*
     * Описываем структуру данных
     */
    private ContentValues createContentValues(String NAME,
                                              String DESC) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, NAME);
        values.put(COLUMN_NAME, DESC);
        return values;
    }
}