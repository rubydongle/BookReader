package com.midas.comicreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.cloudrail.si.CloudRail;
import com.foobnix.R;
import com.foobnix.android.utils.Apps;
import com.foobnix.android.utils.Dips;
import com.foobnix.android.utils.LOG;
import com.foobnix.android.utils.Safe;
import com.foobnix.android.utils.StringDB;
import com.foobnix.android.utils.TxtUtils;
import com.foobnix.comicui.BooksService;
import com.foobnix.comicui.MyContextWrapper;
import com.foobnix.comicui.adapter.TabsAdapter2;
import com.foobnix.comicui.fragment.BookmarksFragment2;
import com.foobnix.comicui.fragment.BrowseFragment2;
import com.foobnix.comicui.fragment.OpdsFragment2;
import com.foobnix.comicui.fragment.PrefFragment2;
import com.foobnix.comicui.fragment.RecentFragment2;
import com.foobnix.comicui.fragment.UIFragment;
import com.foobnix.drive.GFile;
import com.foobnix.ext.CacheZipUtils.CacheDir;
import com.foobnix.model.AppProfile;
import com.foobnix.model.AppSP;
import com.foobnix.model.AppState;
import com.foobnix.pdf.SlidingTabLayout;
import com.foobnix.pdf.info.Android6;
import com.foobnix.pdf.info.AppsConfig;
import com.foobnix.pdf.info.Clouds;
import com.foobnix.pdf.info.FontExtractor;
import com.foobnix.pdf.info.IMG;
import com.foobnix.pdf.info.PasswordDialog;
import com.foobnix.pdf.info.TintUtil;
import com.foobnix.pdf.info.model.BookCSS;
import com.foobnix.pdf.info.view.BrightnessHelper;
import com.foobnix.pdf.info.widget.PrefDialogs;
import com.foobnix.pdf.info.wrapper.DocumentController;
import com.foobnix.pdf.info.wrapper.UITab;
import com.foobnix.pdf.search.activity.HorizontalViewActivity;
import com.foobnix.pdf.search.activity.msg.GDriveSycnEvent;
import com.foobnix.pdf.search.activity.msg.MessageSync;
import com.foobnix.pdf.search.activity.msg.MessegeBrightness;
import com.foobnix.pdf.search.activity.msg.MsgCloseMainTabs;
import com.foobnix.pdf.search.view.CloseAppDialog;
import com.foobnix.sys.TempHolder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.navigation.NavigationView;
import com.midas.comicreader.fragment.BookRackFragment;
import com.midas.comicreader.fragment.BrowseFragment;

import org.ebookdroid.common.settings.books.SharedBooks;
import org.ebookdroid.ui.viewer.VerticalViewActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import test.SvgActivity;


@SuppressLint("NewApi")
public class MainActivity extends AdsAppCompatActivity {
    public static final int REQUEST_CODE_ADD_RESOURCE = 123;
    public static final String EXTRA_EXIT = "EXTRA_EXIT";
    public static final String EXTRA_SHOW_TABS = "EXTRA_SHOW_TABS";
    private static final String TAG = "MainTabs";
    public static String EXTRA_PAGE_NUMBER = "EXTRA_PAGE_NUMBER";
    public static String EXTRA_SEACH_TEXT = "EXTRA_SEACH_TEXT";
    public static String EXTRA_NOTIFY_REFRESH = "EXTRA_NOTIFY_REFRESH";
    public boolean isEink = false;
    ViewPager pager;
    List<UIFragment> tabFragments;
    TabsAdapter2 adapter;
    ImageView imageMenu;
    View imageMenuParent;//, overlay;
//    TextView toastBrightnessText;
    Handler handler;
//    MyProgressBar fab;
//    SwipeRefreshLayout swipeRefreshLayout;
    boolean isMyKey = false;
    OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        UIFragment uiFragment = null;

