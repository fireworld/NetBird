package cc.colorcat.netbirddemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by cxx on 15/10/20.
 * xx.ch@outlook.com
 */
public class ViewHolder {
    private SparseArray<View> mViews = new SparseArray<>();
    private View mConvertView;
    private int mPosition;

    private ViewHolder(Context context, ViewGroup parent, @LayoutRes int layoutResId, int position) {
        mPosition = position;
        mConvertView = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        mConvertView.setTag(this);
    }

    public View getConvertView() {
        return mConvertView;
    }

    public <V extends View> V getView(@IdRes int viewResId) {
        View view = mViews.get(viewResId);
        if (view == null) {
            view = mConvertView.findViewById(viewResId);
            mViews.put(viewResId, view);
        }
        return (V) view;
    }

    public int getPosition() {
        return mPosition;
    }

    public ViewHolder setText(@IdRes int viewResId, String text) {
        TextView tv = getView(viewResId);
        tv.setText(text);
        return this;
    }

    public ViewHolder setText(@IdRes int viewResId, @StringRes int stringResId) {
        TextView tv = getView(viewResId);
        tv.setText(stringResId);
        return this;
    }

    public ViewHolder setPaintFlags(@IdRes int viewResId, int flags) {
        TextView tv = getView(viewResId);
        tv.setPaintFlags(flags);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public ViewHolder setBackground(@IdRes int viewResId, Drawable drawable) {
        View view = getView(viewResId);
        view.setBackground(drawable);
        return this;
    }

    public ViewHolder setBackground(@IdRes int viewResId, @DrawableRes int drawableResId) {
        View view = getView(viewResId);
        view.setBackgroundResource(drawableResId);
        return this;
    }

    public ViewHolder setBackgroundColor(@IdRes int viewResId, @ColorInt int color) {
        View view = getView(viewResId);
        view.setBackgroundColor(color);
        return this;
    }

    public ViewHolder setOnClickListener(@IdRes int viewResId, View.OnClickListener listener) {
        View view = getView(viewResId);
        view.setOnClickListener(listener);
        return this;
    }

    public ViewHolder setVisibility(@IdRes int viewResId, int visibility) {
        View view = getView(viewResId);
        view.setVisibility(visibility);
        return this;
    }

    public ViewHolder setEnabled(@IdRes int viewResId, boolean enabled) {
        View view = getView(viewResId);
        view.setEnabled(enabled);
        return this;
    }

    public ViewHolder setImageResource(@IdRes int viewResId, @DrawableRes int drawableResId) {
        ImageView iv = getView(viewResId);
        iv.setImageResource(drawableResId);
        return this;
    }

    public ViewHolder setTag(@IdRes int viewResId, Object tag) {
        View view = getView(viewResId);
        view.setTag(tag);
        return this;
    }

    public ViewHolder setTag(@IdRes int viewResId, int key, Object tag) {
        View view = getView(viewResId);
        view.setTag(key, tag);
        return this;
    }

    public <E> E getTag(@IdRes int viewResId) {
        View view = getView(viewResId);
        return (E) view.getTag();
    }

    public <E> E getTag(@IdRes int viewResId, int key) {
        View view = getView(viewResId);
        return (E) view.getTag(key);
    }

    public static ViewHolder getHolder(Context context, View convertView, ViewGroup parent, @LayoutRes int layoutResId, int position) {
        if (convertView == null) {
            return new ViewHolder(context, parent, layoutResId, position);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.mPosition = position;
        return holder;
    }
}