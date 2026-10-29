package io.github.jooho2075.statrecord.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

// 기록 테이블용: 작성자, 수정자 포함
@Getter
@MappedSuperclass
public abstract class BasedAuditEntity extends BaseTimeEntity {
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private Long updatedBy;
}
