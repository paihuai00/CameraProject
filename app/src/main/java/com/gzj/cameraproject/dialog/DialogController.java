package com.gzj.cameraproject.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @Created by cuishuxiang
 * @date 2018/2/5.
 */

public class DialogController {
    private static final String TAG = DialogController.class.getSimpleName().toString();

    private CommonDialog mCommonDialog;
    private Window mWindow;
    private DialogViewHelper mDialogViewHelper;

    public DialogController(CommonDialog commonDialog, Window window) {
        mCommonDialog = commonDialog;
        mWindow = window;
    }

    /**
     * 获取 dialog
     *
     * @return
     */
    public CommonDialog getCommonDialog() {
        return mCommonDialog;
    }

    /**
     * 获取window
     *
     * @return
     */
    public Window getWindow() {
        return mWindow;
    }


    private void setDialogViewHelper(DialogViewHelper dialogViewHelper) {
        this.mDialogViewHelper = dialogViewHelper;
    }

    public View getViewById(int viewId) {
        return mDialogViewHelper.getView(viewId);
    }

    /**
     * 设置点击事件
     * @param viewId
     * @param onClickListener
     */
    public void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        mDialogViewHelper.setOnClickListener(viewId,onClickListener);
    }

    /**
     * 设置文本
     * @param viewId
     * @param charSequence
     */
    public void setText(int viewId, CharSequence charSequence) {
        mDialogViewHelper.setText(viewId, charSequence);
    }

    public static class DialogParams {
        public Context mContext;
        public int mThemeResId;
        public LayoutInflater mInflater;
        //是否可以取消
        public boolean mCancelable;
        //dialog 消失/取消/按键 监听
        public DialogInterface.OnCancelListener mOnCancelListener;
        public DialogInterface.OnDismissListener mOnDismissListener;
        public DialogInterface.OnKeyListener mOnKeyListener;

        //布局view
        public View mView;
        public int mLayoutResId;

        //存储输入的文本
        public SparseArray<CharSequence> mTextArray = new SparseArray<>();
        //存储点击事件
        public SparseArray<View.OnClickListener> mListenerArray = new SparseArray<>();
        //默认宽度，自适应
        public int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        public int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        //动画
        public int mAnimation = 0;
        //位置：default center
        public int mGravity = Gravity.CENTER;


        public DialogParams(Context context, int themeResId) {
            mContext = context;
            this.mThemeResId = themeResId;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * 绑定，设置参数
         *
         * @param mAlert
         */
        public void apply(DialogController mAlert) {
            //1，设置 参数 / dialog的布局 使用Help  辅助类
            DialogViewHelper viewHelper = null;
            if (0 != mLayoutResId) {
                viewHelper = new DialogViewHelper(mContext, mLayoutResId);
            }

            if (mView != null) {
                viewHelper = new DialogViewHelper();

                viewHelper.setContentView(mView);

            }

            //设置viewHelper
            mAlert.setDialogViewHelper(viewHelper);

            //setContentView 必须调用
            if (viewHelper == null) {
                throw new IllegalArgumentException("请调用 setContentView 设置布局！");
            }

            //Dialog设置布局
            mAlert.getCommonDialog().setContentView(viewHelper.getContentView());


            //2，设置文本 / 点击事件
            for (int i = 0; i < mTextArray.size(); i++) {
                mAlert.setText(mTextArray.keyAt(i), mTextArray.valueAt(i));
            }

            for (int i = 0; i < mListenerArray.size(); i++) {
                mAlert.setOnClickListener(mListenerArray.keyAt(i), mListenerArray.valueAt(i));
            }

            //3，设置自定义的效果(全屏，底部弹出，效果等等) 需要使用window
            Window window = mAlert.getWindow();
            window.setGravity(mGravity);//设置位置

            //设置动画
            if (mAnimation != 0)
                window.setWindowAnimations(mAnimation);

            //设置 : 宽高
            WindowManager.LayoutParams layoutParam = window.getAttributes();
            layoutParam.width = mWidth;
            layoutParam.height = mHeight;
            //，再添加回去
            window.setAttributes(layoutParam);

        }
    }
}

