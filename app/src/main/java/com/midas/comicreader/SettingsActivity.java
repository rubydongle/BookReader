package com.midas.comicreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.foobnix.R;
import com.foobnix.comicui.BooksService;
import com.foobnix.comicui.fragment.UIFragment;
import com.foobnix.model.AppProfile;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.ExtUtils;
import com.foobnix.pdf.info.wrapper.UITab;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
//                    .replace(R.id.settings, new PrefFragment2())
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static class InnerSettingsFragment extends PreferenceFragmentCompat {
        SwitchPreference openCompressedFilesPreference;
        SwitchPreference openPdfFilesPreference;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            openCompressedFilesPreference = findPreference("open_compressed_files");
            openCompressedFilesPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Toast.makeText(getContext(), "value:" + newValue, Toast.LENGTH_SHORT).show();
                AppState.get().supportArch = (boolean) newValue;
                AppState.get().supportZIP = (boolean) newValue;
                ExtUtils.updateSearchExts();
                AppProfile.save(getActivity());
                BooksService.startForeground(getActivity(), BooksService.ACTION_SEARCH_ALL);
                Intent intent = new Intent(UIFragment.INTENT_TINT_CHANGE)//
                        .putExtra(MainActivity.EXTRA_PAGE_NUMBER, UITab.getCurrentTabIndex(UITab.SearchFragment));//
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                return true;
            });
            openPdfFilesPreference = findPreference("open_compressed_files");
            openPdfFilesPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Toast.makeText(getContext(), "value:" + newValue, Toast.LENGTH_SHORT).show();
                AppState.get().supportPDF = (boolean) newValue;
                ExtUtils.updateSearchExts();
                AppProfile.save(getActivity());
                BooksService.startForeground(getActivity(), BooksService.ACTION_SEARCH_ALL);
                Intent intent = new Intent(UIFragment.INTENT_TINT_CHANGE)//
                        .putExtra(MainActivity.EXTRA_PAGE_NUMBER, UITab.getCurrentTabIndex(UITab.SearchFragment));//
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                return true;
            });
        }
    }
}