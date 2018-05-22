package com.tag18team.tag18;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.renderscript.RenderScript;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.util.Log;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static java.lang.System.exit;

// nav_view -menu (left)
// tag_view -tag menu (right)
// main menu duplicates left one with icons
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final int MY_PERMISSIONS_REQUEST_READ_STORAGE=1;
    final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO=2;
    public Vector<String> includedPaths=new Vector<>();
    public boolean fileChosenMode=false;
    public String fileChosenPath="";
    public TextView chosenTextView=null;
    private void updateFiles(){
        Runnable afsp=new AsyncFileSystemParser(this, includedPaths);
        new Thread(afsp).start();
    }
    public void addBasicTags(){
        DBhandler db=new DBhandler(this);
        db.addTag("Все файлы",null);
        db.addTag("Любимый файл",null);
        db.addTag("Хлам",null);
    }
    private ArrayMap<String, String> tagsInfo=new ArrayMap<String,String>(); // contains tag descriptions
    private void fillTags(){
        Log.d("fillTags","started");
        DBhandler db=new DBhandler(this);
        String[][] allTags=db.getAllRows("TAGS");
        String[] tags=new String[allTags.length];
        for(int row = 0; row < allTags.length; row++) {
            if (!tagsInfo.containsKey(allTags[row][1]))tagsInfo.put(allTags[row][1], allTags[row][2]);
            tags[row]=allTags[row][1];
        }
        GridView gridView = (GridView) findViewById(R.id.tagsGridView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, tags);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (fileChosenMode){
                    DBhandler db=new DBhandler(getApplicationContext());
                    long fileID=db.getIfExists("FILES","FILE_ID","PATH", fileChosenPath);
                    AppCompatTextView text=(AppCompatTextView) v;
                    db.setTag(fileID, ""+text.getText());
                    Toast.makeText(getApplicationContext(),"Тег"+text.getText()+" присвоен файлу "+fileChosenPath, Toast.LENGTH_SHORT).show();
                }
                else {
                    ((EditText)findViewById(R.id.search_box)).setText(""+((TextView) v).getText());
                    fillFiles(new String[]{""+ ((TextView) v).getText()});
                    ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.END);
                }
            }
        });
        }
    public void fillFiles(String[] tags) {
        DBhandler db=new DBhandler(this);
        String[][] allFiles;
        if(tags[0].equals(""))allFiles=db.getAllRows("FILES");else allFiles=db.getFilesWithTags(tags);
        String[] files=new String[allFiles.length];
        for(int row = 0; row < allFiles.length; row++) files[row] = allFiles[row][2];
        GridView gridView = (GridView) findViewById(R.id.filesGridView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, files);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                Toast.makeText(getApplicationContext(),"Вы перешли в режим задания тегов", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),"Нажмите [НАЗАД] для выхода", Toast.LENGTH_SHORT).show();
                Log.d("tag","fileName");
                fileChosenMode=true;
                fileChosenPath=((TextView) v).getText().toString();
                chosenTextView=(TextView)v;
                chosenTextView.setTextColor(Color.RED);
                ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.END);
                /*
                String fileName=(String)(((TextView) v).getText());

                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName);
                Intent i = new Intent();
                i.setAction(android.content.Intent.ACTION_VIEW);
                i.setDataAndType(Uri.parse((String)((TextView) v).getText()), mime);
                startActivity(i);*/
            }
        });
    }
    public void fillFiles(View view) {
        EditText editText=(EditText)findViewById(R.id.search_box);
        String request=editText.getText().toString().toLowerCase();
        fillFiles(request.split(" "));
        fillTags();
    }
    public void recordSound(View view){
        AudioInput audioInput=new AudioInput();
        audioInput.onCreate();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBhandler db=new DBhandler(this);
        updateFiles();
        fillTagTab();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE);
        } // ask for permission in advance
        /*
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } // ask for permission in advance
        */
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        NavigationView tagView = (NavigationView) findViewById(R.id.tag_view);
        tagView.setNavigationItemSelectedListener(this);
        addBasicTags();
        fillTags();
    }
    private void fillTagTab(){
        ButtonLayout tagsLayout = (ButtonLayout) findViewById(R.id.favouriteTags);
        LayoutInflater layoutInflater = getLayoutInflater();
        for (int i = 0; i <= 2; i++) {
            String tag = "#t" + i;
            View tagView = layoutInflater.inflate(R.layout.button_layout, null, false);
            ToggleButton tagToggleButton = (ToggleButton) tagView.findViewById(R.id.tagButton);
            tagToggleButton.setTextOff(tag);
            tagToggleButton.setTextOn(tag+"*");
            tagToggleButton.setChecked(false);
            tagToggleButton.setTextColor(0xffffffff);
            //tagToggleButton.setTextColor(0xffff0000); //красный
            tagsLayout.addView(tagView);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
            if (fileChosenMode) {
                fileChosenMode = false;
                chosenTextView.setTextColor(Color.BLACK);
                Toast.makeText(getApplicationContext(), "Вы снова в обычном режиме", Toast.LENGTH_SHORT).show();
            }
        } else
                {
                    super.onBackPressed();
                }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.file_import) {
            File mPath = new File(""+Environment.getExternalStorageDirectory());
            FileDialog fileDialog = new FileDialog(this, mPath);
            fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    includedPaths.add(file.toString());
                    updateFiles();
                }
            });
            fileDialog.showDialog();
        } else if (id == R.id.folder_import) {
            File mPath = new File(""+Environment.getExternalStorageDirectory());
            FileDialog fileDialog = new FileDialog(this, mPath);
            fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
              public void directorySelected(File directory) {
                  includedPaths.add(directory.toString());
                  updateFiles();
              }
            });
            fileDialog.setSelectDirectoryOption(true);
            fileDialog.showDialog();
        } else if (id == R.id.clear) {
            for (String path: includedPaths) Log.d("path",path);
            DBhandler db=new DBhandler(this);
            db.dropTags();
            db.dropFiles();
            includedPaths.clear();
            addBasicTags();
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_end) {
            exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
