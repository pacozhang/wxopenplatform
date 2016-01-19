package nds.publicplatform.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import com.swetake.util.Qrcode;

import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.publicweixin.ext.tools.RestUtils;
import nds.publicweixin.ext.tools.WeixinSipStatus;
import nds.util.ImageUtils;

public class WePopularizeSupport {
	private static Logger logger= LoggerManager.getInstance().getLogger(WePopularizeSupport.class.getName());
	private static String createquickmarkurl=WebUtils.getProperty("weixin.we_create_quickmark_URL","");
	private static String getquickmarkurl=WebUtils.getProperty("weixin.we_get_quickmark_URL","");
	
	private Quickmark qk=null; 

	/**
	 * ������ʱ��ά��
	 * @param wc
	 * @param pjo
	 * @return
	 */
	public JSONObject createTemporaryQuickmark(WxPublicControl wc,JSONObject pjo){
		String url=createquickmarkurl;
		String result=null;
		String ticket=null;
		JSONObject jo=new JSONObject();
		int validTime=pjo.optInt("validTime",604800);
		String scene_id=pjo.optString("scene_id");

		JSONObject atoken=wc.getAccessToken();
		//�ж�ACCESSTOKEN�Ƿ��ȡ�ɹ�
		if(atoken==null||!"0".equals(atoken.optString("code"))) {
			try {
				jo.put("code", "-1");
				jo.put("message", "��������Ȩ");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return jo;
		}
				
		String token=atoken.optJSONObject("data").optString("authorizer_access_token");
		if(nds.util.Validator.isNull(token)) {
			try {
				jo.put("code", -1);
				jo.put("message", "��������Ȩ");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jo;
		}
		
		HashMap<String,String> paras=new HashMap<String,String>();
		paras.put("access_token",token);
		
		String message="{\"expire_seconds\":"+validTime;
		//message+=",\"action_name\":\"QR_SCENE\",\"action_info\": {\"scene\": {\"scene_id\":"+scene_id+"}}}";
		
		message+=",{\"action_name\":\"QR_SCENE\",\"action_info\":"+pjo.optString("action_info")+"}}";

		ValueHolder vh=null;
		logger.debug("create_temporary_quickmark");
		try{
			url+=RestUtils.delimit(paras.entrySet(), true);

			vh=RestUtils.sendRequest_buff(url, message, "POST");
			result=(String) vh.get("message");
			logger.debug("create_temporary_quickmark result->"+result);
			JSONObject tjo= new JSONObject(result);
			String returns="������ʱ��Ψ��ʧ�ܣ�";
			if(tjo!=null&&tjo.has("errcode")) {
				WeixinSipStatus s=WeixinSipStatus.getStatus(String.valueOf(jo.optInt("errcode",-1)));
				if(s!=null) {returns=s.toString();}
				else if(jo.has("errmsg")){returns=tjo.optString("errmsg");}
				jo.put("code", tjo.optInt("errcode",-1));
				jo.put("message", returns);
			}else if(tjo!=null&&tjo.has("ticket")) {
				jo.put("code", 0);
				jo.put("message", "������ʱ��Ψ��ɹ�");
				jo.put("ticket", tjo.optString("ticket"));
				jo.put("expire_seconds", tjo.optInt("expire_seconds",1800));
				jo.put("url", tjo.optString("url"));
			}else {
				jo.put("code", -1);
				jo.put("message", returns);
			}
		}catch(Exception e){
			result="����ƽ̨����ͨ���ϰ�!";
			logger.debug("����ƽ̨����ͨ���ϰ�!"+e.getMessage());
		}
		
		return jo;
	}
	
	/**
	 * �������ö�ά��
	 * @param wc
	 * @param pjo
	 * @return
	 */
	public JSONObject createPermanenceQuickmark(WxPublicControl wc,JSONObject pjo){
		String url=createquickmarkurl;
		String result=null;
		String ticket=null;
		JSONObject jo=new JSONObject();
		
		JSONObject atoken=wc.getAccessToken();
		//�ж�ACCESSTOKEN�Ƿ��ȡ�ɹ�
		if(atoken==null||!"0".equals(atoken.optString("code"))) {
			try {
				jo.put("code", "-1");
				jo.put("message", "��������Ȩ");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return jo;
		}
				
		String token=atoken.optJSONObject("data").optString("authorizer_access_token");
		if(nds.util.Validator.isNull(token)) {
			try {
				jo.put("code", -1);
				jo.put("message", "��������Ȩ");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jo;
		}
		
		HashMap<String,String> paras=new HashMap<String,String>();
		paras.put("access_token",token);

		//String message ="{\"action_name\":\"QR_LIMIT_STR_SCENE\",\"action_info\": {\"scene\": {\"scene_id\":\"store"+scene_id+"\"}}}";
		String message ="{\"action_name\":\"QR_LIMIT_STR_SCENE\",\"action_info\":"+pjo.optString("action_info")+"}}";
		ValueHolder vh=null;
		logger.debug("create_permanence_quickmark");
		try{
			url+=RestUtils.delimit(paras.entrySet(), true);

			vh=RestUtils.sendRequest_buff(url, message, "POST");
			result=(String) vh.get("message");
			logger.debug("create_permanence_quickmark result->"+result);
			JSONObject tjo= new JSONObject(result);
			String returns="�������ö�Ψ��ʧ�ܣ�";
			if(tjo!=null&&tjo.has("errcode")) {
				WeixinSipStatus s=WeixinSipStatus.getStatus(String.valueOf(jo.optInt("errcode",-1)));
				if(s!=null) {returns=s.toString();}
				else if(jo.has("errmsg")){returns=tjo.optString("errmsg");}
				jo.put("code", tjo.optInt("errcode",-1));
				jo.put("message", returns);
			}else if(tjo!=null&&tjo.has("ticket")) {
				jo.put("code", 0);
				jo.put("message", "�������ö�Ψ��ɹ�");
				jo.put("ticket", tjo.optString("ticket"));
				jo.put("expire_seconds", tjo.optInt("expire_seconds",1800));
				jo.put("url", tjo.optString("url"));
			}else {
				jo.put("code", -1);
				jo.put("message", returns);
			}
		}catch(Exception e){
			logger.debug("����ƽ̨����ͨ���ϰ�->"+e.getMessage());
			try {
				jo.put("code", -1);
				jo.put("message", "����ƽ̨����ͨ���ϰ�->"+e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		return jo;
	}
	
	/**
	 * ��΢�����ض�ά��
	 * @param ticket
	 * @param filePath
	 * @return
	 */
	public JSONObject getQuickmark(String ticket,String filePath){
		String url=getquickmarkurl;
		byte[] result=null;
		JSONObject jo=null;
		HashMap<String,String> paras=new HashMap<String,String>();

		ValueHolder vh=null;
		logger.debug("get_quickmark");
		try{
			paras.put("ticket",URLEncoder.encode(ticket,"utf-8"));
			url+=RestUtils.delimit(paras.entrySet(), false);
			
			vh=RestUtils.sendRequest_buffs(url, null, "GET");
			result=(byte[])vh.get("message");
			OutputStream os = new FileOutputStream(filePath);
			os.write(result);
			os.close();
			logger.debug("resulttype->"+vh.get("message").getClass().getSimpleName());
		}catch(Exception e){
			logger.debug("����ƽ̨����ͨ���ϰ�!");
		}
		
		return jo;
	}
	
	/**
	 * ���ɶ�ά��(QRCode)ͼƬ
	 * @param content �洢����
	 * @param imgPath ͼƬ·��
	 * @param imgType ͼƬ����
	 * @param size ��ά��ߴ�
	 * @param logoPath logoͼƬ��ַ
	 */
	public JSONObject encoderQRCode(String content, String imgPath, String imgType, int size, String logoPath) {
		JSONObject jo=new JSONObject();
		try {
			BufferedImage bufImg = this.crateRCodeCommon(content, imgType, size, logoPath);
			
			File f=new File(imgPath);
			if(!f.exists()) {f.mkdirs();}
			// ���ɶ�ά��QRCodeͼƬ
			ImageIO.write(bufImg, imgType, f);
			jo.put("code", 0);
			jo.put("message", "�����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				jo.put("code", -1);
				jo.put("message", e.getMessage());
			}catch(Exception e1) {
				
			}
		}
		return jo;
	}
	
	/** 
	 * ���ɶ�ά��(QRCode)ͼƬ�Ĺ�������
	 * @param content �洢����
	 * @param imgType ͼƬ����
	 * @param size ��ά��ߴ�
	 * @param logoPath logoͼƬ��ַ
	 * @return
	 * @throws Exception 
	 */
	private BufferedImage crateRCodeCommon(String content, String imgType, int size, String logoPath) throws Exception {
		BufferedImage bufImg = null;
		try {
			Qrcode qrcodeHandler = new Qrcode();
			// ���ö�ά���Ŵ��ʣ���ѡL(7%)��M(15%)��Q(25%)��H(30%)���Ŵ���Խ�߿ɴ洢����ϢԽ�٣����Զ�ά�������ȵ�Ҫ��ԽС
			qrcodeHandler.setQrcodeErrorCorrect('L');
			qrcodeHandler.setQrcodeEncodeMode('B');
			// �������ö�ά��ߴ磬ȡֵ��Χ1-40��ֵԽ��ߴ�Խ�󣬿ɴ洢����ϢԽ��
			qrcodeHandler.setQrcodeVersion(9);
			// ������ݵ��ֽ����飬���ñ����ʽ
			byte[] contentBytes = content.getBytes("utf-8");
			// ͼƬ�ߴ�
			int imgSize = 67 + 12 * (size - 1);
			bufImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D gs = bufImg.createGraphics();
			// ���ñ�����ɫ
			gs.setBackground(null);
			gs.clearRect(0, 0, imgSize, imgSize);

			// �趨ͼ����ɫ> BLACK
			gs.setColor(new Color(0, 84, 165));
			// ����ƫ�����������ÿ��ܵ��½�������
			int pixoff = 2;
			// �������> ��ά��
			if (contentBytes.length > 0 && contentBytes.length < 800) {
				boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);
				for (int i = 0; i < codeOut.length; i++) {
					for (int j = 0; j < codeOut.length; j++) {
						if (codeOut[j][i]) {
							gs.fillRect(j * 5 + pixoff, i * 5 + pixoff, 5, 5);
						}
					}
				}
			} else {
				throw new Exception("QRCode content bytes length = " + contentBytes.length + " not in [0, 800].");
			}
			
			if(nds.util.Validator.isNotNull(logoPath)){
				//kunlun
				boolean istf=logoPath.contains("http:");
				if(istf==true){
					String method="POST";
					try {
						FileInputStream fileinput = (FileInputStream) RestUtils.sendRequest_buffs(logoPath, null , method).get("message");
						fileinput.close();
						BufferedImage srcImage = ImageIO.read(fileinput);
						
						gs.drawImage(srcImage, 106,106, null);  
					} catch (Exception e) {
						// TODO: handle exception
						logger.debug("logopath file is not find->"+logoPath);
					}
					
				}else{
					logger.debug("logoPath is��"+logoPath);
					try{
						BufferedImage bi1=ImageUtils.getScaledInstance(logoPath,60,60,"png",true); 
						//Image img = ImageIO.read(new File(ccbPath));//ʵ����һ��Image����
						//��ȡͼƬ������ͼƬ�ڶ�ά������ʼλ�á�
						//System.out.print(imgSize);
						gs.drawImage(bi1, 106,106, null);
					}catch(Exception w){
						logger.debug("logopath file is not find->"+logoPath);
					}
				}
				//end
				//֮ǰ�ı���
//				//��ȡͷ��ͼƬ������ͼƬ��С��
//				//byte[] bos =changePicSize(logo);
//				//ByteArrayInputStream bais = new ByteArrayInputStream(bos);
//				//BufferedImage bi1 =ImageIO.read(bais);
//				//URL url = new URL(logo);
//				//System.out.print(logo);
//				logger.debug("logoPath is��"+logoPath);
//				try{
//					BufferedImage bi1=ImageUtils.getScaledInstance(logoPath,60,60,"png",true); 
//					//Image img = ImageIO.read(new File(ccbPath));//ʵ����һ��Image����
//					//��ȡͼƬ������ͼƬ�ڶ�ά������ʼλ�á�
//					//System.out.print(imgSize);
//					gs.drawImage(bi1, 106,106, null);
//				}catch(Exception w){
//					logger.debug("logopath file is not find->"+logoPath);
//				}
				//end
			}
			gs.dispose();
			bufImg.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("������ά��ʧ�ܣ�"+e.getMessage());
		}
		return bufImg;
	}
	
	private class Quickmark{
		private int validTime;
		private String quickmarkType;
		private int scene_id;
		private String ticket;
	}
}
