package cn.finalteam.rxgalleryfinal.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.util.List;

import cn.finalteam.rxgalleryfinal.Configuration;
import cn.finalteam.rxgalleryfinal.R;
import cn.finalteam.rxgalleryfinal.bean.MediaBean;
import cn.finalteam.rxgalleryfinal.rxbus.RxBus;
import cn.finalteam.rxgalleryfinal.rxbus.event.MediaCheckChangeEvent;
import cn.finalteam.rxgalleryfinal.rxjob.Job;
import cn.finalteam.rxgalleryfinal.rxjob.RxJob;
import cn.finalteam.rxgalleryfinal.rxjob.job.ImageThmbnailJobCreate;
import cn.finalteam.rxgalleryfinal.ui.activity.MediaActivity;
import cn.finalteam.rxgalleryfinal.ui.base.IMultiImageCheckedListener;
import cn.finalteam.rxgalleryfinal.ui.widget.RecyclerImageView;
import cn.finalteam.rxgalleryfinal.utils.Logger;
import cn.finalteam.rxgalleryfinal.utils.OsCompat;
import cn.finalteam.rxgalleryfinal.utils.ThemeUtils;

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/5/18 下午7:48
 */
public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.GridViewHolder> {

    private MediaActivity mMediaActivity;
    private List<MediaBean> mMediaBeanList;
    private LayoutInflater mInflater;
    private int mImageSize;
    private Configuration mConfiguration;
    private Drawable mDefaultImage;
    private Drawable mImageViewBg;
    private Drawable mCameraImage;
    private int mCameraTextColor;
    //#ADD
    private int imageLoaderType = 0;

    private static IMultiImageCheckedListener iMultiImageCheckedListener;

    public MediaGridAdapter(MediaActivity mediaActivity, List<MediaBean> list, int screenWidth, Configuration configuration) {
        this.mMediaActivity = mediaActivity;
        this.mMediaBeanList = list;
        this.mInflater = LayoutInflater.from(mediaActivity);
        this.mImageSize = screenWidth/3;
        int defaultResId = ThemeUtils.resolveDrawableRes(mediaActivity, R.attr.gallery_default_image, R.drawable.gallery_default_image);
        this.mDefaultImage = mediaActivity.getResources().getDrawable(defaultResId);
        this.mConfiguration = configuration;
        //#ADD
        this.imageLoaderType = configuration.getImageLoaderType();

        this.mImageViewBg = ThemeUtils.resolveDrawable(mMediaActivity,
                R.attr.gallery_imageview_bg, R.drawable.gallery_default_image);
        this.mCameraImage = ThemeUtils.resolveDrawable(mMediaActivity, R.attr.gallery_camera_bg,
                R.drawable.gallery_ic_camera);
        this.mCameraTextColor = ThemeUtils.resolveColor(mMediaActivity, R.attr.gallery_take_image_text_color,
                R.color.gallery_default_take_image_text_color);
    }

    @Override
    public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      //  View view = mInflater.inflate(R.layout.gallery_adapter_media_grid_item, parent, false);
        //#ADD
        View view = null;
        if (imageLoaderType == 0) {
            view = mInflater.inflate(R.layout.gallery_adapter_media_grid_item, parent, false);
        } else {
            view = mInflater.inflate(R.layout.gallery_adapter_media_grid_item_1, parent, false);
        }
        return new GridViewHolder(mMediaActivity, view,viewType);
    }

    @Override
    public void onBindViewHolder(GridViewHolder holder, int position) {
        MediaBean mediaBean = mMediaBeanList.get(position);
        if(mediaBean.getId() == Integer.MIN_VALUE) {
            holder.mCbCheck.setVisibility(View.GONE);
            //#ADD
            if (imageLoaderType == 0) {
                holder.mIvMediaImage.setVisibility(View.GONE);
            } else {
                holder.mIvMediaImage_1.setVisibility(View.GONE);
            }
         //   holder.mIvMediaImage.setVisibility(View.GONE);
            holder.mLlCamera.setVisibility(View.VISIBLE);
            holder.mIvCameraImage.setImageDrawable(mCameraImage);
            holder.mTvCameraTxt.setTextColor(mCameraTextColor);
        } else {
            if(mConfiguration.isRadio()) {
                holder.mCbCheck.setVisibility(View.GONE);
            } else{
                holder.mCbCheck.setVisibility(View.VISIBLE);
                holder.mCbCheck.setOnClickListener(new OnCheckBoxClickListener(mediaBean));
                holder.mCbCheck.setOnCheckedChangeListener(new OnCheckBoxCheckListener(mediaBean));
            }
            //#ADD
            if (imageLoaderType == 0) {
                holder.mIvMediaImage.setVisibility(View.VISIBLE);
            } else {
                holder.mIvMediaImage_1.setVisibility(View.VISIBLE);
            }
           // holder.mIvMediaImage.setVisibility(View.VISIBLE);
            holder.mLlCamera.setVisibility(View.GONE);
            if(mMediaActivity.getCheckedList() != null && mMediaActivity.getCheckedList().contains(mediaBean)){
                holder.mCbCheck.setChecked(true);
            } else {
                holder.mCbCheck.setChecked(false);
            }
            String bitPath = mediaBean.getThumbnailSmallPath();
            String smallPath = mediaBean.getThumbnailSmallPath();

            if(!new File(bitPath).exists() || !new File(smallPath).exists()) {
                Job job = new ImageThmbnailJobCreate(mMediaActivity, mediaBean).create();
                RxJob.getDefault().addJob(job);
            }
            String path = mediaBean.getThumbnailSmallPath();
            if(TextUtils.isEmpty(path)) {
                path = mediaBean.getThumbnailBigPath();
            }
            if(TextUtils.isEmpty(path)) {
                path = mediaBean.getOriginalPath();
            }
            Logger.w("提示path：" + path);
            if (imageLoaderType == 0) {
                OsCompat.setBackgroundDrawableCompat(holder.mIvMediaImage, mImageViewBg);
                mConfiguration.getImageLoader()
                        .displayImage(mMediaActivity, path, holder.mIvMediaImage, mDefaultImage, mConfiguration.getImageConfig(),
                                true, mImageSize, mImageSize, mediaBean.getOrientation());
            } else {
                OsCompat.setBackgroundDrawableCompat(holder.mIvMediaImage_1, mImageViewBg);
                setImageSmall("file://"+path, holder.mIvMediaImage_1, mImageSize, mImageSize);
            }

         /*   mConfiguration.getImageLoader()
                    .displayImage(mMediaActivity, path, holder.mIvMediaImage, mDefaultImage, mConfiguration.getImageConfig(),
                            true, mImageSize, mImageSize, mediaBean.getOrientation());*/
        }
    }

    @Override
    public int getItemCount() {
        return mMediaBeanList.size();
    }

    class OnCheckBoxClickListener implements View.OnClickListener {

        private MediaBean mediaBean;

        public OnCheckBoxClickListener(MediaBean bean) {
            this.mediaBean = bean;
        }

        @Override
        public void onClick(View view) {
            if(mConfiguration.getMaxSize() == mMediaActivity.getCheckedList().size() &&
                    !mMediaActivity.getCheckedList().contains(mediaBean)) {
                AppCompatCheckBox checkBox = (AppCompatCheckBox) view;
                checkBox.setChecked(false);
                Logger.i("=>" + mMediaActivity.getResources().getString(R.string.gallery_image_max_size_tip, mConfiguration.getMaxSize()));
              /*  Toast.makeText(mMediaActivity, mMediaActivity.getResources()
                        .getString(R.string.gallery_image_max_size_tip, mConfiguration.getMaxSize()), Toast.LENGTH_SHORT).show();*/
            } else {
                RxBus.getDefault().post(new MediaCheckChangeEvent(mediaBean));
            }
        }
    }

    /**
     * @author KARL-dujinyang
     */
    class OnCheckBoxCheckListener implements CompoundButton.OnCheckedChangeListener{
        private MediaBean mediaBean;

        public OnCheckBoxCheckListener(MediaBean bean){
            this.mediaBean = bean;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(mConfiguration.getMaxSize() == mMediaActivity.getCheckedList().size() &&
                    !mMediaActivity.getCheckedList().contains(mediaBean)) {
                AppCompatCheckBox checkBox = (AppCompatCheckBox) buttonView;
                checkBox.setChecked(false);
                Logger.i("选中：" + mMediaActivity.getResources().getString(R.string.gallery_image_max_size_tip, mConfiguration.getMaxSize()));
                if (iMultiImageCheckedListener != null){
                    iMultiImageCheckedListener.selectedImgMax(buttonView, isChecked, mConfiguration.getMaxSize());
                }
            } else {
                if(iMultiImageCheckedListener!=null)
                    iMultiImageCheckedListener.selectedImg(buttonView, isChecked);
            }

        }
    }



    /**
     * RecyclerView.ViewHolder
     */
    static class GridViewHolder extends RecyclerView.ViewHolder {

        RecyclerImageView mIvMediaImage;
        AppCompatCheckBox mCbCheck;
        //#ADD
        SimpleDraweeView mIvMediaImage_1;

        LinearLayout mLlCamera;
        TextView mTvCameraTxt;
        ImageView mIvCameraImage;

        public GridViewHolder(Context context, View itemView, int viewType) {
            super(itemView);
            //#ADD
            if (viewType == 0) {
                mIvMediaImage = (RecyclerImageView) itemView.findViewById(R.id.iv_media_image);
            } else {
                mIvMediaImage_1 = (SimpleDraweeView) itemView.findViewById(R.id.iv_media_image);
            }
          //  mIvMediaImage = (RecyclerImageView) itemView.findViewById(R.id.iv_media_image);
            mCbCheck = (AppCompatCheckBox) itemView.findViewById(R.id.cb_check);

            mLlCamera = (LinearLayout) itemView.findViewById(R.id.ll_camera);
            mTvCameraTxt = (TextView) itemView.findViewById(R.id.tv_camera_txt);
            mIvCameraImage = (ImageView) itemView.findViewById(R.id.iv_camera_image);

            int checkTint = ThemeUtils.resolveColor(context, R.attr.gallery_checkbox_button_tint_color, R.color.gallery_default_checkbox_button_tint_color);
            CompoundButtonCompat.setButtonTintList(mCbCheck, ColorStateList.valueOf(checkTint));
        }
    }


    public static void setCheckedListener(IMultiImageCheckedListener checkedListener){
        iMultiImageCheckedListener = checkedListener;
    }



    public void setImageSmall(String url, SimpleDraweeView simpleDraweeView, int width, int height) {
        Uri uri = Uri.parse(url);
        ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(uri)
                .setAutoRotateEnabled(true)
                .setResizeOptions(new ResizeOptions(simpleDraweeView.getLayoutParams().width,
                        simpleDraweeView.getLayoutParams().height))
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .setCacheChoice(ImageRequest.CacheChoice.SMALL)
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setImageRequest(request)
                .setOldController(simpleDraweeView.getController())
                .build();

        simpleDraweeView.setController(controller);
    }
}
