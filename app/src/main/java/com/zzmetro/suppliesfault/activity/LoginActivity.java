package com.zzmetro.suppliesfault.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zzmetro.suppliesfault.service.WebServiceBase;
import com.zzmetro.suppliesfault.util.CTelephoneInfo;
import com.zzmetro.suppliesfault.util.Util;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: LoginActivity
 * 类描述: 登录
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class LoginActivity extends BaseActivity {
	
	private long mExiteTime;
	private Handler handler;
	private EditText staffNum;
	private EditText staffNam;
    private String mStaffNumber = "";
    private String mStaffName = "";
    private String mPDAID = "",mPDAID1,mPDAID2;
    private String registerInfo = "";
    private String result = "";
    private ProgressDialog pd;
    private Button register,setNetWork;
    private String ip = "",port = "",lineCode = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

        CTelephoneInfo cTelephoneInfo = new CTelephoneInfo();

        // 获取控件
        staffNum = (EditText) findViewById(R.id.staffNumber);
        staffNam = (EditText) findViewById(R.id.staffName);
        register = (Button) findViewById(R.id.register);
        setNetWork = (Button) findViewById(R.id.setNetWork);

        // 注册非活性
        register.setEnabled(false);

        // 获取参数信息
        setNetWork.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, LoginAddressActivity.class);
                /*
                 * 如果希望启动另一个Activity，并且希望有返回值，则需要使用startActivityForResult这个方法，
                 * 第一个参数是Intent对象，第二个参数是一个requestCode值，如果有多个按钮都要启动Activity，
                 * 则requestCode标志着每个按钮所启动的Activity
                 */
                startActivityForResult(intent, 0);
            }
        });

        // 获取登录者信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        String imei = sp.getString("pdaID", "").trim();

        // 获取手机IMEI号
        TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        mPDAID = TelephonyMgr.getDeviceId();
        mPDAID1 = cTelephoneInfo.getImeiSIM1();
        mPDAID2 = cTelephoneInfo.getImeiSIM2();
//        mPDAID = "866769029310789";
//        mPDAID = "869894026915583";

        // 首次登录判断
        if(imei.equals(mPDAID)) {
            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
            startActivity(intent);
        } else {
            // 注册
            register.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 获取注册信息
                    mStaffNumber = staffNum.getText().toString().trim();
                    mStaffName = staffNam.getText().toString().trim();
                    registerInfo = mStaffNumber + "," + mPDAID + ",PDARegister";

                    // 员工号／姓名输入判断
                    if (TextUtils.isEmpty(mStaffNumber)) {
                    	// 给出错误提示 
                    	CharSequence html = Html.fromHtml("<font color='red'>员工号不能为空</font>");
                    	staffNum.setError(html);
                    	staffNum.requestFocus();
                    } else if (TextUtils.isEmpty(mStaffName)) {
                    	// 给出错误提示 
                    	CharSequence html = Html.fromHtml("<font color='red'>姓名不能为空</font>");
                    	staffNam.setError(html);
                    	staffNam.requestFocus();
                    } else {
                        if (mStaffNumber.length() < 6) {
                        	// 给出错误提示
                        	CharSequence html = Html.fromHtml("<font color='red'>输入六位员工号</font>");
                        	staffNum.setError(html);
                        	staffNum.requestFocus();
                        } else {
                            // 显示ProgressDialog
                            pd = ProgressDialog.show(LoginActivity.this, null, "正在注册");
                            //调用接口注册 
                        	new Thread(new Runnable() {
                                public void run() {
                                	result = WebServiceBase.getWebServiceReslut(registerInfo, ip, port, lineCode);
                                	handler.sendEmptyMessage(0);
                                }
                            }).start();
                        }
                    }
                }
            });
            
            handler = new Handler() {
				@Override  
        	    public void handleMessage(Message msg) {
        	        super.handleMessage(msg);
                    pd.dismiss();
        	        // 对WebService返回的结果的判断处理 
        	        if (Util.ERR_FAIL.equals(result)){
                        checkRegister("重新注册！");
                    } else if (Util.ERR_TERMINAL.equals(result)){
                        checkRegister("非法终端，请联系管理员！");
                    } else if (Util.ERR_SYSTEM.equals(result)){
                        checkRegister("服务器内部错误，请联系管理员！");
                    } else if (Util.ERR_DB.equals(result)){
                        checkRegister("数据库错误，请联系管理员！");
                    } else if (Util.UNLAWFULUSER.equals(result)){
                        checkRegister("非法用户，不能注册！");
                    } else if (Util.LINEERROR.equals(result)){
                        checkRegister("线路不存在或暂未开通，请确认！");
                    } else if (Util.NETERROR.equals(result)){
                        checkRegister("网络连接异常，请检查网络是否通畅！");
                    } else if (Util.SUCESS.equals(result)) {
                        //获取SharedPreferences对象
                        Context ctx = LoginActivity.this;
                        SharedPreferences sp = ctx.getSharedPreferences("staffInfo", MODE_PRIVATE);
                        if (!Util.checkFile(getResources().getString(R.string.staffInfoPath))) {
                            sp.edit().clear().commit();
                        }
                        //存入数据
                        Editor editor = sp.edit();
                        editor.putString("ip", ip);
                        editor.putString("port", port);
                        editor.putString("lineCode", lineCode);
                        editor.putString("staffNumber", mStaffNumber);
                        editor.putString("staffName", mStaffName);
                        editor.putString("pdaID", mPDAID);
                        editor.commit();
                        Util.showToast(LoginActivity.this, "注册成功");
                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(intent);
                    } else {
                        checkRegister("其它未知错误，请联系管理员！\n" + result);
                    }
                }
        	};
        }

		Log.d("LoginActivity", "注册");
	}

    /**
     * check注册
     */
    private void checkRegister(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
        builder1.setTitle("注册失败");
        builder1.setMessage(message);
        builder1.setPositiveButton("确定", null);
        builder1.show();
    }

    /**
     * 所有的Activity对象的返回值都是由这个方法来接收 requestCode:
     * 表示的是启动一个Activity时传过去的requestCode值 resultCode:
     * 表示的是启动后的Activity回传值时的resultCode值 data: 表示的是启动后的Activity回传过来的Intent对象
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            Bundle bundle = data.getExtras();
            if (requestCode == 0 && resultCode == RESULT_OK) {
                ip = bundle.getString("ip");
                port = bundle.getString("port");
                lineCode = bundle.getString("lineCode");
            }
            //注册活性
            register.setEnabled(true);
        }
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExiteTime) > 2000) {
                Util.showToast(this, "再按一次退出程序");
                mExiteTime = System.currentTimeMillis();
            } else {
                ActivityCollector.finishAll();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
