/*
Copyright 2016 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.example;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;

import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.webclipboard.R;


public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    public Webclipboard webclipboard = null;

    public TextView textView_web = null;
    public TextView textView_local = null;
    public ImageFilterView imageview_web = null;
    public ImageFilterView imageview_local = null;
    public View maskView = null;

    public SharedPreferences sharedPreferences = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        makeStatusBarTransparent(this);
        Log.d(TAG, "onCreate: ");

        requestPermission();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        textView_web = (TextView)findViewById(R.id.textview_web);
        textView_local = (TextView)findViewById(R.id.textview_local);
        imageview_web = (ImageFilterView)findViewById(R.id.imageView_web);
        imageview_local = (ImageFilterView)findViewById(R.id.imageView_local);
        maskView = (View)findViewById(R.id.maskView);
        textView_web.setOnClickListener(this::onClick);
        textView_local.setOnClickListener(this::onClick);
        textView_local.setOnLongClickListener(this::OnLongClick);
        imageview_web.setOnClickListener(this::onClick);
        imageview_local.setOnClickListener(this::onClick);
        imageview_local.setOnLongClickListener(this::OnLongClick);
        maskView.setOnClickListener(this::onClick);


        webclipboard = new Webclipboard(getApplicationContext(), sharedPreferences.getString("server_IP", ""));
        webclipboard.read_clipboard(textView_web, textView_local, imageview_web);

//        Intent intent = getIntent();
//        String action = intent.getAction();//action
//        String type = intent.getType();//类型
//
//        if (Intent.ACTION_SEND.equals(action) && type != null /*&& "video/mp4".equals(type)*/) {
//            finish();
//            Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
//            String filePath= getRealPathFromURI(uri);
//            Webclipboard.upload_img(getApplicationContext() ,filePath);
//        }else{
//            generateBigTextStyleNotification("","",
//                    "",null); // 占位 在数据请求慢的时候获取有用
//            Pop_notification();
//        }

    }

    private void onClick(View view) {
        switch(view.getId()){
            case R.id.textview_local:
                webclipboard.write_clipboard_web(webclipboard.content_local);
                finish();
                break;
            case R.id.textview_web:
                webclipboard.write_clipboard_local(webclipboard.content_web);
                finish();
                break;
            case R.id.imageView_web:
                webclipboard.download_img(imageview_web, imageview_local);
                //finish();
                break;
            case R.id.imageView_local:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
                break;
            case R.id.maskView:
                finish();
                break;
        }
    }

    private boolean OnLongClick(View view){
        switch(view.getId()) {
            case R.id.imageView_local:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.textview_local:
                webclipboard.write_clipboard_web(webclipboard.content_web + "\n" + webclipboard.content_local);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK){
            FileChooseUtil file = new FileChooseUtil(getApplicationContext());
            String path = file.getChooseFileResultPath(data.getData());

            webclipboard.upload_img(path, imageview_web, imageview_local);
        }
    }

    private void makeStatusBarTransparent(Activity activity) {

        Window window = activity.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int option = window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(option);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
        }

    }

    private void requestPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show();

            // 申请 相机 麦克风权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    public static void show_toast(Context mContext, String text, boolean is_long){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable(){
            public void run(){
                if(is_long){
                    Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

}

