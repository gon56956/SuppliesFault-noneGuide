package com.zzmetro.suppliesfault.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: MaintainListActivity
 * 类描述: 维护记录
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class MaintainListActivity extends BaseActivity {
	
    private ArrayAdapter<SpinnerArea> maintainListAdapter = null;
    private List<SpinnerArea> maintainList = new ArrayList<SpinnerArea>();
    private String result = "";
    private String str = "";
    private int newTicket = 0;
    private int oldTicket = 0;
    private ProgressDialog pd;
    private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maintain_list);

        maintainListAdapter = new ArrayAdapter<SpinnerArea>(this, android.R.layout.simple_list_item_1, maintainList);
        ListView listView = (ListView)findViewById(R.id.maintainList);
        listView.setAdapter(maintainListAdapter);

        //初始化故障单数据
        // 显示ProgressDialog
        pd = ProgressDialog.show(MaintainListActivity.this, "维修记录", "数据加载中，请稍候！！！");

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
                maintainListAdapter.notifyDataSetChanged();
                if ("1".equals(result)) {
                    Util.showToast(MaintainListActivity.this, "数据加载成功");
                } else if ("没有维修记录".equals(result)){
                    Util.showToast(MaintainListActivity.this, "＊＊＊没有维修记录＊＊＊");
                } else {
                    Util.showToast(MaintainListActivity.this, "数据加载失败");
                }
            }
        };

        //ListView点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String solutionFaultID = maintainList.get(position).getDomaincode();
                MaintainFaultActivity.actionStart(MaintainListActivity.this, solutionFaultID);
                finish();
            }
        });

        Log.d("MyFaultListActivity", "维护记录");
	}

	/**
	 * 获取我的维修记录信息
	 */
	private String xPathParseUploadTroubleTicketsXml() {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();

		try {
            if (!Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
                InputSource inputSourceNew = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
                NodeList newTicketList = (NodeList)xPath.evaluate("/root/NewTicket/Ticket", inputSourceNew, XPathConstants.NODESET);
                if (newTicketList != null && newTicketList.getLength() > 0) {
                    maintainList.clear();
                    for (int i = 0; i < newTicketList.getLength(); i++) {
                        Element element = (Element)newTicketList.item(i);
                        if ("5".equals(element.getAttribute("Status"))) {
                            String key = element.getAttribute("Downtime");
                            // 故障地址
                            String value = xPathequipmentInfoXml(element.getAttribute("Equipment"));
                            // 故障类型
                            String value1 = xPathProblemClassInfoXml(element.getAttribute("Problem"));
                            maintainList.add(new SpinnerArea(key, value + "★" + value1));
                            newTicket++;
                        }
                    }
                }

                InputSource inputSourceOld = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
                NodeList oldicketList = (NodeList)xPath.evaluate("/root/OldTicket/Ticket", inputSourceOld, XPathConstants.NODESET);
                if (oldicketList != null && oldicketList.getLength() > 0) {
                    for (int i = 0; i < oldicketList.getLength(); i++) {
                        Element element = (Element)oldicketList.item(i);
                        String key = element.getAttribute("ID");
                        String value = xPathTicketInfoXml(element.getAttribute("ID"));
                        maintainList.add(new SpinnerArea(key, value));
                        oldTicket++;
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
            str = "没有维修记录";
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
     * @return value 故障类型
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

    /**
     * 获取设备名称
     *
     * @param equipmentID 故障单号
     */
    private String xPathTicketInfoXml(String equipmentID) {
        String value = "";
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSourceTicket = new InputSource(new FileInputStream(getResources().getString(R.string.ticketPath)));
            NodeList oldTicketList = (NodeList)xPath.evaluate("/root/Ticket", inputSourceTicket, XPathConstants.NODESET);
            if (oldTicketList != null && oldTicketList.getLength() > 0) {
                for (int i = 0; i < oldTicketList.getLength(); i++) {
                    Element element = (Element)oldTicketList.item(i);
                    // 转接故障单
                    if (equipmentID.equals(element.getAttribute("ID"))) {
                        // 故障地址
                        String value1 = xPathequipmentInfoXml(element.getAttribute("Equipment"));
                        // 故障类型
                        String value2 = xPathProblemClassInfoXml(element.getAttribute("Problem"));
                        value = value1 + "★" + value2;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}