package com.technogenis.cafeteriacashier;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.technogenis.cafeteriacashier.fragment.HomeFragment;
import com.technogenis.cafeteriacashier.fragment.ItemPurchaseCash;
import com.technogenis.cafeteriacashier.fragment.ItemsPurchaseHistory;
import com.technogenis.cafeteriacashier.util.EdgeToEdgeHelper;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navMenu;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        toolbar = findViewById(R.id.Toolbar);
        drawerLayout = findViewById(R.id.drawerlayout);
        navMenu = findViewById(R.id.navMenu);
        View mainFrame = findViewById(R.id.main_frame);

        setSupportActionBar(toolbar);

        // Edge-to-edge: status bar pads the toolbar, gesture/nav bar pads the content frame.
        EdgeToEdgeHelper.applySystemBarsPadding(toolbar, true, false, false, true);
        EdgeToEdgeHelper.applySystemBarsPadding(mainFrame, false, true, true, true);
        EdgeToEdgeHelper.applySystemBarsPadding(navMenu, true, true, true, true);

        if (savedInstanceState == null) {
            swapFragment(new HomeFragment());
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        applyDrawerHeader();

        navMenu.setNavigationItemSelectedListener(this::onDrawerItemSelected);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    private void applyDrawerHeader() {
        if (navMenu.getHeaderCount() == 0) return;
        View header = navMenu.getHeaderView(0);
        TextView profileTv = header.findViewById(R.id.dashboardprofile);
        if (profileTv != null) {
            String rfid = MyPreferenceManager.getInstance(this).getString("rfid");
            if (rfid != null && !rfid.isEmpty()) {
                profileTv.setText(getString(R.string.label_rfid) + ": " + rfid);
            }
        }
    }

    private boolean onDrawerItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuHome) {
            swapFragment(new HomeFragment());
        } else if (id == R.id.menuItemPurchase) {
            swapFragment(new ItemsPurchaseHistory());
        } else if (id == R.id.menuItemPurchaseCash) {
            swapFragment(new ItemPurchaseCash());
        } else if (id == R.id.menu_logout) {
            confirmLogout();
        } else if (id == R.id.menuExit) {
            finishAffinity();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void swapFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, fragment)
                .commit();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_logout_title)
                .setMessage(R.string.confirm_logout_message)
                .setPositiveButton(R.string.action_yes, (d, w) -> performLogout())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void performLogout() {
        MyPreferenceManager.getInstance(this).clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
