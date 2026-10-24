package com.smartcare.hrms.common.dbconnection;

import com.smartcare.hrms.common.utility.Constants;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConnectionProvider implements ConnectionProviderInterface {

    public ConnectionProvider() {

    }

    @Override
    public Map<Object, Object> getAllActiveClients() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUsername(Constants.SCHEMA_USERID);
            dataSource.setPassword(Constants.SCHEMA_PASSWORD);
            dataSource.setDriverClassName(Constants.SCHEMA_DRIVER);
            dataSource.setUrl(Constants.ADMIN_HOST);
            map.put("admin",dataSource);
            Class.forName(Constants.SCHEMA_DRIVER);
            Connection connection = DriverManager.getConnection(Constants.ADMIN_HOST,Constants.SCHEMA_USERID,Constants.SCHEMA_PASSWORD);
            String sql= "select clinic_userid, ip_address, is_active, IsOptedForMobileApp from all_client_details where is_active!=0";
            PreparedStatement preparableStatement = connection.prepareStatement(sql);
            ResultSet rs= preparableStatement.executeQuery();
            while(rs.next()) {
                String clinicId= rs.getString(1);
                String ipaddress= rs.getString(2);
                int active= rs.getInt(3);
                if(active!=0) {
                    dataSource = new DriverManagerDataSource();
                    dataSource.setUsername(Constants.SCHEMA_USERID);
                    dataSource.setPassword(Constants.SCHEMA_PASSWORD);
                    dataSource.setDriverClassName(Constants.SCHEMA_DRIVER);
                    StringBuffer url =new StringBuffer();
                    if(active==2) {
                        url.append("jdbc:mysql://");
                        url.append(ipaddress);
                        url.append(":3306/");
                        url.append(clinicId);
                        url.append("?useTimezone=true&serverTimezone=UTC");
                    }
                    else {
                        url.append(Constants.SCHEMA_HOST);
                        url.append(clinicId);
                        url.append("?useTimezone=true&serverTimezone=UTC");
                    }
                    dataSource.setUrl(url.toString());
                    map.put(clinicId, dataSource);
                }
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;

    }
}
