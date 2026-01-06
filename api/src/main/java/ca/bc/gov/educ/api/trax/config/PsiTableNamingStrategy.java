package ca.bc.gov.educ.api.trax.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

/**
 * Custom naming strategy to dynamically set the PSI table name based on configuration.
 * This allows switching between TAB_POSTSEC and ISD_PSI_REGISTRY via environment variable.
 * Uses ApplicationContextAware to access Spring properties since Hibernate instantiates
 * this class directly.
 */
public class PsiTableNamingStrategy extends PhysicalNamingStrategyStandardImpl implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
    
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if ("TAB_POSTSEC".equals(name.getText()) || "ISD_PSI_REGISTRY".equals(name.getText())) {
            String tableName = getTableName();
            return Identifier.toIdentifier(tableName);
        }
        return super.toPhysicalTableName(name, context);
    }
    
    private String getTableName() {
        if (applicationContext != null) {
            Environment env = applicationContext.getEnvironment();
            return env.getProperty("trax.psi.table-name", "TAB_POSTSEC");
        }
        // Fallback if context not available yet
        return System.getProperty("trax.psi.table-name", 
            System.getenv().get("PSI_TABLE_NAME"));
    }
}

