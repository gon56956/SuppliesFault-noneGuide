package com.zzmetro.suppliesfault.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.zzmetro.suppliesfault.util.Util;

import java.lang.reflect.Method;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: MenuActivity
 * 类描述: 主菜单
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class MenuActivity extends AppCompatActivity {

    private long mExiteTime;
    private String lineCode;
    private final static int  FEATUREID = 8;
    private TextView tv_start_off,tv_ma_fault,tv_creat_fault,tv_module_replace,tv_dispose,tv_more;
    private Button myFaultList,createFaultList,dataManage,safeRecord,module,more;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // tollBar设置
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        onMenuOpened(FEATUREID, toolbar.getMenu());

        // 获取参数信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        lineCode = sp.getString("lineCode", "").trim();

        // 获取【我的故障工单】按钮ID
        myFaultList = (Button) findViewById(R.id.myFaultList);
        tv_ma_fault = (TextView) findViewById(R.id.tv_ma_fault);
        // 获取【新建故障工单】按钮ID
        createFaultList = (Button) findViewById(R.id.createFaultList);
        tv_creat_fault = (TextView) findViewById(R.id.tv_creat_fault);
        // 获取【交接班】按钮ID
        dataManage = (Button) findViewById(R.id.dataManage);
        tv_start_off = (TextView) findViewById(R.id.tv_start_off);
        tv_start_off.setTextColor(Color.BLACK);
        // 获取【维修记录】按钮ID
        safeRecord = (Button) findViewById(R.id.safeRecord);
        tv_dispose = (TextView) findViewById(R.id.tv_dispose);
        // 获取【模块更换】按钮ID
        module = (Button) findViewById(R.id.module);
        tv_module_replace = (TextView) findViewById(R.id.tv_module_replace);
        // 我的物资
        more = (Button) findViewById(R.id.more);
        tv_more = (TextView) findViewById(R.id.tv_more);

        ButtonListener bl = new ButtonListener();
        dataManage.setOnTouchListener(bl);
        myFaultList.setOnTouchListener(bl);
        createFaultList.setOnTouchListener(bl);
        safeRecord.setOnTouchListener(bl);
        module.setOnTouchListener(bl);
        more.setOnTouchListener(bl);

        //控件状态
        if (Util.checkFile(getResources().getString(R.string.problemClassPath)) ||
                Util.checkFile(getResources().getString(R.string.ticketPath)) ||
                Util.checkFile(getResources().getString(R.string.materialPath)) ||
                Util.checkFile(getResources().getString(R.string.equipmentPath))) {
            // ［我的故障工单,新建故障工单,维修记录,模块更换］均处于非活性状态
            myFaultList.setEnabled(false);
            createFaultList.setEnabled(false);
            safeRecord.setEnabled(false);
            module.setEnabled(false);
            more.setEnabled(false);
            myFaultList.setBackgroundResource(R.drawable.ic_my_fault_disabled);
            createFaultList.setBackgroundResource(R.drawable.ic_create_fault_disabled);
            safeRecord.setBackgroundResource(R.drawable.ic_maintain_list_disabled);
            module.setBackgroundResource(R.drawable.ic_module_replace_disabled);
            more.setBackgroundResource(R.drawable.ic_more_press);
            tv_ma_fault.setTextColor(Color.rgb(189,189,189));
            tv_creat_fault.setTextColor(Color.rgb(189,189,189));
            tv_dispose.setTextColor(Color.rgb(189,189,189));
            tv_module_replace.setTextColor(Color.rgb(189,189,189));
            tv_more.setTextColor(Color.rgb(189,189,189));
        } else {
            myFaultList.setEnabled(true);
            createFaultList.setEnabled(true);
            safeRecord.setEnabled(true);
            module.setEnabled(true);
            more.setEnabled(true);
            tv_dispose.setTextColor(Color.BLACK);
            tv_ma_fault.setTextColor(Color.BLACK);
            tv_creat_fault.setTextColor(Color.BLACK);
            tv_module_replace.setTextColor(Color.BLACK);
            tv_more.setTextColor(Color.BLACK);
        }

        // 交接班
        dataManage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, StartOffWorkActivity.class);
                startActivity(intent);
            }
        });

        // 我的故障单
        myFaultList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, MyFaultListActivity.class);
                startActivity(intent);
            }
        });
        // 新建故障单
        createFaultList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, CreateFaultListActivity.class);
                startActivity(intent);
            }
        });
        // 模块更换
        module.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ModuleReplaceActivity.class);
                startActivity(intent);
            }
        });
        // 维修记录
        safeRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MaintainListActivity.class);
                startActivity(intent);
            }
        });
        // 我的物资
        more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MySparepartActivity.class);
                startActivity(intent);
            }
        });

        Log.d("MenuActivity", "功能菜单");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // 网络设置
            LoginAddressActivity.actionStart(MenuActivity.this, "false");
            return true;
        } else if (id == R.id.deviceManage) {
            // 注销
            if (!Util.checkFile(getResources().getString(R.string.modulePath)) ||
                    !Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
                Util.showToast(MenuActivity.this, lineCode + "号线有未交班信息！请先交班！！！");
                Intent intent = new Intent(MenuActivity.this, OffworkActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MenuActivity.this, LogoutActivity.class);
                startActivity(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Button的押下、抬起特效
     */
    class ButtonListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // 交接班
            if (view.getId() == R.id.dataManage) {
                // 屏幕按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    dataManage.setBackgroundResource(R.drawable.ic_start_off_work_press);
                    tv_start_off.setTextColor(Color.rgb(189,189,189));
                }
                // 按下抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    dataManage.setBackgroundResource(R.drawable.ic_start_off_work);
                    tv_start_off.setTextColor(Color.BLACK);
                }
            }
            // 我的故障单
            if (view.getId() == R.id.myFaultList) {
                // 屏幕按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    myFaultList.setBackgroundResource(R.drawable.ic_my_fault_press);
                    tv_ma_fault.setTextColor(Color.rgb(189,189,189));
                }
                // 按下抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    myFaultList.setBackgroundResource(R.drawable.ic_my_fault);
                    tv_ma_fault.setTextColor(Color.BLACK);
                }
            }
            // 新建故障单
            if (view.getId() == R.id.createFaultList) {
                // 屏幕按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    createFaultList.setBackgroundResource(R.drawable.ic_create_fault_press);
                    tv_creat_fault.setTextColor(Color.rgb(189,189,189));
                }
                // 按下抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    createFaultList.setBackgroundResource(R.drawable.ic_create_fault);
                    tv_creat_fault.setTextColor(Color.BLACK);
                }
            }
            // 模块更换
            if (view.getId() == R.id.module) {
                // 屏幕按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    module.setBackgroundResource(R.drawable.ic_module_replace_press);
                    tv_module_replace.setTextColor(Color.rgb(189,189,189));
                }
                // 按下抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    module.setBackgroundResource(R.drawable.ic_module_replace);
                    tv_module_replace.setTextColor(Color.BLACK);
                }
            }
            // 维修记录
            if (view.getId() == R.id.safeRecord) {
                // 屏幕按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    safeRecord.setBackgroundResource(R.drawable.ic_maintain_list_press);
                    tv_dispose.setTextColor(Color.rgb(189,189,189));
                }
                // 按下抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    safeRecord.setBackgroundResource(R.drawable.ic_maintain_list);
                    tv_dispose.setTextColor(Color.BLACK);
                }
            }
            // 更多
            if (view.getId() == R.id.more) {
                // 屏幕按下
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    more.setBackgroundResource(R.drawable.ic_more_press);
                    tv_more.setTextColor(Color.rgb(189,189,189));
                }
                // 按下抬起
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    more.setBackgroundResource(R.drawable.ic_more);
                    tv_more.setTextColor(Color.BLACK);
                }
            }
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if ((System.currentTimeMillis() - mExiteTime) > 2000) {
//                Util.showToast(this, "再按一次退出程序");
//                mExiteTime = System.currentTimeMillis();
//            } else {
//                ActivityCollector.finishAll();
//                android.os.Process.killProcess(android.os.Process.myPid());
//            }
            ActivityCollector.finishAll();
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}