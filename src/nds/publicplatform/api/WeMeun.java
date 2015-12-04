package nds.publicplatform.api;

import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.publicweixin.ext.common.WxPublicpartyControl;
import nds.publicweixin.ext.tools.RestUtils;
import nds.util.NDSException;
import nds.publicweixin.ext.tools.WeixinSipStatus;

public class WeMeun {
	private static Logger logger= LoggerManager.getInstance().getLogger(WeMeun.class.getName());
	private static String weCreateURL=WebUtils.getProperty("weixin.we_create_meun_URL","");
	private static String weGetURL=WebUtils.getProperty("weixin.we_get_meun_URL","");
	private static String weDeleteURL=WebUtils.getProperty("weixin.we_delete_meun_URL","");
	
	private WxPublicControl wpc;
	private WxPublicpartyControl wppc;
	private static Hashtable<String,WeMeun> wmenus;
	private WeMeun(WxPublicControl wpc){
		this.wpc=wpc;
		this.wppc=wpc.getPpc();
	}
	
	/**
	 * 
	 * @param pappid ���ں�APPID
	 * @return
	 */
	public static synchronized WeMeun getInstance(String pappid){
		if(nds.util.Validator.isNull(pappid)){return null;}
		
		WeMeun instance=null;
		if(wmenus==null){
			wmenus=new Hashtable<String,WeMeun>();
			
			WxPublicControl twpc=WxPublicControl.getInstance(pappid);
			instance=new WeMeun(twpc);
			wmenus.put(pappid, instance);
		}else if(wmenus.containsKey(pappid)){
			instance=wmenus.get(pappid);
		}else{
			WxPublicControl twpc=WxPublicControl.getInstance(pappid);
			instance=new WeMeun(twpc);
			wmenus.put(pappid, instance);
		}

		return instance;
	}
	
