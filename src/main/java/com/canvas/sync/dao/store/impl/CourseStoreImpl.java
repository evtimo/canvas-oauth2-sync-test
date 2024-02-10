package com.canvas.sync.dao.store.impl;

import com.canvas.sync.dao.entity.CourseEntity;
import com.canvas.sync.dao.store.CourseStore;
import org.springframework.stereotype.Repository;

@Repository
public class CourseStoreImpl extends BaseStoreImpl<CourseEntity> implements CourseStore {

    public CourseStoreImpl() {
        super(CourseEntity.class);
    }
}
