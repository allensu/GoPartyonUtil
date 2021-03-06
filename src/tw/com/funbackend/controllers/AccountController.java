 package tw.com.funbackend.controllers;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.persistence.EnumType;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JsonView;

import com.mchange.v2.c3p0.stmt.GooGooStatementCache;

import tw.com.funbackend.enumeration.FunctionalType;
import tw.com.funbackend.enumeration.OrderDirection;
import tw.com.funbackend.enumeration.UserInfoCategory;
import tw.com.funbackend.form.AccountLoginForm;
import tw.com.funbackend.form.DataTableQueryParam;
import tw.com.funbackend.form.SimpleResult;
import tw.com.funbackend.form.querycond.GraffitiWallCondition;
import tw.com.funbackend.form.querycond.ManageMenuAuthCondition;
import tw.com.funbackend.form.result.GraffitiWallDataTableResult;
import tw.com.funbackend.form.result.ManageMenuAuthDataTableResult;
import tw.com.funbackend.form.tableschema.GraffitiWallTableSchema;
import tw.com.funbackend.form.tableschema.ManageMenuAuthTableSchema;
import tw.com.funbackend.persistence.MenuAuth;
import tw.com.funbackend.persistence.MenuGroup;
import tw.com.funbackend.persistence.MenuItem;
import tw.com.funbackend.persistence.MessageData;
import tw.com.funbackend.persistence.UserInfo;
import tw.com.funbackend.persistence.gopartyon.GraffitiWallItem;
import tw.com.funbackend.pojo.UserBean;
import tw.com.funbackend.service.AccountService;
import tw.com.funbackend.utility.Encrypt;
import tw.com.funbackend.utility.StringUtility;

@SessionAttributes("userBean")
@Controller
public class AccountController {
	//Logger log = Logger.getLogger(AccountController.class);
	protected Logger logger = Logger.getLogger("controller");
	
	@Autowired
	private AccountService accountService;

	@ModelAttribute("userBean")
	public UserBean initUserBean() {
	   return new UserBean(); // populates form for the first time if its null
	}
	
	/**
	 * 帳號系統
	 * 
	 * @return
	 */
	@RequestMapping(value = "/Account/Index", method = RequestMethod.GET)
	public ModelAndView index() {
		return new ModelAndView("/Account/Login");
	}

	/**
	 * 使用者登入作業
	 * 
	 * @param userBean
	 * @param form
	 * @return
	 */
	@RequestMapping(value = "/Account/Login", method = RequestMethod.POST)
	public ModelAndView login(@ModelAttribute("userBean") UserBean userBean,
			@ModelAttribute AccountLoginForm form) {

		UserInfo userInfo = accountService.userLogin(form.getAccount(), form.getPassword());
		
		if(userInfo == null)
		{
			return new ModelAndView("/Account/Login");
		} else {
			userBean.setUserInfoId(userInfo.getId());
			userBean.setAccountId(userInfo.getAccountId());
			userBean.setAccountName(userInfo.getAccountName());
			userBean.setTheme(form.getTheme());
			userBean.setLoginDateTime(userInfo.getLastLoginDateTime());
			return new ModelAndView("redirect:/controller/Home/Index");
		}
		
	}
	
	/**
	 * 使用者登出作業
	 * @return
	 */
	@RequestMapping(value = "/Account/Logout", method = RequestMethod.GET)
	public ModelAndView logout(@ModelAttribute("userBean") UserBean userBean)
	{
		try 
		{
			accountService.userLogout(userBean.getAccountId());
		} 
		catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
				
		return new ModelAndView("/Account/Login");
	}

