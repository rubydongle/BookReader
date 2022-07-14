package com.foobnix.comicui.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.foobnix.R;
import com.foobnix.android.utils.LOG;
import com.foobnix.android.utils.TxtUtils;
import com.foobnix.comicui.AppRecycleAdapter;
import com.foobnix.comicui.fast.FastScroller;
import com.foobnix.model.AppState;
import com.foobnix.pdf.info.TintUtil;

import java.util.Locale;

public class AuthorsAdapter2 extends AppRecycleAdapter<String, AuthorsAdapter2.AuthorViewHolder> implements FastScroller.SectionIndexer {

    public class AuthorViewHolder extends RecyclerView.ViewHolder {
        public TextView letter, text;
        public View parent;

        public AuthorViewHolder(View view) {
            super(view);
            letter = (TextView) view.findViewById(R.id.image1);
            text = (TextView) view.findViewById(R.id.text1);
            parent = view;
        }

        public void onBindViewHolder(final AuthorViewHolder holder, String string) {
            holder.text.setText(string);
            holder.letter.setText(TxtUtils.getFirstLetter(string));

            if (holder.letter.getTag() == null) {
                holder.letter.setTag(TintUtil.randomColor(string.hashCode()));
            }

            GradientDrawable background = (GradientDrawable) holder.letter.getBackground();
            background.setColor((Integer) holder.letter.getTag());

            bindItemClickAndLongClickListeners(holder.parent, string);
            if (!AppState.get().isBorderAndShadow) {
                holder.parent.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }

    @Override
    public AuthorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_author, parent, false);
        return new AuthorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AuthorViewHolder holder, final int position) {
        final String string = getItem(position);
        holder.onBindViewHolder(holder, string);
    }

    @Override
    public String getSectionText(int position) {
        try {
            return TxtUtils.getFirstLetter(items.get(position)).toUpperCase(Locale.US);
        } catch (Exception e) {
            LOG.e(e);
            return "";
        }
    }

}