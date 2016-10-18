package com.zzmetro.suppliesfault.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: MyFaultListActivity
 * 类描述: 我的故障单
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class MyFaultListActivity extends BaseActivity {

    private ArrayAdapter<SpinnerArea> myfaultAdapter = null;
    private List<SpinnerArea> myfaultList = new ArrayList<SpinnerArea>();
    private String result = "";
    private String str = "";
    private int newTicket = 0;
    private int oldTicket = 0;
    private ProgressDialog pd;
    private Handler handler;
    private long mExiteTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myfault_list);

		// 初始化故障单数据
        myfaultAdapter = new ArrayAdapter<SpinnerArea>(this, android.R.layout.simple_list_item_1, myfaultList);
        ListView listView = (ListView) findViewById(R.id.myFaultList);
        listView.setAdapter(myfaultAdapter);

        // 加载故障单数据
        // 显示ProgressDialog
        pd = ProgressDialog.show(MyFaultListActivity.this, "我的故障单", "数据加载中，请稍候！！！");

        // 开启一个新线程，在新线程里执行耗时的方法
        new Thread(new Runnable() {
            public void run() {
                result = xPathParseUploadTroubleTicketsXml();
                handler.sendEmptyMessage(0);
            }
        }).start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                pd.dismiss();
                myfaultAdapter.notifyDataSetChanged();
                if ("1".equals(result)) {
                    Util.showToast(MyFaultListActivity.this, "数据加载成功");
                } else if ("没有故障单".equals(result)){
                    Util.showToast(MyFaultListActivity.this, "＊＊＊没有故障单＊＊＊");
                } else {
                    Util.showToast(MyFaultListActivity.this, "数据加载失败");
                }
            }
        };

		//ListView点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String solutionFaultID = myfaultList.get(position).getDomaincode();
                DisposeFaultActivity.actionStart(MyFaultListActivity.this, solutionFaultID);
                finish();
            }
        });

		Log.d("MyFaultListActivity", "我的故障单");
	}

    /**
     * 获取我的故障单信息
     */
    private String xPathParseUploadTroubleTicketsXml() {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            if (!Util.checkFile(getResources().getString(R.string.ticketPath))) {
                InputSource inputSourceTicket = new InputSource(new FileInputStream(getResources().getString(R.string.ticketPath)));
                NodeList oldTicketList = (NodeList)xPath.evaluate("/root/Ticket", inputSourceTicket, XPathConstants.NODESET);
                if (oldTicketList != null && oldTicketList.getLength() > 0) {
                    myfaultList.clear();
                    for (int i = 0; i < oldTicketList.getLength(); i++) {
                        Element element = (Element)oldTicketList.item(i);
                        // 转接故障单
                        if ("2".equals(element.getAttribute("Status"))) {
                            String key = element.getAttribute("ID");
                            String value = xPathequipmentInfoXml(element.getAttribute("Equipment"));
                            // 故障类型
                            String value1 = xPathProblemClassInfoXml(element.getAttribute("Problem"));
                            myfaultList.add(new SpinnerArea(key, value + "★" + value1));
                            oldTicket++;
                        }
                        // 新建故障单
                        else if ("1".equals(element.getAttribute("Status"))) {
                            String key = element.getAttribute("ID");
                            String value = xPathequipmentInfoXml(element.getAttribute("Equipment"));
                            // 故障类型
                            String value1 = xPathProblemClassInfoXml(element.getAttribute("Problem"));
                            myfaultList.add(new SpinnerArea(key, value + "★" + value1));
                            oldTicket++;
                        }
                    }
                }
            }

            if (!Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
                InputSource inputSourceUploadTroubleTickets = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
                NodeList newTicketList = (NodeList)xPath.evaluate("/root/NewTicket/Ticket", inputSourceUploadTroubleTickets, XPathConstants.NODESET);
                if (newTicketList != null && newTicketList.getLength() > 0) {
                    for (int i = 0; i < newTicketList.getLength(); i++) {
                        Element element = (Element)newTicketList.item(i);
                        if ("1".equals(element.getAttribute("Status"))) {
                            String key = element.getAttribute("Downtime");
                            // 故障地址
                            String value = xPathequipmentInfoXml(element.getAttribute("Equipment"));
                            // 故障类型
                            String value1 = xPathProblemClassInfoXml(element.getAttribute("Problem"));
                            myfaultList.add(new SpinnerArea(key, value + "★" + value1));
                            newTicket++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

        // 判断有无记录
        if (newTicket > 0 || oldTicket > 0) {
            str = "1";
        } else {
            str = "没有故障单";
        }

        return str;
    }

    /**
     * 获取设备名称
     *
     * @param equipmentCode 设备code
     */
    private String xPathequipmentInfoXml(String equipmentCode) {
        String value = "";
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSourceType = new InputSource(new FileInputStream(getResources().getString(R.string.equipmentPath)));
            // 获取故障地点
            NodeList equipmentList = (NodeList)xPath.evaluate("/root/EquipmentType/Equipment[@Code='" + equipmentCode + "']", inputSourceType, XPathConstants.NODESET);
            if (equipmentList != null && equipmentList.getLength() > 0) {
                for (int i = 0; i < equipmentList.getLength(); i++) {
                    Element element = (Element)equipmentList.item(i);
                    // 故障地点
                    value = element.getAttribute("Location");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 获取故障类型
     *
     * @param problemCode 故障描述code
     */
    private String xPathProblemClassInfoXml(String problemCode) {
        String value = "";
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSourceType = new InputSource(new FileInputStream(getResources().getString(R.string.problemClassPath)));
            // 获取故障类型
            NodeList problemClassList = (NodeList)xPath.evaluate("/root/EquipmentType/ProblemClass[@ID='" + problemCode.substring(0,4) + "']", inputSourceType, XPathConstants.NODESET);
            if (problemClassList != null && problemClassList.getLength() > 0) {
                for (int i = 0; i < problemClassList.getLength(); i++) {
                    Element element = (Element)problemClassList.item(i);
                    // 故障类型
                    value = element.getAttribute("Name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExiteTime) > 2000) {
                Util.showToast(this, "再按一次返回");
                mExiteTime = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(MyFaultListActivity.this, MenuActivity.class);
                startActivity(intent);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
