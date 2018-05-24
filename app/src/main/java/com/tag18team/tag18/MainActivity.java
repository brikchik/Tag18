package com.tag18team.tag18;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.Settings;
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

import com.sqlite_sync.SQLiteSync;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static java.lang.System.exit;
import static java.lang.System.getSecurityManager;
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    final int MY_PERMISSIONS_REQUEST_READ_STORAGE=1;
    final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO=2;
    public Vector<String> includedPaths=new Vector<>();
    public boolean fileChosenMode=false;
    public String fileChosenPath="";
    public TextView chosenTextView=null;
    private ArrayMap<String, String> tagsInfo=new ArrayMap<String,String>(); // contains tag descriptions
    private static final int SERVERPORT = 6586;
    private static final String SERVER_IP = "nobodycares.dyndns.org";
    private void synchroniser(){
        Log.d("client","started");
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d("DEVICEID", androidId);
        SQLiteSync sqLite_sync = new SQLiteSync("/data/data/"+getPackageName()+"/data.db", "http://nobodycares.dyndns.org:6586");
        try{
            //sqLite_sync.addSynchrnizedTable("FILES");
            //sqLite_sync.addSynchrnizedTable("TAGS");
            //sqLite_sync.addSynchrnizedTable("RELATIONS");
        }catch (Exception e){e.printStackTrace();}
        sqLite_sync.synchronizeSubscriber(androidId, new SQLiteSync.SQLiteSyncCallback() {
            @Override
            public void onSuccess() {
                Log.d("synchronize","Data synchronization finished successfully");
            }
            @Override
            public void onError(Exception error) {
                Log.d("synchronize","Data synchronization finished with error: \n" + error.getMessage());
            }
        });

    }
    private boolean is_synchronized=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DBhandler db=new DBhandler(this);
        updateFiles();
        while (!is_synchronized){try{Thread.sleep(50);}catch (Exception e){};}
        addBasicTags();
        fillTags();
        fillTagTab();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE);
        } // ask for permission in advance
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
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
        findViewById(R.id.listen_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioInput audioInput=new AudioInput(getApplicationContext());
            }
        });
        findViewById(R.id.menu_open_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.START);
            }
        });
        findViewById(R.id.tags_open_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.END);
            }
        });
        findViewById(R.id.folder_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importFileOrDirectory();
            }
        });
        synchroniser();
    }
    private void updateFiles(){
        Runnable afsp=new AsyncFileSystemParser(this, includedPaths);
        new Thread(afsp).start();
    }
    public void addBasicTags(){
        DBhandler db=new DBhandler(this);
        db.addTag("все",null);
        db.addTag("любимый файл",null);
        db.addTag("хлам",null);
    }
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tags);
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
                fileChosenMode=true;
                fileChosenPath=((TextView) v).getText().toString();
                chosenTextView=(TextView)v;
                chosenTextView.setTextColor(Color.RED);
                ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(GravityCompat.END);
            }
        });
    }
    public void fillFiles(View view) {
        EditText editText=(EditText)findViewById(R.id.search_box);
        String request=editText.getText().toString().toLowerCase();
        fillFiles(request.split(" "));
        fillTags();
    }
    private void fillTagTab() {
        ButtonLayout tagsLayout = (ButtonLayout) findViewById(R.id.favouriteTags);
        LayoutInflater layoutInflater = getLayoutInflater();
        for (int i = 0; i <= 2; i++) {
            String tag = "#t" + i;
            View tagView = layoutInflater.inflate(R.layout.button_layout, null, false);
            ToggleButton tagToggleButton = (ToggleButton) tagView.findViewById(R.id.tagButton);
            tagToggleButton.setTextOff(tag);
            tagToggleButton.setTextOn(tag + "*");
            tagToggleButton.setChecked(false);
            tagToggleButton.setTextColor(0xffffffff);
            //tagToggleButton.setTextColor(0xffff0000); //красный
            tagsLayout.addView(tagView);

        }
    }
    private void importFileOrDirectory(){
        File mPath = new File(""+Environment.getExternalStorageDirectory());
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                includedPaths.add(file.toString());
                updateFiles();
            }
        });
        fileDialog.setSelectDirectoryOption(true);
        fileDialog.showDialog();
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
        } else super.onBackPressed();
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.folder_import) { importFileOrDirectory();}
        else
            if (id == R.id.clear) {
                for (String path: includedPaths) Log.d("path",path);
                DBhandler db=new DBhandler(this);
                db.dropTags();
                db.dropFiles();
                includedPaths.clear();
                addBasicTags();
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_end) {
            exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
