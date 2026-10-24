package com.smartcare.hrms.common.entity.master.common;

import com.smartcare.hrms.common.entity.BaseEntityId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="country_master")
public class Country extends BaseEntityId {


    @Column(name = "name", nullable = false)
    private String name;
}
