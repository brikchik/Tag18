package com.tag18team.tag18;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class AsyncFileSystemParser implements Runnable{
    DBhandler db;
    public void directory(File dir, DBhandler db) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (!(file.getAbsolutePath().contains("/Android/")) && //system files
                    !(file.getAbsolutePath().contains("/."))) //hidden files starting with .
            {
                if ((file.listFiles() != null))
                    directory(file, db);
                else
                    db.addFile(dir.getAbsolutePath());
            }
        }
    }
    public void run(){
        String path = Environment.getExternalStorageDirectory().toString();
        File f = new File(path+"");
        directory(f, db);
    };
    public AsyncFileSystemParser(Context ctx){
        db=new DBhandler(ctx);
    }
}