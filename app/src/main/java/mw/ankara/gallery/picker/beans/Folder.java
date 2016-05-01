package mw.ankara.gallery.picker.beans;

import java.io.Serializable;
import java.util.List;

/**
 * @Class: Folder
 * @Description: 相片文件夹实体类
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class Folder implements Serializable {

    /* 文件夹名 */
    private String mName;
    /* 文件夹路径 */
    private String mDirPath;
    /* 该文件夹下图片列表 */
    private List<Photo> mPhotoList;
    /* 标识是否选中该文件夹 */
    private boolean mIsSelected;

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.mIsSelected = isSelected;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDirPath() {
        return mDirPath;
    }

    public void setDirPath(String dirPath) {
        this.mDirPath = dirPath;
    }

    public List<Photo> getPhotoList() {
        return mPhotoList;
    }

    public void setPhotoList(List<Photo> photoList) {
        this.mPhotoList = photoList;
    }
}
