package com.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.webclipboard.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class Webclipboard {

    //"http://119.29.130.26:5000/"
    public static String base_url = null;
    public static String upload_img_url = null;
    public static String upload_text_url = null;
    public static String download_img_url = null;
    public static String download_text_url = null;

    private Context mContext = null;
    public String content_web = "";
    public String content_local = "";
    public Bitmap thumb_web = null;
    public Bitmap thumb_local = null;

    public Webclipboard(Context mContext, String server_IP){
        this.mContext = mContext;
        base_url = "http://"+server_IP;
        upload_img_url = base_url + "/clipboard/writeImg/wo-jiu-bu-gao-su-ni-ni-cai-ya";
        upload_text_url = base_url + "/clipboard/write/wo-jiu-bu-gao-su-ni-ni-cai-ya";
        download_img_url = base_url + "/clipboard/readImg/wo-jiu-bu-gao-su-ni-ni-cai-ya";
        download_text_url = base_url + "/clipboard/read/wo-jiu-bu-gao-su-ni-ni-cai-ya";
    }

    public void read_clipboard(TextView textView_web, TextView textView_local, ImageView imageview_web){
        long  request_time = System.currentTimeMillis();
        HttpUtil.getRequest(Webclipboard.download_text_url, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MainActivity.show_toast(mContext, "服务器连接失败", false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONObject json = null;
                try {
                    json = new JSONObject(response.body().string());
                    response.close();
                    String thumb_base64 = json.getString("thumb_base64");
                    if(thumb_base64 != null){
                        thumb_web = ImgUtils.stringToBitmap(thumb_base64);
                    }
                    content_web = json.getString("content");
                    int delay = (int)(230 - (System.currentTimeMillis() - request_time));
                    if(delay < 0){
                        delay = 0;
                    }
                    update_textView(textView_web, textView_local, imageview_web, delay);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void write_clipboard_local(String content){
        try {
            Clipboard_Utils.copyToClipboard(mContext, content);
            MainActivity.show_toast(mContext, "已复制", false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void write_clipboard_web(String content) {
        //上传
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(0);
        }
        HttpUtil.postRequest(upload_text_url, json, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MainActivity.show_toast(mContext, "上传失败", false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.show_toast(mContext, "上传完成", false);
            }
        });
    }

    public void download_img(ImageView imageview_web, ImageView imageview_local){
        update_imageView_ing(imageview_web);
        long  request_time = System.currentTimeMillis();
        HttpUtil.getRequest(download_img_url, new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MainActivity.show_toast(mContext, "下载失败", false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String filename =  response.header("content-disposition");
                filename = filename.substring(filename.indexOf("filename=")+10,filename.length()-1);
                Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                response.close();
                if(bitmap != null && ImgUtils.saveImageToGallery(mContext,bitmap,filename)){
                    int delay = (int)(500 - (System.currentTimeMillis() - request_time));
                    if(delay < 0){
                        delay = 0;
                    }
                    update_imageView_ed(imageview_web, imageview_local, thumb_web, delay);
                    bitmap.recycle();
                    System.gc();
                }else {
                    MainActivity.show_toast(mContext, "图片错误", false);
                }
            }
        });
    }

    public void upload_img(String img_path, ImageView imageview_web, ImageView imageview_local){
        if(img_path == null){
            MainActivity.show_toast(mContext, "图片地址空", false);
            return;
        }

        update_imageView_ing(imageview_local);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 10;
        thumb_local = BitmapFactory.decodeFile(img_path,opts);
        long  request_time = System.currentTimeMillis();
        HttpUtil.uploadImage(Webclipboard.upload_img_url, img_path, new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e.getMessage());
                MainActivity.show_toast(mContext, "上传失败", false);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();
                int delay = (int)(500 - (System.currentTimeMillis() - request_time));
                if(delay < 0){
                    delay = 0;
                }
                update_imageView_ed(imageview_local,imageview_web, thumb_local, delay);
                thumb_web = thumb_local;
            }
        });
    }

    private void update_textView(TextView textView_web, TextView textView_local, ImageView imageview_web, int delay) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                AlphaAnimation alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(mContext, R.anim.alpha);
                content_local = Clipboard_Utils.getDataFromClipboard(mContext);
                textView_web.setText(content_web);
                textView_local.setText(content_local);
                imageview_web.setImageBitmap(thumb_web);
                textView_web.startAnimation(alphaAnimation);
                imageview_web.startAnimation(alphaAnimation);
                textView_local.startAnimation(alphaAnimation);
            }
        }, delay);
    }

    private void update_imageView_ing(ImageView imageview){
        //下载中或者上传中的动画
        AlphaAnimation alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(mContext, R.anim.alpha_repeat);
        imageview.startAnimation(alphaAnimation);
    }

    private void update_imageView_ed(ImageView imageview_A,ImageView imageview_B ,Bitmap thumb_B, int delay){
        //下载或上传完成的动画  A -> B
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                imageview_A.clearAnimation();
                AlphaAnimation alphaAnimation_show = (AlphaAnimation) AnimationUtils.loadAnimation(mContext, R.anim.alpha_show);
                AlphaAnimation alphaAnimation_disappear = (AlphaAnimation) AnimationUtils.loadAnimation(mContext, R.anim.alpha_disappear);
                alphaAnimation_disappear.setAnimationListener(new Animation.AnimationListener(){
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imageview_B.setImageBitmap(thumb_B);
                        imageview_B.startAnimation(alphaAnimation_show);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                imageview_B.startAnimation(alphaAnimation_disappear);
            }
        }, delay);
    }
}
