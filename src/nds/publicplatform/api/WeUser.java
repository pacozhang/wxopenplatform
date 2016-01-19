package nds.publicplatform.api;

import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.publicweixin.ext.common.WxPublicpartyControl;
import nds.publicweixin.ext.tools.RestUtils;

public class WeUser {
private static Hashtable<String,WeUser> users;
	
	private static Logger logger= LoggerManager.getInstance().getLogger(WeUser.class.getName());	 
	private static String weURL=WebUtils.getProperty("weixin.we_get_userinfo_URL","");
	private static String weAttentionURL=WebUtils.getProperty("weixin.we_get_attention_user_URL","");
	
	private WxPublicControl wpc;
	private WxPublicpartyControl wppc;
	private static Hashtable<String,WeUser> wusers;
	private WeUser(WxPublicControl wpc){
		this.wpc=wpc;
		this.wppc=wpc.getPpc();
	}
	
	/**
	 * 
	 * @param pappid ���ں�APPID
	 * @return
	 */
	public static synchronized WeUser getInstance(String pappid){
		if(nds.util.Validator.isNull(pappid)){return null;}
		
		WeUser instance=null;
		if(wusers==null){
			wusers=new Hashtable<String,WeUser>();
			
			WxPublicControl twpc=WxPublicControl.getInstance(pappid);
			instance=new WeUser(twpc);
			wusers.put(pappid, instance);
		}else if(wusers.containsKey(pappid)){
			instance=wusers.get(pappid);
		}else{
			WxPublicControl twpc=WxPublicControl.getInstance(pappid);
			instance=new WeUser(twpc);
			wusers.put(pappid, instance);
		}

		return instance;
	}

	public JSONObject getUser(String openid){
		WeUser user=null;
		JSONObject jo=new JSONObject();
		
		JSONObject atoken=wpc.getAccessToken();
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
		
		HashMap<String, String> params =new HashMap<String, String>();
		params.put("access_token",token);
		params.put("openid",openid);
		ValueHolder vh=null;
		logger.debug("get_user");
		
		
		try{
			vh=RestUtils.sendRequest(weURL,params,"POST");
			String result=(String) vh.get("message");
			logger.debug("get user info result->"+result);
			jo= new JSONObject(result);
			/*jo.put("appid", appid);
			user=pars_result(jo);
			if(user!=null&&nds.util.Validator.isNotNull(user.getOpenid())){users.put(user.getOpenid(), user);}*/
		}catch (Throwable tx){
			logger.debug("����ƽ̨����ͨ���ϰ�!"+tx.getMessage());
		}
		
		return jo;
	}
}
