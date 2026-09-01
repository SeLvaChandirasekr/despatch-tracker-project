package com.abi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.abi.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class PersistenceConfig {

    @Value("${spring.jpa.hibernate.ddl-auto:validate}")
    private String ddlAuto;

    @Primary
    @Bean
    public DataSource dataSource(final DataSourceProperties properties, final Environment environment) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(properties.getUrl());
        hikariConfig.setUsername(properties.getUsername());
        hikariConfig.setPassword(properties.getPassword());
        hikariConfig.setDriverClassName(properties.getDriverClassName());

        final String poolName = environment.getProperty("spring.datasource.hikari.pool-name", "EnterpriseHikariPool");
        final int maxPoolSize = Integer.parseInt(environment.getProperty("spring.datasource.hikari.maximum-pool-size", "20"));
        final int minIdle = Integer.parseInt(environment.getProperty("spring.datasource.hikari.minimum-idle", "5"));
        final long idleTimeout = Long.parseLong(environment.getProperty("spring.datasource.hikari.idle-timeout", "300000"));
        final long connectionTimeout = Long.parseLong(environment.getProperty("spring.datasource.hikari.connection-timeout", "30000"));
        final long leakDetectionThreshold = Long.parseLong(environment.getProperty("spring.datasource.hikari.leak-detection-threshold", "60000"));

        hikariConfig.setPoolName(poolName);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(minIdle);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setLeakDetectionThreshold(leakDetectionThreshold);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(hikariConfig);
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            final DataSource dataSource,
            final JpaVendorAdapter jpaVendorAdapter,
            final Environment environment) {

        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.abi.entity");
        em.setJpaVendorAdapter(jpaVendorAdapter);

        final Properties hibernateProperties = new Properties();
        hibernateProperties.put("hibernate.hbm2ddl.auto", ddlAuto);
        hibernateProperties.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
        hibernateProperties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        
        // LOB creation configuration
        hibernateProperties.put("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        // JDBC batching configuration
        hibernateProperties.put("hibernate.jdbc.batch_size", "50");
        hibernateProperties.put("hibernate.order_inserts", "true");
        hibernateProperties.put("hibernate.order_updates", "true");

        // Strict Lazy loading configuration
        hibernateProperties.put("hibernate.enable_lazy_load_no_trans", "false");

        // Environment conditional configuration: Dev vs Prod
        final boolean isDev = environment.acceptsProfiles(Profiles.of("dev"));
        if (isDev) {
            hibernateProperties.put("hibernate.show_sql", "true");
            hibernateProperties.put("hibernate.format_sql", "true");
            hibernateProperties.put("hibernate.generate_statistics", "true");
            hibernateProperties.put("hibernate.highlight_sql", "true");
        } else {
            hibernateProperties.put("hibernate.show_sql", "false");
            hibernateProperties.put("hibernate.format_sql", "false");
            hibernateProperties.put("hibernate.generate_statistics", "false");
        }

        em.setJpaProperties(hibernateProperties);
        return em;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        final HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setGenerateDdl(false);
        adapter.setShowSql(false);
        return adapter;
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
