package com.canvas.sync.dao.store.impl;

import com.canvas.sync.dao.entity.CourseEntity;
import com.canvas.sync.dao.store.CourseStore;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@Repository
public class CourseStoreImpl extends BaseStoreImpl<CourseEntity> implements CourseStore {

    private final DataSource dataSource;

    public CourseStoreImpl(DataSource dataSource) {
        super(CourseEntity.class);
        this.dataSource = dataSource;
    }

    @SneakyThrows
    @Override
    @Transactional
    public void saveAll(Collection<CourseEntity> courses) {
        Thread.sleep(1000);
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO course (id, name, root_account_id, last_sync_at) VALUES (?, ?, ?, ?) ON CONFLICT (id) " +
                "DO UPDATE SET name = EXCLUDED.name, root_account_id = EXCLUDED.root_account_id, last_sync_at = EXCLUDED.last_sync_at";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (CourseEntity course : courses) {
                    statement.setLong(1, course.getId());
                    statement.setString(2, course.getName());
                    statement.setLong(3, course.getRootAccountId());
                    statement.setObject(4, LocalDateTime.now()); // Adapt as necessary
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (Exception e) {
            log.error("Error at saveAll() for courses: {}", e.getMessage());
        }
    }
}
