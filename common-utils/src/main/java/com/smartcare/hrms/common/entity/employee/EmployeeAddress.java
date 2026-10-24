package com.smartcare.hrms.common.entity.employee;

import java.io.Serial;
import java.time.LocalDate;

import com.smartcare.hrms.common.entity.BaseEntityId;

import com.smartcare.hrms.common.enumeration.AddressType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "emp_address")
@Builder
public class EmployeeAddress extends BaseEntityId {

	@Serial
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_id")
	private Employee employee;
	
	@Column(name = "country_id")
    private Integer countryId;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "city_id")
    private Integer cityId;
	
	@Column(name = "address")
    private String address;
	
	@Column(name = "pincode")
	private String pinCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type")
    private AddressType addressType;
	
	@Column(name = "from_date")
	private LocalDate fromDate;
	
	@Column(name = "to_date")
	private LocalDate toDate;
	
	@Transient
    private boolean remove;
}
