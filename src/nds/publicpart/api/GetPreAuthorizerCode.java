package nds.publicpart.api;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicpartyControl;
import nds.publicweixin.ext.tools.RestUtils;

public class GetPreAuthorizerCode {
	private static Logger logger= LoggerManager.getInstance().getLogger(GetPreAuthorizerCode.class.getName());
	private static String weixingetpreauthorizercode=WebUtils.getProperty("weixin.get_pre_authorizer_code_URL","");

	private static Hashtable<String,GetAuthorizerAccessToken> weixinauths;
	
	private WxPublicpartyControl wppc;
	
	//��ȡԤ��Ȩ��
	public JSONObject getPreAuthorizerCode(WxPublicpartyControl wppc) {
		JSONObject preauthcodeinfo =new JSONObject();
		
		JSONObject param=new JSONObject();
		try {
			param.put("component_appid", wppc.getWePublicparty().getAppid());
		}catch(Exception e){
			
		}
		
		ValueHolder vh=null;
		JSONObject atjo=wppc.getAccessToken();
		
		//�ж�ACCESSTOKEN�Ƿ��ȡ�ɹ�
		if(atjo==null||!"0".equals(atjo.optString("code"))) {
			try {
				preauthcodeinfo.put("code", "-1");
				preauthcodeinfo.put("message", "��ȡʧ��");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return preauthcodeinfo;
		}
		
		String accesstoken=atjo.optJSONObject("data").optString("component_access_token");
		String url=weixingetpreauthorizercode+accesstoken;
		logger.debug("get pre authorization code token");
		
		try {
			vh=RestUtils.sendRequest_buff(url, param.toString(), "POST");
			String result=(String) vh.get("message");
			logger.debug("get pre authorization code result->"+result);
			
			JSONObject tjo=new JSONObject(result);
			String returns="��Ԥȡ��Ȩ��ʧ�ܣ�";
			if(tjo!=null&&tjo.has("pre_auth_code")) {
				preauthcodeinfo.put("code", 0);
				preauthcodeinfo.put("message", "��ȡ�ɹ�");
				preauthcodeinfo.put("data", tjo);
			}else {
				preauthcodeinfo.put("code", -1);
				preauthcodeinfo.put("message", returns);
			}
		}catch(Exception e){
			logger.debug("get pre authorization code token error->"+e.getLocalizedMessage());
			e.printStackTrace();
			try {
				preauthcodeinfo.put("code", -1);
				preauthcodeinfo.put("message", "��ȡ��Ȩ��ʧ�ܣ�");
			} catch (JSONException e1) {
				
			}
		}
		
		return preauthcodeinfo;
	}
}
