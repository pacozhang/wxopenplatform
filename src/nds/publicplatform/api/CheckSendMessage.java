package nds.publicplatform.api;

import java.sql.Connection;
import java.util.Hashtable;

import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.publicweixin.ext.tools.RestUtils;
import nds.query.QueryEngine;
import nds.query.QueryException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CheckSendMessage {
	private static Logger logger= LoggerManager.getInstance().getLogger(CheckSendMessage.class.getName());	 
	private static final String check_send_message_url=WebUtils.getProperty("check_send_message_url","https://api.weixin.qq.com/cgi-bin/message/mass/get?access_token=");

	private static Hashtable<String,CheckSendMessage> weNews;
	private CheckSendMessage(){}
	
	public static synchronized CheckSendMessage getInstance(String customId){//Ϊ�˽�ʡ��Դ������newһ������ֱ�Ӵ�����
		if(nds.util.Validator.isNull(customId)){return null;}
		
		CheckSendMessage instance=null;
		if(weNews==null){
			weNews=new Hashtable<String,CheckSendMessage>();
			instance=new CheckSendMessage();
			weNews.put(customId, instance);
		}else if(weNews.containsKey(customId)){
			instance=weNews.get(customId);
		}else{
			instance=new CheckSendMessage();
			weNews.put(customId, instance);
		}
		return instance;
	}
	
	public JSONObject checkmessage(WxPublicControl wc,JSONObject obj) throws JSONException{
		JSONObject result= new JSONObject();
		JSONObject atoken=wc.getAccessToken();
		//�ж�ACCESSTOKEN�Ƿ��ȡ�ɹ�
		if(atoken==null||!"0".equals(atoken.optString("code"))) {
			try {
				result.put("code", "-1");
				result.put("message", "��������Ȩ");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return result;
		}
				
		String token=atoken.optJSONObject("data").optString("authorizer_access_token");
		if(nds.util.Validator.isNull(token)) {
			try {
				result.put("code", -1);
				result.put("message", "��������Ȩ");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}
		String massids = obj.optString("selection");
		JSONArray ja =null; 
		Connection con=null;
		
		try {
			QueryEngine qe=QueryEngine.getInstance();
			con=qe.getConnection();
			ja= new JSONArray(massids);
			String getmass_sql = "select t.msg_id,t.msg_status from wx_mass_message t where t.id =?";
			String update_sql = "update wx_mass_message t set t.msg_status=?,t.errmsg=?,t.modifieddate=sysdate where t.id=?";
			String msg_status="";
			String url=check_send_message_url+token;
			for(int i=0;i<ja.length();i++){
				int massid = Integer.parseInt(ja.optString(i));
				JSONArray rja = qe.doQueryObjectArray(getmass_sql,new Object[]{massid},con,false);
				JSONObject rjo = rja.optJSONObject(0);
				msg_status = rjo.optString("msg_status");
				if(!msg_status.equals("1")){
					continue;
				}
				rjo.remove("msg_status");
				ValueHolder hd =null;
				try {
					hd=RestUtils.sendRequest_buff(url, rjo.toString(), "POST");
					logger.debug("get SendMsg status result--->"+hd.get("message").toString());
					JSONObject jsonObject = new JSONObject(hd.get("message").toString());
					String rt_status = jsonObject.optString("msg_status");
					qe.executeUpdate(update_sql,new Object[]{"2",rt_status,massid},con);
				} catch (Exception e) {
					e.printStackTrace();
					logger.debug("get SendMsg error massid -->"+massid);
					logger.debug("get SendMsg error msg -->"+e.getMessage());
				}
				
			}
			if(ja.length()<1){
				result.put("code", -1);
				result.put("message", "��ѡ����Ҫ��ѯ����Ϣ");
			}else{
				result.put("code", 1);
				result.put("message", "��ѯ�ɹ�������Ϊ��ˢ��ҳ�棬����ȷ�����ˢ��");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			result.put("code", -1);
			result.put("message", "��ѯ��Ϣ��ID����Ϊ��");
		} catch (QueryException e) {
			e.printStackTrace();
			result.put("code", -1);
			result.put("message", "���ݴ����쳣");
		}finally{
	 		if(con!=null){
	 			try{
	 				con.close();
	 			}catch(Exception e){
	 				
	 			}
	 		}
		}
		return result;
		
	}
	
}
