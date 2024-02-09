package com.canvas.sync.dao.store.impl;

import com.canvas.sync.dao.entity.BaseEntity;
import com.canvas.sync.dao.store.BaseStore;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseStoreImpl<T extends BaseEntity> implements BaseStore<T> {

    final Class<T> entityClass;

    @PersistenceContext
    protected EntityManager em;

    @Autowired
    protected JPAQueryFactory jpaQueryFactory;

    public BaseStoreImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Transactional
    public T save(T entity) {
        em.persist(entity);
        return entity;
    }

    @Transactional
    @Override
    public void saveAll(Collection<T> entities) {
        entities.forEach(e -> em.persist(e));
    }

    @Transactional
    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public T findById(Long id) {
        return em.find(entityClass, id);
    }

    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> rootEntry = cq.from(entityClass);
        CriteriaQuery<T> all = cq.select(rootEntry);
        TypedQuery<T> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
        em.remove(findById(id));
    }

    protected void flushAndClear() {
        em.flush();
        em.clear();
    }

    protected <K, V> Map<K, V> toMap(List<Tuple> tupels, Class<K> keyClass, Class<V> valueClass) {
        return tupels.stream()
            .collect(Collectors.toMap(
                t -> t.get(0, keyClass),
                t -> t.get(1, valueClass)));
    }

}
