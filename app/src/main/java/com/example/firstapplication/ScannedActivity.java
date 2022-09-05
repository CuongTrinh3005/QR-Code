package com.example.firstapplication;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.example.firstapplication.adapters.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

import static com.example.firstapplication.utils.Helper.setActionBarBackGroundColor;

public class ScannedActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned);
        initViews();
    }

    public void initViews() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setActionBarBackGroundColor(actionBar, "#000000");

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        tabLayout.setupWithViewPager(viewPager);
        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPageAdapter.addFragment(new ScanningFragment(), "QR Scanner");
        viewPageAdapter.addFragment(new HistoryFragment(), "History Scanned");

        viewPager.setAdapter(viewPageAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
