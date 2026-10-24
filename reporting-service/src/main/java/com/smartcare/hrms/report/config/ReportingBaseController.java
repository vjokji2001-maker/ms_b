package com.smartcare.hrms.report.config;

import com.smartcare.hrms.common.basecontroller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportingBaseController extends BaseController {
    protected @Autowired MessageResource messageResource;
}
