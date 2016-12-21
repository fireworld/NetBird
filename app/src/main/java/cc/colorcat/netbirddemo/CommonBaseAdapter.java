package cc.colorcat.netbirddemo;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Common base adapter
 * Created by cxx on 15/10/21.
 * xx.ch@outlook.com
 */
public abstract class CommonBaseAdapter<T> extends BaseAdapter {
    private Context mContext;
    private List<T> mData;
    private int mLayoutResId;

    public CommonBaseAdapter(@NonNull Context context, @NonNull List<T> data, @LayoutRes int layoutResId) {
        mContext = context;
        mData = data;
        mLayoutResId = layoutResId;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    final public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    final public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.getHolder(mContext, convertView, parent, mLayoutResId, position);
        convert(holder, getItem(position));
        return holder.getConvertView();
    }

    /**
     * e.g.
     * 1. holder.setText(R.id.btn_phone, "18632569632");
     * 2. holder.setOnClickListener(R.id.btn_phone, MainActivity.this);
     * 3. holder.setBackground(R.id.btn_phone, R.mipmap.default_image);
     * 4. ImageView photo = holder.getView(R.id.iv_photo); ImageLoader.getInstance().displayImage(user.getPhotoUrl(), photo);
     * 5. holder.setText(R.id.btn_phone, "18632569632").setOnClickListener(R.id.btn_phone, MainActivity.this);
     *
     * @param holder ViewHolder
     * @param t      Data
     */
    public abstract void convert(ViewHolder holder, T t);
}
