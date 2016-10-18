package com.zzmetro.suppliesfault.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.zzmetro.suppliesfault.model.SpinnerArea;
import com.zzmetro.suppliesfault.service.WebServiceBase;
import com.zzmetro.suppliesfault.util.Util;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * Created by mayunpeng on 16/9/15.
 */
public class MySparepartActivity extends BaseActivity{

    private ArrayAdapter<SpinnerArea> mySparepartAdapter = null;
    private List<SpinnerArea> mySparepartList = new ArrayList<SpinnerArea>();
    private ProgressDialog pd;
    private Handler handler;
    private ListView listView;
    private String staffNumber;
    private String lineCode;
    private String pdaID;
    private String port;
    private String ip;
    private String result = "", materialResult = "";
    private Button getNewSparepart;
    // 个人物资
    private String getMyMaterial = "";
    private String resultForMyMaterial = "";
    private ArrayList<String> materials = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mysparepart_list);

        // 初始化物资数据
        mySparepartAdapter = new ArrayAdapter<SpinnerArea>(this, android.R.layout.simple_list_item_1, mySparepartList);
        listView = (ListView)findViewById(R.id.mySparepartList);
        listView.setAdapter(mySparepartAdapter);

        // 获取最新物资
        getNewSparepart = (Button) findViewById(R.id.getNewSparepart);

        // 获取登陆者信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        staffNumber = sp.getString("staffNumber", "").trim();
        lineCode = sp.getString("lineCode", "").trim();
        pdaID = sp.getString("pdaID", "").trim();
        port = sp.getString("port", "").trim();
        ip = sp.getString("ip", "").trim();

        // 接口参数设定
        getMyMaterial = staffNumber + "," + pdaID + ",GetMyMaterial";

        // 初始化数据
        initMaterial();

        // 获取最新物资监听
        getNewSparepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 删除个人物资
                File myMaterial = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/myMaterial.xml");
                if (myMaterial.exists()) {
                    myMaterial.delete();
                }
                updateMaterial();
            }
        });
    }

    /**
     * 初始化物资
     */
    private void initMaterial() {
        // 加载物资数据
        // 显示ProgressDialog
        pd = ProgressDialog.show(MySparepartActivity.this, "我的物资", "数据加载中，请稍候！！！");

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
                mySparepartAdapter.notifyDataSetChanged();
                if ("1".equals(result)) {
                    Util.showToast(MySparepartActivity.this, "数据加载成功");
                } else {
                    Util.showToast(MySparepartActivity.this, "数据加载失败");
                }
            }
        };
    }

    /**
     * 初始化物资
     */
    private void updateMaterial() {
        // 显示ProgressDialog
        pd = ProgressDialog.show(MySparepartActivity.this, "获取最新物资", "物资正在下载中，请稍候！");

        // 开启一个新线程，在新线程里执行耗时的方法
        new Thread(new Runnable() {
            public void run() {
                // 调用接口 获取 个人物资
                materialResult = downloadMaterialInfo();
                handler.sendEmptyMessage(0);
            }
        }).start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                pd.dismiss();
                if (Util.SUCESS.equals(materialResult)) {
//                    mySparepartList.clear();
//                    initMaterial();
                    //刷新
                    Intent intent = new Intent(MySparepartActivity.this, MenuActivity.class);
                    startActivity(intent);
                } else if (Util.ERR_TERMINAL.equals(materialResult)){
                    Util.showToast(MySparepartActivity.this, "＊＊＊非法终端＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.ERR_SYSTEM.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊服务器内部错误＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.ERR_DB.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊数据库错误＊＊＊" + "\n" + "请联系系统管理员");
                } else if (Util.NETERROR.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊网络连接异常，检查网络连接＊＊＊");
                } else if (Util.ERR_PARSER_XML.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊字符串转化XML错误＊＊＊");
                } else if (Util.ERR_IO.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊IO错误＊＊＊");
                } else if (Util.ERR_SAX.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊SAX解析XML错误＊＊＊");
                } else if (Util.ERR_PARSER_DATE.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊字符串转化日期错误＊＊＊");
                } else if (Util.ERR_PARAM.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊参数不合法＊＊＊");
                } else if (Util.ERR_COMMAND.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊命令错误＊＊＊");
                } else if (Util.UNLAWFULUSER.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊非法用户＊＊＊");
                } else if (Util.ERR_FAIL.equals(materialResult)) {
                    Util.showToast(MySparepartActivity.this, "＊＊＊物资下载失败，继续下载＊＊＊");
                } else {
                    Util.showToast(MySparepartActivity.this, materialResult + "\n" + "＊＊＊继续下载＊＊＊");
                }
            }
        };
    }

    /**
     * 获取个人物资
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
                    String name = element.getAttribute("Name");
                    String amount = element.getAttribute("Amount");
                    mySparepartList.add(new SpinnerArea(key, name + "★剩余:" + amount + "个"));
                }
            } else {
                Util.showToast(MySparepartActivity.this, "请使用PC端系统领取个人物资");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "1";
    }

    /**
     * 下载物资信息
     */
    private String downloadMaterialInfo() {
        // 调用接口 获取 个人物资
        resultForMyMaterial = WebServiceBase.getWebServiceReslut(getMyMaterial, ip, port, lineCode);

        /* 对WebService返回的结果的判断处理 */
        if (Util.ERR_FAIL.equals(resultForMyMaterial)) {
            return Util.ERR_FAIL;
        } else if (Util.ERR_SYSTEM.equals(resultForMyMaterial)) {
            return Util.ERR_SYSTEM;
        } else if (Util.ERR_DB.equals(resultForMyMaterial)) {
            return Util.ERR_DB;
        } else if (Util.NETERROR.equals(resultForMyMaterial)) {
            return Util.NETERROR;
        } else if (Util.ERR_TERMINAL.equals(resultForMyMaterial)) {
            return Util.ERR_TERMINAL;
        } else if (Util.UNLAWFULUSER.equals(resultForMyMaterial)) {
            return Util.UNLAWFULUSER;
        } else if (Util.ERR_PARAM.equals(resultForMyMaterial)) {
            return Util.ERR_PARAM;
        } else if (Util.ERR_COMMAND.equals(resultForMyMaterial)) {
            return Util.ERR_COMMAND;
        } else if (Util.ERR_PARSER_XML.equals(resultForMyMaterial)) {
            return Util.ERR_PARSER_XML;
        } else if (Util.ERR_IO.equals(resultForMyMaterial)) {
            return Util.ERR_IO;
        } else if (Util.ERR_SAX.equals(resultForMyMaterial)) {
            return Util.ERR_SAX;
        } else if (Util.ERR_PARSER_DATE.equals(resultForMyMaterial)) {
            return Util.ERR_PARSER_DATE;
        } else {
            // 保存 个人物资
            if (!"".equals(resultForMyMaterial) && resultForMyMaterial != null) {
                // 返回结果 调用共通方法saveXml()，保存为xml文件
                try {
                    // 返回结果转换为DOCUMENT
                    Document materialDoc = Util.string2Doc(resultForMyMaterial);
                    // 写入文件中
                    Util.saveXML(materialDoc, getResources().getString(R.string.materialPath));
                    // 更新消耗备件信息
                    // 判断文件是否存在
                    if (Util.checkFile(getResources().getString(R.string.materialCountPath))) {} else {
                        // 1.创建一个SAXBuilder对象
                        SAXBuilder saxBuilder = new SAXBuilder();
                        try {
                            // 2.创建一个输入流，将xml文件加载到输入流
                            InputStream inMaterial = new FileInputStream(getResources().getString(R.string.materialCountPath));
                            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
                            Document documentMaterial = saxBuilder.build(inMaterial);
                            // 4.通过Document对象获取xml文件的根节点
                            org.jdom.Element rootElementMaterial = documentMaterial.getRootElement();
                            // 5.根据根节点获取子节点的List集合
                            List<org.jdom.Element> materialList = rootElementMaterial.getChildren();

                            if (materialList != null && materialList.size() > 0) {
                                for (int n = 0; n < materialList.size(); n++) {
                                    org.jdom.Element material = materialList.get(n);
                                    materials.add(material.getAttributeValue("ID") + "," + material.getAttributeValue("Amount"));
                                }
                            }
                            Util.updataXML(materials);
                        } catch (JDOMException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return "物资下载失败";
            }
        }
        return Util.SUCESS;
    }
}
