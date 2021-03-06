/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.data.jpa;

import static org.hibernate.cfg.AvailableSettings.CACHE_REGION_FACTORY;
import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.GENERATE_STATISTICS;
import static org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO;
import static org.hibernate.cfg.AvailableSettings.JPA_JDBC_DRIVER;
import static org.hibernate.cfg.AvailableSettings.JPA_JDBC_PASSWORD;
import static org.hibernate.cfg.AvailableSettings.JPA_JDBC_URL;
import static org.hibernate.cfg.AvailableSettings.JPA_JDBC_USER;
import static org.hibernate.cfg.AvailableSettings.QUERY_STARTUP_CHECKING;
import static org.hibernate.cfg.AvailableSettings.SHOW_SQL;
import static org.hibernate.cfg.AvailableSettings.STATEMENT_BATCH_SIZE;
import static org.hibernate.cfg.AvailableSettings.USE_QUERY_CACHE;
import static org.hibernate.cfg.AvailableSettings.USE_REFLECTION_OPTIMIZER;
import static org.hibernate.cfg.AvailableSettings.USE_SECOND_LEVEL_CACHE;
import static org.hibernate.cfg.AvailableSettings.USE_STRUCTURED_CACHE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import app.owlcms.Main;
import app.owlcms.data.agegroup.AgeGroup;
import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.category.Category;
import app.owlcms.data.competition.Competition;
import app.owlcms.data.group.Group;
import app.owlcms.data.platform.Platform;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class JPAService.
 */
public class JPAService {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(JPAService.class);
    private static final Logger startLogger = (Logger) LoggerFactory.getLogger(Main.class);
    static {
        logger.setLevel(Level.INFO);
    }

    protected static EntityManagerFactory factory;

    private static String dbUrl;

    private static String userName;

    private static String password;

    /**
     * Close.
     */
    public static void close() {
        if (factory != null) {
            factory.close();
        }
        factory = null;
    }

    /**
     * Entity class names.
     *
     * @return the list
     */
    protected static List<String> entityClassNames() {
        ImmutableList<String> vals = new ImmutableList.Builder<String>().add(Group.class.getName())
                .add(Category.class.getName()).add(Athlete.class.getName()).add(Platform.class.getName())
                .add(Competition.class.getName()).add(AgeGroup.class.getName()).build();
        return vals;
    }

    /**
     * Gets the factory from code (without a persistance.xml file)
     *
     * @param memoryMode run from memory if true
     * @return an entity manager factory
     */
    private static EntityManagerFactory getFactory(boolean memoryMode, boolean reset) {
        Properties properties = processSettings(memoryMode, reset);

        PersistenceUnitInfo persistenceUnitInfo = new PersistenceUnitInfoImpl(JPAService.class.getSimpleName(),
                entityClassNames(), properties);
        Map<String, Object> configuration = new HashMap<>();

        factory = new EntityManagerFactoryBuilderImpl(new PersistenceUnitInfoDescriptor(persistenceUnitInfo),
                configuration).build();
        return factory;
    }

    private static Properties h2FileProperties(String schemaGeneration) {
        ImmutableMap<String, Object> vals = jpaProperties();
        Properties props = new Properties();
        props.putAll(vals);

        props.put(JPA_JDBC_URL,
                (dbUrl != null ? dbUrl : "jdbc:h2:file:./database/owlcms") + ";DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4");
        startLogger.debug("Starting in directory {}", System.getProperty("user.dir"));
        props.put(JPA_JDBC_USER, userName != null ? userName : "sa");
        props.put(JPA_JDBC_PASSWORD, password != null ? password : "");

        props.put(JPA_JDBC_DRIVER, org.h2.Driver.class.getName());
        props.put(DIALECT, H2Dialect.class.getName());
        props.put("javax.persistence.schema-generation.database.action", schemaGeneration);

        return props;
    }

    /**
     * Properties for running in memory (used for tests and demos)
     *
     * @return the properties
     */
    protected static Properties h2MemProperties(String schemaGeneration) {
        ImmutableMap<String, Object> vals = jpaProperties();
        Properties props = new Properties();
        props.putAll(vals);

        // keep the database even if all the connections have timed out
        props.put(JPA_JDBC_URL, "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4");
        props.put(JPA_JDBC_USER, "sa");
        props.put(JPA_JDBC_PASSWORD, "");

        props.put(JPA_JDBC_DRIVER, org.h2.Driver.class.getName());
        props.put("javax.persistence.schema-generation.database.action", schemaGeneration);
        props.put(DIALECT, H2Dialect.class.getName());
        return props;
    }

