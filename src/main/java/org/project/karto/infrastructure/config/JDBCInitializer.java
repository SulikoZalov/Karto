package org.project.karto.infrastructure.config;

import com.hadzhy.jdbclight.jdbc.JDBC;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import javax.sql.DataSource;

@Startup
@ApplicationScoped
public class JDBCInitializer {

    private final DataSource dataSource;

    public JDBCInitializer(Instance<DataSource> dataSource) {
        this.dataSource = dataSource.get();
    }

    @PostConstruct
    public void init() {
        JDBC.init(dataSource);
    }
}
