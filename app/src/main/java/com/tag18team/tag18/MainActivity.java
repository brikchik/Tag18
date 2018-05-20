package com.tag18team.tag18;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.widget.RecyclerView;
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
import java.util.Arrays;
import java.util.List;

// nav_view -menu (left)
// tag_view -tag menu (right)
// main menu duplicates left one with icons
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final int MY_PERMISSIONS_REQUEST_READ_STORAGE=1;
    private void fillFileDatabase(){
        Runnable afsp=new AsyncFileSystemParser(this);
        new Thread(afsp).start();
        DBhandler db=new DBhandler(this);
        String[][] s=db.getAllRows("TAGS");
        Log.d("count:",""+s.length);
        String tag_list="";
        for (String[] s1:s){
            tag_list+= s1[0]+"|"+s1[1]+"|"+s1[2]+"|"+s1[3]+'\n';
        }
        TextView tv=(TextView)findViewById(R.id.textView7);
        tv.setText(tag_list);
        }
    private void fillFileView() {
        DBhandler db=new DBhandler(this);
        String[][] allFiles=db.getAllRows("FILES");
        String[] files=new String[allFiles.length];
        for(int row = 0; row < allFiles.length; row++)
        {
            files[row] = allFiles[row][1];
        }
        GridView gridView = (GridView) findViewById(R.id.gridView1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, files);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        fillTagTab();
        fillFileDatabase();
        fillFileView();
    }
    private void fillTagTab(){
        ButtonLayout tagsLayout = (ButtonLayout) findViewById(R.id.chosenTags);
        LayoutInflater layoutInflater = getLayoutInflater();
        String tag;
        for (int i = 0; i <= 2; i++) {
            tag = "#t" + i;
            View tagView = layoutInflater.inflate(R.layout.button_layout, null, false);
            ToggleButton tagToggleButton = (ToggleButton) tagView.findViewById(R.id.tagButton);
            tagToggleButton.setTextOff(tag);
            tagToggleButton.setTextOn(tag+"*");
            tagToggleButton.setChecked(false);
            tagToggleButton.setTextColor(0xffffffff);
            //tagToggleButton.setTextColor(0xffff0000); //красный
            tagsLayout.addView(tagView);
        }
        ButtonLayout suggestionsLayout = (ButtonLayout) findViewById(R.id.favouriteTags);
        for (int i = 0; i <= 4; i++) {
            tag = "#tag" + i;
            View tagView = layoutInflater.inflate(R.layout.button_layout, null, false);
            ToggleButton tagToggleButton = (ToggleButton) tagView.findViewById(R.id.tagButton);
            tagToggleButton.setTextOff(tag);
            tagToggleButton.setTextOn(tag+"*");
            tagToggleButton.setChecked(false);
            tagToggleButton.setTextColor(0xffffffff);
            //tagToggleButton.setTextColor(0xffff0000); //красный
            suggestionsLayout.addView(tagView);
        }
        DBhandler db=new DBhandler(this);
        db.dropFiles();
        db.dropTags();
        long a1=db.addFile("/home/hello");
        long a2=db.addFile("/root/myfile");
        long t1=db.addTag("tag1","desc1");
        long t2=db.addTag("TAG2", "second tag");
        db.setTag(a1, t1);
        db.setTag(a2,t2);
        db.setTag(a1,t2);
        db.unsetTag(a1,t2);
        String[][] s=db.getAllRows("TAGS");
        String tag_list="";
        for (int i=0;i<s.length;i++)
        {
            tag_list+= s[i][0]+"|"+s[i][1]+"|"+s[i][2]+"|"+s[i][3]+'\n';
        }
        TextView tv=(TextView)findViewById(R.id.textView7);
        tv.setText(tag_list);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else{
            super.onBackPressed();
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
