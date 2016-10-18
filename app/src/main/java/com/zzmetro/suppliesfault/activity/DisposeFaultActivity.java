package com.zzmetro.suppliesfault.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.zzmetro.suppliesfault.util.Util;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 *
 * 项目名称: 物资故障
 * 包:       com.zzmetro.suppliesfault
 * 类名称:   DisposeFaultActivity
 * 类描述:   处理故障单
 * 创建人:   mayunpeng
 * 创建时间: 2016/04/21
 * 版本:     [v1.0]
 *
 */
public class DisposeFaultActivity extends BaseActivity {

    // 设备编号,故障时间,故障地点,故障类型,故障描述,处理建议,消耗备件详情,备注,处理故障单号
    private TextView equipmentID,faultDate,faultAddress,faultType,faultInfo,handleSuggest,sparepartInfo, comment, tvSolutionFault;
    // 消耗备件
    private CheckBox useSparepart;
    // 修复,删除
    private Button repairOk,repairDelete;
    // 其它
    private String date = "";
    private String solutionFaultID;
    private ArrayList<String> dataResult = new ArrayList<String>();

    public static void actionStart(Context context, String solutionFaultID) {
        Intent intent = new Intent(context, DisposeFaultActivity.class);
        intent.putExtra("solutionFaultID", solutionFaultID);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispose_fault);

        // 获取登陆者信息
        Intent intent = getIntent();
        solutionFaultID = intent.getStringExtra("solutionFaultID");

        // 处理故障单号
        tvSolutionFault = (TextView) findViewById(R.id.solutionFault);
        // 设备编号
        equipmentID = (TextView) findViewById(R.id.equipmentID);
        // 故障时间
        faultDate = (TextView) findViewById(R.id.faultDate);
        // 故障地点
        faultAddress = (TextView) findViewById(R.id.faultAddress);
        // 故障类型
        faultType = (TextView) findViewById(R.id.faultType);
        // 故障描述
        faultInfo = (TextView) findViewById(R.id.faultInfo);
        // 处理建议
        handleSuggest = (TextView) findViewById(R.id.handleSuggest);
        // 消耗备件详情
        sparepartInfo = (TextView) findViewById(R.id.sparepartInfo);
        // 备注
        comment = (EditText) findViewById(R.id.comments);
        // 修复
        repairOk = (Button) findViewById(R.id.repairOk);
        // 删除
        repairDelete = (Button) findViewById(R.id.repairDelete);
        // 消耗备件
        useSparepart = (CheckBox) findViewById(R.id.useSparepart);

        if (Util.jdomTicketInfoXml(solutionFaultID)) {
            xPathTicketInfoXml(solutionFaultID);
            repairDelete.setVisibility(View.GONE);
        } else {
            // 初始化维修故障单
            xPathFaultInfoXml(solutionFaultID);
        }

