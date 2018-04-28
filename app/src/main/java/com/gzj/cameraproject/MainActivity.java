package com.gzj.cameraproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gzj.cameraproject.dialog.CommonDialog;
import com.gzj.cameraproject.utils.ImageUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.choose_img_btn)
    Button mChooseImgBtn;
    @BindView(R.id.show_choose_img)
    ImageView mShowChooseImg;
    @BindView(R.id.set_iv)
    ImageView mSetIv;
    @BindView(R.id.show_result_tv)
    TextView mShowResultTv;

    //接收，选择图片返回的uri
    private List<Uri> chooseImgs = new ArrayList<>();
    private File imgFile;

    //图片选择器，请求吗
    private static final int CHOOSE_IMG_REQUEST = 0;

    //服务器ip
    private String mServerIp = "10.0.2.138";
    //端口
    private int mServerPort = 8888;
    private Socket mSocketClient;

    //显示消息的弹框
    private CommonDialog mShowMsgDialog;
    private TextView mTextView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            StringBuffer receiveMsg = (StringBuffer) msg.obj;
            Log.d(TAG, "handleMessage: receiveMsg = " + receiveMsg);

            if (mShowMsgDialog != null && mTextView != null) {
//                mTextView.setText(receiveMsg);
//                mShowMsgDialog.show();
                mShowResultTv.setText(receiveMsg);
            } else {
                mShowResultTv.setText("返回数据有误，请重试！");
            }

        }
    };

    //设置弹框
    private CommonDialog mSetDialog;
    private EditText mIpEt;
    private EditText mPortEt;
    private String enterIpString;
    private int enterPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null && getSupportActionBar().isShowing())
            getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        initImmerseBar();
        ButterKnife.bind(this);

        initDialog();

        initSetDialog();
    }


    /**
     * 初始化  回调消息弹框
     */
    private void initDialog() {
        mShowMsgDialog = new CommonDialog.Builder(this)
                .setCancelable(false)
                .setContentView(R.layout.dialog_show_msg)
                .setOnClickListener(R.id.ensure_btn, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mShowMsgDialog.isShowing())
                            mShowMsgDialog.dismiss();
                    }
                })
                .create();

        mTextView = (TextView) mShowMsgDialog.getViewById(R.id.show_msg_tv);
    }

    /**
     * 初始化 设置弹框
     */
    private void initSetDialog() {

        mSetDialog = new CommonDialog.Builder(this)
                .setContentView(R.layout.dialog_setting)
                .setWidthAndHeight(650, ViewGroup.LayoutParams.WRAP_CONTENT)
                .setOnClickListener(R.id.dialog_ensure_btn, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(enterIpString)) {
                            Toast.makeText(getBaseContext(), "输入的Ip不能为空！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (enterPort == 0) {
                            Toast.makeText(getBaseContext(), "输入的port不能为空！", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mServerIp = enterIpString;
                        mServerPort = enterPort;

                        Log.d(TAG, "onClick: 修改后的ip = " + mServerIp + "  mServerPort= " + mServerPort);

                        if (mSetDialog != null && mSetDialog.isShowing())
                            mSetDialog.dismiss();
                    }
                })
                .create();

        mIpEt = (EditText) mSetDialog.getViewById(R.id.dialog_ip_et);
        mIpEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: " + s);
                enterIpString = s.toString();
            }
        });
        mPortEt = (EditText) mSetDialog.getViewById(R.id.dialog_port_et);
        enterPort = Integer.valueOf(mPortEt.getText().toString().trim());
        mPortEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: " + s);
                if (!TextUtils.isEmpty(s))
                    enterPort = Integer.valueOf(s.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //选择图片的返回码
        if (requestCode == CHOOSE_IMG_REQUEST && resultCode == RESULT_OK) {
            chooseImgs = Matisse.obtainResult(data);
            //1 ,拍照得到的图片，需要做处理 (my_images是自己设置的拍照输出路径)
            // 拍照的存储位置："/sdcard/Pictures/JPEG_20171107_140520.jpg"
            if (chooseImgs.get(0).getEncodedPath().contains("my_images")) {
                String imagePath = "/sdcard/Pictures" + chooseImgs.get(0).getEncodedPath().substring(chooseImgs.get(0).getEncodedPath().lastIndexOf("/"));
                imgFile = new File(imagePath);
            } else {
                imgFile = ImageUtils.getFileByUri(this, chooseImgs.get(0));
            }
//            updateImage();

            //耗时，放在子线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (imgFile != null)
                        sendImageToServer(imgFile);
                }
            }).start();

            //图片显示到屏幕上
            Glide.with(this)
                    .load(imgFile)
                    .error(R.drawable.ic_default)
                    .into(mShowChooseImg);

        }
    }

    /**
     * 发送图片给服务器
     *
     * @param imgFile
     */
    public void sendImageToServer(File imgFile) {

        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;

        InputStream inputStream = null;//接收服务器消息
        InputStreamReader reader = null;
        BufferedReader bufReader = null;
        try {
            mSocketClient = new Socket(mServerIp, mServerPort);

            fileInputStream = new FileInputStream(imgFile);

            outputStream = mSocketClient.getOutputStream();

            byte[] bytes = new byte[1024];
            int len = 0;

            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            mSocketClient.shutdownOutput();
            Log.d(TAG, "sendImageToServer: 图片发送完成");


            //接收服务器数据
            //拿到socket的输入流，这里存储的是服务器返回的数据
            inputStream = mSocketClient.getInputStream();
            //解析服务器返回的数据
            reader = new InputStreamReader(inputStream);
            bufReader = new BufferedReader(reader);
            String s = null;
            final StringBuffer sb = new StringBuffer();
            while ((s = bufReader.readLine()) != null) {
                sb.append(s);
            }
            Log.d(TAG, "sendImageToServer: 接收到数据为：" + sb);

            Message message = Message.obtain();//使用handler
            message.obj = sb;

            mHandler.sendMessage(message);

            mSocketClient.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {//释放资源

            try {
                if (outputStream != null)
                    outputStream.close();

                if (fileInputStream != null)
                    fileInputStream.close();

                if (inputStream != null)
                    inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @OnClick({R.id.set_iv, R.id.choose_img_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.set_iv:
                Log.d(TAG, "onViewClicked: 设置");
                if (mSetDialog != null)
                    mSetDialog.show();
                break;
            case R.id.choose_img_btn:
                Log.d(TAG, "onViewClicked: 点击了拍照");
                //开启拍照
                MainActivityPermissionsDispatcher.takePhotoWithPermissionCheck(this);
                break;
        }
    }

    //上传头像
//    private void updateImage() {
//        if (!NetWorkUtil.isNetworkAvailable(this)) {
//            Toast.makeText(this, "请检查当前网络", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        OkHttpUtils.post()
//                .addFile("1", "name.png", imgFile)
//                .url(Constant.UPGATAIMAGE)
//                .addParams("UserLoginName", PreUtil.getUserInfo().getUserLoginName())
//                .addParams("AccessToken", PreUtil.getFormKey(this, Constant.USERTOKEN))
//                .build()
//                .execute(new StringCallback() {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onResponse(String response, int id) {
//                        Log.d("TAG", "------上传头像-------" + response.toString());
//                        try {
//                            JSONObject root = new JSONObject(response);
//                            if (root.getString("").equals("true")) {
//                                PreUtil.updataImage(MainActivity.this, root.getString("allFileUrl"));
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }

    //----------------------------6.0+权限处理-----------------------------------
    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void takePhoto() {

        Matisse.from(MainActivity.this)
                .choose(MimeType.of(MimeType.PNG, MimeType.JPEG))
                .countable(true)
                .maxSelectable(1)//最多选择1张
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片显示表格的大小getResources()
                .capture(true)
                .captureStrategy(new CaptureStrategy(true, "com.gzj.cameraproject.fileprovider"))
                .thumbnailScale(0.8f)//缩放比例
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .imageEngine(new GlideEngine())
                .forResult(CHOOSE_IMG_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void takePhotoRationale(final PermissionRequest request) {
        //提示为什么需要权限
        Log.d(TAG, "takePhotoRationale: ");
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void takePhotoPermissionDenied() {
        //权限被拒绝，会调用
        Log.d(TAG, "takePhotoPermissionDenied: ");
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void takePhotoPermissionNeverAsk() {
        //用户点击了不在询问，会调用
        Log.d(TAG, "takePhotoPermissionNeverAsk: ");
    }


    //----------------------------------设置顶部渐变--------------------------------------------------------

    private View statusBarView;

    /**
     * 渐变
     * http://www.jb51.net/article/124110.htm
     */
    private void initImmerseBar() {
        if (getSupportActionBar() != null) {
            if (getSupportActionBar().isShowing()) {
                getSupportActionBar().hide();
            }
        }
        //延时加载数据.
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                if (isStatusBar()) {
                    initStatusBar();
                    getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            initStatusBar();
                        }
                    });
                }
                //只走一次
                return false;
            }
        });
    }

    private void initStatusBar() {
        if (statusBarView == null) {
            int identifier = getResources().getIdentifier("statusBarBackground", "id", "android");
            statusBarView = getWindow().findViewById(identifier);
        }
        //这里设置自己的文件
        if (statusBarView != null) {
            statusBarView.setBackgroundResource(R.drawable.bg_title_bar);
        }
    }

    protected boolean isStatusBar() {
        return true;
    }

}
