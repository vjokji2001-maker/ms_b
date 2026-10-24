package com.smartcare.hrms.common.entity.master.employment;

import java.io.Serial;

import com.smartcare.hrms.common.entity.BaseEntityId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "grades")
public class Grade extends BaseEntityId {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;

}