	public JSONObject createMenu(String menus) throws NDSException{
		String result=null;
		JSONObject jo=new JSONObject();
		JSONObject sumo=null;
		JSONObject jomenus=null;
		String url=weCreateURL;
		String token=wpc.getAccessToken();
		if(nds.util.Validator.isNull(token)) {
			try {
				jo.put("code", -1);
				jo.put("message", "��������Ȩ");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jo;
		}
		
		
		/*String purl="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wc.getAppid()+"&redirect_uri=http://www.demo.com/index.jsp&response_type=code&scope=snsapi_base&state=1#wechat_redirect"; 
		if(nds.util.Validator.isNull(menus)) {
			menus="{button:[{\"type\":\"click\",name:\"���ո���\",\"key\":\"V1001_TODAY_MUSIC\"},";
			menus+="{\"type\":\"click\",\"name\":\"���ּ��\",\"key\":\"V1001_TODAY_SINGER\"},";
			menus+="{\"name\":\"�˵�\",\"sub_button\":[";
			menus+="{\"type\":\"view\",\"name\":\"����\",\"url\":\"http://www.google.com/\" },";
			menus+="{\"type\":\"view\",\"name\":\"��Ƶ\",\"url\":\""+purl+"\"},";
			menus+="{\"type\":\"click\",\"name\":\"��һ������\",\"key\":\"V1001_GOOD\"}";
			menus+="]}]}";
		}
		
		if(nds.util.Validator.isNull(menus)) {
			String gwurl="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wc.getAppid()+"&redirect_uri=http://www.demo.com&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
			String scurl="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wc.getAppid()+"&redirect_uri=http://www.demo.com/mall.jsp&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
			String hyurl="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+wc.getAppid()+"&redirect_uri=http://www.demo.com/html/nds/oto/webapp/usercenter/index.vml&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
			menus="{button:[{\"type\":\"view\",\"name\":\"�����̳�\",\"url\":\""+scurl+"\"}";
			menus+=",{\"type\":\"view\",\"name\":\"��������\",\"url\":\""+gwurl+"\"}";
			menus+=",{\"type\":\"view\",\"name\":\"������Ա\",\"url\":\""+hyurl+"\"}]}";
		}
		try{jomenus=new JSONObject(menus);}
		catch(Exception e){}
		
		JSONArray subMenus=null;
		
		if(menus==null){
			returns="button is null!";
			logger.debug("button is null!");
			throw new NDSException("button is null");
		}
		JSONArray buttons=jomenus.optJSONArray("button");
		
		if(buttons!=null&&buttons.length()>3){
			returns="button length larger than 3!";
			logger.debug("button length larger than 3!");
			throw new NDSException("button length larger than 3");
		}
		
		for(int i=0;i<buttons.length();i++){
			sumo=(JSONObject)buttons.optJSONObject(i);
			if(sumo!=null&&sumo.has("sub_button")){
				subMenus=sumo.optJSONArray("sub_button");
				if(subMenus.length()>5){
					returns="sub_button length larger than 5!";
					logger.debug("sub_button length larger than 5!");
					throw new NDSException("sub_button length larger than 5");
				}
			}
		}*/
		
		HashMap<String, String> params =new HashMap<String, String>();
		params.put("access_token",token);
		try{
			url+=RestUtils.delimit(params.entrySet(), false);
		}catch(Exception e){}
		
		ValueHolder vh=null;
		logger.debug("create_menu");
		try{
			vh=RestUtils.sendRequest_buff(url,menus,"POST");
			result=(String) vh.get("message");
			logger.debug("create menu result->"+result);
			JSONObject tjo=new JSONObject(result);
			String returns="�����˵�ʧ�ܣ�";
			if(tjo!=null&&tjo.has("errcode")) {
				WeixinSipStatus s=WeixinSipStatus.getStatus(String.valueOf(tjo.optInt("errcode",-1)));
				if(s!=null) {returns=s.toString();}
				else if(tjo.has("errmsg")){returns=tjo.optString("errmsg");}
				jo.put("code", tjo.optInt("errcode",-1));
				jo.put("message", returns);
			}else {
				jo.put("code", -1);
				jo.put("message", returns);
			}
		} catch (Throwable tx) {
			try {
				jo.put("code", "-1");
				jo.put("message", "����ƽ̨����ͨ���ϰ�->"+tx.getMessage());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			logger.debug("����ƽ̨����ͨ���ϰ�->"+tx.getMessage());
		}
		return jo;
	}
		
	public JSONObject getMenus(){
		String result=null;
		JSONObject jo=null;
		String token=wpc.getAccessToken();
		if(nds.util.Validator.isNull(token)) {
			jo=new JSONObject();
			try {
				jo.put("code", -1);
				jo.put("message", "��������Ȩ");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jo;
		}
		
		HashMap<String, String> params =new HashMap<String, String>();
		params.put("access_token",token);
		
		ValueHolder vh=null;
		logger.debug("get_menu");
		try{
			vh=RestUtils.sendRequest(weGetURL,params,"POST");
			result=(String) vh.get("message");
			JSONObject tjo=new JSONObject(result);
			jo= new JSONObject();
			String returns="��ȡ�˵�ʧ�ܣ�";
			if(tjo!=null&&tjo.has("errcode")) {
				WeixinSipStatus s=WeixinSipStatus.getStatus(String.valueOf(tjo.optInt("errcode",-1)));
				if(s!=null) {returns=s.toString();}
				else if(tjo.has("errmsg")){returns=tjo.optString("errmsg");}
				jo.put("code", jo.optInt("errcode",-1));
				jo.put("message", returns);
			}else if(tjo!=null&&tjo.has("button")) {
				jo.put("code", 0);
				jo.put("message", result);
			}else {
				jo.put("code", -1);
				jo.put("message", returns);
			}
		} catch (Throwable tx) {
			logger.debug("����ƽ̨����ͨ���ϰ�!");
			try {
				jo.put("code", -1);
				jo.put("message", "����ƽ̨����ͨ���ϰ�");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//throw new NDSException("sub_button length larger than 5",tx);
		}
		return jo;
	}
	
	public JSONObject deleteMenus(){
		String result=null;
		JSONObject jo=new JSONObject();
		JSONObject atjo=wppc.getAccessToken();
		
		//�ж�ACCESSTOKEN�Ƿ��ȡ�ɹ�
		if(atjo==null||!"0".equals(atjo.optString("code"))) {
			return atjo;
		}
		String token=atjo.optJSONObject("data").optString("component_access_token");
		if(nds.util.Validator.isNull(token)) {
			try {
				jo.put("code", -1);
				jo.put("message", "��������Ȩ");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jo;
		}
		
		HashMap<String, String> params =new HashMap<String, String>();
		params.put("access_token",token);
		
		ValueHolder vh=null;
		logger.debug("delete_menu");
		try{
			vh=RestUtils.sendRequest(weDeleteURL,params,"POST");
			result=(String) vh.get("message");
			logger.debug("delete menu result->"+result);
			JSONObject tjo=new JSONObject(result);
			String returns="ɾ���˵�ʧ�ܣ�";
			if(tjo!=null&&tjo.has("errcode")) {
				WeixinSipStatus s=WeixinSipStatus.getStatus(String.valueOf(tjo.optInt("errcode",-1)));
				if(s!=null) {returns=s.toString();}
				else if(tjo.has("errmsg")){returns=tjo.optString("errmsg");}
				jo= new JSONObject();
				jo.put("code", tjo.optInt("errcode",-1));
				jo.put("message", tjo.optString("errmsg"));
			}else {
				jo= new JSONObject();
				jo.put("code", -1);
				jo.put("message", returns);
			}
		} catch (Throwable tx) {
			logger.debug("����ƽ̨����ͨ���ϰ�->"+tx.getMessage());
			try {
				jo=new JSONObject();
				jo.put("code", "-1");
				jo.put("message", "����ƽ̨����ͨ���ϰ�->"+tx.getMessage());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//throw new NDSException("sub_button length larger than 5",tx);
		}
		
		return jo;
	}
}
