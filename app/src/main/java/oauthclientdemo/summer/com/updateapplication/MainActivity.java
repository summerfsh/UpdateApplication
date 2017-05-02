package oauthclientdemo.summer.com.updateapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.autoupdate.IFlytekUpdate;
import com.iflytek.autoupdate.IFlytekUpdateListener;
import com.iflytek.autoupdate.UpdateConstants;
import com.iflytek.autoupdate.UpdateErrorCode;
import com.iflytek.autoupdate.UpdateInfo;
import com.iflytek.autoupdate.UpdateType;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private IFlytekUpdate updManager;
    final private int REQUEST_WRITE_EXTERNAL_STORAGE = 111;
    private Toast mToast;
    //自动更新回调方法，详情参考demo
    IFlytekUpdateListener updateListener = new IFlytekUpdateListener() {
        @Override

        public void onResult(int errorcode, UpdateInfo result) {
            Log.d(TAG, "result-" + result);

            // 判断是否下载过新app,若有删除，下载最新的app
            if (errorcode == UpdateErrorCode.OK && result != null) {
                if (result.getUpdateType() == UpdateType.NoNeed) {
                    showTip("已经是最新版本！");
                    return;
                }
                updManager.showUpdateInfo(MainActivity.this, result);
            } else {
                showTip("请求更新失败！\n更新错误码：" + errorcode);
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        if (Build.VERSION.SDK_INT >= 23) {
            requestWriterSD();
        } else {
            iFLYTEKUpdate();
        }
    }

    private void iFLYTEKUpdate() {
        //初始化自动更新对象

        updManager = IFlytekUpdate.getInstance(this);

//开启调试模式，默认不开启

        updManager.setDebugMode(true);

//开启wifi环境下检测更新，仅对自动更新有效，强制更新则生效

        updManager.setParameter(UpdateConstants.EXTRA_WIFIONLY, "true");

//设置通知栏使用应用icon，详情请见示例

        updManager.setParameter(UpdateConstants.EXTRA_NOTI_ICON, "true");

//设置更新提示类型，默认为通知栏提示

        updManager.setParameter(UpdateConstants.EXTRA_STYLE, UpdateConstants.UPDATE_UI_DIALOG);
// 启动自动更新

        updManager.autoUpdate(MainActivity.this, updateListener);
        updateIsDownloadApp();

    }


    private void updateIsDownloadApp() {
        // 删除已下载的更新文件
        String path = "";
        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState())) {
            path = Environment.getExternalStorageDirectory() + "/download/iFlyUpdate";
        }
        if (TextUtils.isEmpty(path)) {
//            showTip("文件路径不正确！");
            Log.d(TAG, "文件路径不正确！");
            return;
        }
        File file = new File(path);
        delFile(file);
//        showTip("文件已删除！");
        Log.d(TAG, "文件已删除！");
    }

    private void showTip(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(s);
                mToast.show();
            }
        });
    }

    private void delFile(File deleteFile) {
        if (!deleteFile.exists()) {
            return;
        }
        if (!deleteFile.isDirectory()) {
            deleteFile.delete();
        } else {
            File[] fileList = deleteFile.listFiles();
            if (null == fileList || fileList.length <= 0) {
                deleteFile.delete();
                return;
            }
            for (File file : fileList) {
                delFile(file);
            }
            deleteFile.delete();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    iFLYTEKUpdate();
                } else {
                    Toast.makeText(this, "读取内部存储权限没有打开！", Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestWriterSD() {
        // 需要验证的权限
        int hsaCameraPermission = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hsaCameraPermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (hsaCameraPermission != PackageManager.PERMISSION_GRANTED) {
            // 弹窗询问 ，让用户自己判断
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return;
        } else {
            iFLYTEKUpdate();
        }
    }
}
