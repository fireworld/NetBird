package cc.colorcat.netbirddemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Proxy;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    public static final String DOWNLOAD_TEST = "https://70.miiditest.com/aw/mac.woa?param=Ku3%2BTEeywSRzwzqMa5yfBGvVcaNcEHh66B%2Be%2B1PXMvYxE7v7EhGWnYMwHep%2B2%2BkqbHxvxZgcIIb3c4%2FBfdAcYZHgGp1E%2BQ3ejSReVRf8D%2BK9H8%2FyYnDpnh7rT5rR2iyOGI%2B0sceGJS2IfDJ86B5vRF6bkx8hH9DRzXx%2B0lfTO9zI0VLU7EiUc1XAfM1zwQlpA8Df4u2i57o861c%2FSZ6jPFrPJtIyeng6vKPpTFzDkV8%2F6ae1Ee78T1MpjkZ2WlrOmP35bCpeiyQmkAP6HkcP1W9agPc0hvSFVj18AWtb0ml%2F90qDrT%2FAOjR2Ad0KvXNPq4U4yTLKI16pqXsqaxqDVFcxzThTxYnbtCOTjOOSiUDiCyN4LfgQnf%2Foog0QooWvqxKdDZ749fqJi%2BPBuk7A3%2BUwxZQIUnyxgR7NC%2B4a8cY%2FRy2dVhTLmtmkaOyiIdhidUrZGaYKWpLKDVJc6jBuC8q9qLuWHvKrHrhNO5u7q4Arxp%2FfZrqaqW0yp3sAk0TOTr5f0r9pGea7x6vBhXnbTOOO921vZsQ2yMFGI73ZEAdUw6mXRqmb1XJ7nX8tbiwRigygzQ505DMCVGqVpL7xj9ygI2FecCfaE%2FYkWcZt4MiSdJ7r384ca2qbstVdIkhCpRrZbHXRApQeaeDKJWA0Xd4jgUsdhb%2BTPLxZY5KvBakPnaxI3j4quF5XGF8WQ1nr";
    public static final File userHeardImg = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "test.png");
    public static final String FIREFOX = "http://download.firefox.com.cn/releases/mobile/45.0/zh-CN/Firefox-Android-45.0.apk";

    public static final String HOST_LOCAL = "http://192.168.0.23:8080";
    public static final String PATH_LOCAL = "/sayhello";

    public static final String UPLOAD_URL = "https://www.zzb18.com";
    public static final String UPLOAD_PATH = "/zzbh5/appUserCenter/userImgFileUpload";
    public static final String UPLOAD_R = "EG1%2BqMqxNTqzkZUr%2BC5X%2FqhyyLzSWD3U86itr%2FeAgVF4B%2F3%2BfKfKooXWVt1%2F3ZHhNNUvawzU1fOxNW7jnDBwpk4FMjHvM9In9eADrJTLl2cPcBmHn8KILckYxj5t3BlkCiwivMKW4%2BoD3o989mybD%2BO4q%2Fw6EaAtEZ0GMnzAe3dh9vb%2FBM9JzaQjn2UxXhtsrDikjwx0N0CslyYW7L80L4HA7RQd01qkgPTCRZq%2FxRSXbry%2FBIB6y5%2FBJSW2fm5L2YcCdDMTLQGTfeZwwm9NF%2FEADo5Bz8GXtwtNzo8qEXQMv4DlGmRLx64EC856zw5dTwv%2B4wzfsoGkD%2BgAEs0cTel9kq9qOg88QqyjA%2B%2FDfzZixtNOpJPykBJyYXJTF5hltD7UltGLknZvB2ivG7yiL14d3pT0j543Xwd4Odmpk3C01vBidM43nBTuwYSFn3bF6eS4hG6%2Bo9Xtdkmr2gBm97kxi3ZzPl8EjWOIvIrD4ttzHMdFBtCf3CcVz7wfufu%2BU3ooGv6DqNNKQ6qh8F7Zwm4T4KFfcGgb%2BmFFivxOoeXaWzNKuUrtWkEz%2FAR6gm9o";
    public static final String UPLOAD_SIGN = "5314fe73d7cd3fd28db3d6cdd40b2b5c";

    private Object mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ApiService.init(this);

        LogUtils.init(this);
        showToast("LogUtils.getLevel():" + LogUtils.getLevel());

        ImageView image = (ImageView) findViewById(R.id.image_view);
        image.setImageBitmap(BitmapFactory.decodeFile(userHeardImg.getAbsolutePath()));
        image.setOnClickListener(mClick);

        findViewById(R.id.btn_jump).setOnClickListener(mClick);
        findViewById(R.id.btn_download).setOnClickListener(mClick);
        findViewById(R.id.btn_cancel_download).setOnClickListener(mClick);
        findViewById(R.id.btn_cancel_all).setOnClickListener(mClick);
        findViewById(R.id.btn_get).setOnClickListener(mClick);
        findViewById(R.id.btn_post).setOnClickListener(mClick);
        findViewById(R.id.btn_test).setOnClickListener(mClick);
        findViewById(R.id.btn_download_test).setOnClickListener(mClick);
        findViewById(R.id.btn_test_proxy).setOnClickListener(mClick);

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
                case R.id.btn_download_test:
                    downloadTest();
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
                case R.id.btn_cancel_all:
                    ApiService.cancelAll();
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
                case R.id.btn_test_proxy:
                    testProxy();
                    break;
                default:
                    break;
            }
        }
    };

    private void testProxy() {
        String hostS = Proxy.getDefaultHost();
        int portS = Proxy.getDefaultPort();
        String host = Proxy.getHost(this);
        int port = Proxy.getPort(this);
        Log.d("LaunchActivity", hostS + " : " + portS + "\n" + host + " : " + port);
    }

    private void downloadTest() {
        File path = new File(getExternalCacheDir(), "test.apk");
        Request<File> req = new Request.Builder<>(FileParser.create(path))
                .url("http://dldir1.qq.com/weixin/android/weixin653android980.apk")
                .method(Method.GET)
                .loadListener(new Response.LoadListener() {
                    @Override
                    public void onChanged(long read, long total, int percent) {
                        LogUtils.e("Download_WeChat", read + " : " + total + " : " + percent);
                    }
                })
                .callback(new Response.SimpleCallback<File>() {
                    @Override
                    public void onSuccess(@NonNull File result) {
                        showToast(result.getAbsolutePath());
                        LogUtils.e("Download_WeChat", result.getAbsolutePath());
                    }

                    @Override
                    public void onFailure(int code, @NonNull String msg) {
                        LogUtils.e("Progress", code + " : " + msg);
                    }
                }).build();
        ApiService.call(req);
    }

    private void doGet() {
        showToast("to doGet");
        Request<String> req = new Request.Builder<>(StringParser.getUtf8())
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
        Request<String> req = new Request.Builder<>(StringParser.getUtf8())
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
        Request<String> req = new Request.Builder<>(StringParser.getUtf8())
                .callback(new Response.SimpleCallback<String>() {
                    @Override
                    public void onSuccess(@NonNull String result) {
                        LogUtils.i("Upload", CryptoTool.decryptByDefault(result));
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
                .url(UPLOAD_URL).path(UPLOAD_PATH).add("r", UPLOAD_R).add("sign", UPLOAD_SIGN)
                .addFile("userHeardImg", "image/jpeg", userHeardImg).method(Method.POST).build();
        ApiService.call(req);
    }

    private void download() {
        mTag = "DownloadFirefox";
        File down = getExternalCacheDir();
        File mFile = new File(down, "firefox.apk");
        Request rq = new Request.Builder<>(FileParser.create(mFile)).tag(mTag).callback(new Response.SimpleCallback<File>() {
            @Override
            public void onSuccess(@NonNull File result) {
                String path = result.getAbsolutePath();
                showToast("download success, the path is " + path);
                LogUtils.i("Download_Firefox", "download success, the path is " + path);
            }

            @Override
            public void onFailure(int code, @NonNull String msg) {
                showToast("download failure, " + code + " : " + msg);
                LogUtils.i("Download_Firefox", "download failure, " + code + " : " + msg);
            }
        }).loadListener(new Response.LoadListener() {
            @Override
            public void onChanged(long read, long total, int percent) {
                LogUtils.e("Download_Firefox", read + "/" + total + " " + read * 100 / total + "%" + " percent: " + percent);
            }
        }).url(FIREFOX).build();
        ApiService.call(rq);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
