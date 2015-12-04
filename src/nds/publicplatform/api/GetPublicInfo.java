package nds.publicplatform.api;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;

import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.publicweixin.ext.common.WxPublicpartyControl;
import nds.publicweixin.ext.tools.RestUtils;
import nds.util.Configurations;
import nds.util.WebKeys;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

/**
 * ��ȡ��Ȩ������Ϣ
 * @author paco
 *
 */
public class GetPublicInfo {
	private static Logger logger= LoggerManager.getInstance().getLogger(GetPublicInfo.class.getName());
	private static String weixingetauthorizerinfo=WebUtils.getProperty("weixin.get_authorizer_info","");

	private static Hashtable<String,GetPublicInfo> weixininfos;
	
	private WxPublicControl wpc;
	private WxPublicpartyControl wppc;

	private GetPublicInfo(WxPublicControl wpc) {
		this.wppc=wpc.getPpc();
		this.wpc=wpc;
	}
	
	/**
	 * ��ȡˢ����Ȩʵ����ÿ�����ں�APPIDһ������ʵ��
	 * @param pappid ���ں�APPID
	 * @return
	 */
	public static synchronized GetPublicInfo getInstance(String pappid) {
		GetPublicInfo instance=null;
		logger.debug("pappid->"+pappid);
		
		if(weixininfos==null) {
			weixininfos=new Hashtable<String,GetPublicInfo>();
			WxPublicControl twpc=null;
			
			WeUtilsManager wum=WeUtilsManager.getInstance();
			WeUtils wu=wum.getByAppid(pappid);
			twpc=WxPublicControl.getInstance(pappid);

			instance=new GetPublicInfo(twpc);
			weixininfos.put(pappid, instance);
		}else if(weixininfos.containsKey(pappid)) {
			instance=weixininfos.get(pappid);
		}else {
			WxPublicControl twpc=null;
			
			WeUtilsManager wum=WeUtilsManager.getInstance();
			WeUtils wu=wum.getByAppid(pappid);
			twpc=WxPublicControl.getInstance(pappid);
			
			instance=new GetPublicInfo(twpc);
			weixininfos.put(pappid, instance);
		}
		
		return instance;
	}

	/**
	 * ��ȡ��Ȩ���ں���Ϣ
	 * @return
	 */
	public JSONObject getPublicInfo() {
		JSONObject pinfo=new JSONObject();

		WePublicparty wpp=wppc.getWePublicparty();
		
		JSONObject param=new JSONObject();
		try {
			param.put("component_appid", wpp.getAppid());
			param.put("authorizer_appid", wpc.getWxPublic().getAppId());
		}catch(Exception e){
			
		}
		
		JSONObject atjo=wppc.getAccessToken();
		//�ж�ACCESSTOKEN�Ƿ��ȡ�ɹ�
		if(atjo==null||!"0".equals(atjo.optString("code"))) {
			try {
				pinfo.put("code", "-1");
				pinfo.put("message", "��ȡʧ��");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return pinfo;
		}
		
		String accesstoken=atjo.optJSONObject("data").optString("component_access_token");
		
		ValueHolder vh=null;
		String url=weixingetauthorizerinfo+accesstoken;
		logger.debug("get authorizer info");
		
		try {
			vh=RestUtils.sendRequest_buff(url, param.toString(), "POST");
			String result=(String) vh.get("message");
			logger.debug("get authorizer info result->"+result);
			
			JSONObject tjo=new JSONObject(result);
			if(tjo!=null&&tjo.has("authorizer_info")) {
				//���ع��ںŶ�ά�뵽��������
				String dpath=null;
				String ipath=null;
				JSONObject ttjo=tjo.optJSONObject("authorizer_info");
				String qurl=ttjo.optString("qrcode_url");
				try{
			   		 Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);
			   		ipath = conf.getProperty("webclient.upload", "/act.net/webhome");
				}catch(Exception e) {
					
				}
				ipath+="/"+wpc.getWxPublic().getDoMain()+ "/web_client/wxappcode.jpg";;
				dpath="/servlets/userfolder/web_client/wxappcode.jpg";
				logger.debug("dpath->"+dpath);
				logger.debug("ipath->"+ipath);
				
				try {
					//���ض�ά��
					GetMethod get = new GetMethod(qurl);
					get.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
					HttpClient hc=new HttpClient();
					hc.executeMethod(get);
					BufferedImage image = ImageIO.read(get.getResponseBodyAsStream());
					
					//�����ά��
					File file=new File(ipath.replace("wxappcode.jpg", ""));
					if(!file.exists()) {
						file.mkdirs();
						logger.debug("mkdirs");
					}
					file = new File(ipath);
					if(!file.exists()) {
						logger.debug("mk file");
						file.createNewFile();
					}
					ImageIO.write(image, "jpg", file);
					ttjo.put("old_qrcode_url", qurl);
					ttjo.put("qrcode_url", dpath);
				}catch(Exception e) {
					logger.debug("down qrcode error->"+e.getLocalizedMessage());
					e.printStackTrace();
				}
				
				pinfo.put("code", 0);
				pinfo.put("message", "��ȡ�ɹ�");
				pinfo.put("data", tjo);
			}else {
				pinfo.put("code", -1);
				pinfo.put("message", "��ȡ��Ȩ��Ϣʧ��");
			}
		}catch(Exception e){
			logger.debug("get authorizer infon error->"+e.getLocalizedMessage());
			e.printStackTrace();
			try {
				pinfo.put("code", -1);
				pinfo.put("message", "ˢ����Ȩ��ʧ�ܣ�");
			} catch (JSONException e1) {
				
			}
		}
		
		return pinfo;
	}
}
