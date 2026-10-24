package com.smartcare.hrms.hiring.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.smartcare.hrms.common.dbconnection.ConnectionProvider;
import com.smartcare.hrms.common.dbconnection.CustomRoutingDataSource;
import com.smartcare.hrms.common.dbconnection.ConnectionProviderInterface;

import javax.sql.DataSource;

@Configuration
public class ConnectionConfig {

    public ConnectionProviderInterface getActiveClientConnection() {
        return new ConnectionProvider();
    }

    @Bean("dataSource")
    public DataSource getActiveClientDataSource() {
        CustomRoutingDataSource customRoutingDataSource = new CustomRoutingDataSource();
        customRoutingDataSource.setTargetDataSources(getActiveClientConnection().getAllActiveClients());
        return customRoutingDataSource;
    }
}

