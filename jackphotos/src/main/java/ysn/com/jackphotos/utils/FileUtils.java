package ysn.com.jackphotos.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

import ysn.com.jackphotos.model.bean.Photo;

/**
 * @Author yangsanning
 * @ClassName FileUtils
 * @Description 文件工具类
 * @Date 2019/12/27
 * @History 2019/12/27 author: description:
 */
public class FileUtils {

    /**
     * 从SDCard加载图片
     */
    public static synchronized ArrayList<Photo> loadPhotoForSDCard(Context context) {

        // 扫描图片
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(imageUri, new String[]{
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.MIME_TYPE},
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED);

        ArrayList<Photo> imageList = new ArrayList<>();

        //读取扫描到的图片
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 获取图片的路径
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                //获取图片名称
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));

                //获取图片时间
                long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                if (String.valueOf(time).length() < 13) {
                    time *= 1000;
                }

                //获取图片类型
                String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));

                //获取图片uri
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();

                imageList.add(new Photo(path, time, name, mimeType, uri));
            }
            cursor.close();
        }
        return imageList;
    }

    /**
     * 根据文件路径, 获取文件夹名称
     */
    public static String getFolderName(String path) {
        if (ValidatorUtils.isNotBlank(path)) {
            String[] strings = path.split(File.separator);
            if (strings.length >= 2) {
                return strings[strings.length - 2];
            }
        }
        return "";
    }

    /**
     * 判断文件是否有效(过滤未下载完成或者不存在的文件)
     */
    public static boolean isEffective(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options.outWidth > 0 && options.outHeight > 0;
    }

    /**
     * 判断文件是否有效(过滤未下载完成或者不存在的文件), AndroidQ 需要使用uri(耗时操作)
     */
    public static boolean isEffective(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            return options.outWidth > 0 && options.outHeight > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param rootDirPath 存储目录
     * @param fileName    文件名
     */
    public static File createFile(String rootDirPath, String fileName) {
        return createFile(new File(rootDirPath), fileName);
    }

    /**
     * @param rootDir  存储目录
     * @param fileName 文件名
     */
    public static File createFile(File rootDir, String fileName) {
        if (rootDir.exists()) {
            rootDir.mkdir();
        }
        return new File(rootDir, fileName);
    }

    /**
     * 删除文件
     */
    public static void deleteFile(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= 24) {
            if (uri == null) {
                return;
            }
            context.getContentResolver().delete(uri, null, null);
        } else {
            String path = UriUtils.getPathForUri(context, uri);
            if (path == null) {
                return;
            }
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
