package cn.dev33.satoken.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.dev33.satoken.SaTokenManager;


/**
 * session会话 
 * @author kong
 *
 */
public class SaSession implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id; 					// 会话id 
	private long createTime;			// 当前会话创建时间 
	private Map<String, Object> dataMap;	// 当前会话键值对  
	
	
	/**
	 * 构建一个 session对象
	 * @param id
	 */
 	public SaSession(String id) {
		this.id = id;
		this.createTime = System.currentTimeMillis();
		this.dataMap = new HashMap<String, Object>();
	}
	
 	/**
 	 * 获取会话id 
 	 * @return
 	 */
	public String getId() {
		return id;
	}

	/**
	 * 当前会话创建时间
	 */
	public long getCreateTime() {
		return createTime;
	}
	
	/**
	 * 写入值
	 */
	public void setAttribute(String key, Object value) {
		dataMap.put(key, value);
		update();
	}
	
	/**
	 * 取值  
	 */
	public Object getAttribute(String key) {
		return dataMap.get(key);
	}
	
	/**
	 * 取值，并指定取不到值时的默认值 
	 */
	public Object getAttribute(String key, Object default_value) {
		Object value = getAttribute(key);
		if(value != null) {
			return value;
		}
		return default_value;
	}


	/**
	 * 移除一个key
	 */
	public void removeAttribute(String key) {
		dataMap.remove(key);
		update();
	}
	
	/**
	 * 清空所有key
	 */
	public void clearAttribute() {
		dataMap.clear();
		update();
	}
	
	/**
	 * 是否含有指定key
	 */
	public boolean containsAttribute(String key)  {
		return dataMap.keySet().contains(key);
	}
	
	/**
	 * 当前session会话所有key
	 */
	public Set<String> getAttributeKeys() {
		return dataMap.keySet();
	}

	/**
	 * 获取数据集合（如果更新map里的值，请调用session.update()方法避免数据过时 ）
	 */
	public Map<String, Object> getDataMap() {
		return dataMap;
	}
	
	/**
	 * 将这个session从持久库更新一下  
	 */
	public void update() {
		SaTokenManager.getDao().updateSaSession(this);
	}

	
//	/** 注销会话(注销后，此session会话将不再存储服务器上) */
//	public void logout() {
//		SaTokenManager.getDao().delSaSession(this.id);
//	}
	
	
	
}
