package com.zzmetro.suppliesfault.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.zzmetro.suppliesfault.util.Util;

import java.io.File;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: StartOffWorkActivity
 * 类描述: 交接班
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class StartOffWorkActivity extends BaseActivity {

	private TextView tv_start_work,tv_off_work;
	private Button startWorkSync,offWorkSync;
	private String lineCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startoff_work);

		// 获取登陆者信息
		SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
		lineCode = sp.getString("lineCode", "").trim();
		
        // 获取【接班】按钮ID
		startWorkSync = (Button) findViewById(R.id.startWorkSync);
		tv_start_work = (TextView) findViewById(R.id.tv_start_work);
		tv_start_work.setTextColor(Color.BLACK);
        // 获取【交班】按钮ID
		offWorkSync = (Button) findViewById(R.id.offWorkSync);
		tv_off_work = (TextView) findViewById(R.id.tv_off_work);
		tv_off_work.setTextColor(Color.BLACK);

		ButtonListener bl = new ButtonListener();
		startWorkSync.setOnTouchListener(bl);
		offWorkSync.setOnTouchListener(bl);

		// 接班
		startWorkSync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!Util.checkFile(getResources().getString(R.string.modulePath)) ||
						!Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
					Util.showToast(StartOffWorkActivity.this, lineCode + "号线有未交班信息！请先交班！！！");
				} else {
					checkStart();
				}
			}
		});

        // 交班
		offWorkSync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StartOffWorkActivity.this, OffworkActivity.class);
				startActivity(intent);
			}
		});

		Log.d("StartOffWorkActivity", "交接班管理");
	}

	/**
	 * check接班
	 */
	private void checkStart() {
		AlertDialog.Builder builder1 = new AlertDialog.Builder(StartOffWorkActivity.this);
		builder1.setTitle("确认");
		builder1.setMessage("开始接班，确保网络通畅，是否继续！");
		builder1.setPositiveButton("是", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 删除xml文件
				deleteXml();
				Intent intent = new Intent(StartOffWorkActivity.this, StartworkActivity.class);
				startActivity(intent);
			}

		});
		builder1.setNegativeButton("否", null);
		builder1.show();
	}

	/**
	 * 删除xml文件
	 */
	private void deleteXml() {
		// 删除设备信息
		File equipmentInfo = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/equipmentInfo.xml");
		if (equipmentInfo.exists()) {
			equipmentInfo.delete();
		}
		// 删除故障分类
		File problemClassInfo = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/problemClassInfo.xml");
		if (problemClassInfo.exists()) {
			problemClassInfo.delete();
		}
		// 删除未完成故障单
		File ticketInfo = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/ticketInfo.xml");
		if (ticketInfo.exists()) {
			ticketInfo.delete();
		}
		// 删除个人物资
		File myMaterial = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/myMaterial.xml");
		if (myMaterial.exists()) {
			myMaterial.delete();
		}
	}

	/**
	 * Button的押下、抬起特效
	 */
	class ButtonListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			// 接班
			if (view.getId() == R.id.startWorkSync) {
				// 屏幕按下
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					startWorkSync.setBackgroundResource(R.drawable.ic_start_work_press);
					tv_start_work.setTextColor(Color.rgb(189,189,189));
				}
				// 按下抬起
				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					startWorkSync.setBackgroundResource(R.drawable.ic_start_work);
					tv_start_work.setTextColor(Color.BLACK);
				}
			}
			// 交班
			if (view.getId() == R.id.offWorkSync) {
				// 屏幕按下
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					offWorkSync.setBackgroundResource(R.drawable.ic_off_work_press);
					tv_off_work.setTextColor(Color.rgb(189,189,189));
				}
				// 按下抬起
				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					offWorkSync.setBackgroundResource(R.drawable.ic_off_work);
					tv_off_work.setTextColor(Color.BLACK);
				}
			}
			return false;
		}
	}
}