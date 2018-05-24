package com.tag18team.tag18;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.util.Vector;

public class AsyncFileSystemParser implements Runnable{
    DBhandler db;
    Vector<String> directoryList;
    private void fileAdd(File file){
        String path=file.getAbsolutePath();
        long fileID=db.addFile(path);
        db.setTag(fileID, "все");
        if ((path.lastIndexOf('.')+3)!=path.length()) {
            String ending = path.substring(path.lastIndexOf('.')+1, path.length());
            Log.d("ENDING", ending);
            long tagID=db.addTag(ending,"file extension");
            db.setTag(fileID, tagID);
        }
    }
    public void directory(File dir, DBhandler db) {
        if (dir.isFile())fileAdd(dir); // we want to handle it even if it goes wrong
        if (!dir.isDirectory())return;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile())  fileAdd(file);
            else if ((file.listFiles() != null))directory(file, db);
        }
    }
    public void run(){
        Log.d("run","started");
        for (String dirPath: directoryList)
        {
            File f = new File(dirPath);
            directory(f, db);
        }
    };
    public AsyncFileSystemParser(Context ctx, Vector<String> pathList){
        db=new DBhandler(ctx);
        directoryList=pathList;
    }
}