        @Override
        public void onPageSelected(int pos) {
            uiFragment = tabFragments.get(pos);
            uiFragment.onSelectFragment();
            TempHolder.get().currentTab = pos;

            LOG.d("onPageSelected", uiFragment);
            Apps.accessibilityText(MainActivity.this, adapter.getPageTitle(pos).toString() + " " + getString(R.string.tab_selected));


        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (isPullToRefreshEnable()) {
//                swipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
            LOG.d("onPageSelected onPageScrollStateChanged", state);
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                check();
            }

        }

        public void check() {
            if (isPullToRefreshEnable()) {
//                if (uiFragment instanceof PrefFragment2) {
//                    swipeRefreshLayout.setEnabled(false);
//                } else {
//                    swipeRefreshLayout.setEnabled(true);
//                }
            }
        }
    };
    Runnable closeActivityRunnable = new Runnable() {

        @Override
        public void run() {
//            TTSNotification.hideNotification();
//            TTSEngine.get().shutdown();
//            adsPause();
            finish();
        }
    };
    boolean once = true;
    private SlidingTabLayout indicator;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int pos = intent.getIntExtra(EXTRA_PAGE_NUMBER, -1);
            if (pos != -1) {
                if (pos >= 0) {
                    pager.setCurrentItem(pos);
                }

                if (intent.getBooleanExtra(EXTRA_NOTIFY_REFRESH, false)) {
                    onResume();
                }

            } else {
                if (AppState.get().appTheme == AppState.THEME_INK) {
                    TintUtil.setTintImageNoAlpha(imageMenu, TintUtil.color);
                    indicator.setSelectedIndicatorColors(TintUtil.color);
                    indicator.setDividerColors(TintUtil.color);
                    indicator.updateIcons(pager.getCurrentItem());
                } else {
                    indicator.setBackgroundColor(TintUtil.color);
                    imageMenuParent.setBackgroundColor(TintUtil.color);
                }
            }
        }

    };
    private DrawerLayout drawerLayout;

    public static boolean isPullToRefreshEnable(Context a, View swipeRefreshLayout) {
        if (a == null || swipeRefreshLayout == null) {
            return false;
        }
        return AppSP.get().isEnableSync && GoogleSignIn.getLastSignedInAccount(a) != null && BookCSS.get().isSyncPullToRefresh;
    }

    public static void startActivity(Activity c, int tab) {
        final Intent intent = new Intent(c, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_SHOW_TABS, true);
        intent.putExtra(MainActivity.EXTRA_PAGE_NUMBER, tab);
        intent.putExtra(PasswordDialog.EXTRA_APP_PASSWORD, c.getIntent().getStringExtra(PasswordDialog.EXTRA_APP_PASSWORD));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
        c.overridePendingTransition(0, 0);

    }

    public static void closeApp(Context c) {
        if (c == null) {
            return;
        }
        EventBus.getDefault().post(new MsgCloseMainTabs());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        LOG.d(TAG, "onNewIntent");
        // testIntentHandler();
        if (intent.getBooleanExtra(EXTRA_EXIT, false)) {
            finish();
            return;
        }
        if (intent.getCategories() != null && intent.getCategories().contains("android.intent.category.BROWSABLE")) {
            CloudRail.setAuthenticationResponse(intent);
            LOG.d("CloudRail response", intent);

            Intent intent1 = new Intent(UIFragment.INTENT_TINT_CHANGE)//
                    .putExtra(MainActivity.EXTRA_PAGE_NUMBER, UITab.getCurrentTabIndex(UITab.BrowseFragment));//

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

        }


        checkGoToPage(intent);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Android6.isNeedToGrantAccess(this, requestCode)) {
            Toast.makeText(this, R.string.the_application_needs_storage_permission, Toast.LENGTH_SHORT).show();
            Android6.checkPermissions(this, false);
            return;
        }

        if (Build.VERSION.SDK_INT < Android6.ANDROID_12_INT && resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, R.string.fail, Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == REQUEST_CODE_ADD_RESOURCE && resultCode == Activity.RESULT_OK) {
            getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri uri = data.getData();

            String pathSAF = uri.toString();

            StringDB.add(BookCSS.get().pathSAF, pathSAF, (db) -> BookCSS.get().pathSAF = db);

            LOG.d("REQUEST_CODE_ADD_RESOURCE", pathSAF, BookCSS.get().pathSAF);

            UIFragment uiFragment = tabFragments.get(pager.getCurrentItem());
            if (uiFragment instanceof BrowseFragment2) {
                BrowseFragment2 fr = (BrowseFragment2) uiFragment;
                fr.displayAnyPath(pathSAF);
            }
        } else if (requestCode == GFile.REQUEST_CODE_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(googleAccount -> {
                        AppSP.get().isEnableSync = true;
                        Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().post(new GDriveSycnEvent());
                        GFile.runSyncService(MainActivity.this);

//                        swipeRefreshLayout.setEnabled(isPullToRefreshEnable());

                        AppSP.get().save();

                    })
                    .addOnFailureListener(exception ->
                            {
                                LOG.e(exception);
                                Toast.makeText(this, R.string.fail, Toast.LENGTH_SHORT).show();
                                AppSP.get().isEnableSync = false;
//                                swipeRefreshLayout.setEnabled(false);
                                AppSP.get().save();

                            }
                    );


        }


    }

    public boolean isPullToRefreshEnable() {
        return false;
//        return isPullToRefreshEnable(MainActivity.this, swipeRefreshLayout);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
//        withInterstitial = false;
        super.onPostCreate(savedInstanceState);
        // testIntentHandler();

        if (Android6.canWrite(this)) {
            BrightnessHelper.applyBrigtness(this);
//            BrightnessHelper.updateOverlay(overlay);
        }
        GFile.runSyncService(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(MyContextWrapper.wrap(context));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START, AppState.get().appTheme != AppState.THEME_INK);
                else
                    drawerLayout.openDrawer(GravityCompat.START, AppState.get().appTheme != AppState.THEME_INK);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppState.get().appTheme == AppState.THEME_LIGHT || AppState.get().appTheme == AppState.THEME_INK) {
            setTheme(R.style.StyledIndicatorsWhite);
        } else {
            setTheme(R.style.StyledIndicatorsBlack);
        }
        super.onCreate(savedInstanceState);
        //FirebaseAnalytics.getInstance(this);
        if (false) {
            startActivity(new Intent(this, SvgActivity.class));
            return;
        }
        if (!Android6.canWrite(this)) {
            Android6.checkPermissions(this, true);
            return;
        }
        Clouds.get().init(this);
        //import settings
        if (PasswordDialog.isNeedPasswordDialog(this)) {
            return;
        }
        LOG.d(TAG, "onCreate");
        LOG.d("EXTRA_EXIT", EXTRA_EXIT);
        if (getIntent().getBooleanExtra(EXTRA_EXIT, false)) {
            finish();
            return;
        }
        handler = new Handler();
        isEink = Dips.isEInk();
        TintUtil.setStatusBarColor(this);
        DocumentController.doRotation(this);
        DocumentController.doContextMenu(this);

        // https://www.jianshu.com/p/4df8709a76fa
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Bubble");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageMenu = (ImageView) findViewById(R.id.imageMenu1);
        imageMenuParent = findViewById(R.id.imageParent1);
        imageMenuParent.setBackgroundColor(TintUtil.color);

