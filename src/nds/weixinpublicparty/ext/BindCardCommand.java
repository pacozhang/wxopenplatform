package nds.weixinpublicparty.ext;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.rest.RestUtils;
import nds.util.NDSException;
import nds.util.Tools;
import nds.weixin.ext.tools.VipPhoneVerifyCode;

public class BindCardCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		JSONObject jo=null;
		ValueHolder vh =new ValueHolder();
		
		//�жϹ�˾ID ��VIP ID�Ƿ��ڲ����д���
		try {
			jo = (JSONObject) event.getParameterValue("jsonObject");
			jo=jo.optJSONObject("params");
		}catch(Exception e) {
			logger.error("params error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "���쳣������");
			return vh;
		}
		
		if (jo==null||!jo.has("companyid")||!jo.has("vipid")) {
			logger.error("params error:not put companyid or vipid");
			vh.put("code", "-1");
			vh.put("message", "���쳣������");
			return vh;
		}
		
		int vipid=jo.optInt("vipid",-1);
		int companyid=jo.optInt("companyid",-1);
		
		if (companyid<=0 || vipid<=0) {
			logger.error("params error:companyid:"+companyid+",vipid:"+vipid);
			vh.put("code", "-1");
			vh.put("message", "���쳣������");
			return vh;
		}
		
		//�жϽ�ͨ���²���
		List all=null;
		try {
			all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,wc.wxparam,nvl(ifs.ismesauth,'N') from WX_INTERFACESET ifs join web_client wc on ifs.ad_client_id=wc.ad_client_id WHERE ifs.ad_client_id="+companyid);
		} catch (Exception e) {
			logger.error("select set offline params error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("messae", "���쳣������");
			return vh ;
		}
		if (all==null||all.size()<=0) {
			logger.error("select set offline params error:not find data");
			vh.put("code", "-1");
			vh.put("messae", "���쳣������");
			return vh ;
		}
		all=(List)all.get(0);
		
		String serverUrl=String.valueOf(all.get(0));
		boolean isErp="Y".equalsIgnoreCase(String.valueOf(all.get(2)));
		String SKEY=String.valueOf(all.get(3));
		boolean isVerifyCode="Y".equalsIgnoreCase(String.valueOf(all.get(4)));
		if(isErp&&(nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY))) {
			logger.error("SERVERuRL OR SKEY IS NULL");
			vh.put("code", "-1");
			vh.put("messae", "���쳣������");
			return vh ;
		}
		
		//�ж��Ƿ��н�ͨERP
		if(!isErp){
			logger.error("not connect erp");
			vh.put("code", "-1");
			vh.put("messae", "δ��ͨERP���ܰ�");
			return vh ;
		}
		
		//�ж��Ƿ���Ҫ������֤
		String verifycode=jo.optString("verifycode");
		String phone=jo.optString("PHONENUM");
		if(isVerifyCode) {
			if(nds.util.Validator.isNull(verifycode)) {
				vh.put("code", "-1");
				vh.put("messae", "��֤��Ϊ�գ�������");
				return vh ;
			}
			if(nds.util.Validator.isNull(phone)) {
				vh.put("code", "-1");
				vh.put("messae", "�ֻ���Ϊ�գ�������");
				return vh ;
			}
			vh=VipPhoneVerifyCode.verifyphonecode(vipid, phone, verifycode);
			if(vh==null) {
				logger.error("opencard verifyvipcode error:call VipPhoneVerifyCode.verifyphonecode error");
				vh.put("code", "-1");
				vh.put("message", "��֤����Ϣ�쳣������������");
				return vh;
			}
			if(!"0".equals(vh.get("code"))) {
				logger.error("opencard verifyvipcode error:"+vh.get("message"));
				return vh;
			}
		}
		
		int storeid=0;
		List al=null;
		
		try{
			al = QueryEngine.getInstance().doQueryList("select vp.wechatno,vp.vipcardno,vb.code,nvl(vp.store,0),vp.email,vp.idcard,vp.relname,vp.phonenum,vp.docno,vp.vippassword,nvl(c.isbsintegral,'N'),nvl(c.bsintegral,0),nvl(c.isbscoupon,'N'),nvl(c.bscoupon_id,0),nvl(vp.isbd,'N') from wx_vip vp join wx_vipbaseset vb on vp.viptype=vb.id join web_client c on vp.ad_client_id=c.ad_client_id WHERE vp.id=? ",new Object[] {vipid});
		}catch(Exception e){
			logger.error("bindcard find vip error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "��ʧ�ܣ�������");
			return vh;
		}
		
		if(al==null||al.size()<=0) {
			logger.error("bindcard find vip error:not find data by vipid:"+vipid);
			vh.put("code", "-1");
			vh.put("message", "��ʧ��");
			return vh;
		}
		al=(List)al.get(0);
		
		
		//�ж��Ƿ��Ѿ���
		String isbd=String.valueOf(al.get(14));
		if("Y".equalsIgnoreCase(isbd)){
			vh.put("code", "-1");
			vh.put("message", "�����ظ���");
			return vh;
		}
		
		boolean isbsi=false;
		int bsintegral=0;
		boolean isbsc=false;
		int bscouponid=0;
		String couponcode=null;
		String couponvalue=null;
		String coupontype=null;
		String begindate=null;
		String enddate=null;
		String ticket=null;
		
		JSONObject offparam=new JSONObject();
		JSONObject offvipinfo=new JSONObject();
		JSONObject offcouponinfo=new JSONObject();
		HashMap<String, String> params =new HashMap<String, String>();
		
		String ts=String.valueOf(System.currentTimeMillis());
		String sign=null;
		try {
			sign = nds.util.MD5Sum.toCheckSumStr(companyid + ts+ SKEY);
		} catch (IOException e) {
			logger.debug("bindcard md5 error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "��ʧ�ܣ�������");
			return vh;
		}
		
		try{
			offvipinfo.put("openid",String.valueOf(al.get(0)));
			offvipinfo.put("cardid",String.valueOf(companyid));
			offvipinfo.put("wshno",String.valueOf(al.get(1)));
			offvipinfo.put("name",String.valueOf(al.get(6)));
			offvipinfo.put("phone",phone);
			offvipinfo.put("email",String.valueOf(al.get(4)));
			offvipinfo.put("idno",String.valueOf(al.get(5)));
			offvipinfo.put("cardno",jo.optString("DOCNO"));
			offvipinfo.put("cardpwd",jo.optString("VIPPASSWORD"));
			offvipinfo.put("viptype",String.valueOf(al.get(2)));
			offvipinfo.put("cardpin","");
			storeid=Tools.getInt(al.get(3),0);
			isbsi="Y".equalsIgnoreCase(String.valueOf(al.get(10)));
			if(isbsi){
				bsintegral=Tools.getInt(al.get(11), 0);
				offvipinfo.put("bsintegral",String.valueOf(bsintegral));
			}
			isbsc="Y".equalsIgnoreCase(String.valueOf(al.get(12)));
			if(isbsc){
				bscouponid=Tools.getInt(al.get(13), 0);
				if(bscouponid==0){
					logger.debug("bindcard send coupon error bscoupon_id:"+bscouponid);
					vh.put("code", "-1");
					vh.put("message", "��ʧ��");
					return vh;
				}
				List couponl=null;
				try{
					couponl=QueryEngine.getInstance().doQueryList("select cp.num,cp.value,cp.usetype1,to_char(decode(nvl(cp.validay,0),0,nvl(cp.starttime,sysdate), sysdate), 'YYYYMMDD'),to_char(decode(nvl(cp.validay,0),0, nvl(cp.endtime, add_months(cp.starttime, 1)),sysdate+cp.validay), 'YYYYMMDD') from wx_coupon cp where cp.id=?",new Object[]{bscouponid});
				}catch(Exception e){
					logger.debug("bindcard send coupon error:"+e.getLocalizedMessage());
					e.printStackTrace();
					vh.put("code", "-1");
					vh.put("message", "��ʧ��");
					return vh;
				}
				
				if(couponl==null||couponl.size()<=0){
					logger.debug("bindcard send coupon error not find coupon by id:"+bscouponid);
					vh.put("code", "-1");
					vh.put("message", "��ʧ��");
					return vh;
				}
				couponl=(List)couponl.get(0);
				couponcode=String.valueOf(couponl.get(0));
				couponvalue=String.valueOf(couponl.get(1));
				coupontype=String.valueOf(couponl.get(2));
				begindate=String.valueOf(couponl.get(3));
				enddate=String.valueOf(couponl.get(4));
				
				//�ж�ȯʹ������
				if(!"1".equals(coupontype)){
					offcouponinfo.put("bsccode",couponcode);
					offcouponinfo.put("bscvalue",couponvalue);
					offcouponinfo.put("bscbdate",begindate);
					offcouponinfo.put("bscedate",enddate);
					offparam.put("couponinfo", offcouponinfo);
				}
			}
			offparam.put("vipinfo", offvipinfo);
		}catch(Exception e){
			
		}
		
		
		params.put("args[params]", offparam.toString());
		params.put("args[cardid]",String.valueOf(companyid));
		params.put("format","JSON");
		params.put("client","");
		params.put("ver","1.0");
		params.put("ts",ts);
		params.put("sig",sign);
		params.put("method","bindCard");
		
		//�������¿���
		try{
			vh=RestUtils.sendRequest(serverUrl,params,"POST");
		} catch (Throwable e) {
			logger.debug("bindcard offline error->"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "��ʧ�ܣ�������");
			return vh;
		}
		if(vh==null) {
			logger.error("bindcard offline error->return null");
			vh.put("code", "-1");
			vh.put("message", "��ʧ�ܣ�������");
			return vh;
		}
		
		String result=(String) vh.get("message");
		logger.debug("bidcard offline code result->"+result);
		JSONObject offjo=null;
		try {
			offjo= new JSONObject(result);
		}catch(Exception e) {
			vh.put("code", "-1");
			vh.put("message", "��ʧ�ܣ�������");
			return vh;
		}
		
		//�ж����°��Ƿ�ɹ�
		if(offjo==null||offjo==JSONObject.NULL) {
			vh.put("code", "-1");
			vh.put("message", "���°��쳣��������");
			return vh;
		}
		if(offjo.optInt("errCode",-1)!=0) {
			vh.put("code", "-1");
			vh.put("message", offjo.optString("errMessage"));
			return vh;
		}
		if(!offjo.has("result")) {
			vh.put("code", "-1");
			vh.put("message", "���°��쳣��������");
			return vh;
		}
		
		//�ж����°��Ƿ񷵻ػ�Ա��Ϣ
		JSONObject resjo=offjo.optJSONObject("result");
		if(resjo==null||resjo==JSONObject.NULL||!resjo.has("card")&&resjo.optJSONObject("card").has("no")) {
			vh.put("code", "-1");
			vh.put("message", "���°��쳣��������");
			return vh;
		}
		
		//���ͻ���
		if(bsintegral>0) {
			ArrayList returnparam=new ArrayList();
			ArrayList sendparam=new ArrayList();
			JSONObject sendintegral=new JSONObject();
			try {
				sendintegral.put("vipid", vipid);
				sendintegral.put("integral", bsintegral);
				sendintegral.put("description", "���ͻ���");
				sendparam.add(sendintegral.toString());
				returnparam.add(java.sql.Clob.class);
				
				Collection list=QueryEngine.getInstance().executeFunction("wx_vip_adjustintegral",sendparam,returnparam);
				String res=(String)list.iterator().next();
				logger.debug("online bindcard adjustintegral result->"+res);
			}catch(Exception e) {
				logger.error("bindcard send integral error:"+e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		
		//����ȯ		
		JSONObject vipmessage=resjo.optJSONObject("data");
		if(isbsc&&nds.util.Validator.isNotNull(couponcode)) {
			if(vipmessage!=null&&vipmessage!=JSONObject.NULL&&vipmessage.has("code")) {
				ticket=vipmessage.optString("code");
			}
			JSONObject consumejo=new JSONObject();
			
			ArrayList paramss=new ArrayList();
			paramss.add(companyid);

			ArrayList para=new ArrayList();
			para.add(java.sql.Clob.class);
			
			try {
				consumejo.put("vipid", vipid);
				consumejo.put("couponcode",couponcode);
				consumejo.put("tickno",ticket);
				paramss.add(consumejo.toString());
				
				Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_$r_send",paramss,para);
				String res=(String)list.iterator().next();
				logger.debug("online bindcard brecommend send coupon result->"+res);
			}catch (Exception e) {
				logger.debug("online bindcard brecommend send coupon erroe->"+e.getMessage());
				e.printStackTrace();
			}
		}
		
		JSONObject cardjo=resjo.optJSONObject("card");
		String sql="update wx_vip v"
				   +" set (v.integral,v.lastamt,v.viptype,v.docno,v.relname,v.email,v.phonenum,v.store,v.contactaddress,v.birthday,v.sex,v.province,v.city,v.area,v.isbd,v.bindtime)"
				   +" ="
				   +" (select ?,?,nvl(vbs.id,v.viptype),?,?,?,?,nvl(s.id,v.store),?,?,?,?,?,?,'Y',sysdate from wx_vip v left join wx_vipbaseset vbs on v.id=? and vbs.code=? and vbs.ad_client_id=? left join wx_store s on s.code=? and s.ad_client_id=? where v.id=?)"
				   +" where v.id=?";
		
		try{
			QueryEngine.getInstance().executeUpdate(sql,new Object[] {cardjo.optInt("credit"),cardjo.optDouble("balance"),cardjo.optString("no"),cardjo.optString("name"),cardjo.optString("email"),cardjo.optString("phonenum"),cardjo.optString("address"),cardjo.optString("birthday"),cardjo.optString("gender"),cardjo.optString("province"),cardjo.optString("city"),cardjo.optString("depart"),vipid,cardjo.optString("level"),companyid,cardjo.optString("shopcode"),companyid,vipid,vipid});
		}catch(Exception e){
			logger.debug("online bindcard brecommend send coupon erroe->"+e.getMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "���»�Ա���쳣��������");
			return vh;
		}

		vh.put("code", "0");
		vh.put("message", "�󿨳ɹ�");
		return vh;
	}
}
