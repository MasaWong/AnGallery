package mw.ankara.gallery.picker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mw.ankara.gallery.R;
import mw.ankara.gallery.clip.PhotoClipActivity;
import mw.ankara.gallery.picker.adapters.FloderAdapter;
import mw.ankara.gallery.picker.adapters.PhotoAdapter;
import mw.ankara.gallery.picker.beans.Photo;
import mw.ankara.gallery.picker.beans.PhotoFloder;
import mw.ankara.gallery.picker.utils.ImageLoader;
import mw.ankara.gallery.picker.utils.OtherUtils;
import mw.ankara.gallery.picker.utils.PhotoUtils;

/**
 * @Class: PhotoPickerActivity
 * @Description: 照片选择界面
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class PhotoPickerActivity extends AppCompatActivity implements PhotoAdapter.PhotoClickCallBack {

    public final static String KEY_RESULT = "bitmap";

    public final static int REQUEST_CAMERA = 1;
    public final static int REQUEST_CLIP = 2;

    /**
     * 是否显示相机
     */
    public final static String EXTRA_SHOW_CAMERA = "camera";
    /**
     * 照片选择模式
     */
    public final static String EXTRA_SELECT_MODE = "mode";
    /**
     * 最大选择数量
     */
    public final static String EXTRA_MAX_MUN = "max";
    /**
     * 单选
     */
    public final static int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public final static int MODE_MULTI = 1;
    /**
     * 默认最大选择数量
     */
    public final static int DEFAULT_NUM = 9;

    private final static String ALL_PHOTO = "所有图片";

    /**
     * 是否显示相机，默认不显示
     */
    private boolean mShowCamera = false;
    /**
     * 照片选择模式，默认是单选模式
     */
    private int mSelectMode = MODE_SINGLE;
    /**
     * 最大选择数量，仅多选模式有用
     */
    private int mMaxNum = DEFAULT_NUM;

    private ProgressDialog mProgressDialog;

    private GridView mGridView;
    private List<Photo> mPhotoLists = new ArrayList<>();
    private ArrayList<String> mSelectList = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter;

    private Map<String, PhotoFloder> mFolderMap;
    private ListView mFolderListView;

    private TextView mPhotoNumTV;
    private TextView mPhotoNameTV;
    private MenuItem mMiCommit;

    /**
     * 文件夹列表是否处于显示状态
     */
    boolean mIsFloderViewShow = false;
    /**
     * 文件夹列表是否被初始化，确保只被初始化一次
     */
    boolean mIsFloderViewInit = false;

    /**
     * 初始化文件夹列表的显示隐藏动画
     */
    private Animation mInAnimation;
    private Animation mOutAnimation;

    /**
     * 拍照时存储拍照结果的临时文件
     */
    private File mTmpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 绑定View
        mGridView = (GridView) findViewById(R.id.photo_gridview);
        mPhotoNumTV = (TextView) findViewById(R.id.photo_num);
        mPhotoNameTV = (TextView) findViewById(R.id.floder_name);
        findViewById(R.id.bottom_tab_bar).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //消费触摸事件，防止触摸底部tab栏也会选中图片
                return true;
            }
        });

        /**
         * 初始化选项参数
         */
        Uri uri = getIntent().getData();
        if (uri != null) {
            String showCamera = uri.getQueryParameter(EXTRA_SHOW_CAMERA);
            if (!TextUtils.isEmpty(showCamera)) {
                mShowCamera = Boolean.parseBoolean(showCamera);
            }
            String selectMode = uri.getQueryParameter(EXTRA_SELECT_MODE);
            if (!TextUtils.isEmpty(selectMode)) {
                mSelectMode = Integer.parseInt(selectMode);
            }
            String maxNum = uri.getQueryParameter(EXTRA_MAX_MUN);
            if (!TextUtils.isEmpty(maxNum)) {
                mMaxNum = Integer.parseInt(maxNum);
            }
        }

        if (!OtherUtils.isExternalStorageAvailable()) {
            Toast.makeText(this, "No SD card!", Toast.LENGTH_SHORT).show();
            return;
        }
        getPhotosTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mSelectMode == MODE_MULTI) {
            //如果是多选模式，需要将确定按钮初始化以及绑定事件
            getMenuInflater().inflate(R.menu.send, menu);
            mMiCommit = menu.findItem(R.id.clip_send);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mMiCommit) {
            mSelectList.clear();
            mSelectList.addAll(mPhotoAdapter.getSelectedPhotos());
            returnData();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLoader.releaseInstance();
    }

    /**
     * 点击选择某张照片
     *
     * @param photo
     */
    private void selectPhoto(Photo photo) {
        if (photo == null) {
            return;
        }
        String path = photo.getPath();
        if (mSelectMode == MODE_SINGLE) {
            mSelectList.clear();
            mSelectList.add(path);
            returnData();
        }
    }

    @Override
    public void onPhotoClick() {
        List<String> list = mPhotoAdapter.getSelectedPhotos();
        if (list != null && list.size() > 0) {
            mMiCommit.setEnabled(true);
            setTitle(getString(R.string.photo_picker_format, list.size(), mMaxNum));
        } else {
            mMiCommit.setEnabled(false);
            setTitle(R.string.photo_picker_title);
        }
    }

    /**
     * 选择相机
     */
    private void onCameraClick() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = OtherUtils.createFile(getApplicationContext());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(getApplicationContext(),
                R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 返回选择图片的路径
     */
    private void returnData() {
        Intent intent = new Intent();
        // 返回已选择的图片数据
        if (mSelectMode == MODE_SINGLE) {
            intent.setClass(this, PhotoClipActivity.class);
            intent.putExtra("photo", URLEncoder.encode(mSelectList.get(0)));
            startActivityForResult(intent, REQUEST_CLIP);
        } else {
            intent.putStringArrayListExtra(KEY_RESULT, mSelectList);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     * 显示或者隐藏文件夹列表
     *
     * @param floders
     */
    private void toggleFloderList(final List<PhotoFloder> floders) {
        //初始化文件夹列表
        if (!mIsFloderViewInit) {
            ViewStub floderStub = (ViewStub) findViewById(R.id.floder_stub);
            floderStub.inflate();
            View dimLayout = findViewById(R.id.dim_layout);
            mFolderListView = (ListView) findViewById(R.id.listview_floder);
            final FloderAdapter adapter = new FloderAdapter(this, floders);
            mFolderListView.setAdapter(adapter);
            mFolderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    for (PhotoFloder floder : floders) {
                        floder.setIsSelected(false);
                    }
                    PhotoFloder floder = floders.get(position);
                    floder.setIsSelected(true);
                    adapter.notifyDataSetChanged();

                    mPhotoLists.clear();
                    mPhotoLists.addAll(floder.getPhotoList());
                    //这里重新设置adapter而不是直接notifyDataSetChanged，是让GridView返回顶部
                    mGridView.setAdapter(mPhotoAdapter);
                    mPhotoNumTV.setText(OtherUtils.formatResourceString(getApplicationContext(),
                        R.string.photos_num, mPhotoLists.size()));
                    mPhotoNameTV.setText(floder.getName());
                    toggle();
                }
            });
            dimLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mIsFloderViewShow) {
                        toggle();
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            mInAnimation = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
            mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_bottom);

            mIsFloderViewInit = true;
        }
        toggle();
    }

    /**
     * 弹出或者收起文件夹列表
     */
    private void toggle() {
        if (mIsFloderViewShow) {
            mFolderListView.startAnimation(mOutAnimation);
            mFolderListView.setVisibility(View.GONE);
            mIsFloderViewShow = false;
        } else {
            mFolderListView.startAnimation(mInAnimation);
            mFolderListView.setVisibility(View.VISIBLE);
            mIsFloderViewShow = true;
        }
    }

    /**
     * 选择文件夹
     *
     * @param photoFloder
     */
    public void selectFloder(PhotoFloder photoFloder) {
        mPhotoAdapter.setPhotos(photoFloder.getPhotoList(), mShowCamera);
        mPhotoAdapter.notifyDataSetChanged();
    }

    /**
     * 获取照片的异步任务
     */
    private AsyncTask getPhotosTask = new AsyncTask() {
        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(PhotoPickerActivity.this, 3);
            mProgressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            mFolderMap = PhotoUtils.getPhotos(getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            mPhotoLists.addAll(mFolderMap.get(ALL_PHOTO).getPhotoList());

            mPhotoNumTV.setText(OtherUtils.formatResourceString(getApplicationContext(),
                R.string.photos_num, mPhotoLists.size()));

            mPhotoAdapter = new PhotoAdapter(PhotoPickerActivity.this, mPhotoLists, mShowCamera);
            mPhotoAdapter.setSelectMode(mSelectMode);
            mPhotoAdapter.setMaxNum(mMaxNum);
            mPhotoAdapter.setPhotoClickCallBack(PhotoPickerActivity.this);
            mGridView.setAdapter(mPhotoAdapter);
            Set<String> keys = mFolderMap.keySet();
            final List<PhotoFloder> floders = new ArrayList<>();
            for (String key : keys) {
                if (ALL_PHOTO.equals(key)) {
                    PhotoFloder floder = mFolderMap.get(key);
                    floder.setIsSelected(true);
                    floders.add(0, floder);
                } else {
                    floders.add(mFolderMap.get(key));
                }
            }
            mPhotoNameTV.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    toggleFloderList(floders);
                }
            });

            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Photo photo = mPhotoAdapter.getItem(position);
                    if (photo == PhotoAdapter.CAMERA) {
                        onCameraClick();
                    } else {
                        selectPhoto(photo);
                    }
                }
            });
            mProgressDialog.dismiss();
        }
    };

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CLIP && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        } else if (requestCode == REQUEST_CAMERA) {
            // 相机拍照完成后，返回图片路径
            if (resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    mSelectList.add(mTmpFile.getAbsolutePath());
                    returnData();
                }
            } else {
                if (mTmpFile != null && mTmpFile.exists()) {
                    mTmpFile.delete();
                }
            }
        }
    }
}
