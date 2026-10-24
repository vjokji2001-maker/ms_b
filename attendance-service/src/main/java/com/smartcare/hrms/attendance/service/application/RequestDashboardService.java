package com.smartcare.hrms.attendance.service.application;

import com.smartcare.hrms.attendance.config.MessageResource;
import com.smartcare.hrms.attendance.dao.application.CancelDashboardDAO;
import com.smartcare.hrms.attendance.dao.application.RequestDashboardDAO;
import com.smartcare.hrms.attendance.dto.application.CancelDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.CancelDashboardSearchDTO;
import com.smartcare.hrms.attendance.dto.application.RequestDashboardDTO;
import com.smartcare.hrms.attendance.dto.application.RequestDashboardSearchDTO;
import com.smartcare.hrms.common.globalexception.ValidationException;
import com.smartcare.hrms.common.pagination.PaginatedDataResponse;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestDashboardService {
    private @Autowired RequestDashboardDAO requestDashboardDAO;
    private @Autowired CancelDashboardDAO cancelDashboardDAO;
    @Autowired private MessageResource messageResource;


    public PaginatedDataResponse<RequestDashboardDTO> dashboard(RequestDashboardSearchDTO searchDTO){
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return  requestDashboardDAO.getData(searchDTO);
    }


    public PaginatedDataResponse<CancelDashboardDTO> dashboard(CancelDashboardSearchDTO searchDTO){
        if (ValidateUtils.isZero(searchDTO.getPaginationInfo().getCurrentPage())) {
            throw new ValidationException(messageResource.getEnglishMessage("current.page.zero"));
        }
        return  cancelDashboardDAO.getData(searchDTO);
    }
}
