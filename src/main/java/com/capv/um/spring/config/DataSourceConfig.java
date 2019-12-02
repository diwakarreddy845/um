package com.capv.um.spring.config;

import java.sql.Connection;
import java.sql.Statement;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.util.CapvUtil;

@Configuration
public class DataSourceConfig {

	@Autowired
    private Environment environment;
	
	@Bean(name = "dataSource")
	public DriverManagerDataSource dataSource() {
	    DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
	    driverManagerDataSource.setDriverClassName(environment.getRequiredProperty("jdbc.driverClassName"));
	    driverManagerDataSource.setUrl(environment.getRequiredProperty("jdbc.url"));
	    driverManagerDataSource.setUsername(environment.getRequiredProperty("jdbc.username"));
	    driverManagerDataSource.setPassword(environment.getRequiredProperty("jdbc.password"));
	    return driverManagerDataSource;
	}
	
	
	@PostConstruct
	public void setPropertiesEnvironment() throws Exception {
		CapvClientUserUtil.setPropertiesEnvironment(environment);
		CapvUtil.setPropertiesEnvironment(environment);
		
		try {
			Connection conn = dataSource().getConnection();
			Statement stmt = conn.createStatement();
			
			stmt.executeUpdate("update user set call_status = 1");
			stmt.executeUpdate("update user_call_state set call_status = 4 where call_status in(1,2,3)");
			
			conn.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	@Bean(name = "dataSourceForOpenFire")
	public DriverManagerDataSource dataSourceForOpenFire() {
	    DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
	    driverManagerDataSource.setDriverClassName(environment.getRequiredProperty("jdbc.driverClassName"));
	    driverManagerDataSource.setUrl(environment.getRequiredProperty("jdbc.urlopenfire"));
	    driverManagerDataSource.setUsername(environment.getRequiredProperty("jdbc.username"));
	    driverManagerDataSource.setPassword(environment.getRequiredProperty("jdbc.password"));
	    return driverManagerDataSource;
	}
	
}