//        fab = findViewById(R.id.fab);
//        fab.setVisibility(View.GONE);
//        fab.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Dialogs.showSyncLOGDialog(MainActivity.this);
//            }
//        });
//        fab.setBackgroundResource(R.drawable.bg_circular);
//        TintUtil.setDrawableTint(fab.getBackground().getCurrent(), TintUtil.color);

//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//        swipeRefreshLayout.setColorSchemeColors(TintUtil.color);


//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                swipeRefreshLayout.setRefreshing(false);
//                GFile.runSyncService(MainActivity.this, true);
//            }
//        });


//        overlay = findViewById(R.id.overlay);

//        toastBrightnessText = (TextView) findViewById(R.id.toastBrightnessText);
//        toastBrightnessText.setVisibility(View.GONE);
//        TintUtil.setDrawableTint(toastBrightnessText.getCompoundDrawables()[0], Color.WHITE);

        tabFragments = new ArrayList<UIFragment>();

        boolean useNew = true;
        try {
            if (useNew) {
                throw new Exception();
            }
            for (UITab tab : UITab.getOrdered()) {
                if (tab.isVisible()) {
                    tabFragments.add(tab.getClazz().newInstance());
                }
            }
            if (tabFragments.size() == 0) {
                synchronized (AppState.get().tabsOrder7) {
                    AppState.get().tabsOrder7 = AppState.DEFAULTS_TABS_ORDER;
                }
                for (UITab tab : UITab.getOrdered()) {
                    if (tab.isVisible()) {
                        tabFragments.add(tab.getClazz().newInstance());
                    }
                }
            }

        } catch (Exception e) {
            LOG.e(e);
            Toast.makeText(MainActivity.this, R.string.msg_unexpected_error, Toast.LENGTH_LONG).show();
//            tabFragments.add(new SearchFragment2());
            tabFragments.add(new BookRackFragment());
            tabFragments.add(new BrowseFragment());
            tabFragments.add(new RecentFragment2());
            tabFragments.add(new BookmarksFragment2());
            tabFragments.add(new OpdsFragment2());
            tabFragments.add(new PrefFragment2());
            //tabFragments.add(new CloudsFragment2());
        }
