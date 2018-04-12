package com.gzj.cameraproject.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.gzj.cameraproject.R;


/**
 * @Created by cuishuxiang
 * @date 2018/2/5.
 * 构建common的dialog
 */

public class CommonDialog extends Dialog {
    private static final String TAG = CommonDialog.class.getSimpleName().toString();

    private DialogController mAlert;

    private CommonDialog(@NonNull Context context) {
        this(context, 0);
    }

    private CommonDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        mAlert = new DialogController(this, getWindow());
    }

    /**
     * 通过id，找到view  (如果需要，找到某个view，可以调用该方法)
     * @param viewId
     * @return
     */
    public View getViewById(int viewId) {
        return mAlert.getViewById(viewId);
    }

    /**
     * 通过id ，设置点击事件
     */
    public void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        mAlert.setOnClickListener(viewId, onClickListener);
    }

    /**
     * 通过 id，设置文本
     */
    public void setText(int viewId, CharSequence text) {
        mAlert.setText(viewId, text);
    }


    public static class Builder {
        private DialogController.DialogParams P;
        private int mTheme;

        public Builder(@NonNull Context context) {
            this(context, R.style.Dialog);
        }

        public Builder(@NonNull Context context, @StyleRes int themeResId) {
            P = new DialogController.DialogParams(context, themeResId);
            mTheme = themeResId;
        }

        /**
         * 设置布局，两个重载方法；View resId
         *
         * @param view
         * @return
         */
        public Builder setContentView(View view) {
            P.mView = view;
            P.mLayoutResId = 0;
            return this;
        }

        public Builder setContentView(int layoutResId) {
            P.mView = null;
            P.mLayoutResId = layoutResId;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            P.mOnKeyListener = onKeyListener;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            P.mOnDismissListener = onDismissListener;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            P.mOnCancelListener = onCancelListener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            P.mCancelable = cancelable;
            return this;
        }

        /**
         * 设置view的文本
         *
         * @param viewId
         * @param charSequence
         * @return
         */
        public Builder setText(int viewId, CharSequence charSequence) {
            P.mTextArray.put(viewId, charSequence);
            return this;
        }

        /**
         * 设置View的点击事情，使用SparseArray存储view的点击事件
         *
         * @param viewId
         * @param onClickListener
         * @return
         */
        public Builder setOnClickListener(int viewId, View.OnClickListener onClickListener) {
            P.mListenerArray.put(viewId, onClickListener);
            return this;
        }

        /**
         * 配置全屏
         *
         * @return
         */
        public Builder fullWidth() {
            P.mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            return this;
        }

        /**
         * 从底部弹出
         *
         * @param isAnimation 是否有动画
         * @return
         */
        public Builder showFromBottom(boolean isAnimation) {
            if (isAnimation) {
                //判断是否有动画
                P.mAnimation = R.style.dialog_from_bottom_anim;
            }
            P.mGravity = Gravity.BOTTOM;
            return this;
        }

        /**
         * 设置dialog 宽高
         *
         * @param width
         * @param height
         * @return
         */
        public Builder setWidthAndHeight(int width, int height) {
            P.mWidth = width;
            P.mHeight = height;
            return this;
        }

        /**
         * 设置默认动画
         *
         * @return
         */
        public Builder addDefaultAnimation() {
            //此处可以更改，别的动画
            P.mAnimation = R.style.dialog_from_bottom_anim;
            return this;
        }

        /**
         * 添加一个 style动画
         *
         * @param styleAnimation
         * @return
         */
        public Builder addAnimation(int styleAnimation) {
            P.mAnimation = styleAnimation;
            return this;
        }


        public CommonDialog create() {

            final CommonDialog dialog = new CommonDialog(P.mContext, P.mThemeResId);
            P.apply(dialog.mAlert);
            dialog.setCancelable(P.mCancelable);
            if (P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (P.mOnKeyListener != null) {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }


        public CommonDialog show() {
            final CommonDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

}
