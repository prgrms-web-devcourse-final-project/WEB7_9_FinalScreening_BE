package com.back.matchduo.global.entity;

import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.SQLRestriction;

@MappedSuperclass
@SQLRestriction("is_active = true")
public abstract class SoftDeletableEntity extends BaseEntity {
}
