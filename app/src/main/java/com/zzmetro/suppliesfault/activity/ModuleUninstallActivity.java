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
 * 项目名称: 故障采集 包: com.zzmetro.suppliesfault.activity 类名称: ModuleUninstallActivity
 * 类描述: 卸载
 * 创建人: mayunpeng 创建时间: 2016/08/08 版本: [v1.0]
 */
public class ModuleUninstallActivity extends AppCompatActivity {

    private String date = "";
    // 模块编号
    private EditText moduleCode;
    // 模块名称,设备编号,设备地址
    private TextView moduleName,equipmentCode,equipmentAddress;
    // 卸载
    private Button uninstall, scanModuleCode;
    private String lineCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_uninstall);

        // 获取参数信息
        SharedPreferences sp = getSharedPreferences("staffInfo", MODE_PRIVATE);
        lineCode = sp.getString("lineCode", "").trim();

        // 模块编号
        moduleCode = (EditText) findViewById(R.id.unModuleCode);
        // 模块名称
        moduleName = (TextView) findViewById(R.id.unModuleName);
        // 设备编号
        equipmentCode = (TextView) findViewById(R.id.unEquipmentCode);
        // 设备地址
        equipmentAddress = (TextView) findViewById(R.id.unEquipmentAddress);
        // 获取【卸载】按钮ID
        uninstall = (Button) findViewById(R.id.uninstall);
        // 扫一扫
        scanModuleCode = (Button) findViewById(R.id.scanModuleCode);

        // button初始化为非活性状态
        uninstall.setEnabled(false);

        // 扫一扫
        scanModuleCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openCameraIntent = new Intent(ModuleUninstallActivity.this, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            }
        });

        // 模块编号监听
        moduleCode.setOnKeyListener(onKey);

        // 卸载监听
        uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkModule(moduleCode.getText().toString().trim())) {
                    // 获取当前系统时间
                    date = Util.getSystemDate();
                    // 调共通写入卸载信息
                    if (Util.insertXML(moduleCode.getText().toString().trim(), equipmentCode.getText().toString().trim(), date, "2")) {
                        Util.showToast(ModuleUninstallActivity.this, moduleName.getText().toString() + "卸载成功");
                        finish();
                    }
                } else {
                    if (TextUtils.isEmpty(moduleCode.getText().toString().trim())) {
                        checkScan("模块编号不能为空");
                        moduleCode.requestFocus();
                    }
                }
            }
        });

        Log.d("ModuleUninstallActivity", "卸载 execute");
    }

    View.OnKeyListener onKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            String moduleID = moduleCode.getText().toString().trim();
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                checkModule(moduleID);
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
            }
        }
    }

    /**
     * 判断［模块编号］是否存在 根据［模块编号］获取设备信息
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
                // 设备编号
                equipmentCode.setText(element.getAttribute("Parent"));
                // 设备地址
                equipmentAddress.setText(element.getAttribute("Location"));
                uninstall.setEnabled(true);
            } else {
                checkScan("模块编号不存在");
                moduleCode.requestFocus();
                // 模块名称
                moduleName.setText("");
                // 设备编号
                equipmentCode.setText("");
                // 设备地址
                equipmentAddress.setText("");
                uninstall.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            // 设备编号
            equipmentCode.setText("");
            // 设备地址
            equipmentAddress.setText("");
            uninstall.setEnabled(false);
            return false;
        } else {
            if (moduleID.length() == 10) {
                if (moduleID.indexOf("-") == 3) {
                    xPathParseEquipmentInfoXml(moduleID);
                } else {
                    checkScan("非模块编号格式");
                    moduleCode.requestFocus();
                    // 模块名称
                    moduleName.setText("");
                    // 设备编号
                    equipmentCode.setText("");
                    // 设备地址
                    equipmentAddress.setText("");
                    uninstall.setEnabled(false);
                    return false;
                }
            } else {
                checkScan("模块编号位数不对或者为空");
                moduleCode.requestFocus();
                // 模块名称
                moduleName.setText("");
                // 设备编号
                equipmentCode.setText("");
                // 设备地址
                equipmentAddress.setText("");
                uninstall.setEnabled(false);
                return false;
            }
        }
        return true;
    }

    /**
     * check扫一扫
     */
    private void checkScan(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ModuleUninstallActivity.this);
        builder1.setTitle("扫一扫失败");
        builder1.setMessage(message);
        builder1.setPositiveButton("确定", null);
        builder1.show();
    }
}
