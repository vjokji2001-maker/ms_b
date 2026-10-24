package com.smartcare.hrms.common.dbconnection;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface ConnectionProviderInterface {

    Map<Object, Object> getAllActiveClients();
}
