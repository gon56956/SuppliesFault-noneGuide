package com.zzmetro.suppliesfault.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.zxing.activity.CaptureActivity;
import com.zzmetro.suppliesfault.model.SpinnerArea;
import com.zzmetro.suppliesfault.util.Util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * 项目名称: 物资故障 包: com.zzmetro.suppliesfault.activity 类名称: CreateFaultListActivity
 * 类描述: 新建故障单
 * 创建人: mayunpeng 创建时间: 2016/04/21 版本: [v1.0]
 */
public class CreateFaultListActivity extends AppCompatActivity {

    // 模块名称,设备编号,处理建议,消耗备件详情,备注
    private TextView equipmentInfo,equipmentID, handleSuggest, sparepartInfo, comment;
    // 故障类型,故障描述
    private Spinner sparepartTypeSpinner, sparepartInfoSpinner, spin_SparepartInfo;
    // 故障类型适配器,故障描述适配器
    private ArrayAdapter<SpinnerArea> sparepartTypeAdapter, sparepartInfoAdapter;
    // 消耗备件
    private CheckBox useSparepart;
    // 已修复,未修复
    private Button repairOk, repairNg, scanEquipmentID;
    private long mExiteTime;
    // 故障类型,故障描述
    private List<SpinnerArea> sparepartTypeList = new ArrayList<SpinnerArea>();
    private List<SpinnerArea> sparepartInfoList = new ArrayList<SpinnerArea>();
    // 其它
    private String problemClassID = "";
    private String problemCode = "";
    private String restoreDate = "", createDate = "";
    private String type = "";
    private ArrayList<String> dataResult = new ArrayList<String>();
    private String lineCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_fault);

        // 获取参数信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        lineCode = sp.getString("lineCode", "").trim();
        // 获取当前系统时间
        createDate = Util.getSystemDate();

        // 初始化新建故障单
        // 故障类型
        sparepartTypeSpinner = (Spinner) findViewById(R.id.spin_sparepartType);
        sparepartTypeAdapter = new ArrayAdapter<SpinnerArea>(CreateFaultListActivity.this,
                android.R.layout.simple_spinner_item, sparepartTypeList);
        sparepartTypeAdapter.setDropDownViewResource(R.layout.drop_down_item);
        sparepartTypeSpinner.setAdapter(sparepartTypeAdapter);
        // 故障描述
        sparepartInfoSpinner = (Spinner) findViewById(R.id.spin_sparepartInfo);
        sparepartInfoAdapter = new ArrayAdapter<SpinnerArea>(CreateFaultListActivity.this,
                android.R.layout.simple_spinner_item, sparepartInfoList);
        sparepartInfoAdapter.setDropDownViewResource(R.layout.drop_down_item);
        sparepartInfoSpinner.setAdapter(sparepartInfoAdapter);

        // 模块名称
        equipmentInfo = (TextView) findViewById(R.id.equipmentInfo);
        // 故障描述
        spin_SparepartInfo = (Spinner) findViewById(R.id.spin_sparepartInfo);
        // 处理建议
        handleSuggest = (TextView) findViewById(R.id.handleSuggest);
        // 消耗备件详情
        sparepartInfo = (TextView) findViewById(R.id.sparepartInfo);
        // 备注
        comment = (EditText) findViewById(R.id.comments);
        // 设备编号
        equipmentID = (TextView) findViewById(R.id.equipmentID);
        // 消耗备件
        useSparepart = (CheckBox) findViewById(R.id.useSparepart);
        // 已修复 "0"
        repairOk = (Button) findViewById(R.id.repairOk);
        // 未修复 "1"
        repairNg = (Button) findViewById(R.id.repairNg);
        // 扫一扫
        scanEquipmentID = (Button) findViewById(R.id.scanEquipmentID);

        // 按钮状态
        repairOk.setEnabled(false);
        repairNg.setEnabled(false);
        useSparepart.setEnabled(false);

        // 扫一扫
        scanEquipmentID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openCameraIntent = new Intent(CreateFaultListActivity.this, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            }
        });

        // 设备编号监听
        equipmentID.setOnKeyListener(onKey);

        // 故障类型下拉框监听
        sparepartTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // 表示选项被改变的时候触发此方法，主要实现办法：动态改变故障描述适配器的绑定值
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                // position为当前故障类型选中的值的序号
                SpinnerArea spinnerArea = (SpinnerArea) arg0.getAdapter().getItem(position);
                problemClassID = spinnerArea.getDomaincode();

                type = "/root/EquipmentType/ProblemClass[@ID='" +  problemClassID + "']/Problem";
                xPathParseProblemClassInfoXml(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        // 故障描述下拉监听
        sparepartInfoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                // position为当前故障类型选中的值的序号
                SpinnerArea spinnerArea = (SpinnerArea) arg0.getAdapter().getItem(position);
                problemCode = spinnerArea.getDomaincode();

                type = "/root/EquipmentType/ProblemClass/Problem[@Code='" +  problemCode + "']";
                xPathParseProblemClassInfoXml(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // 消耗备件监听
        useSparepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useSparepart.isChecked()) {
                    Intent intent = new Intent(CreateFaultListActivity.this, UseSparepartActivity.class);
                    /*
                     * 如果希望启动另一个Activity，并且希望有返回值，则需要使用startActivityForResult这个方法，
					 * 第一个参数是Intent对象，第二个参数是一个requestCode值，如果有多个按钮都要启动Activity，
					 * 则requestCode标志着每个按钮所启动的Activity
					 */
                    startActivityForResult(intent, 1);
                } else {
                    useSparepart.setChecked(false);
                    sparepartInfo.setText("");
                    dataResult = null;
                    if (sparepartTypeList.size() > 0) {
                        repairNg.setEnabled(true);
                    } else {
                        repairNg.setEnabled(false);
                    }
                }
            }
        });

        // 已修复监听 "5"
        repairOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkequipment(equipmentID.getText().toString().trim())) {
                    // 获取当前系统时间
                    restoreDate = Util.getSystemDate();

                    /**
                     * 新建修复故障单
                     *
                     * @param 设备编号
                     * @param 故障描述
                     * @param 故障时间
                     * @param 消耗备件
                     * @param 备注
                     * @param 状态
                     */
                    if (Util.insertXML(equipmentID.getText().toString(),
                            problemCode,
                            createDate,
                            restoreDate,
                            dataResult,
                            comment.getText().toString(),
                            "5")) {
                        // 更新消耗备件信息
                        Util.updataXML(dataResult);
                        Util.showToast(CreateFaultListActivity.this, "新建修复故障单已提交");
                        finish();
                    } else {
                        Util.showToast(CreateFaultListActivity.this, "新建修复故障失败");
                    }
                }
            }
        });

        // 未修复监听 "1"
        repairNg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkequipment(equipmentID.getText().toString().trim())) {
                    // 获取当前系统时间
                    restoreDate = Util.getSystemDate();

                    /**
                     * 新建未修复故障单
                     *
                     * @param 设备编号
                     * @param 故障描述
                     * @param 故障时间
                     * @param 消耗备件
                     * @param 备注
                     * @param 状态
                     */
                    if (Util.insertXML(equipmentID.getText().toString(),
                            problemCode,
                            createDate,
                            restoreDate,
                            dataResult,
                            comment.getText().toString(),
                            "1")) {
                        Util.showToast(CreateFaultListActivity.this, "新建未修复故障单已提交");
                        finish();
                    } else {
                        Util.showToast(CreateFaultListActivity.this, "新建未修复故障失败");
                    }
                }
            }
        });

        Log.d("CreateFaultListActivity", "新建故障单");
    }

    View.OnKeyListener onKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            String equipmentCode = equipmentID.getText().toString().trim();
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                checkequipment(equipmentCode);
            }
            return false;
        }
    };

    /**
     * 所有的Activity对象的返回值都是由这个方法来接收 requestCode:
     * 表示的是启动一个Activity时传过去的requestCode值 resultCode:
     * 表示的是启动后的Activity回传值时的resultCode值 data: 表示的是启动后的Activity回传过来的Intent对象
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            Bundle bundle = data.getExtras();
            if (requestCode == 0 && resultCode == RESULT_OK) {
                String scanResult = bundle.getString("result");
                equipmentID.setText(scanResult);
                xPathParseEquipmentInfoXml(scanResult);
            } else if (requestCode == 1 && resultCode == RESULT_OK) {
                Boolean certain = bundle.getBoolean("result");
                String infoResult = bundle.getString("useSparepartinfo");
                dataResult = bundle.getStringArrayList("useSparepartData");
                useSparepart.setChecked(certain);
                sparepartInfo.setText(infoResult);
                repairNg.setEnabled(false);
            } else if (requestCode == 1 && resultCode == RESULT_CANCELED) {
                Boolean certain = bundle.getBoolean("result");
                useSparepart.setChecked(certain);
            }
        }
    }

    /**
     * 判断［设备编号］是否存在 根据［设备编号］获取设备故障信息
     * @param equipmentCode
     */
    private void xPathParseEquipmentInfoXml(String equipmentCode) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.equipmentPath)));
            NodeList equipmentList = (NodeList)xPath.evaluate("/root/EquipmentType/Equipment[@Code='" + equipmentCode + "']", inputSource, XPathConstants.NODESET);
            if (equipmentList != null && equipmentList.getLength() > 0) {
                Element element = (Element)equipmentList.item(0);
                equipmentInfo.setText(element.getAttribute("Name") + "\n" + element.getAttribute("Location"));
                type = "/root/EquipmentType[@Type='" +  equipmentCode.substring(5, 7) + "']/ProblemClass";
                xPathParseProblemClassInfoXml(type);
                repairOk.setEnabled(true);
                repairNg.setEnabled(true);
                useSparepart.setEnabled(true);
            } else {
                CharSequence html = Html.fromHtml("<font color='red'>设备编号不存在</font>");
                equipmentID.setError(html);
                equipmentID.requestFocus();
                sparepartTypeList.clear();
                sparepartInfoList.clear();
                equipmentInfo.setText("");
                sparepartTypeAdapter.notifyDataSetChanged();
                sparepartInfoAdapter.notifyDataSetChanged();
                handleSuggest.setText("");
                repairOk.setEnabled(false);
                repairNg.setEnabled(false);
                useSparepart.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取故障分类信息设置三级下拉列表
     * @param type
     */
    private void xPathParseProblemClassInfoXml(String type) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.problemClassPath)));
            if (type.indexOf("]/ProblemClass") != -1) {
                NodeList problemClassList = (NodeList)xPath.evaluate(type, inputSource, XPathConstants.NODESET);
                if (problemClassList != null && problemClassList.getLength() > 0) {
                    sparepartTypeList.clear();
                    for (int i = 0; i < problemClassList.getLength(); i++) {
                        Element element = (Element)problemClassList.item(i);
                        String key = element.getAttribute("ID");
                        String value = element.getAttribute("Name");
                        sparepartTypeList.add(new SpinnerArea(key, value));
                    }
                    sparepartTypeAdapter.notifyDataSetChanged();
                    NodeList problemList = problemClassList.item(0).getChildNodes();
                    boolean flag = false;
                    if (problemList != null && problemList.getLength() > 0) {
                        sparepartInfoList.clear();
                        for (int j = 0; j < problemList.getLength(); j++) {
                            Node node = problemList.item(j);

                            if(node.getNodeType() != Node.ELEMENT_NODE){
                                continue;
                            }
                            Element element1 = (Element)node;
                            String key1 = element1.getAttribute("Code");
                            String value1 = element1.getAttribute("Comment");
                            if(flag == false){
                                handleSuggest.setText(element1.getAttribute("Tips"));
                                flag = true;
                            }
                            sparepartInfoList.add(new SpinnerArea(key1, value1));
                        }
                        sparepartInfoAdapter.notifyDataSetChanged();
                    }
                }
            } else if (type.indexOf("]/Problem") != -1) {
                NodeList problemList = (NodeList)xPath.evaluate(type, inputSource, XPathConstants.NODESET);
                boolean flag = false;
                if (problemList != null && problemList.getLength() > 0) {
                    sparepartInfoList.clear();
                    for (int j = 0; j < problemList.getLength(); j++) {
                        Node node = problemList.item(j);

                        if(node.getNodeType() != Node.ELEMENT_NODE){
                            continue;
                        }
                        Element element2 = (Element)node;
                        String key2 = element2.getAttribute("Code");
                        String value2 = element2.getAttribute("Comment");
                        if(flag == false){
                            handleSuggest.setText(element2.getAttribute("Tips"));
                            flag = true;
                        }
                        sparepartInfoList.add(new SpinnerArea(key2, value2));
                    }
                    sparepartInfoAdapter.notifyDataSetChanged();
                    sparepartInfoSpinner.setSelection(0, true);
                }
            } else {
                Node problem = (Node)xPath.evaluate(type, inputSource, XPathConstants.NODE);
                Element element3 = (Element)problem;
                handleSuggest.setText(element3.getAttribute("Tips"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * check设备编码
     * @param equipmentCode
     */
    protected Boolean checkequipment(String equipmentCode) {
        if (equipmentCode.length() == 10) {
            xPathParseEquipmentInfoXml(equipmentCode);
        } else {
            CharSequence html = Html.fromHtml("<font color='red'>设备编号不正确或者为空</font>");
            equipmentID.setError(html);
            equipmentID.requestFocus();
            sparepartTypeList.clear();
            sparepartInfoList.clear();
            equipmentInfo.setText("");
            sparepartTypeAdapter.notifyDataSetChanged();
            sparepartInfoAdapter.notifyDataSetChanged();
            handleSuggest.setText("");
            repairOk.setEnabled(false);
            repairNg.setEnabled(false);
            useSparepart.setEnabled(false);
            return false;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if ((System.currentTimeMillis() - mExiteTime) > 2000) {
//                Util.showToast(this, "再按一次返回");
//                mExiteTime = System.currentTimeMillis();
//            } else {
//                finish();
//            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
