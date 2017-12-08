package cn.com.pyc.drm.model.db.bean;

import java.util.LinkedList;
import java.util.List;

public class account {

	private String username;
	
	private String create_time;
	
	private List<Right> rights = new LinkedList <Right>();
	
	private String id = "";
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	
	public void setRights(LinkedList<Right> rights){
		this.rights = rights;;
	}
	
	public List<Right> getRights(){
		return rights;
	}
	public String getId(){
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
//	ACCOUNT account = new ACCOUNT();
	//account.setUsername("");
	
//	List<ROLE> roles = new LinkedList <Role>;
	//roles.se
	//account.setRoles(roles);
	
	//acountDAO.cascadeSave();
}
