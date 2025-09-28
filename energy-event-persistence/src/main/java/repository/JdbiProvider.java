package repository;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import org.jdbi.v3.core.Jdbi;

public class JdbiProvider {

    private static Jdbi jdbi;

    public static Jdbi provideJdbi(Environment environment, DataSourceFactory dataSourceFactory) {
        if (jdbi == null) {
            jdbi = createJdbi(environment, dataSourceFactory);
        }
        return jdbi;
    }

    private static Jdbi createJdbi(Environment environment, DataSourceFactory dataSourceFactory) {
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, dataSourceFactory, "postgresql");
        return jdbi;
    }
}
