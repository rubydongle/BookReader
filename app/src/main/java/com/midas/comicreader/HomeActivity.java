package com.midas.comicreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.foobnix.R;
import com.foobnix.comicui.fragment.BrowseFragment2;
import com.foobnix.comicui.fragment.SearchFragment2;
import com.google.android.material.navigation.NavigationView;
//import com.midas.comics.fragment.BrowserFragment;
//import com.midas.comics.fragment.home.HomeFragment;
//import com.midas.comics.managers.LocalCoverHandler;
//import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;

//    private Picasso mPicasso;


    public static final int REQUEST_CODE = 99;
    void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "权限拒绝了", Toast.LENGTH_SHORT).show();
            // 类似 startActivityForResult()中的REQUEST_CODE
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_CODE);
        } else {
//            Toast.makeText(this, "权限同意了", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                // grantResults是一个数组，和申请的数组一一对应。
                // 如果请求被取消，则结果数组为空。
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限同意了，做相应处理
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
                    }
                    // 权限被拒绝了
                    finish();
                }
                return;
            }
            default:
                break;
        }
    }

    Fragment mHomeFragment;
    Fragment mBrowserFragment;
    FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_home);
//        mHomeFragment = new HomeFragment();
        mHomeFragment = new SearchFragment2();
        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mFragmentTransaction.replace(R.id.fragment_container, mHomeFragment).commitAllowingStateLoss();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);//::onNavigationItemSelected);

//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        mPicasso = new Picasso.Builder(this)
//                .addRequestHandler(new LocalCoverHandler(this))
//                .build();

//        mBrowserFragment = new BrowserFragment();
        mBrowserFragment = new BrowseFragment2();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    private boolean isBackToDesktop() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//        return (currentFragment instanceof HomeFragment || currentFragment instanceof BrowserFragment);
        return (currentFragment instanceof SearchFragment2 || currentFragment instanceof BrowseFragment2);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isBackToDesktop()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    super.onBackPressed();
                }
                return true;
            case R.id.action_settings:
//                Intent intent = new Intent(this, GeneralPreferenceActivity.class);
//                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());
//                intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
//                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.nav_settings:
//                Intent intent = new Intent(this, GeneralPreferenceActivity.class);
//                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName());
//                intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
//                startActivity(intent);
//                break;
//            case R.id.nav_scan_games:
//                startActivity(new Intent(this, GameScanActivity.class));
//                break;
            case R.id.drawer_menu_library:
//                mHomeFragment = new HomeFragment();
                mHomeFragment = new SearchFragment2();
                mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                mFragmentTransaction.replace(R.id.fragment_container, mHomeFragment).commitAllowingStateLoss();
                break;
            case R.id.drawer_menu_browser:
//                mBrowserFragment = new BrowserFragment();
                mBrowserFragment = new BrowseFragment2();
                mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                mFragmentTransaction.replace(R.id.fragment_container, mBrowserFragment).commitAllowingStateLoss();
                break;
            case R.id.drawer_menu_scan:
//                startActivity(new Intent(this, ScanActivity.class));
                break;
            case R.id.drawer_menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.drawer_menu_about:
//                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        mDrawerLayout.closeDrawers();
        return false;
    }

//    public Picasso getPicasso() {
//        return mPicasso;
//    }


}