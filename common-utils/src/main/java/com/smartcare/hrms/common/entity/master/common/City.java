package com.smartcare.hrms.common.entity.master.common;

import com.smartcare.hrms.common.entity.BaseEntityId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="city_master")
public class City extends BaseEntityId {
    @Column(name="name")
    private String name;

    @ManyToOne
    @JoinColumn(name="state_id",  nullable = false)
    private State state;
}
