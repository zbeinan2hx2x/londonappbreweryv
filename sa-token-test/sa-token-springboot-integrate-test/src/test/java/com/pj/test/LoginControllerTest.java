package com.pj.test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.pj.test.util.SoMap;

/**
 * Sa-Token 登录API测试 
 * 
 * @author Auster 
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = StartUpApplication.class)
public class LoginControllerTest {
	
	@Autowired
	private WebApplicationContext wac;
	 
	private MockMvc mvc;
	
	// 开始 
	@Before
    public void before() {
		mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
	
    @Test
    public void testLogin() throws Exception{
    	// 请求 
		MvcResult mvcResult = mvc.perform(
			MockMvcRequestBuilders.post("/acc/doLogin")
				.param("name", "zhang")
				.param("pwd", "123456")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8)
			)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andReturn();
		
		// 拿到结果 
		SoMap so = SoMap.getSoMap().setJsonString(
				mvcResult.getResponse().getContentAsString()
				);
		String token = so.getString("token");
		
		// 断言 
		Assert.assertTrue(mvcResult.getResponse().getHeader("Set-Cookie") != null);
		Assert.assertEquals(so.getInt("code"), 200);
		Assert.assertNotNull(token);
    }

	@Test
	@SuppressWarnings("unchecked")
    public void testLogin2() throws Exception{
    	// 获取token 
    	SoMap so = request("/acc/doLogin?name=zhang&pwd=123456");
		Assert.assertNotNull(so.getString("token"));

    	String token = so.getString("token");

    	// 是否登录
    	SoMap so2 = request("/acc/isLogin?satoken=" + token);
		Assert.assertTrue(so2.getBoolean("data"));

    	// tokenInfo 
    	SoMap so3 = request("/acc/tokenInfo?satoken=" + token);
    	SoMap so4 = SoMap.getSoMap((Map<String, ?>)so3.get("data"));
		Assert.assertEquals(so4.getString("tokenName"), "satoken");
		Assert.assertEquals(so4.getString("tokenValue"), token);
    	
		// 注销
		request("/acc/logout?satoken=" + token);

    	// 是否登录 
    	SoMap so5 = request("/acc/isLogin?satoken=" + token);
		Assert.assertFalse(so5.getBoolean("data"));
    }
    
    // 封装请求 
    private SoMap request(String path) throws Exception {
    	MvcResult mvcResult = mvc.perform(
    			MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.accept(MediaType.APPLICATION_JSON_UTF8)
    			)
    			.andExpect(MockMvcResultMatchers.status().isOk())
    			.andReturn();
    	
		SoMap so = SoMap.getSoMap().setJsonString(
				mvcResult.getResponse().getContentAsString()
				);
		
		return so;
    }
    
}
