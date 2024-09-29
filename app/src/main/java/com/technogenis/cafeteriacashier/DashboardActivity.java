package com.technogenis.cafeteriacashier;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.technogenis.cafeteriacashier.fragment.HomeFragment;
import com.technogenis.cafeteriacashier.fragment.ItemPurchaseCash;
import com.technogenis.cafeteriacashier.fragment.ItemsPurchaseHistory;

public class DashboardActivity extends AppCompatActivity {

    NavigationView navMenu;
    ActionBarDrawerToggle toggle;
    DrawerLayout drayerLayout;

    FragmentManager fragmentManager;
    Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Ensure that the status bar is visible
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        Toolbar toolbar=findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);

        navMenu=findViewById(R.id.navMenu);
        drayerLayout=findViewById(R.id.drawerlayout);


        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,new HomeFragment()).commit();


        toggle=new ActionBarDrawerToggle(this,drayerLayout,toolbar,R.string.app_name,R.string.app_name);
        drayerLayout.addDrawerListener(toggle);
        toggle.syncState();

        fragmentManager = getSupportFragmentManager();

        navMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                if (item.getItemId() == R.id.menuHome){
                    fragment = new HomeFragment();
                    drayerLayout.closeDrawer(GravityCompat.START);

                }
                else if(item.getItemId() == R.id.menuItemPurchase){
                    fragment = new ItemsPurchaseHistory();
                    drayerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.menuItemPurchaseCash){
                    fragment = new ItemPurchaseCash();
                    drayerLayout.closeDrawer(GravityCompat.START);
                }

                else if(item.getItemId() == R.id.menuExit){
                    System.exit(0);
                    drayerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.menu_logout){
                    Intent logIntent=new Intent(DashboardActivity.this,LoginActivity.class);
                    startActivity(logIntent);
                    finish();
                    Toast.makeText(DashboardActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                    drayerLayout.closeDrawer(GravityCompat.START);
                }

                if (fragment != null) {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.main_frame, fragment);
                    transaction.addToBackStack(null); // Optional: to add fragment to back stack
                    transaction.commit();
                }

                drayerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });

    }
}