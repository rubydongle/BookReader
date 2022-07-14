package com.foobnix.pdf.info.wrapper;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.foobnix.R;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.AppsConfig;
import com.foobnix.pdf.info.Urls;
import com.foobnix.pdf.info.view.MyPopupMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class PopupHelper {

    public static void updateGridOrListIcon(ImageView gridList, int libraryMode) {
        if (gridList == null) {
            return;
        }
        int stringId = R.string.list;
        if (libraryMode == AppState.MODE_LIST) {
            gridList.setImageResource(R.drawable.glyphicons_114_justify);
            stringId = R.string.list;
        } else if (libraryMode == AppState.MODE_LIST_COMPACT) {
            gridList.setImageResource(R.drawable.glyphicons_114_justify_compact);
            stringId = R.string.compact;
        } else if (libraryMode == AppState.MODE_GRID) {
            gridList.setImageResource(R.drawable.glyphicons_156_show_big_thumbnails);
            stringId = R.string.grid;
        } else if (libraryMode == AppState.MODE_COVERS) {
            gridList.setImageResource(R.drawable.glyphicons_157_show_thumbnails);
            stringId = R.string.cover;
        } else if (libraryMode == AppState.MODE_AUTHORS) {
            gridList.setImageResource(R.drawable.glyphicons_4_user);
            stringId = R.string.author;
        } else if (libraryMode == AppState.MODE_SERIES) {
            gridList.setImageResource(R.drawable.glyphicons_710_list_numbered);
            stringId = R.string.serie;
        } else if (libraryMode == AppState.MODE_GENRE) {
            gridList.setImageResource(R.drawable.glyphicons_66_tag);
            stringId = R.string.keywords;
        } else if (libraryMode == AppState.MODE_USER_TAGS) {
            gridList.setImageResource(R.drawable.glyphicons_67_tags);
            stringId = R.string.my_tags;
        } else if (libraryMode == AppState.MODE_KEYWORDS) {
            gridList.setImageResource(R.drawable.glyphicons_67_keywords);
            stringId = R.string.keywords;
        } else if (libraryMode == AppState.MODE_PUBLISHER) {
            gridList.setImageResource(R.drawable.glyphicons_4_thumbs_up);
            stringId = R.string.publisher;
        }else if (libraryMode == AppState.MODE_PUBLICATION_DATE) {
            gridList.setImageResource(R.drawable.glyphicons_2_book_open);
            stringId = R.string.publication_date;
        }

        gridList.setContentDescription(gridList.getContext().getString(R.string.cd_view_menu)+" "+gridList.getContext().getString(stringId));


    }

    public static void addPROIcon(final PopupMenu menu, final Context c) {
        if (!AppsConfig.checkIsProInstalled(c)) {
            menu.getMenu().add("Librera PRO").setIcon(R.mipmap.icon_pdf_pro).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Urls.openPdfPro(c);
                    return false;
                }
            });
        }
    }

    public static void addPROIcon(final MyPopupMenu menu, final Context c) {
        if (true) {
            // templorary hide this;
            return;

        }
        if (!AppsConfig.checkIsProInstalled(c)) {
            menu.getMenu().add(R.string.app_name_pro).setIcon(R.mipmap.icon_pdf_pro).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Urls.openPdfPro(c);
                    return false;
                }
            });
        }
    }

    public static void initIcons(final PopupMenu menu, int color) {

        try {
            Field[] fields = menu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(menu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }

            for (int i = 0; i < menu.getMenu().size(); i++) {
                MenuItem item = menu.getMenu().getItem(i);
                if (item.getTitle().toString().contains("Librera")) {
                    continue;
                }
                Drawable icon = item.getIcon().getConstantState().newDrawable();
                icon = icon.mutate();
                icon.setColorFilter(color, Mode.SRC_ATOP);
                item.setIcon(icon);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
