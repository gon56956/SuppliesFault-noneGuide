package com.zzmetro.suppliesfault.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzmetro.suppliesfault.service.WebServiceBase;
import com.zzmetro.suppliesfault.util.Util;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: LogoutActivity
 * 类描述: 注销
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class LogoutActivity extends BaseActivity {
	
	private Handler handler;
    private Button logout;
	private String canaelledInfo = "";
	private String result = "";
    private String staffNumber;
    private String staffName;
    private String lineCode;
    private String pdaID;
    private String port;
    private String ip;
    private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        // 获取登陆者信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        staffNumber = sp.getString("staffNumber", "").trim();
        staffName = sp.getString("staffName", "").trim();
        lineCode = sp.getString("lineCode", "").trim();
        pdaID = sp.getString("pdaID", "").trim();
        port = sp.getString("port", "").trim();
        ip = sp.getString("ip", "").trim();

        TextView tvStaffNumber = (TextView) findViewById(R.id.staffNumber);
        tvStaffNumber.setText(staffNumber);
        TextView tvStaffName = (TextView) findViewById(R.id.staffName);
        tvStaffName.setText(staffName);
        TextView line = (TextView) findViewById(R.id.line);
        line.setText(lineCode + "号线");

        // 获取控件
        logout = (Button) findViewById(R.id.logout);

        canaelledInfo = staffNumber + "," + pdaID + ",PDACancelledRegister";
        		
        // 注销
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示ProgressDialog
                pd = ProgressDialog.show(LogoutActivity.this, null, "正在注销");
                //调用接口注销
                new Thread(new Runnable() {
                    public void run() {
                        result = WebServiceBase.getWebServiceReslut(canaelledInfo, ip, port, lineCode);
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        });
        
        handler = new Handler() {
			@Override  
    	    public void handleMessage(Message msg) {
    	        super.handleMessage(msg);
                pd.dismiss();
    	        // 对WebService返回的结果的判断处理
    	        if (Util.ERR_FAIL.equals(result)){
                    Util.showToast(LogoutActivity.this, "注销失败，重新注销！");
                } else if (Util.ERR_TERMINAL.equals(result)){
                    Util.showToast(LogoutActivity.this, "非法终端，请联系管理员！");
                } else if (Util.UNLAWFULUSER.equals(result)){
                    Util.showToast(LogoutActivity.this, "非法用户，请联系管理员！");
                } else if (Util.ERR_SYSTEM.equals(result)){
                    Util.showToast(LogoutActivity.this, "服务器内部错误，请联系管理员！");
                } else if (Util.ERR_DB.equals(result)){
                    Util.showToast(LogoutActivity.this, "数据库错误，请联系管理员！");
                } else if (Util.NETERROR.equals(result)){
                    Util.showToast(LogoutActivity.this, "网络连接异常，请检查网络是否通畅！");
                } else if (Util.SUCESS.equals(result)) {
                    //获取SharedPreferences对象
                    SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
                    sp.edit().clear().commit();
                    //存入数据
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("ip", ip);
                    editor.putString("port", port);
                    editor.putString("lineCode", lineCode);
                    editor.commit();
                    Util.showToast(LogoutActivity.this, "注销成功");
                    // 删除xml文件
                    Util.deleteXml();
                    Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Util.showToast(LogoutActivity.this, "注销失败\n" + result);
                }
            }
    	};

		Log.d("LogoutActivity", "注销");
	}
}