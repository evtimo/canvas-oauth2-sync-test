package com.canvas.sync.dao.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
public abstract class BaseEntity implements Serializable {

    @LastModifiedDate
    private Instant lastSyncAt;

}
