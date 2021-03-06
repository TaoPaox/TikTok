package com.taopao.tiktok;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.gyf.barlibrary.ImmersionBar;
import com.taopao.commonsdk.AppSettingUtils;
import com.taopao.commonsdk.RouterHub;
import com.taopao.commonsdk.Utils;
import com.taopao.commonsdk.permission.PermissionSettingPage;
import com.taopao.commonsdk.permission.RequestPermissions;
import com.taopao.rxtoast.RxToast;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;

@Route(path = RouterHub.APP_HOMEACTIVITY)
public class HomeActivity extends AppCompatActivity {
    @BindView(R.id.homemenu)
    HomeMenuLayout mHomemenu;
    private ArrayList<Fragment> mFragments;
    private ImmersionBar mImmersionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);


        AppSettingUtils.setFirstStart(false);

//        StatusBarUtil.setTranslucent(this);//设置状态栏透明
        statusBarConfig().init();
        initView();
    }

    /**
     * 初始化沉浸式状态栏
     */
    private ImmersionBar statusBarConfig() {
        //在BaseActivity里初始化
        mImmersionBar = ImmersionBar.with(this)
                .statusBarDarkFont(statusBarDarkFont())    //默认状态栏字体颜色为黑色
                .keyboardEnable(false, WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);  //解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
        //必须设置View树布局变化监听，否则软键盘无法顶上去，还有模式必须是SOFT_INPUT_ADJUST_PAN
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

            }
        });
        return mImmersionBar;
    }

    /**
     * 获取状态栏字体颜色
     */
    public boolean statusBarDarkFont() {
        //返回false表示白色字体
        return false;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mHomemenu.setHomeLoadingFinish();
                    break;
                case 1:
                    mHomemenu.setFollowLoadingFinish();
                    break;
            }
        }
    };

    private void initView() {
        mFragments = new ArrayList<>();

        mFragments.add(VerVideoFragment.newInstance());
        mFragments.add(FollowFragment.newInstance());
        mFragments.add(MessageFragment.newInstance());
        mFragments.add(MineFragment.newInstance());

        com.blankj.utilcode.util.FragmentUtils.add(getSupportFragmentManager(), mFragments, R.id.fl_content, 0);

        mHomemenu.setOnHomeMenuClickListener(new HomeMenuLayout.OnHomeMenuItemClickListener() {
            @Override
            public void onTabSelect(int postion) {
                Log.d("mHomemenu", "onTabSelect: " + postion);
                com.blankj.utilcode.util.FragmentUtils.showHide(mFragments.get(postion), mFragments);
            }

            @Override
            public void onTabReSelect(int postion) {
                Log.d("mHomemenu", "onTabReSelect: " + postion);
                mHandler.sendEmptyMessageDelayed(postion, 3000);
            }

            @Override
            public void OnAddClick(View view) {
                RxPermissions rxPermissions = new RxPermissions(HomeActivity.this);
                rxPermissions.requestEachCombined(RequestPermissions.CAMERA)
                        .subscribe(new Consumer<Permission>() {
                            @Override
                            public void accept(Permission permission) throws Exception {
                                if (permission.granted) {
                                    //全部同意后调用
                                    Utils.navigation(HomeActivity.this, RouterHub.CAMERA_HOMEACTIVITY);
                                } else if (permission.shouldShowRequestPermissionRationale) {
                                    //只要有一个选择：禁止，但没有选择“以后不再询问”，以后申请权限，会继续弹出提示
                                    RxToast.show("您拒绝了权限，无法开启相机");
                                } else {
                                    //只要有一个选择：禁止，但选择“以后不再询问”，以后申请权限，不会继续弹出提示
                                    new AlertDialog.Builder(HomeActivity.this)
                                            .setMessage("您永久关闭了相机权限，请到设置页面手动开启")
                                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    PermissionSettingPage.start(HomeActivity.this, true);
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            }
                        });

            }
        });
    }
}