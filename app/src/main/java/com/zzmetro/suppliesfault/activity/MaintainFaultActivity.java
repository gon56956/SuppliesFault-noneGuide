package com.zzmetro.suppliesfault.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzmetro.suppliesfault.util.Util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.FileInputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 *
 * 项目名称: 物资故障
 * 包:       com.zzmetro.suppliesfault
 * 类名称:   DisposeFaultActivity
 * 类描述:   维修单详情
 * 创建人:   mayunpeng
 * 创建时间: 2016/04/21
 * 版本:     [v1.0]
 *
 */
public class MaintainFaultActivity extends BaseActivity {

    // 设备编号,故障时间,故障地点,故障类型,故障描述,处理建议,消耗备件详情,备注,处理故障单号
    private TextView equipmentID,faultDate,faultAddress,faultType,faultInfo,handleSuggest,sparepartInfo,comment,tvSolutionFault,useSparepart;
    // 其它
    private String solutionFaultID;
    // 删除
    private Button repairDelete;

    public static void actionStart(Context context, String solutionFaultID) {
        Intent intent = new Intent(context, MaintainFaultActivity.class);
        intent.putExtra("solutionFaultID", solutionFaultID);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintain_fault);

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
        comment = (TextView) findViewById(R.id.comments);
        // 消耗备件
        useSparepart = (TextView) findViewById(R.id.useSparepart);
        // 删除
        repairDelete = (Button) findViewById(R.id.repairDelete);

        if (Util.jdomUploadTroubleTicketsInfoXml(solutionFaultID)) {
            xPathTicketInfoXml(solutionFaultID);
        } else {
            // 初始化维修故障单
            xPathFaultInfoXml(solutionFaultID);
        }

        // 删除监听
        repairDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除节点
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MaintainFaultActivity.this);
                builder1.setTitle("确认");
                builder1.setMessage("****是否继续执行删除操作!****\n删除后此故障单可在【我的故障单】中查看");
                builder1.setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Util.removeXML(solutionFaultID, getResources().getString(R.string.uploadTroubleTicketsPath), "新建已修复");
                        Util.showToast(MaintainFaultActivity.this, "删除成功!");
                        finish();
                        Intent intent = new Intent(MaintainFaultActivity.this, MaintainListActivity.class);
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
                    // 故障地点
                    xPathEquipmentInfoXml(element.getAttribute("Equipment"));
                    // 故障类型、故障描述、处理建议
                    xPathProblemClassInfoXml(element.getAttribute("Problem"));
                    // 修复时间、备注、消耗备件
                    xPathuploadTroubleTicketsXml(solutionFaultID);
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
                    faultDate.setText(element.getAttribute("Downtime"));
                    // 故障地点
                    xPathEquipmentInfoXml(element.getAttribute("Equipment"));
                    // 故障类型、故障描述、处理建议
                    xPathProblemClassInfoXml(element.getAttribute("Problem"));
                    // 备注
                    if ("".equals(element.getAttribute("Comment")) || element.getAttribute("Comment") == null) {
                        comment.setText("无");
                    } else {
                        comment.setText(element.getAttribute("Comment"));
                    }
                    // 消耗备件
                    if (!"0".equals(element.getAttribute("Materials"))) {
                        xPathParseMyMaterialXmlNew(downtime);
                    } else {
                        useSparepart.setText("无");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修复时间、备注、消耗备件
     *
     * @param solutionFaultID 修复时间、备注、消耗备件
     */
    private boolean xPathuploadTroubleTicketsXml(String solutionFaultID) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
            NodeList ticketList = (NodeList)xPath.evaluate("/root/OldTicket/Ticket[@ID='" + solutionFaultID + "']", inputSource, XPathConstants.NODESET);

            if (ticketList != null && ticketList.getLength() > 0) {
                for (int i = 0; i < ticketList.getLength(); i++) {
                    Element element = (Element)ticketList.item(i);
                    // 修复时间
                    faultDate.setText(element.getAttribute("RestoreTime"));
                    // 备注
                    if ("".equals(element.getAttribute("Comment")) || element.getAttribute("Comment") == null) {
                        comment.setText("无");
                    } else {
                        comment.setText(element.getAttribute("Comment"));
                    }
                    // 消耗备件
                    if (!"0".equals(element.getAttribute("Materials"))) {
                        xPathParseMyMaterialXmlOld(solutionFaultID);
                    } else {
                        useSparepart.setText("无");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取个人物资
     * @param solutionFaultID
     */
    private void xPathParseMyMaterialXmlOld(String solutionFaultID) {
        String materialCountOld = "";
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
            NodeList materialList = (NodeList)xPath.evaluate("/root/OldTicket/Ticket[@ID='" + solutionFaultID + "']/Material", inputSource, XPathConstants.NODESET);
            if (materialList != null && materialList.getLength() > 0) {
                for (int i = 0; i < materialList.getLength(); i++) {
                    Element element = (Element)materialList.item(i);
                    String name = Util.xPathParseMyMaterialXml(element.getAttribute("ID"));
                    String amount = element.getAttribute("Amount");
                    materialCountOld += name + "    " + amount + "个" + "\n";
                }
            }
            // 物资消耗
            useSparepart.setText(materialCountOld);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void xPathParseMyMaterialXmlNew(String downtime) {
        String materialCountNew = "";
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.uploadTroubleTicketsPath)));
            NodeList materialList = (NodeList)xPath.evaluate("/root/NewTicket/Ticket[@Downtime='" + downtime + "']/Material", inputSource, XPathConstants.NODESET);
            if (materialList != null && materialList.getLength() > 0) {
                for (int i = 0; i < materialList.getLength(); i++) {
                    Element element = (Element)materialList.item(i);
                    String name = Util.xPathParseMyMaterialXml(element.getAttribute("ID"));
                    String amount = element.getAttribute("Amount");
                    materialCountNew += name + "    " + amount + "个" + "\n";
                }
            }
            // 物资消耗
            useSparepart.setText(materialCountNew);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
