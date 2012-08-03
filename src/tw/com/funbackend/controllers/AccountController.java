package tw.com.funbackend.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JsonView;

import com.mchange.v2.c3p0.stmt.GooGooStatementCache;

import tw.com.funbackend.enumeration.UserInfoCategory;
import tw.com.funbackend.form.AccountLoginForm;
import tw.com.funbackend.persistence.MenuGroup;
import tw.com.funbackend.persistence.MenuItem;
import tw.com.funbackend.persistence.UserInfo;
import tw.com.funbackend.pojo.UserBean;
import tw.com.funbackend.service.AccountService;
import tw.com.funbackend.utility.Encrypt;

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

		logger.info("Log4J Log Test");
		
		// UserBean userBean = new UserBean();
		userBean.setAccountId(form.getAccount());
		// modelMap.addAttribute("userBean", userBean);
		// modelMap.put("userBean", userBean);

		return new ModelAndView("redirect:/controller/Home/Index");
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

		userInfo.setAccountPass(Encrypt.encodePassword(userInfo
				.getAccountPass()));
		userInfo.setCategory(UserInfoCategory.Admin);
		userInfo.setCreateDateTime(new Date());

		UserInfo userInfoResult = accountService.createUser(userInfo);

		return new ModelAndView("/Account/ManageUser");
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
	 * 取得使用者功能清單
	 * 
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/Account/MenuList")
	public @ResponseBody
	ArrayList getMenuList(HttpServletResponse response) {
		UserBean userBean = new UserBean();
		userBean.setAccountId("allensu");

		ArrayList list = new ArrayList();
		ArrayList list1 = new ArrayList();

		Hashtable ht1 = new Hashtable();
		ht1.put("title", "FunCube");
		Hashtable ht1_1 = new Hashtable();
		ht1_1.put("title", "會員基本資料查詢");
		ht1_1.put("url", "/funbackend/controller/Member/MemberDataQuery");
		ht1_1.put("id", "MemberDataQuery");
		Hashtable ht1_2 = new Hashtable();
		ht1_2.put("title", "會員黑名單");
		ht1_2.put("url", "/funbackend/controller/Member/BlackMember");
		ht1_2.put("id", "BlackMember");
		Hashtable ht1_3 = new Hashtable();
		ht1_3.put("title", "會員登入/出時間");
		ht1_3.put("url", "/funbackend/controller/Member/MemberLoginRecord");
		ht1_3.put("id", "MemberLoginRecord");
		Hashtable ht1_5 = new Hashtable();
		ht1_5.put("title", "各地區會員數");
		ht1_5.put("url", "/funbackend/controller/Member/MemberPlace");
		ht1_5.put("id", "MemberPlace");
		Hashtable ht1_4 = new Hashtable();
		ht1_4.put("title", "聊天室記錄查詢");
		ht1_4.put("url", "/funbackend/controller/Chatroom/ChatroomMessageRecord");
		ht1_4.put("id", "ChatroomMessageRecord");
		Hashtable ht1_6 = new Hashtable();
		ht1_6.put("title", "最多贊會員排行");
		ht1_6.put("url", "/funbackend/controller/Rank/LikeRank");
		ht1_6.put("id", "LikeRank");
		Hashtable ht1_7 = new Hashtable();
		ht1_7.put("title", "Po文排行");
		ht1_7.put("url", "/funbackend/controller/Rank/ArticleRank");
		ht1_7.put("id", "ArticleRank");
		Hashtable ht1_8 = new Hashtable();
		ht1_8.put("title", "上傳圖片排行");
		ht1_8.put("url", "/funbackend/controller/Rank/UploadPicRank");
		ht1_8.put("id", "UploadPicRank");
		list1.add(ht1_1);
		list1.add(ht1_2);
		list1.add(ht1_3);
		list1.add(ht1_4);
		list1.add(ht1_5);
		list1.add(ht1_6);
		list1.add(ht1_7);
		list1.add(ht1_8);
		ht1.put("content", list1);

		Hashtable ht5 = new Hashtable();
		ht5.put("title", "帳號管理");
		ArrayList list5 = new ArrayList();
		Hashtable ht5_1 = new Hashtable();
		ht5_1.put("title", "帳號管理");
		ht5_1.put("url", "/funbackend/controller/Account/ManageUser");
		ht5_1.put("id", "ManageUser");
		list5.add(ht5_1);
		ht5.put("content", list5);

		list.add(ht1);
		list.add(ht5);

		
		
		// 取得使用帳號的權限
		List<MenuGroup> menuGroupListResult = accountService
				.getMenuList(userBean.getAccountId());

		MenuGroup accountManageGroup = new MenuGroup();
		List<MenuItem> accountManageItemList = new ArrayList<MenuItem>();
		accountManageGroup.setTitle("帳號管理");
		MenuItem menuItem3_1 = new MenuItem();
		menuItem3_1.setTitle("帳號管理");
		menuItem3_1.setUrl("/funbackend/controller/Account/ManageUser");
		accountManageItemList.add(menuItem3_1);
		accountManageGroup.setContent(accountManageItemList);

		menuGroupListResult.add(accountManageGroup);

		return list;
	}

}