        // 消耗备件监听
        useSparepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useSparepart.isChecked()) {
                    Intent intent = new Intent(DisposeFaultActivity.this, UseSparepartActivity.class);
            		/*
            		 * 如果希望启动另一个Activity，并且希望有返回值，则需要使用startActivityForResult这个方法，
            		 * 第一个参数是Intent对象，第二个参数是一个requestCode值，如果有多个按钮都要启动Activity，则requestCode标志着每个按钮所启动的Activity
            		 */
                    startActivityForResult(intent, 0);
                } else {
                    useSparepart.setChecked(false);
                    sparepartInfo.setText("");
                    dataResult = null;
                    repairDelete.setEnabled(true);
                }
            }
        });

        // 修复监听
        repairOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前系统时间
                date = Util.getSystemDate();
                /**
                 * 修复故障单
                 *
                 * @param 备注
                 * @param 处理故障单号
                 * @param 消耗备件
                 * @param 修复时间
                 */
                if (Util.updataXML(comment.getText().toString(),
                        solutionFaultID,
                        dataResult,
                        date)) {
                    if (Util.jdomTicketInfoXml(solutionFaultID)) {
                        updataXML(solutionFaultID);
                    }
                    // 更新消耗备件信息
                    Util.updataXML(dataResult);
                    Util.showToast(DisposeFaultActivity.this, "故障修复已提交");
                    finish();
                    Intent intent = new Intent(DisposeFaultActivity.this, MyFaultListActivity.class);
                    startActivity(intent);
                } else {
                    Util.showToast(DisposeFaultActivity.this, "故障修复提交失败");
                }
            }
        });

        // 删除监听
        repairDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除节点
                AlertDialog.Builder builder1 = new AlertDialog.Builder(DisposeFaultActivity.this);
                builder1.setTitle("确认");
                builder1.setMessage("是否继续执行删除操作！");
                builder1.setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Util.removeXML(solutionFaultID, getResources().getString(R.string.uploadTroubleTicketsPath), "新建未修复");
                        Util.showToast(DisposeFaultActivity.this, "删除成功！");
                        finish();
                        Intent intent = new Intent(DisposeFaultActivity.this, MyFaultListActivity.class);
                        startActivity(intent);
                    }

                });
                builder1.setNegativeButton("否", null);
                builder1.show();
            }
        });

        Log.d("DisposeFaultActivity", "处理故障单 execute");
    }

    /**
     * 所有的Activity对象的返回值都是由这个方法来接收
     * requestCode: 表示的是启动一个Activity时传过去的requestCode值
     * resultCode:  表示的是启动后的Activity回传值时的resultCode值
     * data:        表示的是启动后的Activity回传过来的Intent对象
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle bundle = data.getExtras();
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Boolean certain = bundle.getBoolean("result");
            String infoResult = bundle.getString("useSparepartinfo");
            dataResult = bundle.getStringArrayList("useSparepartData");
            useSparepart.setChecked(certain);
            sparepartInfo.setText(infoResult);
            repairDelete.setEnabled(false);
        } else if (requestCode == 0 && resultCode == RESULT_CANCELED) {
            Boolean certain = bundle.getBoolean("result");
            useSparepart.setChecked(certain);
        }
    }

    /**
     * 获取未处理故障信息
     *
     * @param solutionFaultID 处理故障单号
     */
    private void xPathTicketInfoXml(String solutionFaultID) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.ticketPath)));
            NodeList ticketList = (NodeList)xPath.evaluate("/root/Ticket[@ID='" + solutionFaultID + "']", inputSource, XPathConstants.NODESET);

            if (ticketList != null && ticketList.getLength() > 0) {
                for (int i = 0; i < ticketList.getLength(); i++) {
                    Element element = (Element)ticketList.item(i);
                    // 处理故障单号
                    tvSolutionFault.setText(getResources().getString(R.string.solution_fault) + solutionFaultID);
                    // 设备编号
                    equipmentID.setText(element.getAttribute("Equipment"));
                    // 故障时间
                    faultDate.setText(element.getAttribute("DownTime"));
                    // 故障地点
                    xPathEquipmentInfoXml(element.getAttribute("Equipment"));
                    // 故障类型、故障描述、处理建议
                    xPathProblemClassInfoXml(element.getAttribute("Problem"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化维修故障单
     *
     * @param downtime 创建故障单时间
     */
    private boolean xPathFaultInfoXml(String downtime) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
            NodeList ticketList = (NodeList)xPath.evaluate("/root/NewTicket/Ticket[@Downtime='" + downtime + "']", inputSource, XPathConstants.NODESET);

            if (ticketList != null && ticketList.getLength() > 0) {
                for (int i = 0; i < ticketList.getLength(); i++) {
                    Element element = (Element)ticketList.item(i);
                    // 设备编号
                    equipmentID.setText(element.getAttribute("Equipment"));
                    // 故障时间
                    faultDate.setText(downtime);
                    // 故障地点
                    xPathEquipmentInfoXml(element.getAttribute("Equipment"));
                    // 故障类型、故障描述、处理建议
                    xPathProblemClassInfoXml(element.getAttribute("Problem"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取故障地点
     *
     * @param equipmentCode 设备编号
     */
    private void xPathEquipmentInfoXml(String equipmentCode) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.equipmentPath)));
            NodeList equipmentList = (NodeList)xPath.evaluate("/root/EquipmentType/Equipment[@Code='" + equipmentCode + "']", inputSource, XPathConstants.NODESET);

            if (equipmentList != null && equipmentList.getLength() > 0) {
                for (int i = 0; i < equipmentList.getLength(); i++) {
                    Element element = (Element)equipmentList.item(i);
                    // 故障地点
                    faultAddress.setText(element.getAttribute("Location"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取故障类型、故障描述、处理建议
     *
     * @param problemCode 故障描述code
     */
    private void xPathProblemClassInfoXml(String problemCode) {
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
                    faultType.setText(element.getAttribute("Name"));
                }
            }

            InputSource inputSourceComment = new InputSource(new FileInputStream(getResources().getString(R.string.problemClassPath)));
            // 故障描述、处理建议
            NodeList problemList = (NodeList)xPath.evaluate("/root/EquipmentType/ProblemClass/Problem[@Code='" + problemCode + "']", inputSourceComment, XPathConstants.NODESET);

            if (problemList != null && problemList.getLength() > 0) {
                for (int i = 0; i < problemList.getLength(); i++) {
                    Element element = (Element)problemList.item(i);
                    // 故障描述
                    faultInfo.setText(element.getAttribute("Comment"));
                    // 处理建议
                    handleSuggest.setText(element.getAttribute("Tips"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新节点
     *
     * @param solutionFaultID
     */
    private void updataXML(String solutionFaultID) {
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
            // 删除节点
            for (int i = 0; i < ticketList.size(); i++) {
                org.jdom.Element elTicket = ticketList.get(i);
                if (elTicket.getAttributeValue("ID").equals(solutionFaultID)) {
                    elTicket.setAttribute("Status", "5");
                }
            }
            // 6.设置输出流和输出格式
            Util.saveXML(document, getResources().getString(R.string.ticketPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}