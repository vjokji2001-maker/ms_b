package com.smartcare.hrms.common.entity.master.attendance.weekOff;

import java.io.Serial;
import java.util.List;

import com.smartcare.hrms.common.entity.BaseEntityId;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
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
@Table(name = "wk_off_prnt")
public class WeekOffParent extends BaseEntityId {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;
	
	@OneToMany(mappedBy = "weekOff", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<WeekOffDetail> weekOffDetail;	

}