//        getSupportFragmentManager().beginTransaction().replace(R.id.left_drawer, new PrefFragment2()).commit();
        AppCompatActivity activity = this;
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.set_comic_library:
                        PrefDialogs.chooseFolderDialog(activity, () -> AppProfile.save(activity), () -> {
                            AppProfile.save(activity);
                            BooksService.startForeground(activity, BooksService.ACTION_SEARCH_ALL);
                            Intent intent = new Intent(UIFragment.INTENT_TINT_CHANGE)//
                                    .putExtra(MainActivity.EXTRA_PAGE_NUMBER, UITab.getCurrentTabIndex(UITab.SearchFragment));//
                            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
                        });
                        break;
                    case R.id.settings:
                        startActivity(new Intent(activity, SettingsActivity.class));
                        break;
                    case R.id.drawer_menu_library:
//                        setFragment(new LibraryFragment());
//                        mCurrentNavItem = menuItem.getItemId();
//                menuItem.setChecked(true);
                        break;
                    case R.id.drawer_menu_browser:
//                        setFragment(new BrowserFragment());
//                        mCurrentNavItem = menuItem.getItemId();
//                menuItem.setChecked(true);
                        break;
                    case R.id.drawer_menu_about:
//                        startActivity(new Intent(this, AboutActivity.class));
//                setTitle(R.string.menu_about);
//                setFragment(new AboutFragment());
                        break;
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
//        MenuItem menuItem = navigationView.getMenu().findItem(R.id.drawer_menu_about);
//        menuItem.setVisible(false);

