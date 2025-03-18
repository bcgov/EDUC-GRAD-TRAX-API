package ca.bc.gov.educ.api.trax.config;

import ca.bc.gov.educ.api.trax.model.dto.District;
import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.dto.School;
import ca.bc.gov.educ.api.trax.model.entity.DistrictEntity;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.LogHelper;
import ca.bc.gov.educ.api.trax.util.ThreadLocalStateUtil;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@EnableAsync
@Configuration
@Profile("!test")
public class GradTraxConfig {

	LogHelper logHelper;
	EducGradTraxApiConstants constants;

	@Autowired
	public GradTraxConfig(LogHelper logHelper, EducGradTraxApiConstants constants) {
		this.logHelper = logHelper;
		this.constants = constants;
	}

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

	@Bean("traxClient")
	public WebClient getTraxClientWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		filter.setDefaultClientRegistrationId("traxclient");
		return WebClient.builder()
				.filter(setRequestHeaders())
				.exchangeStrategies(ExchangeStrategies
						.builder()
						.codecs(codecs -> codecs
								.defaultCodecs()
								.maxInMemorySize(50 * 1024 * 1024))
						.build())
				.apply(filter.oauth2Configuration())
				.filter(this.log())
				.build();
	}


	@Bean("instituteWebClient")
	public WebClient getInstituteWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		filter.setDefaultClientRegistrationId("institute-web-client");
		return WebClient.builder()
				.exchangeStrategies(ExchangeStrategies
						.builder()
						.codecs(codecs -> codecs
								.defaultCodecs()
								.maxInMemorySize(50 * 1024 * 1024))
						.build())
				.apply(filter.oauth2Configuration())
				.filter(this.log())
				.build();
	}

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientService clientService) {

		OAuth2AuthorizedClientProvider authorizedClientProvider =
				OAuth2AuthorizedClientProviderBuilder.builder()
						.clientCredentials()
						.build();
		AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
				new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

		return authorizedClientManager;
	}



	@Bean
	public WebClient webClient() {
		//extend buffer to 50MB
		Integer CODEC_50_MB_SIZE = 50 * 1024 * 1024;
		HttpClient client = HttpClient.create();
		client.warmup().block();
		return WebClient.builder()
				.filter(setRequestHeaders())
				.codecs(clientCodecConfigurer -> {
			var codec = new Jackson2JsonDecoder();
			codec.setMaxInMemorySize(CODEC_50_MB_SIZE);
			clientCodecConfigurer.customCodecs().register(codec);
			clientCodecConfigurer.customCodecs().register(new Jackson2JsonEncoder());})
				.filter(this.log())
				.build();
	}
	private ExchangeFilterFunction setRequestHeaders() {
		return (clientRequest, next) -> {
			ClientRequest modifiedRequest = ClientRequest.from(clientRequest)
					.header(EducGradTraxApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID())
					.header(EducGradTraxApiConstants.USER_NAME, ThreadLocalStateUtil.getCurrentUser())
					.header(EducGradTraxApiConstants.REQUEST_SOURCE, EducGradTraxApiConstants.API_NAME)
					.build();
			return next.exchange(modifiedRequest);
		};
	}

	private ExchangeFilterFunction log() {
		return (clientRequest, next) -> next
				.exchange(clientRequest)
				.doOnNext((clientResponse -> logHelper.logClientHttpReqResponseDetails(
						clientRequest.method(),
						clientRequest.url().toString(),
						clientResponse.statusCode().value(),
						clientRequest.headers().get(EducGradTraxApiConstants.CORRELATION_ID),
						clientRequest.headers().get(EducGradTraxApiConstants.REQUEST_SOURCE),
						constants.isSplunkLogHelperEnabled())
				));
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

	/**
	 * Thread pool task scheduler thread pool task scheduler.
	 *
	 * @return the thread pool task scheduler
	 */
	@Bean(name = "taskExecutor")
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		return threadPoolTaskScheduler;
	}

}
