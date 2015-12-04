package nds.publicweixin.ext.common;

import java.util.Hashtable;

import org.json.JSONObject;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicplatform.api.RefreshAccessToken;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class WxPublicControl {
	private static Logger logger= LoggerManager.getInstance().getLogger(WxPublicControl.class.getName());
	
	private final WeUtils wu;
	private final WxPublicpartyControl wppc;
	private static Hashtable<String,WxPublicControl> wpcs;
	private WxPublicControl(WxPublicpartyControl wppc,WeUtils wu) {
		this.wu=wu;
		this.wppc=wppc;
	}
	
	/**
	 * 
	 * @param pappid	public	APPID(¹«ÖÚºÅAPPID)
	 * @return
	 */
	public static synchronized WxPublicControl getInstance(String pappid) {
		logger.debug("pappid is->"+pappid);
		WxPublicControl twpc=null;
		WxPublicpartyControl twppc=null;
		if(wpcs==null) {
			wpcs=new Hashtable<String,WxPublicControl>();
			WeUtilsManager wum=WeUtilsManager.getInstance();
			WeUtils twu=wum.getByAppid(pappid);
			twppc=WxPublicpartyControl.getInstance(twu.getPublicpartyappid());
			twpc=new WxPublicControl(twppc,twu);
			wpcs.put(pappid, twpc);
			return twpc;
		}else if(wpcs.containsKey(pappid)) {
			return wpcs.get(pappid);
		}else {
			WeUtilsManager wum=WeUtilsManager.getInstance();
			WeUtils twu=wum.getByAppid(pappid);
			twppc=WxPublicpartyControl.getInstance(twu.getPublicpartyappid());
			twpc=new WxPublicControl(twppc,twu);
			wpcs.put(pappid, twpc);
			return twpc;
		}
	}

	public String getAccessToken() {
		if(nds.util.Validator.isNull(wu.getAuthorizer_access_token())||wu.getAattime()<System.currentTimeMillis()) {
			RefreshAccessToken rat=RefreshAccessToken.getInstance(this);
			JSONObject jo=rat.refreshAccessToken();
			
			if(jo!=null&&"0".equals(jo.optString("code"))) {
				jo=jo.optJSONObject("data");
				if(jo!=null&&jo.has("authorizer_access_token")) {wu.setAuthorizerinfo(jo);}
			}
		}
		
		return wu.getAuthorizer_access_token();
	}
	
	public WeUtils getWxPublic() {
		return this.wu;
	}

	public WxPublicpartyControl getPpc() {
		return this.wppc;
	}
}
