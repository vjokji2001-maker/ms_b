package com.smartcare.hrms.attendance.config;

import com.smartcare.hrms.common.dbconnection.ConnectionProvider;
import com.smartcare.hrms.common.dbconnection.ConnectionProviderInterface;
import com.smartcare.hrms.common.dbconnection.CustomRoutingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