//        Menu menu = navigationView.getMenu();
//        menu.removeGroup(R.id.quck_nav);
//        navigationView.getMenu().add(R.id.quck_nav, 1, 1, "testadd" );
//        int TEST_ADD_ID = 7878001;
//        navigationView.getMenu().add(R.id.quck_nav, TEST_ADD_ID, 98, "testadd" );
//        navigationView.getMenu().add(R.id.quck_nav, 2, 98, "testppp" );
//        menu.findItem(TEST_ADD_ID).setIcon(R.drawable.ic_menu_white);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        imageMenu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START, AppState.get().appTheme != AppState.THEME_INK);
                else
                    drawerLayout.openDrawer(GravityCompat.START, AppState.get().appTheme != AppState.THEME_INK);
            }
        });

        if (UITab.isShowPreferences()) {
            imageMenu.setVisibility(View.GONE);
//            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            imageMenu.setVisibility(View.VISIBLE);
        }

        if (AppState.get().isEnableAccessibility) {
            imageMenu.setVisibility(View.VISIBLE);
        }


        if(UITab.isShowLibrary()) {
            imageMenu.setVisibility(View.GONE);
        }

        // ((BrigtnessDraw)
        // findViewById(R.id.brigtnessProgressView)).setActivity(this);

        adapter = new TabsAdapter2(this, tabFragments);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAccessibilityDelegate(new View.AccessibilityDelegate());


        if (Android6.canWrite(this)) {
            pager.setAdapter(adapter);
        }

        if (AppState.get().appTheme == AppState.THEME_DARK_OLED) {
            pager.setBackgroundColor(Color.BLACK);
        }

        pager.setOffscreenPageLimit(10);
        pager.addOnPageChangeListener(onPageChangeListener);

        drawerLayout.addDrawerListener(new DrawerListener() {

            @Override
            public void onDrawerStateChanged(int arg0) {
                LOG.d("drawerLayout-onDrawerStateChanged", arg0);

            }

            @Override
            public void onDrawerSlide(View arg0, float arg1) {
                LOG.d("drawerLayout-onDrawerSlide");
//                if (AppSP.get().isEnableSync) {
//                    swipeRefreshLayout.setEnabled(false);
//                }

            }

            @Override
            public void onDrawerOpened(View arg0) {
                LOG.d("drawerLayout-onDrawerOpened");
//                if (AppSP.get().isEnableSync) {
//                    swipeRefreshLayout.setEnabled(false);
//                }
            }

            @Override
            public void onDrawerClosed(View arg0) {
                LOG.d("drawerLayout-onDrawerClosed");
                try {
                    tabFragments.get(pager.getCurrentItem()).onSelectFragment();

//                    if (isPullToRefreshEnable(MainActivity.this, swipeRefreshLayout)) {
//                        swipeRefreshLayout.setEnabled(true);
//                        swipeRefreshLayout.setColorSchemeColors(TintUtil.color);
//
//                    }
//                    TintUtil.setDrawableTint(fab.getBackground().getCurrent(), TintUtil.color);


                } catch (Exception e) {
                    LOG.e(e);
                }
            }
        });

        if (AppState.get().tapPositionTop) {
            indicator = (SlidingTabLayout) findViewById(R.id.slidingTabs1);
        } else {
            indicator = (SlidingTabLayout) findViewById(R.id.slidingTabs2);
        }
