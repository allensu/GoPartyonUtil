package tw.com.funbackend.pojo;

import java.util.Date;

public class UserBean {
	private String userInfoId;
	private String accountId;
	private String accountName;
	private String theme;
	private Date loginDateTime;
	
	
	
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public Date getLoginDateTime() {
		return loginDateTime;
	}

	public void setLoginDateTime(Date loginDateTime) {
		this.loginDateTime = loginDateTime;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getUserInfoId() {
		return userInfoId;
	}

	public void setUserInfoId(String userInfoId) {
		this.userInfoId = userInfoId;
	}
	
	
}
