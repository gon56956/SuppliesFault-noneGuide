package com.zzmetro.suppliesfault.service;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.util.Log;

import com.zzmetro.suppliesfault.util.Util;

/**
 * 访问WebService的工具类
 * 
 * @author mayunpeng
 *
 */
public class WebServiceBase {

	private static String result = "";

	public static String getWebServiceReslut(String arg0, String ip, String port, String lineCode) {
		// WebService 命名空间
		String nameSpace = "http://ws.zzmetro.com/";
		// WebService 调用方法
		String methodName = "Services";
		// EndPoint
        String endPoint;
		if ("1".equals(lineCode.trim())) {
            endPoint = "http://" + ip + ":" + port + "/Maintenance/DataHandlePort";
        } else if ("2".equals(lineCode.trim())){
            endPoint = "http://" + ip + ":" + port + "/Maintenance02/DataHandlePort";
        } else {
            return Util.LINEERROR;
        }
		// SOAP Action
		String soapAction = "http://ws.zzmetro.com/Services";

		// 指定WebService的命名空间和调用的方法名
		SoapObject rpc = new SoapObject(nameSpace, methodName);

		// 设置需调用WebService接口需要传入的一个参数arg0
		rpc.addProperty("arg0", arg0);

		// 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = rpc;

		// 设置是否调用的是dotNet开发的WebService
		envelope.dotNet = false;

		HttpTransportSE transport = new HttpTransportSE(endPoint);

		try {
			// 调用WebService
			transport.call(soapAction, envelope);
		} catch (Exception e) {
			e.printStackTrace();

			String msg = e.getMessage();
			if (e instanceof java.net.ConnectException) {
				// 连接服务器超时，请检查网络
				msg = "3";
			}
			result = msg;
			Log.d("LoginActivity", e.getMessage());
			return result;
		}
		// 获取返回的数据
		SoapObject object = (SoapObject) envelope.bodyIn;
		// 获取返回的结果
		result = object.getProperty(0).toString();
		return result;
	}

}