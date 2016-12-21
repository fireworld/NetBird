package cc.colorcat.netbird.parser;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cc.colorcat.netbird.response.NetworkData;
import cc.colorcat.netbird.response.Response;
import cc.colorcat.netbird.util.IoUtils;
import cc.colorcat.netbird.util.Utils;

/**
 * Created by cxx on 2016/12/14.
 * xx.ch@outlook.com
 */

public class FileParser implements Parser<File> {
    private File file;

    public static FileParser create(File file) {
        Utils.nonNull(file, "file == null");
        File parent = file.getParentFile();
        if (parent.exists() || parent.mkdirs()) {
            return new FileParser(file);
        }
        throw new RuntimeException("can't create directory: " + parent.getAbsolutePath());
    }

    private FileParser(File file) {
        this.file = file;
    }

    @NonNull
    @Override
    public NetworkData<? extends File> parse(@NonNull Response data) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            IoUtils.dumpAndClose(data.body().stream(), fos);
            return NetworkData.onSuccess(file);
        } catch (IOException e) {
            return NetworkData.onFailure(data.code(), Utils.formatMsg(data.msg(), e));
        }
    }
}
