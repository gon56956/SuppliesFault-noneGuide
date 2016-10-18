package com.zzmetro.suppliesfault.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: ModuleReplaceActivity
 * 类描述: 模块更换
 * 创建人: mayunpeng 创建时间: 2016/08/08 版本: [v1.0]
 */
public class ModuleReplaceActivity extends BaseActivity {

    private TextView tv_uninstall_module,tv_install_module;
    private Button uninstallModule,installModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_replace);

        // 获取【卸载】按钮ID
        uninstallModule = (Button) findViewById(R.id.uninstallModule);
        tv_uninstall_module = (TextView) findViewById(R.id.tv_uninstall_module);
        tv_uninstall_module.setTextColor(Color.BLACK);
        // 获取【安装】按钮ID
        installModule = (Button) findViewById(R.id.installModule);
        tv_install_module = (TextView) findViewById(R.id.tv_install_module);
        tv_install_module.setTextColor(Color.BLACK);

        ButtonListener bl = new ButtonListener();
        uninstallModule.setOnTouchListener(bl);
        installModule.setOnTouchListener(bl);

        // 卸载
        uninstallModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModuleReplaceActivity.this, ModuleUninstallActivity.class);
                startActivity(intent);
            }
        });

        // 安装
        installModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModuleReplaceActivity.this, ModuleInstallActivity.class);
                startActivity(intent);
            }
        });

        Log.d("ModuleReplaceActivity", "模块更换");
    }

    /**
     * Button的押下、抬起特效
     */
    class ButtonListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // 卸载
            if (view.getId() == R.id.uninstallModule) {
                // 屏幕按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    uninstallModule.setBackgroundResource(R.drawable.ic_uninstall_module_press);
                    tv_uninstall_module.setTextColor(Color.rgb(189,189,189));
                }
                // 按下抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    uninstallModule.setBackgroundResource(R.drawable.ic_uninstall_module);
                    tv_uninstall_module.setTextColor(Color.BLACK);
                }
            }
            // 安装
            if (view.getId() == R.id.installModule) {
                // 屏幕按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    installModule.setBackgroundResource(R.drawable.ic_install_module_press);
                    tv_install_module.setTextColor(Color.rgb(189,189,189));
                }
                // 按下抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    installModule.setBackgroundResource(R.drawable.ic_install_module);
                    tv_install_module.setTextColor(Color.BLACK);
                }
            }
            return false;
        }
    }
}
