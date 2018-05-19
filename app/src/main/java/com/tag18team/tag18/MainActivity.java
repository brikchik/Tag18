package com.tag18team.tag18;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.util.Log;

// nav_view -menu (left)
// tag_view -tag menu (right)
// main menu duplicates left one with icons
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fillTagTab();
        //Cursor c=db.getTag(0);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "s", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    private void fillTagTab(){
        /*ListView tagsView = (ListView) findViewById(R.id.allTags);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, new String[]{"a","b","c","d"});
        tagsView.setAdapter(adapter);
*/
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
