package com.canvas.sync.dao.store.impl;

import com.canvas.sync.dao.entity.BaseEntity;
import com.canvas.sync.dao.store.BaseStore;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.engine.spi.ActionQueue;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Autowired;

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

    private final int BATCH_SIZE = 10;

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
        entities.forEach(e -> em.merge(e));
        /*entities.forEach(entity -> {
            em.persist(entity);
            flushAndClearIfBatchSizeReached(BATCH_SIZE);
        });
        flushAndClear();*/
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

    protected void flushAndClearIfBatchSizeReached(int batchSize) {
        Session session = em.unwrap(Session.class);
        SessionImplementor sessionImplementor = (SessionImplementor) session;
        ActionQueue actionQueue = sessionImplementor.getActionQueue();

        if (actionQueue.numberOfCollectionRemovals() % batchSize == 0) {
            flushAndClear();
        }
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
