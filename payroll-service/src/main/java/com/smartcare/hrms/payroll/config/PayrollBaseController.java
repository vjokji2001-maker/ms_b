package com.smartcare.hrms.payroll.config;

import com.smartcare.hrms.common.basecontroller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PayrollBaseController extends BaseController {
    protected @Autowired MessageResource messageResource;
}
