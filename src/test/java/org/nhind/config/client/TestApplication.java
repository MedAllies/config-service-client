package org.nhind.config.client;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.nhindirect.config.processor.BundleCacheUpdateProcessor;
import org.nhindirect.config.processor.BundleRefreshProcessor;
import org.nhindirect.config.processor.impl.DefaultBundleCacheUpdateProcessorImpl;
import org.nhindirect.config.processor.impl.DefaultBundleRefreshProcessorImpl;
import org.nhindirect.config.store.dao.TrustBundleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.util.ClassUtils;

@EnableFeignClients({"org.nhind.config.rest.feign"})
@SpringBootApplication
@ComponentScan({"org.nhindirect.config", "org.nhind.config"})
public class TestApplication
{
	@Autowired
	protected CamelContext camelContext;
	
    public static void main(String[] args) 
    {
        SpringApplication.run(TestApplication.class, args);
    }  
    
    @Bean
    public ProducerTemplate bundleRefresh() throws Exception 
    {
    	final ProducerTemplate template = camelContext.createProducerTemplate();
    	template.setDefaultEndpointUri("direct:refresh-start");
        return template;
    }  
    
    @Bean
	public BundleRefreshProcessor bundleRefreshProcessor(TrustBundleDao trustBundleDao)
	{
		final DefaultBundleRefreshProcessorImpl retVal = new DefaultBundleRefreshProcessorImpl();
		retVal.setDao(trustBundleDao);
		
		return retVal;
	}
	
	@Bean 
	public BundleCacheUpdateProcessor bundleCacheUpdateProcessor(BundleRefreshProcessor refreshProc, TrustBundleDao trustBundleDao)
	{
		DefaultBundleCacheUpdateProcessorImpl proc = new DefaultBundleCacheUpdateProcessorImpl();
		proc.setRefreshProcessor(refreshProc);
		proc.setDao(trustBundleDao);
		
		return proc;
	}    
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource)
	{		   
		  try
		  {
			 final HibernateJpaVendorAdapter jpaAdaptor = new HibernateJpaVendorAdapter();
			 jpaAdaptor.setGenerateDdl(true);
			 
			 final LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
			 entityManagerFactory.setDataSource(dataSource);
			 entityManagerFactory.setPersistenceUnitName("config-store");
			 entityManagerFactory.setJpaVendorAdapter(jpaAdaptor);
			 
			 entityManagerFactory.setPackagesToScan(ClassUtils.getPackageName(org.nhindirect.config.store.Anchor.class));

			 return entityManagerFactory;		
		  }
		  catch (Exception e)
		  {
			  throw new IllegalStateException("Failed to build entity factory manager.", e);
		  }
	}	
}
