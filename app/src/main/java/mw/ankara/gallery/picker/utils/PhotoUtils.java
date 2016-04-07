package mw.ankara.gallery.picker.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mw.ankara.gallery.picker.beans.Photo;
import mw.ankara.gallery.picker.beans.PhotoFolder;

/**
 * @Class: PhotoUtils
 * @Description:
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class PhotoUtils {

    public static Map<String, PhotoFolder> getPhotos(Context context) {
        Map<String, PhotoFolder> folderMap = new HashMap<>();

        String allPhotosKey = "所有图片";
        PhotoFolder folder = new PhotoFolder();
        folder.setName(allPhotosKey);
        folder.setDirPath(allPhotosKey);
        folder.setPhotoList(new ArrayList<Photo>());
        folderMap.put(allPhotosKey, folder);

        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();

        // 只查询jpeg和png的图片
        Cursor cursor = mContentResolver.query(imageUri, null,
            MediaStore.Images.Media.MIME_TYPE + " in(?, ?)",
            new String[]{"image/jpeg", "image/png"},
            MediaStore.Images.Media.DATE_MODIFIED + " desc");

        if (cursor != null) {
            int pathIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                // 获取图片的路径
                String path = cursor.getString(pathIndex);

                // 获取该图片的父路径名
                File parentFile = new File(path).getParentFile();
                if (parentFile == null) {
                    continue;
                }
                String dirPath = parentFile.getAbsolutePath();

                if (folderMap.containsKey(dirPath)) {
                    Photo photo = new Photo(path);
                    PhotoFolder photoFolder = folderMap.get(dirPath);
                    photoFolder.getPhotoList().add(photo);
                    folderMap.get(allPhotosKey).getPhotoList().add(photo);
                } else {
                    // 初始化imageFloder
                    PhotoFolder photoFolder = new PhotoFolder();
                    List<Photo> photoList = new ArrayList<Photo>();
                    Photo photo = new Photo(path);
                    photoList.add(photo);
                    photoFolder.setPhotoList(photoList);
                    photoFolder.setDirPath(dirPath);
                    photoFolder.setName(dirPath.substring(dirPath.lastIndexOf(File.separator) + 1, dirPath.length()));
                    folderMap.put(dirPath, photoFolder);
                    folderMap.get(allPhotosKey).getPhotoList().add(photo);
                }
            }
            cursor.close();
        }
        return folderMap;
    }

}
