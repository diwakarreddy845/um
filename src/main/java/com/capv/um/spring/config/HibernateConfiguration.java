package com.capv.um.spring.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class HibernateConfiguration {

	@Autowired
	private Environment environment;

	@Bean
	public LocalContainerEntityManagerFactoryBean sessionFactory(@Qualifier("dataSource") DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean sessionFactory = new LocalContainerEntityManagerFactoryBean();
		sessionFactory.setDataSource(dataSource);
		sessionFactory.setPackagesToScan("com.capv.um.model");
		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		sessionFactory.setJpaVendorAdapter(vendorAdapter);
		sessionFactory.setJpaProperties(hibernateProperties());
		return sessionFactory;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean sessionFactoryForOpenFire(@Qualifier("dataSourceForOpenFire") DataSource dataSourceForOpenFire) {
		LocalContainerEntityManagerFactoryBean sessionFactory = new LocalContainerEntityManagerFactoryBean();
		sessionFactory.setDataSource(dataSourceForOpenFire);
		sessionFactory.setPackagesToScan("com.capv.um.chat.model");
		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		sessionFactory.setJpaVendorAdapter(vendorAdapter);
		sessionFactory.setJpaProperties(hibernateProperties());
		return sessionFactory;
	}

	private Properties hibernateProperties() {
		Properties properties = new Properties();
		properties.put("hibernate.dialect", environment.getRequiredProperty("hibernate.dialect"));
		properties.put("hibernate.show_sql", environment.getRequiredProperty("hibernate.show_sql"));
		properties.put("hibernate.format_sql", environment.getRequiredProperty("hibernate.format_sql"));
		properties.put("hibernate.hbm2ddl.auto", environment.getRequiredProperty("hibernate.hbm2ddl"));
		return properties;
	}
}
