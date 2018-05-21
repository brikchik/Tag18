package com.tag18team.tag18;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.util.Vector;

public class AsyncFileSystemParser implements Runnable{
    DBhandler db;
    String[] directoryList;
    Vector<String> filesPresent=new Vector<String>();
    public void directory(File dir, DBhandler db) {
        if (!dir.isDirectory())return;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile() && !(filesPresent.contains(file.getAbsolutePath())))
            {
                long fileID=db.addFile(file.getAbsolutePath());
                if (file.getAbsolutePath().endsWith("pdf"))db.setTag(fileID, 1);
            }
            else if ((file.listFiles() != null))directory(file, db);
        }
    }
    public void run(){
        Log.d("run","started");
        String[][] existingFiles=db.getAllRows("FILES");
        for (String[] row:existingFiles) filesPresent.add(row[2]);
        for (String dirPath: directoryList)
        {
            File f = new File(dirPath);
            directory(f, db);
        }
    };
    public AsyncFileSystemParser(Context ctx, String[] pathList){
        db=new DBhandler(ctx);
        directoryList=pathList;
    }
}