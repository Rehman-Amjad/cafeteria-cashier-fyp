package com.technogenis.cafeteriacashier.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.technogenis.cafeteriacashier.LoginActivity;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.admin.fragment.AdminCustomersFragment;
import com.technogenis.cafeteriacashier.admin.fragment.AdminOverviewFragment;
import com.technogenis.cafeteriacashier.admin.fragment.AdminReportsFragment;
import com.technogenis.cafeteriacashier.util.EdgeToEdgeHelper;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navMenu;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        toolbar = findViewById(R.id.Toolbar);
        drawerLayout = findViewById(R.id.drawerlayout);
        navMenu = findViewById(R.id.navMenu);

        setSupportActionBar(toolbar);

        EdgeToEdgeHelper.applySystemBarsPadding(toolbar, true, false, false, true);
        EdgeToEdgeHelper.applySystemBarsPadding(findViewById(R.id.main_frame), false, true, true, true);
        EdgeToEdgeHelper.applySystemBarsPadding(navMenu, true, true, true, true);

        if (savedInstanceState == null) {
            swapFragment(new AdminOverviewFragment());
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::syncToolbarIndicator);
        syncToolbarIndicator();

        navMenu.setNavigationItemSelectedListener(this::onDrawerItemSelected);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });
    }

    private void syncToolbarIndicator() {
        boolean atRoot = getSupportFragmentManager().getBackStackEntryCount() == 0;
        toolbar.setNavigationIcon(atRoot ? R.drawable.ic_menu : R.drawable.ic_arrow_back);
        toolbar.setNavigationContentDescription(
                atRoot ? R.string.cd_open_menu : R.string.cd_go_back);
    }

    private boolean onDrawerItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuAdminOverview) {
            getSupportFragmentManager()
                    .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (id == R.id.menuAdminCustomers) {
            swapSubFragment(new AdminCustomersFragment());
        } else if (id == R.id.menuAdminReports) {
            swapSubFragment(new AdminReportsFragment());
        } else if (id == R.id.menuAdminLogout) {
            performLogout();
        } else if (id == R.id.menuAdminExit) {
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

    private void swapSubFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void performLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