    /**
     * Inits the database
     *
     * @param inMemory if true, start with in-memory database
     */
    public static void init(boolean inMemory, boolean reset) {
        if (factory == null) {
            factory = getFactory(inMemory, reset);
        }
    }

    private static ImmutableMap<String, Object> jpaProperties() {
        ImmutableMap<String, Object> vals = new ImmutableMap.Builder<String, Object>().put(HBM2DDL_AUTO, "update")
                .put(SHOW_SQL, false).put(QUERY_STARTUP_CHECKING, false).put(GENERATE_STATISTICS, false)
                .put(USE_REFLECTION_OPTIMIZER, false).put(USE_SECOND_LEVEL_CACHE, true).put(USE_QUERY_CACHE, false)
                .put(USE_STRUCTURED_CACHE, false).put(STATEMENT_BATCH_SIZE, 20)
                .put(CACHE_REGION_FACTORY, "org.hibernate.cache.jcache.JCacheRegionFactory")
                .put("hibernate.javax.cache.provider", "org.ehcache.jsr107.EhcacheCachingProvider")
                .put("hibernate.javax.cache.missing_cache_strategy", "create")
                .put("javax.persistence.sharedCache.mode", "ALL").put("hibernate.c3p0.min_size", 5)
                .put("hibernate.c3p0.max_size", 20).put("hibernate.c3p0.acquire_increment", 5)
                .put("hibernate.c3p0.timeout", 84200).put("hibernate.c3p0.preferredTestQuery", "SELECT 1")
                .put("hibernate.c3p0.testConnectionOnCheckout", true).put("hibernate.c3p0.idle_test_period", 500)
                .build();
        return vals;
    }

    private static Properties pgProperties(String schemaGeneration) {
        ImmutableMap<String, Object> vals = jpaProperties();
        Properties props = new Properties();
        props.putAll(vals);

        // if running on Heroku, the following three settings will come from the
        // environment (see processSettings)
        props.put(JPA_JDBC_URL, dbUrl != null ? dbUrl : "jdbc:postgresql://localhost:5432/owlcms");
        props.put(JPA_JDBC_USER, userName != null ? userName : "owlcms");
        props.put(JPA_JDBC_PASSWORD, password != null ? password : "db_owlcms");

        props.put(JPA_JDBC_DRIVER, org.postgresql.Driver.class.getName());
        props.put(DIALECT, org.hibernate.dialect.PostgreSQL95Dialect.class.getName());
        props.put("javax.persistence.schema-generation.database.action", schemaGeneration);

        return props;
    }

    public static Properties processSettings(boolean inMemory, boolean reset) throws RuntimeException {
        Properties properties;
        String schemaGeneration = reset ? "drop-and-create" : "update";

        // Environment variables (set by the operating system)
        dbUrl = System.getenv("JDBC_DATABASE_URL");
        userName = System.getenv("JDBC_DATABASE_USERNAME");
        password = System.getenv("JDBC_DATABASE_PASSWORD");

        if (inMemory || (dbUrl != null && dbUrl.startsWith("jdbc:h2:mem"))) {
            properties = h2MemProperties(schemaGeneration);
        } else if (dbUrl == null || (dbUrl != null && dbUrl.startsWith("jdbc:h2:file"))) {
            properties = h2FileProperties(schemaGeneration);
        } else if (dbUrl != null && dbUrl.startsWith("jdbc:postgres")) {
            properties = pgProperties(schemaGeneration);
        } else {
            throw new RuntimeException("Unsupported database: " + dbUrl);
        }
        startLogger.info("Database: {}, inMemory={}, reset={}", properties.get(JPA_JDBC_URL), inMemory, reset);
        return properties;
    }

    /**
     * Run in transaction.
     *
     * @param <T>      the generic type
     * @param function the function
     * @return the t
     */
    public static <T> T runInTransaction(Function<EntityManager, T> function) {
        EntityManager entityManager = null;

        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();

            T result = function.apply(entityManager);

            entityManager.getTransaction().commit();
            return result;

        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

}
