package ca.bc.gov.educ.api.trax.util;

import ca.bc.gov.educ.api.trax.model.dto.ResponseObj;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class RestUtils {
    private final EducGradTraxApiConstants constants;

    private final WebClient webClient;

    @Autowired
    public RestUtils(final EducGradTraxApiConstants constants, final WebClient webClient) {
        this.constants = constants;
        this.webClient = webClient;
    }

    @Retry(name = "rt-getToken", fallbackMethod = "rtGetTokenFallback")
    public ResponseObj getTokenResponseObject() {
        log.info("Fetching the access token from KeyCloak API");
        HttpHeaders httpHeadersKC = EducGradTraxApiUtils.getHeaders(
                constants.getUserName(), constants.getPassword());
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return this.webClient.post().uri(constants.getTokenUrl())
                .headers(h -> h.addAll(httpHeadersKC))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(map))
                .retrieve()
                .bodyToMono(ResponseObj.class).block();
    }

    @Retry(name = "rt-getToken-institute", fallbackMethod = "rtGetTokenFallback")
    public ResponseObj getTokenResponseObject(String clientId,  String clientSecret) {
        log.info("Fetching the Institute api access token from KeyCloak.");
        HttpHeaders httpHeadersKC = EducGradTraxApiUtils.getHeaders(
                clientId, clientSecret);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return this.webClient.post().uri(constants.getTokenUrl())
                .headers(h -> h.addAll(httpHeadersKC))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(map))
                .retrieve()
                .bodyToMono(ResponseObj.class).block();
    }

    public ResponseObj rtGetTokenFallBack(HttpServerErrorException exception){
        log.error("{} NOT REACHABLE after many attempts: {}", constants.getTokenUrl(), exception.getMessage());
        return null;
    }
}
