package com.capv.um.spring.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.capv.client.user.websocket.CapvWebSocketConfig;
import com.capv.um.rest.controller.WebSocketPingScheduler;
import com.capv.um.security.OAuth2ServerConfiguration;
import com.capv.um.security.WebSecurityConfig;
import com.capv.um.web.filter.RequestPreProcessFilter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableScheduling
@EnableWebMvc
@EnableSwagger2
@EnableAspectJAutoProxy
@PropertySources({
@PropertySource(value="file:///${UMConfigPath}"),
@PropertySource(value="classpath:messages.properties")
})
@ComponentScan(basePackages = { "com.capv.um.repository", "com.capv.um.service", 
								"com.capv.um.rest.controller", "com.capv.um.rest.exception", 
								"com.capv.um.spring.aop", "com.capv.um.cache"})
@Import({ WebSecurityConfig.class, OAuth2ServerConfiguration.class, 
			OAuth2ServerConfiguration.ResourceServerConfiguration.class,
			OAuth2ServerConfiguration.AuthorizationServerConfiguration.class,
			DataSourceConfig.class, HibernateConfiguration.class, 
			EmailConfig.class, XmppRestClientConfig.class, CapvWebSocketConfig.class })
public class AppConfig implements WebMvcConfigurer {
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public Docket api(){
	    return new Docket(DocumentationType.SWAGGER_2)
	        .select()
	        .apis(RequestHandlerSelectors.any())
	        .paths(paths())
	        .build()
	        .apiInfo(apiInfo());
	}
	
	@Bean
	public RequestPreProcessFilter requestPreProcessFilter() {
		return new RequestPreProcessFilter();
	}
	
	private ApiInfo apiInfo() {
	    ApiInfo apiInfo = new ApiInfo(
	        "Capv User Management REST API",
	        "Enables video calling and chatting services.",
	        "1.0",
	        "API TOS",
	        "capv@caprusit.com",
	        "",
	        ""
	    );
	    return apiInfo;
	}
	
	@SuppressWarnings("unchecked")
	private Predicate<String> paths() {
		
		return Predicates.or(PathSelectors.regex("/user.*"), 
							PathSelectors.regex("/turn.*"), 
							PathSelectors.regex("/video/recording.*"), 
							PathSelectors.regex("/roomList.*"), 
							PathSelectors.regex("/updateS3path.*"));
	}
	
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JodaModule());
		mapper.registerModule(new Hibernate4Module());
		mapper.setSerializationInclusion(Include.NON_NULL);
	    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(mapper);
	    
	    converters.add(jsonConverter);
	    converters.add(new StringHttpMessageConverter());
	    
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/Html/**").addResourceLocations("Html/");
		registry.addResourceHandler("/templates/**").addResourceLocations("templates/");
		registry.addResourceHandler("/imgs/**").addResourceLocations("imgs/");
		registry.addResourceHandler("/images/**").addResourceLocations("images/");
	    registry.addResourceHandler("/css/**").addResourceLocations("css/");
	    registry.addResourceHandler("/js/**").addResourceLocations("js/");
	    registry.addResourceHandler("/sounds/**").addResourceLocations("sounds/");
	    registry.addResourceHandler("/fonts/**").addResourceLocations("fonts/");
	    registry.addResourceHandler("/home/*.html").addResourceLocations("home/");
	    registry.addResourceHandler("/admin/*.html").addResourceLocations("admin/");
	    registry.addResourceHandler("/user/*.html").addResourceLocations("user/");
	    registry.addResourceHandler("/*.html").addResourceLocations("/");
	    registry.addResourceHandler("/*.txt").addResourceLocations("/");
	    registry.addResourceHandler("/*.xml").addResourceLocations("/");
	    registry.addResourceHandler("/capv.jsp").addResourceLocations("/");
	    registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
	}
	
	@Bean
    public WebSocketPingScheduler sendPingTask() {
        return new WebSocketPingScheduler();
    }
	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
	    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
	    multipartResolver.setMaxUploadSize(536870912);
	    return multipartResolver;
	}
}
