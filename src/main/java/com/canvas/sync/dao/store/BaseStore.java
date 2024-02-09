package com.canvas.sync.dao.store;

import com.canvas.sync.dao.entity.BaseEntity;

import java.util.Collection;
import java.util.List;

public interface BaseStore<T extends BaseEntity> {


    T findById(Long id);

    List<T> findAll();

    T save(T entity);

    void saveAll(Collection<T> entities);

    void delete(T entity);

    void deleteById(Long id);
}
