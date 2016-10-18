package com.zzmetro.suppliesfault.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zzmetro.suppliesfault.util.Util;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: LoginAddressActivity
 * 类描述: 网络参数设置
 * 创建人: mayunpeng 创建时间: 2016/08/14 版本: [v1.0]
 */
public class LoginAddressActivity extends BaseActivity {

    private EditText ip;
    private EditText port;
    private EditText lineCode;
    private Button setNetWork,usePublickNetWork,useProtectNetWork;
    private String ipInfo = "",portInfo = "",lineInfo = "",staffNum = "",staffNam = "",pdaID = "", oldLineCode = "";
    private String falg;
    private boolean publicFalg = true;

    public static void actionStart(Context context, String falg) {
        Intent intent = new Intent(context, LoginAddressActivity.class);
        intent.putExtra("falg", falg);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_address);

        // 获取注册控件
        ip = (EditText) findViewById(R.id.ip);
        port = (EditText) findViewById(R.id.port);
        lineCode = (EditText) findViewById(R.id.lineCode);
        // 获取【确定】按钮ID
        setNetWork = (Button) findViewById(R.id.setNetWork);
        usePublickNetWork = (Button) findViewById(R.id.usePublickNetWork);
        useProtectNetWork = (Button) findViewById(R.id.useProtectNetWork);

        // 获取控件状态信息
        Intent intent = getIntent();
        falg = intent.getStringExtra("falg");

        if ("false".equals(falg)) {
            ip.setEnabled(false);
            port.setEnabled(false);
            lineCode.setEnabled(false);
        } else {
            ip.setEnabled(false);
            port.setEnabled(false);
            lineCode.setEnabled(true);
            usePublickNetWork.setEnabled(false);
        }

        if (!Util.checkFile(getResources().getString(R.string.staffInfoPath))) {
            // 获取参数信息
            SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
            ip.setText(sp.getString("ip", "").trim());
            port.setText(sp.getString("port", "").trim());
            lineCode.setText(sp.getString("lineCode", "").trim());
            staffNum = sp.getString("staffNumber", "").trim();
            staffNam = sp.getString("staffName", "").trim();
            pdaID = sp.getString("pdaID", "").trim();
            oldLineCode = sp.getString("lineCode", "").trim();
        } else {
            // 定义初始网络配置
            ip.setText("www.afchome.cn");
            port.setText("80");
            lineCode.setText("1");
        }

        usePublickNetWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usePublickNetWork.setEnabled(false);
                useProtectNetWork.setEnabled(true);
                ip.setEnabled(false);
                port.setEnabled(false);
                // 定义初始网络配置
                ip.setText("www.afchome.cn");
                port.setText("80");
                lineCode.setText(lineCode.getText().toString().trim());
                publicFalg = true;
            }
        });

        useProtectNetWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usePublickNetWork.setEnabled(true);
                useProtectNetWork.setEnabled(false);
                ip.setEnabled(true);
                port.setEnabled(true);
                // 定义初始网络配置
                ip.setText("10.202.205.21");
                port.setText("8080");
                lineCode.setText(lineCode.getText().toString().trim());
                publicFalg = false;
            }
        });

        setNetWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取IP及端口号信息
                ipInfo = ip.getText().toString().trim();
                portInfo = port.getText().toString().trim();
                lineInfo = lineCode.getText().toString().trim();
                if (publicFalg) {
                    // 判断线路的合法性
                    if (TextUtils.isEmpty(lineInfo)) {
                        // 给出错误提示
                        CharSequence html = Html.fromHtml("<font color='red'>线路不能为空</font>");
                        lineCode.setError(html);
                        lineCode.requestFocus();
                    } else {
                        if ("0".equals(lineInfo)) {
                            // 给出错误提示
                            CharSequence html = Html.fromHtml("<font color='red'>输入线路不正确</font>");
                            lineCode.setError(html);
                            lineCode.requestFocus();
                        } else {
                            updataNetWork(lineInfo);

                            Intent intent = new Intent();
                            intent.putExtra("ip", ipInfo);
                            intent.putExtra("port", portInfo);
                            intent.putExtra("lineCode", lineInfo);
                            /*
                             * 调用setResult方法表示我将Intent对象返回给之前的那个Activity，
                             * 这样就可以在onActivityResult方法中得到Intent对象，
                             */
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                } else {
                    if (!Util.checkFile(getResources().getString(R.string.modulePath)) ||
                            !Util.checkFile(getResources().getString(R.string.ticketPath)) ||
                            !Util.checkFile(getResources().getString(R.string.materialPath)) ||
                            !Util.checkFile(getResources().getString(R.string.equipmentPath)) ||
                            !Util.checkFile(getResources().getString(R.string.problemClassPath)) ||
                            !Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
                        if (checkNetWork(ipInfo, portInfo, lineInfo)) {
                            if (lineInfo.equals(oldLineCode)) {
                                updataNetWork(lineInfo);
                                finish();
                            } else {
                                updataNetWork(oldLineCode);
                                Util.showToast(LoginAddressActivity.this, "更改线路前！请先交班！！！");
                                Intent intent = new Intent(LoginAddressActivity.this, OffworkActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    } else {
                        if (checkNetWork(ipInfo, portInfo, lineInfo)) {
                            updataNetWork(lineInfo);

                            Intent intent = new Intent();
                            intent.putExtra("ip", ipInfo);
                            intent.putExtra("port", portInfo);
                            intent.putExtra("lineCode", lineInfo);
                            /*
                             * 调用setResult方法表示我将Intent对象返回给之前的那个Activity，
                             * 这样就可以在onActivityResult方法中得到Intent对象，
                             */
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
            }
        });

        Log.d("LoginAddressActivity", "网络设置");
    }

    /**
     * check最新网络配置
     * @param ipInfo
     * @param portInfo
     * @param lineInfo
     *
     * @return
     */
    public Boolean checkNetWork(String ipInfo, String portInfo, String lineInfo) {
        // 判断IP的合法性
        if (TextUtils.isEmpty(ipInfo)) {
            // 给出错误提示
            CharSequence html = Html.fromHtml("<font color='red'>服务器地址不能为空</font>");
            ip.setError(html);
            ip.requestFocus();
            return false;
        } else {
            if (!Util.isIPAddress(ipInfo)) {
                // 给出错误提示
                CharSequence html = Html.fromHtml("<font color='red'>输入服务器地址不正确</font>");
                ip.setError(html);
                ip.requestFocus();
                return false;
            }
        }
        // 判断端口号的合法性
        if (TextUtils.isEmpty(portInfo)) {
            // 给出错误提示
            CharSequence html = Html.fromHtml("<font color='red'>服务器端口不能为空</font>");
            port.setError(html);
            port.requestFocus();
            return false;
        } else {
            if (portInfo.length() != 4) {
                // 给出错误提示
                CharSequence html = Html.fromHtml("<font color='red'>输入端口位数不正确</font>");
                port.setError(html);
                port.requestFocus();
                return false;
            }
        }
        // 判断线路的合法性
        if (TextUtils.isEmpty(lineInfo)) {
            // 给出错误提示
            CharSequence html = Html.fromHtml("<font color='red'>线路不能为空</font>");
            lineCode.setError(html);
            lineCode.requestFocus();
            return false;
        } else {
            if ("0".equals(lineInfo)) {
                // 给出错误提示
                CharSequence html = Html.fromHtml("<font color='red'>输入线路不正确</font>");
                lineCode.setError(html);
                lineCode.requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * 记录最新网络配置
     * @param lingCode
     */
    public void updataNetWork(String lingCode) {
        //获取SharedPreferences对象
        Context ctx = LoginAddressActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("staffInfo", MODE_PRIVATE);
        if (!Util.checkFile(getResources().getString(R.string.staffInfoPath))) {
            sp.edit().clear().commit();
        }
        //存入数据
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ip", ipInfo);
        editor.putString("port", portInfo);
        editor.putString("lineCode", lingCode);
        editor.putString("staffNumber", staffNum);
        editor.putString("staffName", staffNam);
        editor.putString("pdaID", pdaID);
        editor.commit();
    }

    /**
     * 屏蔽物理返回键
     * @param keyCode
     * @param event
     *
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
