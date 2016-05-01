package mw.ankara.gallery.picker.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mw.ankara.gallery.R;
import mw.ankara.gallery.picker.PhotoPickerActivity;
import mw.ankara.gallery.picker.beans.Photo;
import mw.ankara.uikit.image.SquareImageView;

/**
 * @Class: PhotoAdapter
 * @Description: 图片适配器
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class PhotoAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_PHOTO = 1;

    public static final Photo CAMERA = null;

    private List<Photo> mPhotos = new ArrayList<>();
    //存放已选中的Photo数据
    private List<String> mSelectedPhotos = new ArrayList<>();
    private int mSize;

    //是否显示相机
    private boolean mShowCamera = true;
    //照片选择模式，默认单选
    private int mSelectMode = PhotoPickerActivity.MODE_SINGLE;
    //图片选择数量
    private int mMaxNum = PhotoPickerActivity.DEFAULT_NUM;

    private View.OnClickListener mPhotoClickListener;
    private PhotoClickCallBack mCallBack;

    public PhotoAdapter(Context context, List<Photo> photos) {
        setPhotos(photos, mShowCamera);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        mSize = (screenWidth - (int) (8 * metrics.density + 0.5)) / 3;
    }

    public void setShowCamera(boolean showCamera) {
        mShowCamera = showCamera;
    }

    public void setPhotos(List<Photo> photos) {
        setPhotos(photos, mShowCamera);
    }

    public void setPhotos(List<Photo> photos, boolean showCamera) {
        mPhotos.clear();
        mPhotos.addAll(photos);
        if (showCamera) { // 显示相机的话加入空占位符
            mPhotos.add(0, CAMERA);
        }
    }

    public void setMaxNum(int maxNum) {
        this.mMaxNum = maxNum;
    }

    public void setPhotoClickCallBack(PhotoClickCallBack callback) {
        mCallBack = callback;
    }

    public void setSelectMode(int selectMode) {
        this.mSelectMode = selectMode;
        if (mSelectMode == PhotoPickerActivity.MODE_MULTI && mPhotoClickListener == null) {
            /**
             * 初始化多选模式所需要的参数
             */
            mPhotoClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = v.findViewById(R.id.imageview_photo).getTag().toString();
                    if (mSelectedPhotos.contains(path)) {
                        v.findViewById(R.id.mask).setVisibility(View.GONE);
                        v.findViewById(R.id.checkmark).setSelected(false);
                        mSelectedPhotos.remove(path);
                    } else {
                        if (mSelectedPhotos.size() >= mMaxNum) {
                            Toast.makeText(v.getContext(), R.string.msg_maxi_capacity,
                                Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mSelectedPhotos.add(path);
                        v.findViewById(R.id.mask).setVisibility(View.VISIBLE);
                        v.findViewById(R.id.checkmark).setSelected(true);
                    }
                    if (mCallBack != null) {
                        mCallBack.onPhotoClick();
                    }
                }
            };
        }
    }

    /**
     * 获取已选中相片
     */
    public List<String> getSelectedPhotos() {
        return mSelectedPhotos;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) == null ? TYPE_CAMERA : TYPE_PHOTO;
    }

    @Override
    public int getCount() {
        return mPhotos.size();
    }

    @Override
    public Photo getItem(int position) {
        return mPhotos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_CAMERA) {
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.item_camera_layout, null);
            }
            //设置高度等于宽度
            GridView.LayoutParams lp = new GridView.LayoutParams(mSize, mSize);
            convertView.setLayoutParams(lp);
        } else {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.item_photo_layout, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.setPhoto(getItem(position));
        }
        return convertView;
    }

    private class ViewHolder {
        private SquareImageView photoImageView;
        private ImageView selectView;
        private View maskView;
        private FrameLayout wrapLayout;

        public ViewHolder(View convertView) {
            photoImageView = (SquareImageView) convertView.findViewById(R.id.imageview_photo);
            selectView = (ImageView) convertView.findViewById(R.id.checkmark);
            maskView = convertView.findViewById(R.id.mask);
            wrapLayout = (FrameLayout) convertView.findViewById(R.id.wrap_layout);
        }

        public void setPhoto(Photo photo) {
            photoImageView.setImagePathWithHolder(photo.getPath(), R.drawable.img_photo_default, 0);

            if (mSelectMode == PhotoPickerActivity.MODE_MULTI) {
                wrapLayout.setOnClickListener(mPhotoClickListener);
                photoImageView.setTag(photo.getPath());
                selectView.setVisibility(View.VISIBLE);
                if (mSelectedPhotos != null && mSelectedPhotos.contains(photo.getPath())) {
                    selectView.setSelected(true);
                    maskView.setVisibility(View.VISIBLE);
                } else {
                    selectView.setSelected(false);
                    maskView.setVisibility(View.GONE);
                }
            } else {
                selectView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 多选时，点击相片的回调接口
     */
    public interface PhotoClickCallBack {
        void onPhotoClick();
    }
}