	/**
	 * 建立使用者帳號
	 * 
	 * @param userBean
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/Account/CreateUser", method = RequestMethod.POST)
	public ModelAndView createUser(
			@ModelAttribute("userBean") UserBean userBean,
			@ModelAttribute UserInfo userInfo) {

		userInfo.setAccountPass(Encrypt.encodePassword(userInfo.getAccountPass()));
		userInfo.setCreateDateTime(new Date());

		UserInfo userInfoResult = accountService.createUser(userInfo);
		
		return new ModelAndView("/Account/ManageUser");
	}
	
	/**
	 * 修改使用者帳號資訊
	 * @param userBean
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/Account/UpdateUser", method = RequestMethod.POST)
	public @ResponseBody UserInfo updateUser(
			@ModelAttribute("userBean") UserBean userBean,
			@ModelAttribute UserInfo userInfo) {

		boolean result = false;
		
		try {
			result = accountService.updateUser(userInfo);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
		}
		
		if(result)
			return userInfo;
		else 
			return new UserInfo();
	}
	
	@RequestMapping(value = "/Account/ReadUser")
	public @ResponseBody List<UserInfo> readUser() {
		List<UserInfo> userInfoList = new ArrayList<UserInfo>();
		
		try {
			userInfoList = accountService.readUserAll();
			
			if(userInfoList == null || userInfoList.size() == 0)
			{
				userInfoList = new ArrayList<UserInfo>();
			}
		} catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return userInfoList;
	}
	
	/**
	 * 刪除資料
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = "/Account/DeleteAccount", method = RequestMethod.POST)
	public @ResponseBody List<UserInfo> deleteAccount(
			@RequestParam(value="ids") List<String> ids) {
		List<UserInfo> userInfoDataList = new ArrayList<UserInfo>();
		
		try {
			// 刪除資料
			accountService.removeUserInfo(ids);
			
			// 重新查詢
			userInfoDataList = accountService.readUserAll();
			
			if(userInfoDataList == null || userInfoDataList.size() == 0)
			{
				userInfoDataList = new ArrayList<UserInfo>();
			}
			
		} catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return userInfoDataList;
	}

	/**
	 * 開啟使用者帳號管理頁面
	 * 
	 * @param userBean
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageUser", method = RequestMethod.GET)
	public ModelAndView manageUser(@ModelAttribute("userBean") UserBean userBean) {    
		return new ModelAndView("/Account/ManageUser");
	}

	/**
	 * 開啟使用者帳號權限管理頁面
	 * 
	 * @param userBean
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageMenuAuth", method = RequestMethod.GET)
	public ModelAndView manageMenuAuth(@ModelAttribute("userBean") UserBean userBean) {    
		return new ModelAndView("/Account/ManageMenuAuth");
	}
	
	/**
	 * 開啟功能群組管理頁面
	 * 
	 * @param userBean
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageMenu", method = RequestMethod.GET)
	public ModelAndView manageMenuGroup(@ModelAttribute("userBean") UserBean userBean) {    
		return new ModelAndView("/Account/ManageMenu");
	}
	
	/**
	 * 取得使用者功能清單
	 * 
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/Account/MenuList")
	public @ResponseBody List<MenuGroup> getMenuList(@ModelAttribute("userBean") UserBean userBean) {
		
		
		// 取得使用帳號的權限
		List<MenuGroup> menuGroupListResult = accountService
				.getMenuList(userBean.getAccountId());
		
		List<MenuAuth> menuAuthList = accountService.readMenuAuthByUserInfoId(userBean.getUserInfoId());
		
		Hashtable<String, MenuAuth> menuAuthKV = new Hashtable<String, MenuAuth>();
		
		for(MenuAuth currMAData : menuAuthList) {
			menuAuthKV.put(currMAData.getMenuItem().getId(), currMAData);
		}
		
		for(MenuGroup currData : menuGroupListResult) {
			List<MenuItem> enabledMenuItemList = new ArrayList<MenuItem>();
			
			for(MenuItem currMIData : currData.getContent()) {
				if(menuAuthKV.get(currMIData.getId()).isEnabled())
				{
					enabledMenuItemList.add(currMIData);
				}
			}
			
			currData.setContent(enabledMenuItemList);
		}
		
		return menuGroupListResult;
	}
	
	/**
	 * 建立新群組
	 * 
	 * @param userBean
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageMenu/GroupItem/Create", method = RequestMethod.POST)
	public @ResponseBody List<MenuGroup> createGroupItem(
			@ModelAttribute("userBean") UserBean userBean,
			@ModelAttribute MenuGroup menuGroup) {

		List<MenuGroup> result = new ArrayList<MenuGroup>();
		
		try 
		{
			accountService.createMenuGroup(menuGroup);
			
			result = accountService.getMenuList(userBean.getAccountId());			
		} 
		catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 建立新功能項目
	 * 
	 * @param userBean
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageMenu/MenuItem/Create", method = RequestMethod.POST)
	public @ResponseBody List<MenuGroup> createMenuItem(
			@ModelAttribute("userBean") UserBean userBean,
			@RequestParam(value="groupId") String groupId,
			@ModelAttribute MenuItem menuItem) {

		List<MenuGroup> result = new ArrayList<MenuGroup>();
		
		try 
		{
			accountService.createMenuItem(menuItem);
			
			if(StringUtility.isNotEmpty(menuItem.getId()))
			{
				MenuGroup menuGroup = accountService.getMenuGroup(groupId);			
				menuGroup.getContent().add(menuItem);
				accountService.createMenuGroup(menuGroup);
			}
			
			result = accountService.getMenuList(userBean.getAccountId());			
		} 
		catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 修改群組
	 * 
	 * @param userBean
	 * @param menuGroup
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageMenu/GroupItem/Update", method = RequestMethod.POST)
	public @ResponseBody List<MenuGroup> updateGroupItem(
			@ModelAttribute("userBean") UserBean userBean,
			@ModelAttribute MenuGroup menuGroup) {

		List<MenuGroup> result = new ArrayList<MenuGroup>();
		
		try 
		{
			accountService.updateMenuGroup(menuGroup);
			
			result = accountService.getMenuList(userBean.getAccountId());			
		} 
		catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 修改功能項目
	 * 
	 * @param userBean
	 * @param groupId
	 * @param menuItem
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageMenu/MenuItem/Update", method = RequestMethod.POST)
	public @ResponseBody List<MenuGroup> updateMenuItem(
			@ModelAttribute("userBean") UserBean userBean,
			@RequestParam(value="orgGroupId") String orgGroupId,
			@RequestParam(value="groupId") String groupId,
			@ModelAttribute MenuItem menuItem) {

		List<MenuGroup> result = new ArrayList<MenuGroup>();
		
		try 
		{
			accountService.updateMenuItem(orgGroupId, groupId, menuItem);				
			
			result = accountService.getMenuList(userBean.getAccountId());			
		}
		catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return result;
	}
	
	@RequestMapping(value = "/Account/ManageMenu/MenuItem/Remove", method = RequestMethod.POST)
	public @ResponseBody List<MenuGroup> removeMenuItem(
			@ModelAttribute("userBean") UserBean userBean,
			@RequestParam(value="itemId") String itemId) {
		
		List<MenuGroup> result = new ArrayList<MenuGroup>();
		
		try {
			// 刪除資料
			accountService.removeMenuItem(itemId);
					
			result = accountService.getMenuList(userBean.getAccountId());			
		} catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return result;
	}
	
	@RequestMapping(value = "/Account/ManageMenu/GroupItem/Remove", method = RequestMethod.POST)
	public @ResponseBody List<MenuGroup> removeGroupItem(
			@ModelAttribute("userBean") UserBean userBean,
			@RequestParam(value="groupId") String groupId) {
		
		List<MenuGroup> result = new ArrayList<MenuGroup>();
		
		try {
			// 刪除資料
			accountService.removeMenuGroup(groupId);
					
			result = accountService.getMenuList(userBean.getAccountId());			
		} catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 取得分頁帳號資料
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageMenuAuth/ReadPages", method = RequestMethod.POST)
	public @ResponseBody ManageMenuAuthDataTableResult readPagesManageMenuAuth(
			@ModelAttribute ManageMenuAuthCondition qCondition,
			@ModelAttribute DataTableQueryParam tableParm) {
		
		ManageMenuAuthDataTableResult result = new ManageMenuAuthDataTableResult();
		List<MenuAuth> menuAuthDataList = new ArrayList<MenuAuth>();
		
		
		try {
			
			// 排序處理
			String orderColName = ManageMenuAuthTableSchema.MapColumns[tableParm.getiSortCol_0()];
			int sortDir = OrderDirection.asc.toString().equals(tableParm.getsSortDir_0()) ? 1 : -1;
					
			menuAuthDataList = accountService.readMenuAuthPageByCondSort(qCondition, tableParm.getiDisplayStart(), tableParm.getiDisplayLength(), orderColName, sortDir);
			
//			if("".equals(orderColName))
//				menuAuthDataList = graffitiWallService.readGraffitiWallPageByCond(qCondition, tableParm.getiDisplayStart(), tableParm.getiDisplayLength());
//			else 
//				menuAuthDataList = graffitiWallService.readGraffitiWallPageByCondSort(qCondition, tableParm.getiDisplayStart(), tableParm.getiDisplayLength(), orderColName, sortDir);
			
			if(menuAuthDataList == null || menuAuthDataList.size() == 0)
			{
				menuAuthDataList = new ArrayList<MenuAuth>();
			}
			
			int totalCount = accountService.readMenuAuthCountByCond(qCondition);
			
			List<ManageMenuAuthTableSchema> menuAuthDataTable = new ArrayList<ManageMenuAuthTableSchema>();
			
			for(MenuAuth currData : menuAuthDataList)
			{
				ManageMenuAuthTableSchema data = new ManageMenuAuthTableSchema();
				data.setId(currData.getId());
				data.setUserInfo(currData.getUserInfo());
				data.setMenuItem(currData.getMenuItem());
				data.setEnabled(currData.isEnabled());
				data.setNewAuth(currData.isNewAuth());
				data.setUpdateAuth(currData.isUpdateAuth());
				data.setDeleteAuth(currData.isDeleteAuth());
				data.setQueryAuth(currData.isQueryAuth());				
				menuAuthDataTable.add(data);
			}
			
			result.setAaData(menuAuthDataTable);
			result.setsEcho(tableParm.getsEcho());
			result.setiTotalDisplayRecords(totalCount);
			result.setiTotalRecords(totalCount);
		} catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return result;
	}
	
	/**
	 * 更新功能權限項目
	 * 
	 * @param userBean
	 * @param groupId
	 * @param menuItem
	 * @return
	 */
	@RequestMapping(value = "/Account/ManageMenuAuth/Update", method = RequestMethod.POST)
	public @ResponseBody MenuAuth updateMenuAuth(
			@ModelAttribute("userBean") UserBean userBean,
			@RequestParam(value="menuAuthId") String menuAuthId,
			@RequestParam(value="enabled") boolean enabled,
			@RequestParam(value="newAuth") boolean newAuth,
			@RequestParam(value="updateAuth") boolean updateAuth,
			@RequestParam(value="deleteAuth") boolean deleteAuth,
			@RequestParam(value="queryAuth") boolean queryAuth) {

		MenuAuth result = new MenuAuth();
		
		try 
		{
			result = accountService.updateMenuAuth(menuAuthId, enabled, newAuth, updateAuth, deleteAuth, queryAuth);				
		}
		catch(Exception ex)
		{
			logger.error(ex.getMessage());
		}
		
		return result;
	}
	
