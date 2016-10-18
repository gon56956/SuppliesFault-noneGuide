package com.zzmetro.suppliesfault.util;

import android.content.Context;
import android.widget.Toast;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * Created by mayunpeng on 16/7/26.
 */
public class Util {

    private static Toast toast;
    // 上传故障单路径
    private static String uploadTroubleTicketsPath = "/data/data/com.zzmetro.suppliesfault/shared_prefs/uploadTroubleTickets.xml";
    // 未完成故障单路径
    private static String ticketPath = "/data/data/com.zzmetro.suppliesfault/shared_prefs/ticketInfo.xml";
    // 模块信息路径
    private static String modulePath = "/data/data/com.zzmetro.suppliesfault/shared_prefs/module.xml";
    // 个人物资路径
    private static String myMaterialPath = "/data/data/com.zzmetro.suppliesfault/shared_prefs/myMaterial.xml";
    // 消耗物资总数路径
    private static String materialCountPath = "/data/data/com.zzmetro.suppliesfault/shared_prefs/materialCount.xml";
    // XML头
    private static String titleXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<root>\r\n<NewTicket Count=\"0\">\r\n</NewTicket>\r\n<OldTicket Count=\"0\">\r\n</OldTicket>\r\n</root>";
    private static String materialTitleXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<root>\r\n</root>";
    private static String modulelTitleXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<root>\r\n<Install Count=\"0\">\r\n</Install>\r\n<Uninstall Count=\"0\">\r\n</Uninstall>\r\n</root>";
    // 正则表达式：验证IP
    private static final String IP = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    // 执行成功
    public static final String SUCESS = "1";
    // 执行失败
    public static final String ERR_FAIL = "0";
    // 网络异常
    public static final String NETERROR = "3";
    // 非法用户
    public static final String UNLAWFULUSER = "21";
    // 字符串转化XML
    public static final String ERR_PARSER_XML = "-1";
    // IO错误
    public static final String ERR_IO = "-2";
    // 服务器内部错误
    public static final String ERR_SYSTEM = "-3";
    // SAX解析XML错误
    public static final String ERR_SAX = "-4";
    // 数据库执行错误
    public static final String ERR_DB = "-5";
    // 字符串转化日期错误
    public static final String ERR_PARSER_DATE = "-6";
    // 参数不合法
    public static final String ERR_PARAM = "-7";
    // 非法终端
    public static final String ERR_TERMINAL = "-8";
    // 命令错误
    public static final String ERR_COMMAND = "-9";
    // 线路错误
    public static final String LINEERROR = "-10";

    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    /**
     * 检验IP地址
     * @param ipAddress
     * @return
     */
    public static boolean isIPAddress(String ipAddress) {
        return Pattern.matches(IP, ipAddress);
    }

    /**
     * 获取系统时间
     */
    public static String getSystemDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd    HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String date = formatter.format(curDate);

        return date;
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return 文件存在返回 false
     */
    public static boolean checkFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 判断文件是否存在，不存在创建文件
     *
     * @param fileName
     */
    public static void createFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 字符串转换为DOCUMENT
     *
     * @param xmlStr 字符串
     * @return doc JDOM的Document
     * @throws Exception
     */
    public static Document string2Doc(String xmlStr) throws Exception {
        Reader in = new StringReader(xmlStr);
        Document doc = (new SAXBuilder()).build(in);
        return doc;
    }

