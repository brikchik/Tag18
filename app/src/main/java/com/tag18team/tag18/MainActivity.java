package com.tag18team.tag18;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.System.exit;
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final int MY_PERMISSIONS_REQUEST_READ_STORAGE=1;
    public Vector<String> includedPaths=new Vector<>();
    public static Vector<String> pictureFormats=new Vector<>();
    public static Vector<String> officeDocumentsFormats=new Vector<>();
    public static Vector<String> programFormats=new Vector<>();
    public boolean fileChosenMode=false;
    public String fileChosenPath="";
    private ArrayList<ItemAdapter.ItemHolder> chosenFiles=new ArrayList<>();
    private void updateFiles(){
        Runnable afsp=new AsyncFileSystemParser(this, includedPaths);
        new Thread(afsp).start();
    }
    private ArrayMap<String, String> tagsInfo=new ArrayMap<String,String>(); // contains tag descriptions
    private void fillTags() {
        DBhandler db = new DBhandler(this);
        String[][] allTags = db.getAllRows("TAGS");
        String[] tags = new String[allTags.length];
        ArrayList<Item> tagList=new ArrayList<>();
        for (int row = 0; row < allTags.length; row++) {
            if (!tagsInfo.containsKey(allTags[row][1]))
                tagsInfo.put(allTags[row][1], allTags[row][2]);
            tags[row] = allTags[row][1];
            tagList.add(new Item(Integer.parseInt(allTags[row][0]), allTags[row][1],
                    allTags[row][2], Boolean.parseBoolean(allTags[row][3])));
        }
        if (fileChosenMode) {
            long fileID = db.getIfExists("FILES", "FILE_ID", "PATH", fileChosenPath);
            for (int row = 0; row < allTags.length; row++) {
                Cursor result = db.getReadableDatabase().rawQuery(
                        "SELECT FILE_ID FROM RELATIONS " +
                                "WHERE TAG_ID=" + allTags[row][0] + " " +
                                "AND FILE_ID=" + fileID + ";", null);
                if (result.getCount() > 0) tags[row] = "[Выбран] " + tags[row];
            }
        }
        ListView listView = (ListView) findViewById(R.id.tagsListView);
        ItemAdapter adapter = new ItemAdapter(tagList, this, true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                setTag(""+((ItemAdapter.ItemHolder)v.getTag()).ItemNameView.getText());
            }
        });
    }
    public void fillFiles(String[] tags) {
        DBhandler db=new DBhandler(this);
        String[][] allFiles;
        if(tags[0].equals(""))allFiles=db.getAllRows("FILES");else allFiles=db.getFilesWithTags(tags);
        ArrayList<Item> fileList=new ArrayList<>();
        for(int row = 0; row < allFiles.length; row++){
            fileList.add(new Item(Integer.parseInt(allFiles[row][0]),allFiles[row][1],
                    allFiles[row][2],Boolean.parseBoolean(allFiles[row][3])));
        }
        ListView listView = (ListView) findViewById(R.id.file_list_view);
        ItemAdapter adapter = new ItemAdapter(fileList, this, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                fileChosenMode=true;
                Log.d("view",""+v.getTag());
                fileChosenPath=""+((ItemAdapter.ItemHolder)v.getTag()).distView.getText();
                chosenFiles.add((ItemAdapter.ItemHolder)v.getTag());
                //chosenTextView=v;
                //chosenTextView.setTextColor(Color.RED);
                fillTags();
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String[] formats={"jpg","png","bmp","gif","jpeg","ico"};
        for (String one: formats)pictureFormats.add(one);
        formats= new String[]{"docx","doc","pptx","ppt","xlsx","xls","pdf"};
        for (String one: formats)officeDocumentsFormats.add(one);
        formats= new String[]{"apk","jar","jad","exe"};
        for (String one: formats)programFormats.add(one);
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
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        NavigationView tagView = (NavigationView) findViewById(R.id.tag_view);
        tagView.setNavigationItemSelectedListener(this);
        findViewById(R.id.listen_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
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
                chooseFile(true);
            }
        });
        DrawerLayout drawerLayout=findViewById(R.id.drawer_layout);
        DrawerLayout.DrawerListener drawerListener=new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if(fileChosenMode && ((DrawerLayout)findViewById(R.id.drawer_layout)).isDrawerOpen(GravityCompat.END)){
                    Toast.makeText(getApplicationContext(),"Вы перешли в режим задания тегов", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                fileChosenMode=false;
                fillTags();
            }
            @Override
            public void onDrawerStateChanged(int newState) { }
        };
        drawerLayout.addDrawerListener(drawerListener);
    }
    public void clearSearchBox(View view){
        ((EditText)findViewById(R.id.search_box)).setText("");
    }
    public void chooseFile(boolean chooseFolderInstead){
        File mPath = new File(""+Environment.getExternalStorageDirectory());
        FileDialog fileDialog = new FileDialog(this, mPath);
        if (chooseFolderInstead){
            fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
                public void directorySelected(File file) {
                    includedPaths.add(file.toString());
                    updateFiles();
                }
            });
        }
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                includedPaths.add(file.toString());
                updateFiles();
            }
        });
        if (chooseFolderInstead)fileDialog.setSelectDirectoryOption(true);
        fileDialog.showDialog();
    }
    private void fillTagTab(){
        fillTags();
        ButtonLayout tagsLayout = (ButtonLayout) findViewById(R.id.favouriteTags);
        LayoutInflater layoutInflater = getLayoutInflater();
        View tagView = layoutInflater.inflate(R.layout.button_layout, null, false);
        ((Button) tagView.findViewById(R.id.tagButton)).setText("все");
        ((Button) tagView.findViewById(R.id.tagButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { setTag("все"); }
        });
        tagsLayout.addView(tagView);
        tagView = layoutInflater.inflate(R.layout.button_layout, null, false);
        ((Button) tagView.findViewById(R.id.tagButton)).setText("любимое");
        ((Button) tagView.findViewById(R.id.tagButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { setTag("любимое"); }
        });
        tagsLayout.addView(tagView);
        tagView = layoutInflater.inflate(R.layout.button_layout, null, false);
        ((Button) tagView.findViewById(R.id.tagButton)).setText("хлам");
        ((Button) tagView.findViewById(R.id.tagButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { setTag("хлам"); }
        });
        tagsLayout.addView(tagView);
        tagView = layoutInflater.inflate(R.layout.button_layout, null, false);
        ((Button) tagView.findViewById(R.id.tagButton)).setText("добавить тег");
        ((Button) tagView.findViewById(R.id.tagButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTag();
            }
        });
        tagsLayout.addView(tagView);
    }
    public void addNewTag(){

    }
    public void setTag(String tag){
        if (fileChosenMode){
            DBhandler db=new DBhandler(getApplicationContext());
            long fileID=db.getIfExists("FILES","FILE_ID","PATH", fileChosenPath);
            long tagID=-1;
            if (tag.length()>9)tagID = db.getIfExists("TAGS", "TAG_ID", "NAME", tag.substring(9));
            Cursor result=db.getReadableDatabase().rawQuery(
                        "SELECT TAG_ID FROM RELATIONS " +
                                "WHERE TAG_ID="+tagID+" " +
                                "AND FILE_ID="+fileID+";",null);
            if (result.getCount()>0) {
                db.unsetTag(fileID, tagID);
                Toast.makeText(getApplicationContext(), "Тег убран", Toast.LENGTH_SHORT).show();
            }
            else {
                db.setTag(fileID, tag);
                Toast.makeText(getApplicationContext(), "Тег " + tag + " присвоен файлу " + fileChosenPath, Toast.LENGTH_SHORT).show();
            }
            fillTags();
        }
        else {
            EditText editText=((EditText)findViewById(R.id.search_box));
            String pattern=""+editText.getText();
            if((pattern=="") || (pattern.endsWith(" ")))pattern+=tag;
            else pattern+=(" "+tag);
            editText.setText(pattern);
            fillFiles(new View(this));
            ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.END);
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
        } else
                {
                    super.onBackPressed();
                }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {// Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.file_import) {
            chooseFile(false);
        } else if (id == R.id.folder_import) {
            chooseFile(true);
        } else if (id == R.id.clear) {
            for (String path: includedPaths) Log.d("path",path);
            DBhandler db=new DBhandler(this);
            db.dropTags();
            db.dropFiles();
            includedPaths.clear();
        } else if (id == R.id.nav_end) {
            exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void startVoiceRecognitionActivity() {
        boolean can_recognize_speech=false;
        try {
            PackageManager pm = getPackageManager();
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() != 0) {
                 can_recognize_speech=true;
            }
        } catch (Exception e) {}
        if (!can_recognize_speech) {
            Toast.makeText(this, "Чтобы активировать голосовой поиск необходимо установить \"Голосовой поиск Google\"", Toast.LENGTH_LONG).show();
            installGoogleVoiceSearch(this);
            }
            else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Назовите теги");
            startActivityForResult(intent, 5);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 5 && resultCode == RESULT_OK) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> textMatchList = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                setTag(textMatchList.get(0));
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
    private static void installGoogleVoiceSearch(final Activity ownerActivity) {
        Dialog dialog = new AlertDialog.Builder(ownerActivity)
                .setMessage("Для распознавания речи необходимо установить \"Голосовой поиск Google\"")
                .setTitle("Внимание")	// заголовок диалога
                .setPositiveButton("Установить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            ownerActivity.startActivity(intent);
                        } catch (Exception ex) {}
                    }})
                .setNegativeButton("Отмена", null)	// негативная кнопка
                .create();
        dialog.show();
    }
}