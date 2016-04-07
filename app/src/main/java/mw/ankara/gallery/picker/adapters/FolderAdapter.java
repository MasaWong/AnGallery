package mw.ankara.gallery.picker.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import mw.ankara.gallery.R;
import mw.ankara.gallery.picker.beans.PhotoFolder;
import mw.ankara.gallery.ImageUtils;
import mw.ankara.gallery.picker.utils.OtherUtils;

/**
 * @Class: FolderAdapter
 * @Description: 图片目录适配器
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/6
 */
public class FolderAdapter extends BaseAdapter {

    private List<PhotoFolder> mFolders;
    Context mContext;
    int mWidth;

    public FolderAdapter(Context context, List<PhotoFolder> folders) {
        this.mFolders = folders;
        this.mContext = context;
        mWidth = OtherUtils.dip2px(context, 90);
    }

    @Override
    public int getCount() {
        return mFolders.size();
    }

    @Override
    public Object getItem(int position) {
        return mFolders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_floder_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.setData(mFolders.get(position));

        return convertView;
    }

    private class ViewHolder {
        private ImageView mIvPhoto;
        private TextView folderNameTV;
        private TextView photoNumTV;
        private ImageView selectIV;

        public ViewHolder(View convertView) {
            mIvPhoto = (ImageView) convertView.findViewById(R.id.imageview_floder_img);
            folderNameTV = (TextView) convertView.findViewById(R.id.textview_floder_name);
            photoNumTV = (TextView) convertView.findViewById(R.id.textview_photo_num);
            selectIV = (ImageView) convertView.findViewById(R.id.imageview_floder_select);
        }

        public void setData(PhotoFolder folder) {
            mIvPhoto.setImageResource(R.drawable.img_photo_default);
            selectIV.setVisibility(folder.isSelected() ? View.VISIBLE : View.GONE);
            folderNameTV.setText(folder.getName());
            photoNumTV.setText(folder.getPhotoList().size() + "张");
            ImageUtils.display(folder.getPhotoList().get(0).getPath(), mIvPhoto,
                mWidth);

        }
    }

}
