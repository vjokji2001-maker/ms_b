package com.smartcare.hrms.common.basecontroller;

import com.smartcare.hrms.common.response.ResponseGenerator;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseController {

    @Autowired
    protected ResponseGenerator responseGenerator;

}