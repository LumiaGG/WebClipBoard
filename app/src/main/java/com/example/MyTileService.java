package com.example;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MyTileService extends TileService {
    public static final String TAG = MyTileService.class.getSimpleName();

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        // 当用户添加Tile到快速设置区域时调用，可以在这里进行一次性的初始化操作。
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        // 当Tile变为可见时调用，这里可以进行更新Tile，注册监听或回调等操作。
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        // 当Tile变为不可见时调用，这里可以进行注销监听或回调等操作。
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        // 当用户从快速设置区域移除一个Tile时调用，这里不要做有关于此Tile的任何操作。
    }
    @Override
    public void onClick() {
        super.onClick();

        collapseStatusBar(getApplicationContext());
        Intent dialogIntent = new Intent(getBaseContext(), MainActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(dialogIntent);

    }

    /**
     *
     * 收起通知栏
     * @param context
     */
    public static void collapseStatusBar(Context context) {
        try {
            @SuppressLint("WrongConstant") Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;

            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}