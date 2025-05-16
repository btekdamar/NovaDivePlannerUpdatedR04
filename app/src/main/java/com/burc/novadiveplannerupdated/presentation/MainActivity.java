package com.burc.novadiveplannerupdated.presentation;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.burc.novadiveplannerupdated.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Removed this as we manually handle insets
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Apply window insets to the root CoordinatorLayout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinator_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply padding for status bar and navigation bar
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            // Let the CoordinatorLayout consume the insets
            return WindowInsetsCompat.CONSUMED;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            // Handle error: NavHostFragment not found
            return;
        }
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);

        // Define top-level destinations for AppBarConfiguration
        // These destinations will not show the 'Up' button (back arrow) in the Toolbar
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.navigation_settings);
        topLevelDestinations.add(R.id.navigation_gases);
        topLevelDestinations.add(R.id.navigation_plan);
        topLevelDestinations.add(R.id.navigation_graph);
        topLevelDestinations.add(R.id.navigation_segments);
        appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();

        // Setup ActionBar (Toolbar) with NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Setup BottomNavigationView with NavController
        NavigationUI.setupWithNavController(bottomNavView, navController);

        // TODO: Add logic for hamburger menu icon click listener
        // TODO: Add logic to manage dive tabs in the toolbar
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the 'Up' button press in the toolbar
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return super.onSupportNavigateUp(); // Fallback
        }
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}