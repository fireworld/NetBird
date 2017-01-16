package cc.colorcat.netbirddemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import cc.colorcat.netbird.NetBird;
import cc.colorcat.netbird.parser.FileParser;
import cc.colorcat.netbird.request.Request;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2016/12/29.
 * xx.ch@outlook.com
 */

public class DownloadManager {
    private static final int REQUEST_LAUNCH = 0x12;
    private static final int REQUEST_INSTALL = 0x13;
    private static final int ID_LOADING = 0x01;
    private static final int ID_SUCCESS = 0x02;
    private static final int ID_FAILURE = 0x03;
    private static final int[] IDS = {ID_LOADING, ID_SUCCESS, ID_FAILURE};

    private Queue<Task> taskQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isRunning = false;
    private NetBird netBird;
    private Context context;
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private PendingIntent launchPi;

    {
        netBird = new NetBird.Builder("http://cn.bing.com").build();
    }

    public DownloadManager(Context ctx) {
        this.context = ctx;
    }

    public boolean put(Task task) {
        Utils.nonNull(task, "task == null");
        boolean result = !taskQueue.contains(task) && taskQueue.offer(task);
        if (result) {
            notifyNewTask();
        }
        return result;
    }

    private void notifyNewTask() {
        if (!isRunning && !taskQueue.isEmpty()) {
            isRunning = true;
            Task task = taskQueue.poll();
            execute(task);
        }
    }

    private void execute(Task task) {
        Request<File> req = new Request.Builder<>(FileParser.create(task.savePath))
                .url(task.url)
                .loadListener(new Response.LoadListener() {
                    @Override
                    public void onChanged(long read, long total, int percent) {

                    }
                }).callback(new Response.SimpleCallback<File>() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(@NonNull File result) {

                    }

                    @Override
                    public void onFailure(int code, @NonNull String msg) {

                    }

                    @Override
                    public void onFinish() {
                        isRunning = false;
                        notifyNewTask();
                    }
                }).build();
        netBird.dispatch(req);
    }

    private void initNotificationManager() {
        if (manager == null) {
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    private void createLaunchPi() {
        if (launchPi == null) {
            Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            launchPi = PendingIntent.getActivity(context, REQUEST_LAUNCH, launch, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void createBuilder() {

    }


    public static class Task {
        private String url;
        private String savePath;

        public Task(String url, String savePath) {
            this.url = url;
            this.savePath = savePath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Task task = (Task) o;

            if (url != null ? !url.equals(task.url) : task.url != null) return false;
            return savePath != null ? savePath.equals(task.savePath) : task.savePath == null;

        }

        @Override
        public int hashCode() {
            int result = url != null ? url.hashCode() : 0;
            result = 31 * result + (savePath != null ? savePath.hashCode() : 0);
            return result;
        }
    }
}
