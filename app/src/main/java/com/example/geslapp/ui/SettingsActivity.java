package com.example.geslapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.geslapp.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.navigation.NavigationView;


@SuppressLint("UseSwitchCompatOrMaterialCode")
public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Switch.OnCheckedChangeListener {

    private DrawerLayout drawer;
    private Switch switchRepeatCodePermission;
    public static ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    Toolbar toolbar;

    public static String PREFERENCIAS_SWITCH = "preferenciasSwitch";
    public static String ACTIVATE = "activate";
    public static String CHECKED = "checked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

       navDrawer();
    }

    private void navDrawer() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);
        drawer = findViewById(R.id.drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        switchRepeatCodePermission = (Switch) navigationView.getMenu().findItem(R.id.checkPermisoEtqs).getActionView();
        switchRepeatCodePermission.setOnCheckedChangeListener(this);

        new AppBarConfiguration.Builder().setDrawerLayout(drawer).build();
        comprobandoSwitch();
    }

    private void comprobandoSwitch() {

        if(switchRepeatCodePermission.isChecked()) {
            switchActivate();
        } else {
            switchNoActivate();
        }
    }

    private void switchActivate() {

        navigationView.getMenu().getItem(4).setIcon(R.drawable.ic_check_circle);
        navigationView.getMenu().getItem(4).setTitle("Repetición códigos");
        switchRepeatCodePermission.setChecked(true);
    }

    private void switchNoActivate() {

        navigationView.getMenu().getItem(4).setIcon(R.drawable.cancel);
        navigationView.getMenu().getItem(4).setTitle("NO Repetición códigos");
        switchRepeatCodePermission.setChecked(true);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);

        int FlagsModoOscuro = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (FlagsModoOscuro) {

            case Configuration.UI_MODE_NIGHT_YES:
                menu.getItem(1).setIcon(R.drawable.moon);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                menu.getItem(1).setIcon(R.drawable.sun);
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                break;
        }

        return true;
    }*/

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawer.closeDrawer(GravityCompat.START);

        if(item.getItemId() == R.id.principal){
            showHome();
        }
        if(item.getItemId() == R.id.editNumEtqsCaja){
            showAjustesCaja();
        }
        if(item.getItemId() == R.id.formatoArchivo){
            showFormatFile();
        }
        if(item.getItemId() == R.id.checkPermisoEtqs){
            ((Switch)item.getActionView()).toggle();
            return false;
        }
        if(item.getItemId() == R.id.vaciar_carpeta){
            showDialogVaciarCarpeta();
        }
        return false;
    }

    private void showHome() {

        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void showAjustesCaja() {

        Intent i = new Intent(this, AjustesCajaActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void showFormatFile() {

        Toast.makeText(this, "Sin Implementar", Toast.LENGTH_SHORT).show();
    }

    private void showDialogVaciarCarpeta() {

        Toast.makeText(this, "Sin Implementar", Toast.LENGTH_SHORT).show();
    }

    public void savePositionSwitch(boolean activate) {

        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCIAS_SWITCH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ACTIVATE, activate);
        editor.apply();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(buttonView.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean checked;

        if (isChecked) {
            checked = true;
            savePositionSwitch(true);

        } else {
            checked = false;
            savePositionSwitch(false);
        }
        editor.putBoolean(CHECKED, checked);
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}