package net.sidland.apesay.domain;

/**
 * 
 * ClassName: TradeState 
 * date: 2015年9月22日 下午5:00:59 
 *
 * @author sid
 */
public enum OrderTradeState {  
	ORDER_WZF("未支付", "ORDER_WZF",0), 
	ORDER_ZFZ("支付中", "ORDER_ZFZ",1), 
	ORDER_ZFCG("支付成功", "ORDER_ZFCG",2), 
	ORDER_ZFSB("支付失败", "ORDER_ZFSB",3), 
	ORDER_QXDD("取消订单", "ORDER_QXDD",4),
	ORDER_JYCS("交易超时", "ORDER_JYCS",5);  
    // 成员变量  
    private String des;  
    private String name;  
    private Integer val;
    // 构造方法  
    private OrderTradeState(String des, String name, Integer val) {
		this.des = des;
		this.name = name;
		this.val = val;
	}
    // get set 方法  
    public String getName() {  
        return name;  
    }  
    public void setName(String name) {  
        this.name = name;  
    }  
	public String getDes() {
		return des;
	}
	public void setDes(String des) {
		this.des = des;
	}
	public int getVal() {
		return val;
	}
	public void setVal(Integer val) {
		this.val = val;
	}
	public static OrderTradeState getByName(String name) {
        for (OrderTradeState c : OrderTradeState.values()) {  
            if (c.getName().equals(name)) {  
                return c;  
            }  
        }  
        return null;  
	}
	public static OrderTradeState getByVal(Integer val) {
        for (OrderTradeState c : OrderTradeState.values()) {  
            if (c.getVal()==val) {  
                return c;  
            }  
        }  
        return null;  
	}  
}  