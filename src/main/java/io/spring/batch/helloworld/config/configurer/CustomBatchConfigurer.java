package io.spring.batch.helloworld.config.configurer;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

public class CustomBatchConfigurer extends DefaultBatchConfigurer {
    @Autowired
    private DataSource dataSource;

    @Override
    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
        factoryBean.setDatabaseType(DatabaseType.MYSQL.getProductName());//default: 자동 식
        factoryBean.setTablePrefix("FOO_"); //default: BATCH_
        factoryBean.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ"); //default: serealizable별
        factoryBean.setDataSource(dataSource);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}
