package com.midas.comicreader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
//                    .replace(R.id.settings, new PrefFragment2())
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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