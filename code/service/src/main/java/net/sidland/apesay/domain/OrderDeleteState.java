package net.sidland.apesay.domain;

/**
 * 
 * ClassName: OrderDeleteState 
 * date: 2015年9月22日 下午5:00:59 
 *
 * @author sid
 */
public enum OrderDeleteState {  
	ORDER_ZC("正常", "ORDER_ZC",0), 
	ORDER_SC("删除", "ORDER_SC",1);  
    // 成员变量  
    private String des;  
    private String name;  
    private Integer val;
    // 构造方法  
    private OrderDeleteState(String des, String name, Integer val) {
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
	public static OrderDeleteState getByName(String name) {
        for (OrderDeleteState c : OrderDeleteState.values()) {  
            if (c.getName().equals(name)) {  
                return c;  
            }  
        }  
        return null;  
	}
	public static OrderDeleteState getByVal(Integer val) {
        for (OrderDeleteState c : OrderDeleteState.values()) {  
            if (c.getVal()==val) {  
                return c;  
            }  
        }  
        return null;  
	}  
}  