	@RequestMapping(value = "/Account/ManageMenuAuth/HasFunAuth", method = RequestMethod.GET)
	public @ResponseBody SimpleResult hasFunAuth(
			@ModelAttribute("userBean") UserBean userBean,
			@RequestParam(value="authType") String authType,
			@RequestParam(value="menuItemId") String menuItemId) {

		SimpleResult result = new SimpleResult();
		
		try {
			
			boolean funAuthResult = false;
					
			if(FunctionalType.NewAuth.toString().equals(authType))
				funAuthResult = accountService.hasFuncationalAuth(FunctionalType.NewAuth, userBean.getUserInfoId(), menuItemId);
			else if(FunctionalType.UpdateAuth.toString().equals(authType))
				funAuthResult = accountService.hasFuncationalAuth(FunctionalType.UpdateAuth, userBean.getUserInfoId(), menuItemId);
			else if(FunctionalType.DeleteAuth.toString().equals(authType))
				funAuthResult = accountService.hasFuncationalAuth(FunctionalType.DeleteAuth, userBean.getUserInfoId(), menuItemId);
			else if(FunctionalType.QueryAuth.toString().equals(authType))
				funAuthResult = accountService.hasFuncationalAuth(FunctionalType.QueryAuth, userBean.getUserInfoId(), menuItemId);
			
			if(funAuthResult)
				result.setResultCode(0);
			else 
				result.setResultCode(-1);
			
		} catch(Exception ex) {
			logger.error(ex.getMessage());
		}
		
		return result;
	}
}
