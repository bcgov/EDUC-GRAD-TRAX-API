package ca.bc.gov.educ.api.trax.config;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class GradTraxConfig {

	@Bean
	public ModelMapper modelMapper() {

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.typeMap(SchoolEntity.class, School.class);
		modelMapper.typeMap(School.class, SchoolEntity.class);

		modelMapper.typeMap(DistrictEntity.class, District.class);
		modelMapper.typeMap(District.class, DistrictEntity.class);

		modelMapper.typeMap(PsiEntity.class, Psi.class);
		modelMapper.typeMap(Psi.class, PsiEntity.class);
		return modelMapper;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public WebClient webClient() {
		HttpClient client = HttpClient.create();
		client.warmup().block();
		return WebClient.builder().build();
	}

	/**
	 * Lock provider lock provider.
	 *
	 * @param jdbcTemplate       the jdbc template
	 * @param transactionManager the transaction manager
	 * @return the lock provider
	 */
	@Bean
	public LockProvider lockProvider(@Autowired JdbcTemplate jdbcTemplate, @Autowired PlatformTransactionManager transactionManager) {
		return new JdbcTemplateLockProvider(jdbcTemplate, transactionManager, "REPLICATION_SHEDLOCK");
	}


}
