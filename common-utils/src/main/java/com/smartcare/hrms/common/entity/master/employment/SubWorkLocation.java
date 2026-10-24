package com.smartcare.hrms.common.entity.master.employment;

import java.io.Serial;

import com.smartcare.hrms.common.entity.BaseEntityId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "sub_wrk_loc")
public class SubWorkLocation extends BaseEntityId {
	
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;

	@ManyToOne
	@JoinColumn(name = "wrk_loc_id", referencedColumnName = "id", nullable = true)
	private WorkLocation workLocation;

}