//        indicator.addSwipeRefreshLayout(swipeRefreshLayout);
        indicator.setVisibility(View.VISIBLE);
        indicator.init();

        indicator.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                LOG.d("OnFocusChangeListener", hasFocus);
            }
        });


        indicator.setViewPager(pager);

        indicator.setDividerColors(getResources().getColor(R.color.tint_divider));
        indicator.setSelectedIndicatorColors(Color.WHITE);
        indicator.setBackgroundColor(TintUtil.color);

        if (!AppState.get().tapPositionTop || !AppState.get().tabWithNames) {
            imageMenu.setVisibility(View.GONE);
            indicator.setDividerColors(Color.TRANSPARENT);
            indicator.setSelectedIndicatorColors(Color.TRANSPARENT);
            for (int i = 0; i < indicator.getmTabStrip().getChildCount(); i++) {
                View child = indicator.getmTabStrip().getChildAt(i);
                child.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        imageMenu.performClick();
                        return true;
                    }
                });
            }
        }
        if (AppState.get().isEnableAccessibility) {
            imageMenu.setVisibility(View.VISIBLE);
        }

        if (AppState.get().appTheme == AppState.THEME_INK) {
            TintUtil.setTintImageNoAlpha(imageMenu, TintUtil.color);
            indicator.setSelectedIndicatorColors(TintUtil.color);
            indicator.setDividerColors(TintUtil.color);
            indicator.setBackgroundColor(Color.TRANSPARENT);
            imageMenuParent.setBackgroundColor(Color.TRANSPARENT);

        }


        Android6.checkPermissions(this, true);
        // Analytics.onStart(this);

        List<String> actions = Arrays.asList("android.intent.action.PROCESS_TEXT", "android.intent.action.SEARCH", "android.intent.action.SEND");
        List<String> extras = Arrays.asList(Intent.EXTRA_PROCESS_TEXT_READONLY, Intent.EXTRA_PROCESS_TEXT, SearchManager.QUERY, Intent.EXTRA_TEXT);
        if (getIntent() != null && getIntent().getAction() != null) {
            if (actions.contains(getIntent().getAction())) {
                for (String extra : extras) {
                    final String text = getIntent().getStringExtra(extra);
                    if (TxtUtils.isNotEmpty(text)) {
                        pager.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                ((BookRackFragment) tabFragments.get(0)).searchAndOrderExteral(text);
                            }
                        }, 250);
                        break;
                    }
                }

            }

        }

        if (Android6.canWrite(this)) {
            FontExtractor.extractFonts(this);
        }
        EventBus.getDefault().register(this);

        boolean showTabs = getIntent().getBooleanExtra(EXTRA_SHOW_TABS, false);
        LOG.d("EXTRA_SHOW_TABS", showTabs, AppSP.get().lastClosedActivity);
        if (showTabs == false && AppState.get().isOpenLastBook) {
            LOG.d("Open lastBookPath", AppSP.get().lastBookPath);
            if (AppSP.get().lastBookPath == null || !new File(AppSP.get().lastBookPath).isFile()) {
                LOG.d("Open Last book not found");
                return;
            }

            Safe.run(() -> {
                boolean isEasyMode = HorizontalViewActivity.class.getSimpleName().equals(AppSP.get().lastClosedActivity);
                Intent intent = new Intent(MainActivity.this, isEasyMode ? HorizontalViewActivity.class : VerticalViewActivity.class);
                intent.putExtra(PasswordDialog.EXTRA_APP_PASSWORD, getIntent().getStringExtra(PasswordDialog.EXTRA_APP_PASSWORD));
                intent.setData(Uri.fromFile(new File(AppSP.get().lastBookPath)));
                startActivity(intent);
            });
        } else if (false && !AppState.get().isOpenLastBook) {//templorary disable this feature
            LOG.d("Open book lastA", AppSP.get().lastClosedActivity);

            if (AppSP.get().lastBookPath == null || !new File(AppSP.get().lastBookPath).isFile()) {
                LOG.d("Open Last book not found");
                return;
            }
            final String saveMode = AppSP.get().lastClosedActivity;
            Safe.run(new Runnable() {

                @Override
                public void run() {
                    LOG.d("Open AppSP.get().lastBookPath", saveMode);
                    if (HorizontalViewActivity.class.getSimpleName().equals(saveMode)) {
                        Intent intent = new Intent(MainActivity.this, HorizontalViewActivity.class);
                        intent.setData(Uri.fromFile(new File(AppSP.get().lastBookPath)));
                        startActivity(intent);
                        LOG.d("Start lastA", saveMode);
                    } else if (VerticalViewActivity.class.getSimpleName().equals(saveMode)) {
                        Intent intent = new Intent(MainActivity.this, VerticalViewActivity.class);
                        intent.setData(Uri.fromFile(new File(AppSP.get().lastBookPath)));
                        startActivity(intent);
                        LOG.d("Start lastA", saveMode);
                    }

                }
            });

        }

        checkGoToPage(getIntent());

        if (!AppState.get().isEnableAccessibility && once) {
            once = false;
            handler.postDelayed(() -> {
                Apps.accessibilityText(MainActivity.this, getString(R.string.welcome_accessibility));
            }, 5000);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShowSycn(MessageSync msg) {

        try {
            if (msg.state == MessageSync.STATE_VISIBLE) {
                if (BookCSS.get().isSyncAnimation) {
//                    fab.setVisibility(View.VISIBLE);
                }
//                swipeRefreshLayout.setRefreshing(false);
            } else if (msg.state == MessageSync.STATE_FAILE) {
//                fab.setVisibility(View.GONE);
//                swipeRefreshLayout.setRefreshing(false);
                //Toast.makeText(this, getString(R.string.sync_error), Toast.LENGTH_LONG).show();
            } else {
//                fab.setVisibility(View.GONE);
//                swipeRefreshLayout.setRefreshing(false);

            }
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    @Subscribe
    public void onMessegeBrightness(MessegeBrightness msg) {
//        BrightnessHelper.onMessegeBrightness(handler, msg, toastBrightnessText, overlay);
    }

    public void checkGoToPage(Intent intent) {
        try {
            int pos = intent.getIntExtra(EXTRA_PAGE_NUMBER, -1);
            if (pos != -1) {
                pager.setCurrentItem(pos);
            }
        }catch (Exception e){
            LOG.e(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        AppProfile.save(this);
        IMG.pauseRequests(this);

        if (Dips.isEInk()) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppsConfig.isCloudsEnable = UITab.isShowCloudsPreferences();

        LOG.d(TAG, "onResume");
        if (Dips.isEInk()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        LOG.d("FLAG clearFlags", "FLAG_KEEP_SCREEN_ON", "clear");


        DocumentController.chooseFullScreen(this, AppState.get().fullScreenMainMode);
        TintUtil.updateAll();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(UIFragment.INTENT_TINT_CHANGE));
//        if (swipeRefreshLayout != null) {
//            swipeRefreshLayout.setEnabled(isPullToRefreshEnable());
//        }

        try {
            if (pager != null) {
                final UIFragment uiFragment = tabFragments.get(pager.getCurrentItem());
                uiFragment.onSelectFragment();
            }


        } catch (Exception e) {
            LOG.e(e);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        IMG.resumeRequests(this);
        //AppSP.get().lastClosedActivity = MainTabs2.class.getSimpleName();
        //LOG.d("lasta save", AppSP.get().lastClosedActivity);

    }

    public void updateCurrentFragment() {
        tabFragments.get(pager.getCurrentItem()).onSelectFragment();
    }

    @Override
    public boolean onKeyDown(int keyCode1, KeyEvent event) {
        if (!isEink) {
            return super.onKeyDown(keyCode1, event);
        }

        int keyCode = event.getKeyCode();
        if (keyCode == 0) {
            keyCode = event.getScanCode();
        }
        isMyKey = false;
        if (tabFragments.get(pager.getCurrentItem()).onKeyDown(keyCode)) {
            isMyKey = true;
            return true;
        }

        return super.onKeyDown(keyCode1, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isEink) {
            return super.onKeyUp(keyCode, event);
        }

        if (isMyKey) {
            return true;
        }
        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }


    @Override
    protected void onStop() {
        super.onStop();
        SharedBooks.cache.clear();
    }

    @Override
    public void onDestroy() {
        GFile.timeout = 0;
        GFile.runSyncService(this);

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        LOG.d(TAG, "onDestroy");
        if (pager != null) {
            try {
                pager.setAdapter(null);
            } catch (Exception e) {
                LOG.e(e);
            }
        }
        // Analytics.onStop(this);
        CacheDir.ZipApp.removeCacheContent();
        // ImageExtractor.clearErrors();
        // ImageExtractor.clearCodeDocument();


        EventBus.getDefault().unregister(this);
        IMG.clearMemoryCache();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        String language = newConfig.locale.getLanguage();
        float fontScale = newConfig.fontScale;

        LOG.d("ContextWrapper ConfigChanged", language, fontScale);

        if (pager != null) {
            int currentItem = pager.getCurrentItem();
            //pager.setAdapter(adapter); //WHY???
            pager.setCurrentItem(currentItem);
            IMG.clearMemoryCache();
        }
        activateAds();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Android6.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {
        if (CloseAppDialog.checkLongPress(this, event)) {
            CloseAppDialog.show(this, closeActivityRunnable);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onFinishActivity() {
        finish();
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START, AppState.get().appTheme != AppState.THEME_INK);
            return;
        }

        if (tabFragments != null) {
            if (!tabFragments.isEmpty() && tabFragments.get(pager.getCurrentItem()).isBackPressed()) {
                return;
            }

            CloseAppDialog.show(this, closeActivityRunnable);
        } else {
            closeActivityRunnable.run();
        }
    }

    @Subscribe
    public void onCloseAppMsg(MsgCloseMainTabs event) {
        onFinishActivity();
    }
}
