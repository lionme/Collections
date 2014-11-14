package com.crediteuropebank.vacationsmanager.springconfig;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.sql.DataSource;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Log4jConfigurer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.crediteuropebank.vacationsmanager.server.PropertiesBean;
import com.crediteuropebank.vacationsmanager.server.mail.MailSender;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 
 * <p>This class is used to configure Spring for testing purposes. As you can see
 * for test purposes we use fully Java-based configuration.</p>
 * 
 * <p>There is still present xml config file, but it is used only for loading properties files
 * because I don't find a way to do this properly using Java configuration.</p>
 * 
 * @author DIMAS
 *
 */
@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement(mode=AdviceMode.PROXY, order=1, proxyTargetClass=true)
@EnableCaching
@ComponentScan(basePackages={"com.crediteuropebank.vacationsmanager.server.dao", 
		"com.crediteuropebank.vacationsmanager.server.manager",
		"com.crediteuropebank.vacationsmanager.server.validation",
		"com.crediteuropebank.vacationsmanager.server.dblogging",
		"com.crediteuropebank.vacationsmanager.logging"}, 
		basePackageClasses = PropertiesBean.class)
/*@PropertySource(value={"classpath:db.properties",  // Such property configuration works only with Environment
		"classpath:mail.properties", 
		"classpath:vacations.properties"})*/
/* import xml file with configurations that we cannot move to class*/
@ImportResource(value="classpath:testApplicationContext.xml")
public class TestApplicationConfig {

	private static final String LOG4J_PRPERTIES_LOCATION = "classpath:log4j-test.properties";
	
	static {
		try {
			Log4jConfigurer.initLogging(LOG4J_PRPERTIES_LOCATION);
		} catch (FileNotFoundException e) {
			// Simply rethrow exception as RuntimeException
			throw new RuntimeException(e);
		}
	}
	
	/*@Autowired
	private Environment environment;*/
	// If you want to use Environment you can get property like this: environment.getProperty(propName);
	
	// Properties for data source
	private @Value("${jdbc.driverClassName}") String driverClass;
	private @Value("${jdbc.url}") String jdbcUrl;
	private @Value("${jdbc.username}") String user;
	private @Value("${jdbc.password}") String password;
	
	// Properties for mail sender
	private @Value("${mail.host}") String host;
	private @Value("${mail.port}") int port;
	private @Value("${mail.username}") String mailUsername;
	private @Value("${mail.password}") String mailPassword;
	private @Value("${mail.timeout}") int timeout;

	/**
	 * Defines mock implementation of the MailSender.
	 * 
	 * @return
	 */
	@Bean(name="mailSender")
	public MailSender mailSender() {

		/*
		 *  Create simple mock object and return it instead of real mail sender for not to send
		 *  email messages during run of the test.
		 */
		MailSender mockMailSender = Mockito.mock(MailSender.class);
		
		return mockMailSender;
	}
	
	/**
	 * Defines data source
	 * 
	 * @return
	 */
	@Bean(name="dataSource")
	public DataSource dataSource() {
		final ComboPooledDataSource dataSource = new ComboPooledDataSource();
		
		try {
			dataSource.setDriverClass(driverClass);
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
		
		dataSource.setJdbcUrl(jdbcUrl);
		dataSource.setUser(user);
		dataSource.setPassword(password);
		dataSource.setMinPoolSize(3);
		dataSource.setMaxPoolSize(15);
		dataSource.setDebugUnreturnedConnectionStackTraces(true);
	
		return dataSource;
	}
	
	/**
	 * Defines a standard spring's mail sender
	 * 
	 * @return
	 */
	@Bean(name="javaMailSender")
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(mailUsername);
		mailSender.setPassword(mailPassword);
		mailSender.setProtocol("smtp");
		
		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.auth", true);
		javaMailProperties.put("mail.smtp.starttls.enable", true);
		javaMailProperties.put("mail.smtp.timeout", timeout);
		
		mailSender.setJavaMailProperties(javaMailProperties);
		
		return mailSender;
	}
	
	/**
	 * Defines a password encoder.
	 * 
	 * @return
	 */
	@Bean(name="passwordEncoder")
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder passwordEncoder = new ShaPasswordEncoder();
		
		return passwordEncoder;
	}
	
	/**
	 * Defines transaction manager
	 * 
	 * @return
	 */
	@Bean(name="txManager")
	public DataSourceTransactionManager transactionManager() {
		DataSourceTransactionManager instance = new DataSourceTransactionManager();
		instance.setDataSource(dataSource());
		
		return instance;
	}
	
	/**
	 * Defines JdmcTemplate
	 * 
	 * @return
	 */
	@Bean
	public JdbcTemplate jdbcTemplate() {
		JdbcTemplate instance = new JdbcTemplate();
		instance.setDataSource(dataSource());
		
		return instance;
	}
	
	/**
	 * Defines validator
	 * 
	 * @return
	 */
	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}
	
	/*
	 * Defines classes necessary for correct work of the cache.
	 */
	
	@Bean
	public EhCacheManagerFactoryBean ehCache() {
		EhCacheManagerFactoryBean instance = new EhCacheManagerFactoryBean();
		instance.setConfigLocation(new ClassPathResource("ehcache.xml"));
		
		return instance;
	}
	
	@Bean
	public EhCacheCacheManager cacheManager() {
		EhCacheCacheManager instance = new EhCacheCacheManager();
		instance.setCacheManager(ehCache().getObject());
		
		return instance;
	}
}
