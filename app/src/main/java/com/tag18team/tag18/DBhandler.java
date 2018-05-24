package com.tag18team.tag18;

import android.content.ContentValues;
import android.content.Context;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;
public class DBhandler extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;
    // таблицы
    private static final String TAG_TABLE = "TAGS";
    private static final String FILE_TABLE = "FILES";
    private static final String REL_TABLE = "RELATIONS";
    // поля таблицы для хранения
    public static final String TAG_ID = "TAG_ID";
    public static final String FILE_ID = "FILE_ID";
    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }
    // формируем запрос для создания базы данных
    private static final String CREATE_TAGS = "create table "
            + TAG_TABLE + "(" + TAG_ID + " integer primary key autoincrement, " + "NAME"
            + " text not null, " + "DESCRIPTION" + " text, " + "IS_FAVOURITE" + " integer not null default 0"+");";
    private static final String CREATE_FILES = "create table "
            + FILE_TABLE + "(" + FILE_ID + " integer primary key autoincrement, " + "NAME"
            + " text not null, " + "PATH" + " text not null, " + "IS_EXTERNAL" + " integer not null default 0"+");";
    private static final String CREATE_RELATIONS = "create table "
            + REL_TABLE + "(" + FILE_ID + " integer not null, " + TAG_ID
            + " integer not null, primary key("+FILE_ID+","+"TAG_ID"+"));";
    public DBhandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TAGS);
        db.execSQL(CREATE_FILES);
        db.execSQL(CREATE_RELATIONS);
        Log.d("log","DB created");
        Log.d("log", CREATE_TAGS);
        }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+REL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+FILE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+TAG_TABLE);
        onCreate(db);
    }
    public long addTag(String NAME, String DESC) {
        long ID=getIfExists("TAGS", "TAG_ID", "NAME", NAME);
        Log.d("addtag",""+ID);
        if (ID!=-1)return ID;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME",NAME);
        values.put("DESCRIPTION",DESC);
        values.put("IS_FAVOURITE",0);
        long row = db.insert(TAG_TABLE, null, values);
        db.close();
        return row; //номер строки или 1
    }
    public boolean updateTag(long rowId, String NAME, String DESC, boolean favourite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME",NAME);
        values.put("DESCRIPTION",DESC);
        values.put("IS_FAVOURITE",0);
        if (favourite) values.put("IS_FAVOURITE",1);
        boolean success=db.update(TAG_TABLE, values, TAG_ID + "=" + rowId,
                null) > 0;
        db.close();
        return success;
    }
    public void deleteTag(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TAG_TABLE, TAG_ID + "=" + rowId, null);
        db.close();
    }
    public long addFile(String path){
        long ID=getIfExists("FILES", "FILE_ID", "PATH", path);
        if (ID!=-1)return ID;
        int indexOfName=path.lastIndexOf('/');
        String name="folder";
        try{ name=path.substring(indexOfName+1); } catch (Exception e) {}
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME",name);
        values.put("PATH",path);
        values.put("IS_EXTERNAL",0);
        long row = db.insert(FILE_TABLE, null, values);
        db.close();
        return row;
    }
    public boolean delFile(long rowId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(REL_TABLE, FILE_ID + "=" + rowId, null);
        int deleted=db.delete(FILE_TABLE, FILE_ID + "=" + rowId, null);
        db.close();
        if (deleted==0)return false; else return true;
    }
    public void dropTags(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TAG_TABLE,null,null);
        db.delete(FILE_TABLE,null,null);
        db.close();
    }
    public void dropFiles(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(REL_TABLE,null,null);
        db.delete(FILE_TABLE,null,null);
        db.close();
    }
    public long getIfExists(String table, String column, String testColumn, String label){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor myCursor=db.rawQuery("SELECT "+column+" FROM "+table+" WHERE "+testColumn+"='"+label+"';",null);
        myCursor.moveToFirst();
        long ID;
        try{ID=myCursor.getLong(0);}catch(Exception e){ID=-1;}
        db.close();
        return ID;
    }
    public void setTag(long fileID, String tagName){ // do we REALLY need to check success?
        long tagID=getIfExists("TAGS","TAG_ID", "NAME",tagName);
        long a=getIfExists("RELATIONS","TAG_ID","FILE_ID", ""+fileID);
        Log.d("DB_tagID",""+tagID);
        Log.d("DB_relationID",""+a);
        SQLiteDatabase db = this.getWritableDatabase();
        if (a!=tagID || a==-1) {
            ContentValues values = new ContentValues();
            values.put("FILE_ID", fileID);
            values.put("TAG_ID", tagID);
            try {
                long row = db.insert(REL_TABLE, null, values);
                Log.d("DB", "pair inserted into RELATIONS: " + fileID + ":" + tagID);
            } catch (Exception e) {
                Log.d("DB", "pair already exists in RELATIONS: " + fileID + ":" + tagID);
            } // case pair exists
        }
        if(db.isOpen())db.close();
    };
    public void setTag(long fileID, long tagID){ // do we REALLY need to check success?
        long a=getIfExists("RELATIONS","TAG_ID","FILE_ID", ""+fileID);
        Log.d("DB",""+a);
        Log.d("DB",""+tagID);
        SQLiteDatabase db = this.getWritableDatabase();
        if (a!=tagID) {
            ContentValues values = new ContentValues();
            values.put("FILE_ID", fileID);
            values.put("TAG_ID", tagID);
            try {
                long row = db.insert(REL_TABLE, null, values);
                Log.d("DB", "pair inserted into RELATIONS: " + fileID + ":" + tagID);
            } catch (SQLException e) {
                Log.d("DB", "pair already exists in RELATIONS: " + fileID + ":" + tagID);
            } // case pair exists
        }
        if(db.isOpen())db.close();
    };
    public void unsetTag(long fileID, long tagID){ // do we REALLY need to check success?
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(REL_TABLE, FILE_ID + "=" + fileID +" and "+ TAG_ID+"="+tagID, null);
        db.close();
        Log.d("DB","pair removed from RELATIONS: "+fileID+":"+tagID);
    };
    public String[][] getAllRows(String table) { // files and relations
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c=db.query(table, new String[]{"*"},null,null,null,null,null);
        c.moveToFirst();
        String[][] s=new String[c.getCount()][4];
        for (int i=0; i<c.getCount();i++)
        {
            s[i][0]=""+c.getInt(0);
            s[i][1]=c.getString(1);
            s[i][2]=c.getString(2);
            s[i][3]=""+c.getInt(3);
            c.moveToNext();
        }
        db.close();
        return s;
    }
    public String[][] getFilesWithTags(String[] tagNames){
        if (tagNames==null)return getAllRows("FILES");
        else {
            String query = "";
            for (int i = 0; i < tagNames.length; i++) {
                query += "SELECT DISTINCT FILES.FILE_ID, FILES.NAME, FILES.PATH, FILES.IS_EXTERNAL FROM FILES JOIN RELATIONS " +
                        "ON FILES.FILE_ID = RELATIONS.FILE_ID JOIN TAGS ON TAGS.TAG_ID=RELATIONS.TAG_ID" +
                        " WHERE TAGS.NAME='" + tagNames[i]+"'";
                if (i + 1 != tagNames.length) query += " INTERSECT ";
            }
            query += ";";
            Log.d("DB", "QUERY for getting files: \n" + query);
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(query, null);
            /////////////////////// we may have to limit row number if database is too big
            int NUMBER = c.getCount();
            //if (NUMBER>100) NUMBER=100;
            /////////////////////// is to be fixed one day
            c.moveToFirst();
            Log.d("count", "" + c.getCount());
            String[][] s = new String[c.getCount()][4];
            for (int i = 0; i < NUMBER; i++) {
                s[i][0] = "" + c.getInt(0);
                s[i][1] = c.getString(1);
                s[i][2] = c.getString(2);
                s[i][3] = "" + c.getInt(3);
                c.moveToNext();
                Log.d("step", s[i][0] + ' ' + s[i][1] + ' ' + s[i][2] + ' ' + s[i][3]);
            }
            db.close();
            return s;
        }
        }
}