package com.zzmetro.suppliesfault.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.zzmetro.suppliesfault.model.SparepartList;
import com.zzmetro.suppliesfault.model.SpinnerArea;
import com.zzmetro.suppliesfault.util.Util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: UseSparepartActivity
 * 类描述: 消耗备件
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class UseSparepartActivity extends BaseActivity {

    private UseSparepartAdapter adapter;
    private List<SparepartList> sparepartList = new ArrayList<SparepartList>();
	private List<SpinnerArea> myMaterialList = new ArrayList<SpinnerArea>();
    private ArrayList<String> useSparepartList = new ArrayList<String>();
	private Button certain;
	private ListView listView;
    private String result = "";
    private ProgressDialog pd;
    private Handler handler;
    private int emptyCount = 0;
    private boolean sparepartFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sparepart_use);

        // 初始化物资数据
        adapter = new UseSparepartAdapter(UseSparepartActivity.this, sparepartList);
        listView = (ListView) findViewById(R.id.sparepartList);
        listView.setAdapter(adapter);

		// 加载物资数据
        // 显示ProgressDialog
        pd = ProgressDialog.show(UseSparepartActivity.this, "物资备件", "数据加载中，请稍候！！！");

        // 开启一个新线程，在新线程里执行耗时的方法
        new Thread(new Runnable() {
            public void run() {
                result = xPathParseMyMaterialXml();
                handler.sendEmptyMessage(0);
            }
        }).start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                pd.dismiss();
                adapter.notifyDataSetChanged();
                if ("1".equals(result)) {
                    Util.showToast(UseSparepartActivity.this, "数据加载成功");
                } else {
                    Util.showToast(UseSparepartActivity.this, "数据加载失败");
                }
            }
        };

		// 确定
		certain = (Button) findViewById(R.id.certain);
		certain.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (adapter.hashMap.size() == 0) {
                    checkUseSparepart();
				} else {
                    Boolean result = true;
                    Intent intent = new Intent();
					String sparepartInfo = "";
                    boolean error = false;
					for (int i = 0; i < sparepartList.size(); i++) {
                        String str = adapter.hashMap.get(i);

                        if (!"".equals(str) && str != null) {
                            // 获得子item的layout
                            LinearLayout layout = (LinearLayout) listView.getChildAt(i);
                            // 从layout中获得控件,根据其id
                            EditText et = (EditText) layout.findViewById(R.id.useNumber);
                            if (Integer.parseInt(str) > Integer.parseInt(sparepartList.get(i).getNumber())) {
                                // 给出错误提示
                                CharSequence html = Html.fromHtml("<font color='red'>消耗大于持有量</font>");
                                et.setError(html);
                                et.requestFocus();
                                error = true;
                                break;
                            } else if (Integer.parseInt(str) == 0){
                                // 给出错误提示
                                CharSequence html = Html.fromHtml("<font color='red'>消耗不能未0</font>");
                                et.setError(html);
                                et.requestFocus();
                                emptyCount = 0;
                                error = true;
                                break;
                            } else {
                                sparepartInfo += "物资:" + sparepartList.get(i).getSparepartInfo() + "    消耗:" + Integer.parseInt(str) + "\n";
                                useSparepartList.add(myMaterialList.get(i).getDomaincode() + "," + Integer.parseInt(str));
                                intent.putExtra("result", result);
                                intent.putExtra("useSparepartinfo", sparepartInfo);
                                intent.putStringArrayListExtra("useSparepartData", useSparepartList);
                            }
                        } else if ("".equals(str)) {
                            emptyCount++;
                        }
                    }

                    if (error) {} else {
                        if (emptyCount == adapter.hashMap.size()) {
                            emptyCount = 0;
                            checkUseSparepart();
                        } else {
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

		Log.d("UseSparepartActivity", "消耗备件 execute");
	}

    /**
     * 获取获取个人物资
     */
    private String xPathParseMyMaterialXml() {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.materialPath)));
            NodeList materialList = (NodeList)xPath.evaluate("/root/Material", inputSource, XPathConstants.NODESET);
            if (materialList != null && materialList.getLength() > 0) {
                for (int i = 0; i < materialList.getLength(); i++) {
                    Element element = (Element)materialList.item(i);
                    String key = element.getAttribute("ID");
                    String value = element.getAttribute("Name");
                    String amount = element.getAttribute("Amount");
                    if (Integer.parseInt(amount) > 0) {
                        myMaterialList.add(new SpinnerArea(key, value));
                        sparepartList.add(new SparepartList(myMaterialList.get(i).getDomainname(), amount));
                        sparepartFlag = true;
                    }
                }
                if (!sparepartFlag) {
                    Util.showToast(UseSparepartActivity.this, "***物资已消耗完***\n请使用PC端系统借用物资");
                }
            } else {
                Util.showToast(UseSparepartActivity.this, "请使用PC端系统领取个人物资");
			}
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "1";
    }

    /**
     * check物资消耗量
     */
    private void checkUseSparepart() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(UseSparepartActivity.this);
        builder1.setTitle("确认");
        builder1.setMessage("没有备件消耗,是否返回！");
        builder1.setPositiveButton("是", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Boolean result = false;
                Intent intent = new Intent();
                intent.putExtra("result", result);
                /*
                 * 调用setResult方法表示我将Intent对象返回给之前的那个Activity，
                 * 这样就可以在onActivityResult方法中得到Intent对象，
                 */
                setResult(RESULT_CANCELED, intent);
                finish();
            }

        });
        builder1.setNegativeButton("否", null);
        builder1.show();
    }

	/**
	 * 屏蔽物理返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

}