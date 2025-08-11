package cn.dev33.satoken.quick;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.quick.config.SaQuickConfig;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaTokenConsts;

/**
 * Quick-Bean 注册 
 * 
 * @author kong
 *
 */
@Configuration
public class SaQuickRegister {

	/**
	 * 注册 Quick-Login 配置
	 * 
	 * @return see note
	 */
	@Bean
	@ConfigurationProperties(prefix = "sa")
	public SaQuickConfig getSaQuickConfig() {
		return new SaQuickConfig();
	}
	
	/**
	 * 注册 [sa-token全局过滤器] 
	 * 
	 * @return see note
	 */
	@Bean
	@Order(SaTokenConsts.ASSEMBLY_ORDER - 1)
	public SaServletFilter getSaServletFilter() {
		return new SaServletFilter()
			// 拦截路由 & 放行路由
			.addInclude(SaQuickManager.getConfig().getInclude().split(","))
			.addExclude(SaQuickManager.getConfig().getExclude().split(","))
			.addExclude("/favicon.ico", "/saLogin", "/doLogin", "/sa-res/**").
			// 认证函数: 每次请求执行
			setAuth(r -> {
				// 未登录时直接转发到login.html页面 
				if (SaQuickManager.getConfig().getAuth() && StpUtil.isLogin() == false) {
					SaHolder.getRequest().forward("/saLogin");
					SaRouter.back();
				}
			}).
	
			// 异常处理函数：每次认证函数发生异常时执行此函数
			setError(e -> {
				return e.getMessage();
			});
	}

}