    /**
     * DOCUMENT转换为字符串
     *
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static String doc2String(String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        FileInputStream inputStream = null;
        org.w3c.dom.Document doc = null;
        try {
            builder = factory.newDocumentBuilder();
            /* 通过文件方式读取,注意文件保存的编码和文件的声明编码要一致(默认文件声明是UTF-8) */
            File file = new File(filePath);
            doc = builder.parse(file);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transFormer = transFactory.newTransformer();
            transFormer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transFormer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource();
            domSource.setNode(doc);
            StringWriter sw = new StringWriter();
            StreamResult xmlResult = new StreamResult(sw);
            transFormer.transform(domSource, xmlResult);

            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.toString();
        }
    }


    /**
     * 写入文件中
     *
     * @param doc
     * @param xmlName
     */
    public static void saveXML(Document doc, String xmlName) {
        // 将doc对象输出到文件
        try {
            // 创建xml文件输出流
            XMLOutputter xmlopt = new XMLOutputter();

            // 创建文件输出流
            FileWriter writer = new FileWriter(xmlName);

            // 指定文档格式
            Format fm = Format.getPrettyFormat();
            fm.setEncoding("UTF-8");
            xmlopt.setFormat(fm);

            // 将doc写入到指定的文件中
            xmlopt.output(doc, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除节点
     *
     * @param solutionFaultID
     */
    public static void removeXML(String solutionFaultID, String path, String flag) {
        int oldCount = 0;
        int newCount = 0;
        Map<String, String> map = new HashMap<String, String>();
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(path);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<Element> ticketList = rootElement.getChildren();
            if (jdomTicketInfoXml(solutionFaultID)) {
                // 5.根据根节点获取子节点的List集合
                List<Element> oldTicketList = ((Element) rootElement.getChildren().get(1)).getChildren();
                // 删除节点
                for (int i = 0; i < oldTicketList.size(); i++) {
                    Element elTicket = oldTicketList.get(i);
                    if (elTicket.getAttributeValue("ID").equals(solutionFaultID)) {
                        // 5.根据根节点获取子节点的List集合
                        List<Element> materialList = elTicket.getChildren();
                        for (int j = 0; j < materialList.size(); j++) {
                            Element material = materialList.get(j);
                            String id = material.getAttributeValue("ID");
                            String amount = material.getAttributeValue("Amount");
                            map.put(id, amount);
                        }
                        oldTicketList.remove(elTicket);
                        updataXML(solutionFaultID);
                        // 获取故障单条数
                        if (!"".equals(ticketList.get(1).getAttributeValue("Count")) && ticketList.get(1).getAttributeValue("Count") != null) {
                            oldCount = Integer.parseInt(ticketList.get(1).getAttributeValue("Count"));
                        }
                        ticketList.get(1).setAttribute("Count", String.valueOf(oldCount - 1));
                    }
                    Set set = map.keySet();
                    updataXML(map, set, materialCountPath, "subtract");
                    updataXML(map, set, myMaterialPath, "add");
                }
            } else {
                if ("新建未修复".equals(flag)) {
                    // 5.根据根节点获取子节点的List集合
                    List<Element> newTicketList = ((Element) rootElement.getChildren().get(0)).getChildren();
                    // 删除节点
                    for (int i = 0; i < newTicketList.size(); i++) {
                        Element elTicket = newTicketList.get(i);
                        if (elTicket.getAttributeValue("Downtime").equals(solutionFaultID)) {
                            newTicketList.remove(elTicket);
                        }
                    }
                    // 获取故障单条数
                    if (!"".equals(ticketList.get(0).getAttributeValue("Count")) && ticketList.get(0).getAttributeValue("Count") != null) {
                        newCount = Integer.parseInt(ticketList.get(0).getAttributeValue("Count"));
                    }
                    ticketList.get(0).setAttribute("Count", String.valueOf(newCount - 1));
                } else {
                    // 5.根据根节点获取子节点的List集合
                    List<Element> newTicketList = ((Element) rootElement.getChildren().get(0)).getChildren();
                    // 删除节点
                    for (int i = 0; i < newTicketList.size(); i++) {
                        Element elTicket = newTicketList.get(i);
                        if (elTicket.getAttributeValue("Downtime").equals(solutionFaultID)) {
                            elTicket.setAttribute("Materials", "0");
                            elTicket.setAttribute("Comment", "");
                            elTicket.setAttribute("Status", "1");
                            elTicket.setAttribute("RestoreTime", "");
                            // 5.根据根节点获取子节点的List集合
                            List<Element> materialList = elTicket.getChildren();
                            for (int j = materialList.size() - 1; j >= 0; j--) {
                                Element material = materialList.get(j);
                                String id = material.getAttributeValue("ID");
                                String amount = material.getAttributeValue("Amount");
                                map.put(id, amount);
                                materialList.remove(material);
                            }
                        }
                        Set set = map.keySet();
                        updataXML(map, set, materialCountPath, "subtract");
                        updataXML(map, set, myMaterialPath, "add");
                    }
                }
            }
            // 6.设置输出流和输出格式
            saveXML(document, path);
            // 删除故障单信息
            if ("0".equals(ticketList.get(0).getAttributeValue("Count")) && "0".equals(ticketList.get(1).getAttributeValue("Count"))) {
                File uploadTroubleTickets = new File(uploadTroubleTicketsPath);
                if (uploadTroubleTickets.exists()) {
                    uploadTroubleTickets.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新[未完成故障单]节点
     *
     * @param solutionFaultID
     */
    public static void updataXML(String solutionFaultID) {
        int oldCount = 0;
        int newCount = 0;
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(ticketPath);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<Element> ticketList = rootElement.getChildren();
            // 删除节点
            for (int i = 0; i < ticketList.size(); i++) {
                Element elTicket = ticketList.get(i);
                if (elTicket.getAttributeValue("ID").equals(solutionFaultID)) {
                    elTicket.setAttribute("Status", "1");
                }
            }
            // 6.设置输出流和输出格式
            saveXML(document, ticketPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新[备件]节点
     *
     * @param map
     * @param set
     * @param path
     */
    public static void updataXML(Map<String, String> map, Set set, String path, String countFlag) {
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(path);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<Element> ticketList = rootElement.getChildren();
            // 删除节点
            Iterator iterator = set.iterator();
            if ("subtract".equals(countFlag)) {
                while (iterator.hasNext()) {
                    String id = (String) iterator.next();
                    String amount = map.get(id);
                    for (int i = ticketList.size() - 1; i >= 0; i--) {
                        Element elTicket = ticketList.get(i);
                        if (elTicket.getAttributeValue("ID").equals(id)) {
                            int count = Integer.parseInt(String.valueOf(elTicket.getAttributeValue("Amount")));
                            if (count - Integer.parseInt(amount) <= 0) {
                                ticketList.remove(elTicket);
                            } else {
                                elTicket.setAttribute("Amount", String.valueOf(count - Integer.parseInt(amount)));
                            }
                        }
                    }
                }
            } else {
                while (iterator.hasNext()) {
                    String id = (String) iterator.next();
                    String amount = map.get(id);
                    for (int i = 0; i < ticketList.size(); i++) {
                        Element elTicket = ticketList.get(i);
                        if (elTicket.getAttributeValue("ID").equals(id)) {
                            int count = Integer.parseInt(String.valueOf(elTicket.getAttributeValue("Amount")));
                            elTicket.setAttribute("Amount", String.valueOf(count + Integer.parseInt(amount)));
                        }
                    }
                }
            }
            // 6.设置输出流和输出格式
            saveXML(document, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新建故障单插入[上传故障单XML]
     *
     * @param equipment
     * @param problem
     * @param createDate
     * @param restoreDate
     * @param materials
     * @param comment
     * @param status
     * @return
     */
    public static boolean insertXML(String equipment, String problem, String createDate, String restoreDate, ArrayList<String> materials, String comment, String status) {
        String material = "";
        int count = 0;
        if (materials == null) {
            material = "0";
        } else {
            material = String.valueOf(materials.size());
        }
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 判断文件是否存在
            if (checkFile(uploadTroubleTicketsPath)) {
                // 创建并初始化
                createFile(uploadTroubleTicketsPath);
                Document doc = string2Doc(titleXml);
                saveXML(doc, uploadTroubleTicketsPath);
            }
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(uploadTroubleTicketsPath);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<Element> ticketList = rootElement.getChildren();
            // 获取故障单条数
            if (!"".equals(ticketList.get(0).getAttributeValue("Count")) && ticketList.get(0).getAttributeValue("Count") != null) {
                count = Integer.parseInt(ticketList.get(0).getAttributeValue("Count"));
            }
            ticketList.get(0).setAttribute("Count", String.valueOf(count + 1));
            // 6.追加节点
            Element ticketElement = new Element("Ticket");
            if ("5".equals(status)) {
                ticketElement.setAttribute("Equipment", equipment)
                        .setAttribute("Problem", problem)
                        .setAttribute("Downtime", createDate)
                        .setAttribute("Materials", material)
                        .setAttribute("Comment", comment)
                        .setAttribute("RestoreTime", restoreDate)
                        .setAttribute("Status", status);
            } else {
                ticketElement.setAttribute("Equipment", equipment)
                        .setAttribute("Problem", problem)
                        .setAttribute("Downtime", createDate)
                        .setAttribute("Materials", material)
                        .setAttribute("Comment", comment)
                        .setAttribute("Status", status);
            }
            ticketList.get(0).addContent(ticketElement);
            if (materials != null) {
                for (int i = 0; i < materials.size(); i++) {
                    Element materialsElement = new Element("Material");
                    int x = materials.get(i).indexOf(",");
                    materialsElement.setAttribute("Amount", materials.get(i).substring(x+1,materials.get(i).length())).setAttribute("ID", materials.get(i).substring(0,x));
                    ticketElement.addContent(materialsElement);
                }
                materialCountXml(materials);
            }
            // 7.设置输出流和输出格式
            saveXML(document, uploadTroubleTicketsPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (JDOMException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 修改故障单插入[上传故障单XML]
     *
     * @param comment
     * @param solutionFaultID
     * @param materials
     * @param restoreTime
     * @return
     */
    public static boolean updataXML(String comment, String solutionFaultID, ArrayList<String> materials, String restoreTime) {
        String material = "";
        int count = 0;
        if (materials == null) {
            material = "0";
        } else {
            material = String.valueOf(materials.size());
        }
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 判断文件是否存在
            if (checkFile(uploadTroubleTicketsPath)) {
                // 创建并初始化
                createFile(uploadTroubleTicketsPath);
                Document doc = string2Doc(titleXml);
                saveXML(doc, uploadTroubleTicketsPath);
            }
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(uploadTroubleTicketsPath);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<Element> ticketList = rootElement.getChildren();

            if (jdomTicketInfoXml(solutionFaultID)) {
                // 获取故障单条数
                if (!"".equals(ticketList.get(1).getAttributeValue("Count")) && ticketList.get(1).getAttributeValue("Count") != null) {
                    count = Integer.parseInt(ticketList.get(1).getAttributeValue("Count"));
                }
                ticketList.get(1).setAttribute("Count", String.valueOf(count + 1));
                // 6.追加节点
                Element ticketElement = new Element("Ticket");
                ticketElement.setAttribute("Comment", comment)
                        .setAttribute("ID", solutionFaultID)
                        .setAttribute("Materials", material)
                        .setAttribute("RestoreTime", restoreTime);
                ticketList.get(1).addContent(ticketElement);
                if (materials != null) {
                    for (int i = 0; i < materials.size(); i++) {
                        Element materialsElement = new Element("Material");
                        int x = materials.get(i).indexOf(",");
                        materialsElement.setAttribute("Amount", materials.get(i).substring(x+1,materials.get(i).length())).setAttribute("ID", materials.get(i).substring(0,x));
                        ticketElement.addContent(materialsElement);
                    }
                    materialCountXml(materials);
                }
                // 7.设置输出流和输出格式
                saveXML(document, uploadTroubleTicketsPath);
            } else {
                // 5.根据根节点获取子节点的List集合
                List<Element> newTicketList = ((Element) rootElement.getChildren().get(0)).getChildren();
                for (int i = 0; i < newTicketList.size(); i++) {
                    Element elTicket = newTicketList.get(i);
                    if (elTicket.getAttributeValue("Downtime").equals(solutionFaultID)) {
                        elTicket.setAttribute("Status", "5");
                        elTicket.setAttribute("Comment", comment);
                        elTicket.setAttribute("RestoreTime", restoreTime);
                        elTicket.setAttribute("Materials", String.valueOf(materials.size()));
                        if (materials != null) {
                            for (int j = 0; j < materials.size(); j++) {
                                Element materialsElement = new Element("Material");
                                int x = materials.get(j).indexOf(",");
                                materialsElement.setAttribute("Amount", materials.get(j).substring(x+1,materials.get(j).length())).setAttribute("ID", materials.get(j).substring(0,x));
                                elTicket.addContent(materialsElement);
                            }
                            materialCountXml(materials);
                        }
                    }
                }
                // 7.设置输出流和输出格式
                saveXML(document, uploadTroubleTicketsPath);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (JDOMException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断［处理故障单号］是否存在 根据［处理故障单号］设置控件活性非活性
     *
     * @param solutionFaultID
     */
    public static boolean jdomTicketInfoXml(String solutionFaultID) {
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(ticketPath);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 判断［处理故障单号］是否存在
            List<Element> ticketList = rootElement.getChildren();
            for (int i = 0; i < ticketList.size(); i++) {
                Element elTicket = ticketList.get(i);
                if (solutionFaultID.equals(elTicket.getAttributeValue("ID"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断［处理故障单号］是否存在 根据［处理故障单号］设置控件活性非活性
     *
     * @param solutionFaultID
     */
    public static boolean jdomUploadTroubleTicketsInfoXml(String solutionFaultID) {
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(uploadTroubleTicketsPath);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 判断［处理故障单号］是否存在
            List<Element> ticketList = rootElement.getChildren();
            List<Element> elTicket = ticketList.get(1).getChildren();
            for (int i = 0; i < elTicket.size(); i++) {
                Element ticket = elTicket.get(i);
                if (solutionFaultID.equals(ticket.getAttributeValue("ID"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 消耗物资总数
     * @param materials
     */
    public static void materialCountXml(ArrayList<String> materials) {
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 判断文件是否存在
            if (checkFile(materialCountPath)) {
                // 创建并初始化
                createFile(materialCountPath);
                Document doc = string2Doc(materialTitleXml);
                saveXML(doc, materialCountPath);
            }
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream inMaterial = new FileInputStream(materialCountPath);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document documentMaterial = saxBuilder.build(inMaterial);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElementMaterial = documentMaterial.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<Element> materialList = rootElementMaterial.getChildren();

            if (materialList != null && materialList.size() > 0) {
                boolean flg = false;
                // 消耗物资
                for (int m = 0; m < materials.size(); m++) {
                    int x = materials.get(m).indexOf(",");

                    // 消耗总物资
                    for (int n = 0; n < materialList.size(); n++) {
                        Element material = materialList.get(n);
                        if (String.valueOf(material.getAttributeValue("ID")).equals(materials.get(m).substring(0,x))) {
                            int amount = Integer.parseInt(String.valueOf(material.getAttributeValue("Amount")));
                            int count = Integer.parseInt(materials.get(m).substring(x+1,materials.get(m).length()));
                            material.setAttribute("Amount", String.valueOf(amount + count));
                            flg = true;
                        }
                    }

                    if (flg) {} else {
                        // 没有匹配到追加
                        Element materialsElement = new Element("Material");
                        materialsElement.setAttribute("Amount", materials.get(m).substring(x+1,materials.get(m).length())).setAttribute("ID", materials.get(m).substring(0,x));
                        rootElementMaterial.addContent(materialsElement);
                    }
                }
            } else {
                for (int i = 0; i < materials.size(); i++) {
                    Element materialsElement = new Element("Material");
                    int x = materials.get(i).indexOf(",");
                    materialsElement.setAttribute("Amount", materials.get(i).substring(x+1,materials.get(i).length())).setAttribute("ID", materials.get(i).substring(0,x));
                    rootElementMaterial.addContent(materialsElement);
                }
            }
            saveXML(documentMaterial, materialCountPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 新建模块安装卸载信息插入[模块信息XML]
     *
     * @param moduleCode
     * @param equipmentCode
     * @param operateTime
     * @param installFlg ["1" 安装; "2" 卸载]
     * @return
     */
    public static boolean insertXML(String moduleCode , String equipmentCode, String operateTime, String installFlg) {
        int count = 0;
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 判断文件是否存在
            if (checkFile(modulePath)) {
                // 创建并初始化
                createFile(modulePath);
                Document doc = string2Doc(modulelTitleXml);
                saveXML(doc, modulePath);
            }
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(modulePath);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<Element> childrenList = rootElement.getChildren();
            if ("1".equals(installFlg)) {
                // 获取故障单条数
                if (!"".equals(childrenList.get(0).getAttributeValue("Count")) && childrenList.get(0).getAttributeValue("Count") != null) {
                    count = Integer.parseInt(childrenList.get(0).getAttributeValue("Count"));
                }
                childrenList.get(0).setAttribute("Count", String.valueOf(count + 1));
                // 6.追加节点
                Element itemElement = new Element("Item");
                itemElement.setAttribute("equipmentCode", equipmentCode)
                        .setAttribute("operateTime", operateTime)
                        .setAttribute("moduleCode", moduleCode);
                childrenList.get(0).addContent(itemElement);
            } else if ("2".equals(installFlg)) {
                // 获取故障单条数
                if (!"".equals(childrenList.get(1).getAttributeValue("Count")) && childrenList.get(1).getAttributeValue("Count") != null) {
                    count = Integer.parseInt(childrenList.get(1).getAttributeValue("Count"));
                }
                childrenList.get(1).setAttribute("Count", String.valueOf(count + 1));
                // 6.追加节点
                Element itemElement = new Element("Item");
                itemElement.setAttribute("equipmentCode", equipmentCode)
                        .setAttribute("operateTime", operateTime)
                        .setAttribute("moduleCode", moduleCode);
                childrenList.get(1).addContent(itemElement);
            }
            // 7.设置输出流和输出格式
            saveXML(document, modulePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (JDOMException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 修改个人物资插入[个人物资XML]
     *
     * @param materials
     * @return
     */
    public static boolean updataXML(ArrayList<String> materials) {
        // 1.创建一个SAXBuilder对象
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            // 2.创建一个输入流，将xml文件加载到输入流
            InputStream in = new FileInputStream(myMaterialPath);
            // 3.通过SAXBuilder的build方法将输入流加载到SAXBuilder中
            Document document = saxBuilder.build(in);
            // 4.通过Document对象获取xml文件的根节点
            Element rootElement = document.getRootElement();
            // 5.根据根节点获取子节点的List集合
            List<Element> materialsList = rootElement.getChildren();
            if (materialsList != null && materialsList.size() > 0) {
                // 消耗物资
                for (int i = 0; i < materials.size(); i++) {
                    int x = materials.get(i).indexOf(",");

                    // 消耗总物资
                    for (int j = 0; j < materialsList.size(); j++) {
                        // 6.设置节点属性
                        Element material = materialsList.get(j);
                        if (String.valueOf(material.getAttributeValue("ID")).equals(materials.get(i).substring(0,x))) {
                            int amount = Integer.parseInt(String.valueOf(material.getAttributeValue("Amount")));
                            int count = Integer.parseInt(materials.get(i).substring(x+1,materials.get(i).length()));
                            material.setAttribute("Amount", String.valueOf(amount - count));
                        }
                    }
                }
            }
            // 7.设置输出流和输出格式
            saveXML(document, myMaterialPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (JDOMException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除xml文件
     */
    public static void deleteXml() {
        // 删除设备信息
        File equipmentInfo = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/equipmentInfo.xml");
        if (equipmentInfo.exists()) {
            equipmentInfo.delete();
        }
        // 删除故障分类
        File problemClassInfo = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/problemClassInfo.xml");
        if (problemClassInfo.exists()) {
            problemClassInfo.delete();
        }
        // 删除未完成故障单
        File ticketInfo = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/ticketInfo.xml");
        if (ticketInfo.exists()) {
            ticketInfo.delete();
        }
        // 删除个人物资
        File myMaterial = new File("/data/data/com.zzmetro.suppliesfault/shared_prefs/myMaterial.xml");
        if (myMaterial.exists()) {
            myMaterial.delete();
        }
    }

    /**
     * 获取获取个人物资
     */
    public static String xPathParseMyMaterialXml(String id) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            InputSource inputSource = new InputSource(new FileInputStream(myMaterialPath));
            NodeList materialList = (NodeList)xPath.evaluate("/root/Material[@ID='" + id + "']", inputSource, XPathConstants.NODESET);
            if (materialList != null && materialList.getLength() > 0) {
                org.w3c.dom.Element element = (org.w3c.dom.Element)materialList.item(0);
                return element.getAttribute("Name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
