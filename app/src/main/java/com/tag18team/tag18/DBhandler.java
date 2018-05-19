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
            + " text not null, " + "DESCRIPTION" + " text not null, " + "IS_FAVOURITE" + " integer not null default 0"+");";
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
        return db.update(TAG_TABLE, values, TAG_ID + "=" + rowId,
                null) > 0;
    }
    public void deleteTag(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TAG_TABLE, TAG_ID + "=" + rowId, null);
        db.close();
    }
    public Cursor getTag(long rowId) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.query(true, TAG_TABLE,
                new String[]{TAG_ID, "NAME", "DESCRIPTION"}, TAG_ID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    public void dropTags(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TAG_TABLE,null,null);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public long addFile(String path){
        int indexOfName=path.lastIndexOf('/');
        String name="folder";
        try{
            name=path.substring(indexOfName+1);
        }
        catch (Exception e)
        {
            Log.d("log","It's a folder");
        }
        Log.d("files",name);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME",name);
        values.put("PATH",path);
        values.put("IS_EXTERNAL",0);
        long row = db.insert(FILE_TABLE, null, values);
        db.close();
        return row;
    };
    public boolean changeFilePath(long fileID, String newPath){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("PATH",newPath);
        int success=db.update(FILE_TABLE,values,FILE_ID+"="+fileID, null);
        Log.d("DB","path of file "+fileID+" changed to "+newPath);
        return (success==1);
    }
    public boolean delFile(long rowId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(REL_TABLE, FILE_ID + "=" + rowId, null);
        int deleted=db.delete(FILE_TABLE, FILE_ID + "=" + rowId, null);
        db.close();
        if (deleted==0)return false; else return true;
    };
    public void dropFiles(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(REL_TABLE,null,null);
        db.delete(FILE_TABLE,null,null);
    }
    public void setTag(long fileID, long tagID){ // do we REALLY need to check success?
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("FILE_ID", fileID);
        values.put("TAG_ID",tagID);
        try {
            long row = db.insert(REL_TABLE, null, values);
            Log.d("DB","pair inserted into RELATIONS: "+fileID+":"+tagID);
        } catch (Exception e){
            Log.d("DB","pair already exists in RELATIONS: "+fileID+":"+tagID);
        } // case pair exists
        db.close();
    };
    public void unsetTag(long fileID, long tagID){ // do we REALLY need to check success?
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(REL_TABLE, FILE_ID + "=" + fileID +" and "+ TAG_ID+"="+tagID, null);
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
        return s;
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    public String[][] getFilesWithTags(int[] tagIDs){
        String query="";

        for (int i=0; i< tagIDs.length;i++){
            query+="SELECT DISTINCT FILES.FILE_ID, NAME, PATH, IS_EXTERNAL FROM FILES JOIN RELATIONS " +
                    "ON FILES.FILE_ID = RELATIONS.FILE_ID WHERE RELATIONS.TAG_ID="+tagIDs[i];
            if (i+1!=tagIDs.length)query+=" INTERSECT ";
        }
        query+=";";
        Log.d("DB","QUERY for getting files: \n"+query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c=db.rawQuery(query,null);
        /////////////////////// we may have to limit row number if database is too big
        int NUMBER=c.getCount();
        if (NUMBER>100) NUMBER=100;
        /////////////////////// is to be fixed one day
        c.moveToFirst();
        Log.d("count", ""+c.getCount());
        String[][] s=new String[c.getCount()][4];
        for (int i=0; i<NUMBER;i++)
        {
            s[i][0]=""+c.getInt(0);
            s[i][1]=c.getString(1);
            s[i][2]=c.getString(2);
            s[i][3]=""+c.getInt(3);
            c.moveToNext();
            Log.d("step",s[i][0]+' '+s[i][1]+' '+s[i][2]+' '+s[i][3]);
        }
        return s;
    }
}