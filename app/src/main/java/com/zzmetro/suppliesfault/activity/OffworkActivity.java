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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: OffworkActivity
 * 类描述: 交班
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class OffworkActivity extends BaseActivity {

    private String staffNumber;
    private String lineCode;
    private String pdaID;
    private String port;
    private String ip;
    private Handler handler;
    private String resultTroubleTickets = "无";
    private String resultModule = "无";
    private String result = "";
    private String offWorkMessage = "";
    private ProgressDialog pd;
    private Button offWork;

    // 上传故障单
    private String uploadTroubleTickets = "";
    private String module = "";
    // 我的故障单情况,物资消耗
    private TextView faultCount,useSparepartList,moduleInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_off_work);

		// 获取登陆者信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        staffNumber = sp.getString("staffNumber", "").trim();
        lineCode = sp.getString("lineCode", "").trim();
        pdaID = sp.getString("pdaID", "").trim();
        port = sp.getString("port", "").trim();
        ip = sp.getString("ip", "").trim();

        // 获取控件
        faultCount = (TextView) findViewById(R.id.faultCount);
        useSparepartList = (TextView) findViewById(R.id.useSparepartList);
        moduleInfo = (TextView) findViewById(R.id.moduleInfo);
        offWork = (Button) findViewById(R.id.offWork);

        // 初始化UI
        xPathParseUploadTroubleTicketsXml();
        // 上传故障单数据
        String xmlData = "";
        String moduleData = "";
        try {
            if (!Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
                xmlData = Util.doc2String(getResources().getString(R.string.uploadTroubleTicketsPath));
            }
            if (!Util.checkFile(getResources().getString(R.string.modulePath))) {
                moduleData = Util.doc2String(getResources().getString(R.string.modulePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 接口参数设定
        uploadTroubleTickets = staffNumber + "," + pdaID + ",UploadTroubleTickets," + xmlData;
        module = staffNumber + "," + pdaID + ",PDAUpdateModuleInfo," + moduleData;

        // 获取【交班】按钮ID
        offWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Util.checkFile(getResources().getString(R.string.modulePath)) ||
                        !Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
                    // 显示ProgressDialog
                    pd = ProgressDialog.show(OffworkActivity.this, "交班", "信息正在上传中，请稍候！");

                    // 开启一个新线程，在新线程里执行耗时的方法
                    new Thread(new Runnable() {
                        public void run() {
                            result = uploadWorkInfo();
                            handler.sendEmptyMessage(0);
                        }
                    }).start();
                } else {
                    // 虚拟交班
                    Util.showToast(OffworkActivity.this, "无需提交数据，交班结束");
                    // 删除xml文件
                    Util.deleteXml();
                    getHomeActivity();
                }
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                pd.dismiss();
                if (Util.SUCESS.equals(result)) {
                    Util.showToast(OffworkActivity.this, "交班成功" + "\n" + offWorkMessage);
                    // 删除xml文件
                    Util.deleteXml();
                    getHomeActivity();
                } else if (Util.ERR_TERMINAL.equals(result)){
                    Util.showToast(OffworkActivity.this, "＊＊＊非法终端＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.ERR_SYSTEM.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊服务器内部错误＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.ERR_DB.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊数据库错误＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.NETERROR.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊网络连接异常，检查网络连接＊＊＊");
                } else if (Util.ERR_PARSER_XML.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊字符串转化XML错误＊＊＊");
                } else if (Util.ERR_IO.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊IO错误＊＊＊");
                } else if (Util.ERR_SAX.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊SAX解析XML错误＊＊＊");
                } else if (Util.ERR_PARSER_DATE.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊字符串转化日期错误＊＊＊");
                } else if (Util.ERR_PARAM.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊参数不合法＊＊＊");
                } else if (Util.ERR_COMMAND.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊命令错误＊＊＊");
                } else if (Util.UNLAWFULUSER.equals(result)) {
                    Util.showToast(OffworkActivity.this, "＊＊＊非法用户＊＊＊");
                } else {
                    Util.showToast(OffworkActivity.this, result + "\n" + "＊＊＊继续交班＊＊＊");
                }
            }
        };

		Log.d("OffworkActivity", "交班同步");
	}

    /**
     * 获取我的故障单信息
     */
    private void xPathParseUploadTroubleTicketsXml() {
        int faultOK = 0;
        int faultNG = 0;
        int oldOK = 0;
        int moduleInstall = 0;
        int moduleUninstall = 0;
        String materialCount = "";
        String module = "";
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            if (!Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
                InputSource inputSourceNew = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
                NodeList newTicketList = (NodeList)xPath.evaluate("/root/NewTicket/Ticket", inputSourceNew, XPathConstants.NODESET);
                if (newTicketList != null && newTicketList.getLength() > 0) {
                    for (int i = 0; i < newTicketList.getLength(); i++) {
                        Element element = (Element)newTicketList.item(i);
                        if ("5".equals(element.getAttribute("Status"))) {
                            faultOK++;
                        } else {
                            faultNG++;
                        }
                    }
                }

                InputSource inputSourceOld = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
                NodeList oldTicketList = (NodeList)xPath.evaluate("/root/OldTicket/Ticket", inputSourceOld, XPathConstants.NODESET);
                if (oldTicketList != null && oldTicketList.getLength() > 0) {
                    for (int i = 0; i < oldTicketList.getLength(); i++) {
                        oldOK++;
                    }
                }
            }

            if (!Util.checkFile(getResources().getString(R.string.materialCountPath))) {
                InputSource inputSourceMaterialCount = new InputSource(new FileInputStream(getResources().getString(R.string.materialCountPath)));
                NodeList materialCountList = (NodeList)xPath.evaluate("/root/Material", inputSourceMaterialCount, XPathConstants.NODESET);
                if (materialCountList != null && materialCountList.getLength() > 0) {
                    for (int i = 0; i < materialCountList.getLength(); i++) {
                        Element element = (Element)materialCountList.item(i);
                        String name = Util.xPathParseMyMaterialXml(element.getAttribute("ID"));
                        String amount = element.getAttribute("Amount");
                        materialCount += "    " + name + "    " + amount + "个" + "\n";
                    }
                } else {
                    materialCount = "    没有物资消耗" + "\n";
                }
            } else {
                materialCount = "    没有物资消耗" + "\n";
            }

            if (!Util.checkFile(getResources().getString(R.string.modulePath))) {
                InputSource inputModuleInstall = new InputSource(new FileInputStream(getResources().getString(R.string.modulePath)));
                NodeList installItem = (NodeList)xPath.evaluate("/root/Install/Item", inputModuleInstall, XPathConstants.NODESET);
                if (installItem != null && installItem.getLength() > 0) {
                    moduleInstall = installItem.getLength();
                }

                InputSource inputModuleUninstall = new InputSource(new FileInputStream(getResources().getString(R.string.modulePath)));
                NodeList UnInstallItem = (NodeList)xPath.evaluate("/root/Uninstall/Item", inputModuleUninstall, XPathConstants.NODESET);
                if (UnInstallItem != null && UnInstallItem.getLength() > 0) {
                    moduleUninstall = UnInstallItem.getLength();
                }

                module = "    模块安装：" + "    " + moduleInstall + "条" + "\n" + "    模块卸载：" + "    " + moduleUninstall + "条";
            } else {
                module = "    无模块安装卸载操作";
            }

            // 我的故障单情况
            faultCount.setText("    修复接班故障：" + "    " + oldOK + "条" + "\n" + "    新建修复故障：" + "    " + faultOK + "条" + "\n" + "    新建未修复故障：" + "" + faultNG + "条" + "\n");
            // 物资消耗
            useSparepartList.setText(materialCount);
            // 模块信息
            moduleInfo.setText(module);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传交班信息
     */
    private String uploadWorkInfo() {
        if (!Util.checkFile(getResources().getString(R.string.modulePath))) {
            resultModule = WebServiceBase.getWebServiceReslut(module, ip, port, lineCode);
        }
        if (!Util.checkFile(getResources().getString(R.string.uploadTroubleTicketsPath))) {
            resultTroubleTickets = WebServiceBase.getWebServiceReslut(uploadTroubleTickets, ip, port, lineCode);
        }
        if ("无".equals(resultModule)) {}
        else if (Util.SUCESS.equals(resultModule)) {
            // 删除模块
            File module = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/module.xml");
            if (module.exists()) {
                module.delete();
            }
        } else if (Util.ERR_TERMINAL.equals(resultModule)) {
            return Util.ERR_TERMINAL;
        } else if (Util.ERR_SYSTEM.equals(resultModule)) {
            return Util.ERR_SYSTEM;
        } else if (Util.ERR_DB.equals(resultModule)) {
            return Util.ERR_DB;
        } else if (Util.NETERROR.equals(resultModule)) {
            return Util.NETERROR;
        } else if (Util.ERR_PARSER_XML.equals(resultModule)) {
            return Util.ERR_PARSER_XML;
        } else if (Util.ERR_IO.equals(resultModule)) {
            return Util.ERR_IO;
        } else if (Util.ERR_SAX.equals(resultModule)) {
            return Util.ERR_SAX;
        } else if (Util.ERR_PARSER_DATE.equals(resultModule)) {
            return Util.ERR_PARSER_DATE;
        } else if (Util.ERR_PARAM.equals(resultModule)) {
            return Util.ERR_PARAM;
        } else if (Util.ERR_COMMAND.equals(resultModule)) {
            return Util.ERR_COMMAND;
        } else if (Util.UNLAWFULUSER.equals(resultModule)) {
            return Util.UNLAWFULUSER;
        } else {
            return "模块信息交班失败";
        }
        if ("无".equals(resultTroubleTickets)) {}
        else if (Util.SUCESS.equals(resultTroubleTickets)) {
            // 删除新建故障单
            File uploadTroubleTickets = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/uploadTroubleTickets.xml");
            if (uploadTroubleTickets.exists()) {
                uploadTroubleTickets.delete();
            }
            // 删除物资
            File materialCount = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/materialCount.xml");
            if (materialCount.exists()) {
                materialCount.delete();
            }
        } else if (Util.ERR_TERMINAL.equals(resultTroubleTickets)) {
            return Util.ERR_TERMINAL;
        } else if (Util.ERR_SYSTEM.equals(resultTroubleTickets)) {
            return Util.ERR_SYSTEM;
        } else if (Util.ERR_DB.equals(resultTroubleTickets)) {
            return Util.ERR_DB;
        } else if (Util.NETERROR.equals(resultTroubleTickets)) {
            return Util.NETERROR;
        } else if (Util.ERR_PARSER_XML.equals(resultTroubleTickets)) {
            return Util.ERR_PARSER_XML;
        } else if (Util.ERR_IO.equals(resultTroubleTickets)) {
            return Util.ERR_IO;
        } else if (Util.ERR_SAX.equals(resultTroubleTickets)) {
            return Util.ERR_SAX;
        } else if (Util.ERR_PARSER_DATE.equals(resultTroubleTickets)) {
            return Util.ERR_PARSER_DATE;
        } else if (Util.ERR_PARAM.equals(resultTroubleTickets)) {
            return Util.ERR_PARAM;
        } else if (Util.ERR_COMMAND.equals(resultTroubleTickets)) {
            return Util.ERR_COMMAND;
        } else if (Util.UNLAWFULUSER.equals(resultTroubleTickets)) {
            return Util.UNLAWFULUSER;
        } else {
            return "故障单交班失败";
        }

        offWorkMessage = "＊＊＊＊模块信息交班成功＊＊＊＊" + "\n" + "＊＊＊＊故障单交班成功＊＊＊＊";

        return Util.SUCESS;
    }

    /**
     * 等待2秒，在返回menu画面
     */
    private void getHomeActivity() {
        Timer timer=new Timer();
        TimerTask task=new TimerTask(){
            public void run(){
                Intent intent = new Intent(OffworkActivity.this, MenuActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        };
        timer.schedule(task, 2000);
    }
}
