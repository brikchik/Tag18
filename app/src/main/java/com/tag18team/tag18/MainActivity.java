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

// nav_view -menu (left)
// tag_view -tag menu (right)
// main menu duplicates left one with icons
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fillTagTab();
        DBhandler db=new DBhandler(this);
        db.createNewTable("TAGS", "table with tags and their settings");
        Cursor c=db.getFullTable();
        final String s=""+c.getCount();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, s, Snackbar.LENGTH_LONG)
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
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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
