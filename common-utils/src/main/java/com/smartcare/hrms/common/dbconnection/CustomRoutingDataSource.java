package com.smartcare.hrms.common.dbconnection;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CustomRoutingDataSource extends AbstractRoutingDataSource {

    private String tenant;

    @Override
    protected Object determineCurrentLookupKey() {

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(null != servletRequestAttributes) {

            HttpServletRequest request=servletRequestAttributes.getRequest();
            String tenant=request.getHeader("clinicid");
            String url= request.getRequestURI();
            if(url==null) {
                url="";
            }
            if(tenant==null && url.contains("/clinicid")) {
                String[] temp =url.split("clinicid/");
                setTenant(temp[1]);
                return getTenant();
            }
            else if(tenant==null) {
                throw new RuntimeException("Unautherized request");
            }else {
                setTenant(tenant);
                return tenant;
            }
        }

        return "admin";
    }
    public String getTenant() {
        return tenant;
    }
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

}

