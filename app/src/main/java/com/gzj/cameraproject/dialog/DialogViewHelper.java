package com.gzj.cameraproject.dialog;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * @Created by cuishuxiang
 * @date 2018/2/5.
 * <p>
 * dialog view的辅助类
 */

class DialogViewHelper {
    private static final String TAG = DialogViewHelper.class.getSimpleName().toString();

    //创建显示的界面
    private View mContentView;

    //存储view，减少每次findViewById的次数;防止内存泄漏
    private SparseArray<WeakReference<View>> mViewArray;


    public DialogViewHelper(Context context, int layoutResId) {

        mContentView = LayoutInflater.from(context).inflate(layoutResId, null);

        if (mViewArray == null)
            mViewArray = new SparseArray<>();
    }

    public DialogViewHelper() {
        if (mViewArray == null)
            mViewArray = new SparseArray<>();
    }

    public void setContentView(View contentView) {
        this.mContentView = contentView;
    }

    /**
     * 设置文本
     *
     * @param viewId
     * @param charSequence
     */
    public void setText(int viewId, CharSequence charSequence) {
        //通过存储view，减少findViewById次数

        TextView mTextView = getView(viewId);
        if (mTextView != null)
            mTextView.setText(charSequence);
    }

    /**
     * 设置 mViewArray ，减少findViewById次数
     * @param viewId
     * @param <T>
     * @return
     */
    public  <T extends View> T getView(int viewId) {
        //首先通过viewId，从集合取；
        WeakReference<View> weakReference = mViewArray.get(viewId);
        View view = null;
        if (weakReference != null) {
            view= weakReference.get();
        }

        if (view == null) {
            view = mContentView.findViewById(viewId);
            if (view != null) {
                //如果没有，就存储；
                mViewArray.put(viewId, new WeakReference<>(view));
            }

        }
        return (T) view;
    }

    /**
     * 设置点击事件
     *
     * @param viewId          点击事件的ViewId
     * @param onClickListener
     */
    public void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        View mView = getView(viewId);
        if (mView != null)
            mView.setOnClickListener(onClickListener);
    }

    /**
     * 获取 contentView
     *
     * @return
     */
    public View getContentView() {
        return mContentView;
    }
}
