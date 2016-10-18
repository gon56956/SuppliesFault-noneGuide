package com.zzmetro.suppliesfault.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zxing.activity.CaptureActivity;
import com.zzmetro.suppliesfault.util.Util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.FileInputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: ModuleInstallActivity
 * 类描述: 安装
 * 创建人: mayunpeng 创建时间: 2016/08/08 版本: [v1.0]
 */
public class ModuleInstallActivity extends AppCompatActivity {

    private String date = "";
    // 模块编号,设备编号
    private EditText moduleCode,equipmentCode;
    // 模块名称,设备地址
    private TextView moduleName,equipmentAddress;
    // 安装
    private Button install, scanModuleCode, scanEquipmentCode;
    private String lineCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_install);

        // 获取参数信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        lineCode = sp.getString("lineCode", "").trim();

        // 模块编号
        moduleCode = (EditText) findViewById(R.id.moduleCode);
        // 模块名称
        moduleName = (TextView) findViewById(R.id.moduleName);
        // 设备编号
        equipmentCode = (EditText) findViewById(R.id.equipmentCode);
        // 设备地址
        equipmentAddress = (TextView) findViewById(R.id.equipmentAddress);
        // 获取【安装】按钮ID
        install = (Button) findViewById(R.id.install);
        // 扫一扫
        scanModuleCode = (Button) findViewById(R.id.scanModuleCode);
        scanEquipmentCode = (Button) findViewById(R.id.scanEquipmentCode);

        // 控件状态
        install.setEnabled(false);

        // 扫一扫
        scanModuleCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openCameraIntent = new Intent(ModuleInstallActivity.this, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            }
        });
        scanEquipmentCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openCameraIntent = new Intent(ModuleInstallActivity.this, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 1);
            }
        });

        // 模块编号监听
        moduleCode.setOnKeyListener(moduleOnKey);

        // 设备编号监听
        equipmentCode.setOnKeyListener(equipmentOnKey);

        // 安装监听
        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkModule(moduleCode.getText().toString().trim()) && checkEquipment(equipmentCode.getText().toString().trim())) {
                    // 获取当前系统时间
                    date = Util.getSystemDate();
                    // 调共通写入安装信息
                    if (Util.insertXML(moduleCode.getText().toString().trim(), equipmentCode.getText().toString().trim(), date, "1")) {
                        Util.showToast(ModuleInstallActivity.this, moduleName.getText().toString() + "安装成功");
                        finish();
                    }
                } else {
                    if (TextUtils.isEmpty(moduleCode.getText().toString().trim())) {
                        checkScan("模块编号不能为空");
                        moduleCode.requestFocus();
                    } else if (TextUtils.isEmpty(equipmentCode.getText().toString().trim())) {
                        checkScan("设备编号不能为空");
                        equipmentCode.requestFocus();
                    }
                }
            }
        });

        Log.d("ModuleInstallActivity", "安装 execute");
    }

    View.OnKeyListener moduleOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            String moduleID = moduleCode.getText().toString().trim();
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                checkModule(moduleID);
            }
            return false;
        }
    };

    View.OnKeyListener equipmentOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            String equipmentID = equipmentCode.getText().toString().trim();
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                checkEquipment(equipmentID);
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
                moduleCode.setText(scanResult);
                checkModule(scanResult);
            } else if (requestCode == 1 && resultCode == RESULT_OK) {
                String scanResult = bundle.getString("result");
                equipmentCode.setText(scanResult);
                checkEquipment(scanResult);
            }
        }
    }

    /**
     * check模块编码
     * @param moduleID
     */
    protected Boolean checkModule(String moduleID) {
        if ("".equals(moduleID) || moduleID == null) {
            // 模块名称
            moduleName.setText("");
            return false;
        } else {
            if (moduleID.length() == 10) {
                if (moduleID.indexOf("-") == 3) {
                    xPathParseEquipmentInfoXml(moduleID);
                } else {
                    checkScan("非模块编号格式");
                    moduleCode.requestFocus();
                    moduleName.setText("");
                    return false;
                }
            } else {
                checkScan("模块编号位数不对或者为空");
                moduleCode.requestFocus();
                moduleName.setText("");
                return false;
            }
        }
        install.setEnabled(true);
        return true;
    }

    /**
     * check设备编号
     * @param equipmentID
     */
    protected Boolean checkEquipment(String equipmentID) {
        if ("".equals(equipmentID) || equipmentID == null) {
            // 设备地址
            equipmentAddress.setText("");
            return false;
        } else {
            if (equipmentID.length() == 10) {
                if (equipmentID.indexOf("-") == -1) {
                    xPathEquipmentInfoXml(equipmentID);
                } else {
                    checkScan("非设备编号格式");
                    equipmentCode.requestFocus();
                    equipmentAddress.setText("");
                    return false;
                }
            } else {
                checkScan("设备编号位数不对或者为空");
                equipmentCode.requestFocus();
                equipmentAddress.setText("");
                return false;
            }
        }
        install.setEnabled(true);
        return true;
    }

    /**
     * 判断［模块编号］是否存在 根据［模块编号］获取模块名称
     * @param moduleID
     */
    private void xPathParseEquipmentInfoXml(String moduleID) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.equipmentPath)));
            NodeList equipmentList = (NodeList)xPath.evaluate("/root/EquipmentType/Equipment[@Code='" + moduleID + "']", inputSource, XPathConstants.NODESET);
            if (equipmentList != null && equipmentList.getLength() > 0) {
                Element element = (Element)equipmentList.item(0);
                // 模块名称
                moduleName.setText(element.getAttribute("Name"));
                install.setEnabled(true);
            } else {
                checkScan("模块编号不存在");
                moduleCode.requestFocus();
                moduleName.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断［设备编号］是否存在 根据［设备编号］获取设备地址
     * @param equipmentID
     */
    private void xPathEquipmentInfoXml(String equipmentID) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(getResources().getString(R.string.equipmentPath)));
            NodeList equipmentList = (NodeList)xPath.evaluate("/root/EquipmentType/Equipment[@Parent='" + equipmentID + "']", inputSource, XPathConstants.NODESET);
            if (equipmentList != null && equipmentList.getLength() > 0) {
                Element element = (Element)equipmentList.item(0);
                // 设备地址
                equipmentAddress.setText(element.getAttribute("Location"));
                install.setEnabled(true);
            } else {
                checkScan("设备编号不存在");
                equipmentCode.requestFocus();
                equipmentAddress.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * check扫一扫
     */
    private void checkScan(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ModuleInstallActivity.this);
        builder1.setTitle("扫一扫失败");
        builder1.setMessage(message);
        builder1.setPositiveButton("确定", null);
        builder1.show();
    }
}
