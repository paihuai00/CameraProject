package com.gzj.cameraproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
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

    //接收，选择图片返回的uri
    private List<Uri> chooseImgs = new ArrayList<>();
    private File imgFile;

    //图片选择器，请求吗
    private static final int CHOOSE_IMG_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();

    }

    private void init() {


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

            //图片显示到屏幕上
            Glide.with(this)
                    .load(imgFile)
                    .error(R.drawable.ic_default)
                    .into(mShowChooseImg);

        }
    }

    @OnClick(R.id.choose_img_btn)
    public void onViewClicked() {
        //开启拍照

        MainActivityPermissionsDispatcher.takePhotoWithPermissionCheck(this);
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
        Log.d(TAG, "takePhotoRationale: ");
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void takePhotoPermissionDenied() {
        Log.d(TAG, "takePhotoPermissionDenied: ");
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void takePhotoPermissionNeverAsk() {
        Log.d(TAG, "takePhotoPermissionNeverAsk: ");
    }


}
