package com.smartcare.hrms.common.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Date;


@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditable<U> {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    protected U createdBy;

    @CreatedDate
    @Column(name = "created_datetime", updatable = false)
    protected Instant createdDate;

    @LastModifiedBy
    @Column(name = "modified_by")
    protected U lastModifiedBy;

    @LastModifiedDate
    @Column(name = "modified_datetime")
    protected Instant modifiedDate;

}
