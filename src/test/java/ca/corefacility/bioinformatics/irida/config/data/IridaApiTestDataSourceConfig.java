package ca.corefacility.bioinformatics.irida.config.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import ca.corefacility.bioinformatics.irida.util.RecursiveDeleteVisitor;

@Configuration
@Profile({ "test", "it" })
public class IridaApiTestDataSourceConfig implements DataConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(IridaApiTestDataSourceConfig.class);

	private Set<Path> baseDirectory = new HashSet<>();

	private final Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr-xr-x");

	// Franklin: I assume that the scope of a configuration bean is the lifetime
	// of the application, so the directory should only get deleted *after* the
	// tests have finished running.
	@PreDestroy
	public void tearDown() throws IOException {
		for (Path b : baseDirectory) {
			Files.walkFileTree(b, new RecursiveDeleteVisitor());
		}
	}

	/**
	 	Path to root of temporary directory where tests will copy files for use in Galaxy
	 */
	@Bean(name = "rootTempDirectory")
	public Path rootTempDirectory() {
		
		String rootTempDirectory = "/tmp/irida";
		
		/*
		 * Set irida.it.rootdirectory property to change root test file
		 * directory
		 */
		String configuredRoot = System.getProperty("irida.it.rootdirectory");
		if(configuredRoot != null){
			rootTempDirectory = configuredRoot;
		}
		
		return Paths.get(rootTempDirectory);
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
		adapter.setShowSql(false);
		adapter.setGenerateDdl(true);
		adapter.setDatabase(Database.H2);
		return adapter;
	}

	@Bean
	public DataSource dataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
	}

	@Override
	@Bean
	public Properties getJpaProperties() {
		return new Properties();
	}

	@Bean(name = "sequenceFileBaseDirectory")
	public Path baseDirectory() throws IOException {
		Path b = Files.createTempDirectory(rootTempDirectory(), "irida-sequence-file-dir",
				PosixFilePermissions.asFileAttribute(permissions));
		logger.info("Created directory for sequence files at [" + b.toString() + "] for integration test");
		baseDirectory.add(b);
		return b;
	}

	@Bean(name = "referenceFileBaseDirectory")
	public Path referenceFileBaseDirectory() throws IOException {
		Path b = Files.createTempDirectory(rootTempDirectory(), "irida-reference-file-dir",
				PosixFilePermissions.asFileAttribute(permissions));
		logger.info("Created directory for reference files at [" + b.toString() + "] for integration test");
		baseDirectory.add(b);
		return b;
	}
	
	@Bean(name = "outputFileBaseDirectory")
	public Path outputFileBaseDirectory() throws IOException {
		Path b = Files.createTempDirectory(rootTempDirectory(), "irida-output-file-dir",
				PosixFilePermissions.asFileAttribute(permissions));
		logger.info("Created directory for output files at [" + b.toString() + "] for integration test");
		baseDirectory.add(b);
		return b;
	}
}
