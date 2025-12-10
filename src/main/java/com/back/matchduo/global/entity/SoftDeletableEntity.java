package com.back.matchduo.global.entity;

import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;

@MappedSuperclass
@FilterDef(name = "softDeleteFilter")
@Filter(name = "softDeleteFilter", condition = "is_active = true")
public abstract class SoftDeletableEntity extends BaseEntity {
}
