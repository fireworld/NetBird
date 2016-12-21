package cc.colorcat.netbirddemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import cc.colorcat.netbird.parser.FileParser;
import cc.colorcat.netbird.parser.StringParser;
import cc.colorcat.netbird.request.Method;
import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.util.LogUtils;

/**
 * Created by mic on 16-2-29.
 * xx.ch@outlook.com
 */
public class LaunchActivity extends AppCompatActivity {
    public static final File userHeardImg = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "1478852074829.png");
    public static final String FIREFOX = "http://download.firefox.com.cn/releases/mobile/45.0/zh-CN/Firefox-Android-45.0.apk";

    public static final String HOST_LOCAL = "http://192.168.0.23:8080";
    public static final String PATH_LOCAL = "/sayhello";

    private Object mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        LogUtils.init(this);
        showToast("LogUtils.getLevel():" + LogUtils.getLevel());

        ImageView image = (ImageView) findViewById(R.id.image_view);
        image.setImageBitmap(BitmapFactory.decodeFile(userHeardImg.getAbsolutePath()));
        image.setOnClickListener(mClick);

        findViewById(R.id.btn_jump).setOnClickListener(mClick);
        findViewById(R.id.btn_download).setOnClickListener(mClick);
        findViewById(R.id.btn_cancel_download).setOnClickListener(mClick);
        findViewById(R.id.btn_get).setOnClickListener(mClick);
        findViewById(R.id.btn_post).setOnClickListener(mClick);
        findViewById(R.id.btn_test).setOnClickListener(mClick);

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x12);
        }
    }

    private View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_jump:
                    startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                    break;
                case R.id.btn_download:
                    download();
                    break;
                case R.id.image_view:
                    uploadImage();
                    break;
                case R.id.btn_cancel_download:
                    ApiService.cancel(mTag);
                    break;
                case R.id.btn_get:
                    doGet();
                    break;
                case R.id.btn_post:
                    doPost();
                    break;
                case R.id.btn_test:
                    Request<String> req = new Request.Builder<>(StringParser.getUtf8())
                            .url("http://sdfsdf.com")
                            .build();
                    ApiService.call(req);
                    break;
                default:
                    break;
            }
        }
    };

    private void doGet() {
        showToast("to doGet");
        Request<String> req = new Request.Builder<>(StringParser.getDefault())
                .callback(new Response.SimpleCallback<String>() {
                    @Override
                    public void onSuccess(@NonNull String result) {
                        showToast(result);
                    }

                    @Override
                    public void onFailure(int code, @NonNull String msg) {
                        showToast(code + " : " + msg);
                    }
                }).url(HOST_LOCAL).path(PATH_LOCAL).add("name", "cxx").add("pwd", "123456").add("zh", "中文测试")
                .addHeader("get_Header1", "HeaderValue1").addHeader("get_Header2", "HeaderValue2").method(Method.GET).build();
        ApiService.call(req);
    }

    private void doPost() {
        showToast("to doPost");
        Request<String> req = new Request.Builder<>(StringParser.getDefault())
                .callback(new Response.SimpleCallback<String>() {
                    @Override
                    public void onSuccess(@NonNull String result) {
                        showToast(result);
                    }

                    @Override
                    public void onFailure(int code, @NonNull String msg) {
                        showToast(code + " : " + msg);
                    }
                })
                .url(HOST_LOCAL).path(PATH_LOCAL).add("name", "cxx").add("pwd", "123456").add("zh", "中文测试")
                .addHeader("post_Header1", "HeaderValue1").addHeader("post_Header2", "HeaderValue2")
                .method(Method.POST).build();
        ApiService.call(req);
    }

    private void uploadImage() {
        Request<String> req = new Request.Builder<>(StringParser.getDefault())
                .callback(new Response.SimpleCallback<String>() {
                    @Override
                    public void onSuccess(@NonNull String result) {
                        LogUtils.i("Upload", result);
                        showToast("上传任务完成");
                    }

                    @Override
                    public void onFailure(int code, @NonNull String msg) {
                        LogUtils.i("Upload", code + " : " + msg);
                    }
                })
                .uploadListener(new Request.UploadListener() {
                    @Override
                    public void onChanged(long written, long total, int percent) {
                        LogUtils.e("Upload", written + "/" + total + " " + written * 100 / total + "%" + " percent: " + percent);
                    }
                })
                .url(HOST_LOCAL).path(PATH_LOCAL).add("name", "cxx").add("pwd", "123456").add("zh", "中文测试")
                .addFile("userHeardImg", "image/jpeg", userHeardImg).method(Method.POST).build();
        ApiService.call(req);
    }

    private void download() {
        File down = getExternalCacheDir();
        File mFile = new File(down, "firefox.apk");
        Request rq = new Request.Builder<>(FileParser.create(mFile)).callback(new Response.SimpleCallback<File>() {
            @Override
            public void onSuccess(@NonNull File result) {
                String path = result.getAbsolutePath();
                showToast("download success, the path is " + path);
                LogUtils.i("Download", "download success, the path is " + path);
            }

            @Override
            public void onFailure(int code, @NonNull String msg) {
                showToast("download failure, " + code + " : " + msg);
                LogUtils.i("Download", "download failure, " + code + " : " + msg);
            }
        }).loadListener(new Response.LoadListener() {
            @Override
            public void onChanged(long read, long total, int percent) {
                LogUtils.e("Download", read + "/" + total + " " + read * 100 / total + "%" + " percent: " + percent);

            }
        }).url(FIREFOX).build();
        mTag = ApiService.call(rq);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
