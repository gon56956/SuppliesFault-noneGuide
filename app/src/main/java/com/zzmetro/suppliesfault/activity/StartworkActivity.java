package com.zzmetro.suppliesfault.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzmetro.suppliesfault.service.WebServiceBase;
import com.zzmetro.suppliesfault.util.Util;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: StartworkActivity
 * 类描述: 接班
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class StartworkActivity extends BaseActivity {

    private String staffNumber;
    private String lineCode;
    private String pdaID;
    private String date;
    private String port;
    private String ip;
    private String result = "";
    private String startWorkMessage = "";
    private Handler handler;
    private Button startWork;
    private ProgressDialog pd;

    // 未完成的故障单,借用/领取物资
    private TextView myFaultList,suppliesList;
    // 设备信息
    private String getEquipmentInfo = "";
    private String resultForEquipmentInfo = "";
    // 故障分类
    private String getProblemClassInfo = "";
    private String resultForProblemClassInfo = "";
    // 未完成的故障单
    private String getTicketInfo = "";
    private String resultForTicketInfo = "";
    // 个人物资
    private String getMyMaterial = "";
    private String resultForMyMaterial = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_work);

        // 获取当前时间
        date = Util.getSystemDate();

        // 获取登陆者信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        staffNumber = sp.getString("staffNumber", "").trim();
        lineCode = sp.getString("lineCode", "").trim();
        pdaID = sp.getString("pdaID", "").trim();
        port = sp.getString("port", "").trim();
        ip = sp.getString("ip", "").trim();

        // 接口参数设定
        getEquipmentInfo = staffNumber + "," + pdaID + ",GetEquipmentInfo," + date;
        getProblemClassInfo = staffNumber + "," + pdaID + ",GetProblemClassInfo," + date;
        getTicketInfo = staffNumber + "," + pdaID + ",GetTicketInfo,1";
        getMyMaterial = staffNumber + "," + pdaID + ",GetMyMaterial";

        // 未完成的故障单
        myFaultList = (TextView) findViewById(R.id.myFaultList);
        myFaultList.setText("    无未完成故障单");
        // 借用/领取物资
        suppliesList = (TextView) findViewById(R.id.suppliesList);
        suppliesList.setText("    无物资\n\n\n点击接班，进行相关信息的下载！");
        // 获取【接班】按钮ID
        startWork = (Button) findViewById(R.id.startWork);

        //控件状态
        startWork.setEnabled(true);

        startWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示ProgressDialog
                pd = ProgressDialog.show(StartworkActivity.this, "接班", "信息正在下载中，请稍候！");

                // 开启一个新线程，在新线程里执行耗时的方法
                new Thread(new Runnable() {
                    public void run() {
                        result = downloadWorkInfo();
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
                if (Util.SUCESS.equals(result)) {
                    xPathParseTicketInfoXml();
                    xPathParseMyMaterialXml();
                    startWork.setEnabled(false);
                    Util.showToast(StartworkActivity.this, "接班成功" + "\n" + startWorkMessage);
                    getHomeActivity();
                } else if (Util.ERR_TERMINAL.equals(result)){
                    Util.showToast(StartworkActivity.this, "＊＊＊非法终端＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.ERR_SYSTEM.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊服务器内部错误＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.ERR_DB.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊数据库错误＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.NETERROR.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊网络连接异常，检查网络连接＊＊＊");
                } else if (Util.ERR_PARSER_XML.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊字符串转化XML错误＊＊＊");
                } else if (Util.ERR_IO.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊IO错误＊＊＊");
                } else if (Util.ERR_SAX.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊SAX解析XML错误＊＊＊");
                } else if (Util.ERR_PARSER_DATE.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊字符串转化日期错误＊＊＊");
                } else if (Util.ERR_PARAM.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊参数不合法＊＊＊");
                } else if (Util.ERR_COMMAND.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊命令错误＊＊＊");
                } else if (Util.UNLAWFULUSER.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊非法用户＊＊＊");
                } else if (Util.ERR_FAIL.equals(result)) {
                    Util.showToast(StartworkActivity.this, "＊＊＊接班失败，继续接班＊＊＊");
                } else {
                    Util.showToast(StartworkActivity.this, result + "\n" + "＊＊＊继续接班＊＊＊");
                }
            }
        };

        Log.d("StartworkActivity", "接班同步");
    }

    /**
     * 统计未完成故障单条数
     */
    private void xPathParseTicketInfoXml() {
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(getResources().getString(R.string.ticketPath));
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            org.jdom.Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<org.jdom.Element> ticketList = rootElement.getChildren();
            if (ticketList != null && ticketList.size() > 0) {
                // 未完成故障单总数
                myFaultList.setText("    未完成故障单：" + ticketList.size() + "条" + "\n");
            } else {
                // 未完成故障单总数
                myFaultList.setText("    未完成故障单：0条");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 统计借用/领取物资
     */
    private void xPathParseMyMaterialXml() {
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        String material = "";

        try {
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(getResources().getString(R.string.materialPath));
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            org.jdom.Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<org.jdom.Element> materialListCount = rootElement.getChildren();
            if (materialListCount != null && materialListCount.size() > 0) {
                InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.materialPath)));
                NodeList materialList = (NodeList)xPath.evaluate("/root/Material", inputSource, XPathConstants.NODESET);
                if (materialList != null && materialList.getLength() > 10) {
                    for (int i = 0; i < 10; i++) {
                        Element element = (Element)materialList.item(i);
                        String name = element.getAttribute("Name");
                        String amount = element.getAttribute("Amount");
                        material += "    " + name + "    " + amount + "个" + "\n";
                    }
                    // 借用/领取物资详情
                    suppliesList.setText(material + "\n" + "    .........");
                } else {
                    for (int j = 0; j < materialList.getLength(); j++) {
                        Element element = (Element)materialList.item(j);
                        String name = element.getAttribute("Name");
                        String amount = element.getAttribute("Amount");
                        material += "    " + name + "    " + amount + "个" + "\n";
                    }
                    // 借用/领取物资详情
                    suppliesList.setText(material);
                }
            } else {
                // 借用/领取物资详情
                suppliesList.setText("    物资暂无,请先从PC端领取物资！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载设备、故障、个人工单及物资信息
     */
    private String downloadWorkInfo() {
        // 调用接口 获取 设备信息
        resultForEquipmentInfo = WebServiceBase.getWebServiceReslut(getEquipmentInfo, ip, port, lineCode);
        // 调用接口 获取 故障分类
        resultForProblemClassInfo = WebServiceBase.getWebServiceReslut(getProblemClassInfo, ip, port, lineCode);
        // 调用接口 获取 未完成的故障单
        resultForTicketInfo = WebServiceBase.getWebServiceReslut(getTicketInfo, ip, port, lineCode);
        // 调用接口 获取 个人物资
        resultForMyMaterial = WebServiceBase.getWebServiceReslut(getMyMaterial, ip, port, lineCode);

        /* 对WebService返回的结果的判断处理 */
        if (Util.ERR_FAIL.equals(resultForEquipmentInfo)
                || Util.ERR_FAIL.equals(resultForProblemClassInfo)
                || Util.ERR_FAIL.equals(resultForTicketInfo)
                || Util.ERR_FAIL.equals(resultForMyMaterial)) {
            return Util.ERR_FAIL;
        } else if (Util.ERR_SYSTEM.equals(resultForEquipmentInfo)
                || Util.ERR_SYSTEM.equals(resultForProblemClassInfo)
                || Util.ERR_SYSTEM.equals(resultForTicketInfo)
                || Util.ERR_SYSTEM.equals(resultForMyMaterial)) {
            return Util.ERR_SYSTEM;
        } else if (Util.ERR_DB.equals(resultForEquipmentInfo)
                || Util.ERR_DB.equals(resultForProblemClassInfo)
                || Util.ERR_DB.equals(resultForTicketInfo)
                || Util.ERR_DB.equals(resultForMyMaterial)) {
            return Util.ERR_DB;
        } else if (Util.NETERROR.equals(resultForEquipmentInfo)
                || Util.NETERROR.equals(resultForProblemClassInfo)
                || Util.NETERROR.equals(resultForTicketInfo)
                || Util.NETERROR.equals(resultForMyMaterial)) {
            return Util.NETERROR;
        } else if (Util.ERR_TERMINAL.equals(resultForEquipmentInfo)
                || Util.ERR_TERMINAL.equals(resultForProblemClassInfo)
                || Util.ERR_TERMINAL.equals(resultForTicketInfo)
                || Util.ERR_TERMINAL.equals(resultForMyMaterial)) {
            return Util.ERR_TERMINAL;
        } else if (Util.UNLAWFULUSER.equals(resultForEquipmentInfo)
                || Util.UNLAWFULUSER.equals(resultForProblemClassInfo)
                || Util.UNLAWFULUSER.equals(resultForTicketInfo)
                || Util.UNLAWFULUSER.equals(resultForMyMaterial)) {
            return Util.UNLAWFULUSER;
        } else if (Util.ERR_PARAM.equals(resultForEquipmentInfo)
                || Util.ERR_PARAM.equals(resultForProblemClassInfo)
                || Util.ERR_PARAM.equals(resultForTicketInfo)
                || Util.ERR_PARAM.equals(resultForMyMaterial)) {
            return Util.ERR_PARAM;
        } else if (Util.ERR_COMMAND.equals(resultForEquipmentInfo)
                || Util.ERR_COMMAND.equals(resultForProblemClassInfo)
                || Util.ERR_COMMAND.equals(resultForTicketInfo)
                || Util.ERR_COMMAND.equals(resultForMyMaterial)) {
            return Util.ERR_COMMAND;
        } else if (Util.ERR_PARSER_XML.equals(resultForEquipmentInfo)
                || Util.ERR_PARSER_XML.equals(resultForProblemClassInfo)
                || Util.ERR_PARSER_XML.equals(resultForTicketInfo)
                || Util.ERR_PARSER_XML.equals(resultForMyMaterial)) {
            return Util.ERR_PARSER_XML;
        } else if (Util.ERR_IO.equals(resultForEquipmentInfo)
                || Util.ERR_IO.equals(resultForProblemClassInfo)
                || Util.ERR_IO.equals(resultForTicketInfo)
                || Util.ERR_IO.equals(resultForMyMaterial)) {
            return Util.ERR_IO;
        } else if (Util.ERR_SAX.equals(resultForEquipmentInfo)
                || Util.ERR_SAX.equals(resultForProblemClassInfo)
                || Util.ERR_SAX.equals(resultForTicketInfo)
                || Util.ERR_SAX.equals(resultForMyMaterial)) {
            return Util.ERR_SAX;
        } else if (Util.ERR_PARSER_DATE.equals(resultForEquipmentInfo)
                || Util.ERR_PARSER_DATE.equals(resultForProblemClassInfo)
                || Util.ERR_PARSER_DATE.equals(resultForTicketInfo)
                || Util.ERR_PARSER_DATE.equals(resultForMyMaterial)) {
            return Util.ERR_PARSER_DATE;
        } else {
            // 保存 设备信息
            if (!"".equals(resultForEquipmentInfo) && resultForEquipmentInfo != null) {
                // 返回结果 调用共通方法saveXml()，保存为xml文件
                try {
                    // 返回结果转换为DOCUMENT
                    Document equipmentDoc = Util.string2Doc(resultForEquipmentInfo);
                    // 写入文件中
                    Util.saveXML(equipmentDoc, getResources().getString(R.string.equipmentPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return "设备信息下载失败";
            }
            // 保存 故障分类
            if (!"".equals(resultForProblemClassInfo) && resultForProblemClassInfo != null) {
                // 返回结果 调用共通方法saveXml()，保存为xml文件
                try {
                    // 返回结果转换为DOCUMENT
                    Document problemClassDoc = Util.string2Doc(resultForProblemClassInfo);
                    // 写入文件中
                    Util.saveXML(problemClassDoc, getResources().getString(R.string.problemClassPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return "故障分类下载失败";
            }
            // 保存 个人物资
            if (!"".equals(resultForMyMaterial) && resultForMyMaterial != null) {
                // 返回结果 调用共通方法saveXml()，保存为xml文件
                try {
                    // 返回结果转换为DOCUMENT
                    Document materialDoc = Util.string2Doc(resultForMyMaterial);
                    // 写入文件中
                    Util.saveXML(materialDoc, getResources().getString(R.string.materialPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return "个人物资下载失败";
            }
            // 保存 未完成的故障单
            if (!"".equals(resultForTicketInfo) && resultForTicketInfo != null) {
                // 返回结果 调用共通方法saveXml()，保存为xml文件
                try {
                    // 返回结果转换为DOCUMENT
                    Document ticketDoc = Util.string2Doc(resultForTicketInfo);
                    // 写入文件中
                    Util.saveXML(ticketDoc, getResources().getString(R.string.ticketPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return "未完成的故障单下载失败";
            }
        }
        startWorkMessage = "＊＊＊＊设备信息下载成功＊＊＊＊" + "\n"
                            + "＊＊＊＊故障分类下载成功＊＊＊＊" + "\n"
                            + "＊＊＊＊个人物资下载成功＊＊＊＊" + "\n"
                            + "＊＊＊未完成的故障单下载成功＊＊";
        return Util.SUCESS;
    }

    /**
     * 等待3秒，在返回menu画面
     */
    private void getHomeActivity() {
        Timer timer=new Timer();
        TimerTask task=new TimerTask(){
            public void run(){
                Intent intent = new Intent(StartworkActivity.this, MenuActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        };
        timer.schedule(task, 3000);
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
