package com.smartcare.hrms.migration.config;

import com.smartcare.hrms.common.dbconnection.ConnectionProvider;
import com.smartcare.hrms.common.dbconnection.ConnectionProviderInterface;
import com.smartcare.hrms.common.dbconnection.CustomRoutingDataSource;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@Log4j2
public class ConnectionConfig {

    public ConnectionProviderInterface getActiveClientConnection(){
        return new ConnectionProvider();
    }

    @Bean("dataSource")
    public DataSource getActiveClientDataSource(){
        CustomRoutingDataSource customRoutingDataSource= new CustomRoutingDataSource();
        customRoutingDataSource.setTargetDataSources(getActiveClientConnection().getAllActiveClients());
        return customRoutingDataSource;
    }
}
