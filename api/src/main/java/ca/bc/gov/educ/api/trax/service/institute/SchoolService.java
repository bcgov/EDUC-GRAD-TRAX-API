package ca.bc.gov.educ.api.trax.service.institute;

import ca.bc.gov.educ.api.trax.model.dto.institute.School;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import ca.bc.gov.educ.api.trax.model.transformer.institute.SchoolTransformer;
import ca.bc.gov.educ.api.trax.repository.redis.SchoolRedisRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Service("InstituteSchoolService")
public class SchoolService {

	@Autowired
	private EducGradTraxApiConstants constants;

	@Autowired
	private WebClient webClient;

	@Autowired
	SchoolRedisRepository schoolRedisRepository;

	@Autowired
	SchoolTransformer schoolTransformer;

	@Autowired
	private RestUtils restUtils;

	public List<School> getSchoolsFromInstituteApi() {
		try {
			log.debug("****Before Calling Institute API");
			List<SchoolEntity> schools;
			schools = webClient.get()
					.uri(constants.getAllSchoolsFromInstituteApiUrl())
					.headers(h -> {
						h.setBearerAuth(restUtils.getTokenResponseObject(
								constants.getInstituteClientId(),
								constants.getInstituteClientSecret()
						).getAccess_token());
					})
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<SchoolEntity>>(){}).block();
			//assert schools != null;
			//log.debug("# of Schools: " + schools.size());
			return  schoolTransformer.transformToDTO(schools);
		} catch (WebClientResponseException e) {
			log.warn(String.format("Error getting Common School List: %s", e.getMessage()));
		} catch (Exception e) {
			log.error(String.format("Error while calling school-api: %s", e.getMessage()));
		}
		return null;
	}

	public void loadSchoolsIntoRedisCache(List<ca.bc.gov.educ.api.trax.model.dto.institute.School> schools) {
		schoolRedisRepository
				.saveAll(schoolTransformer.transformToEntity(schools));
	}

    /*public SchoolDetail getCommonSchoolDetailById(String schoolId, String accessToken) {
        try {
            return webClient.get().uri(
                            String.format(constants.getSchoolDetailsByIdFromInstituteApiUrl(), schoolId))
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                    })
                    .retrieve().bodyToMono(SchoolDetail.class).block();
        } catch (WebClientResponseException e) {
            logger.warn("Error getting Common School Details");
        } catch (Exception e) {
            logger.error(String.format("Error while calling school-api: %s", e.getMessage()));
        }
        return null;
    }*/

    /*public List<SchoolDetail> getAllSchoolDetails() {

        String accessToken = getAccessToken();
        List<School> schools = getCommonSchools(accessToken);
        List<SchoolDetail> schoolDetails = new ArrayList<SchoolDetail>();
        Address address = new Address();
        int counter = 1;

        for (School s : schools) {
            SchoolDetail sd = new SchoolDetail();

            if (counter%100 == 0)
                accessToken = getAccessToken();
            sd = getCommonSchoolDetailById(s.getSchoolId(), accessToken);

            address = null;
            if (sd.getAddresses() == null || sd.getAddresses().isEmpty()) {
                logger.debug("," + sd.getMincode() + "," + "," + "," + "," + "," + "," + "," + ",");
            } else {
                address = sd.getAddresses().get(0);
                logger.debug("," + sd.getMincode() + ","
                        + sd.getAddresses().get(0).getAddressLine1() + ","
                        + sd.getAddresses().get(0).getAddressLine2() + ","
                        + sd.getAddresses().get(0).getCity() + ","
                        + sd.getAddresses().get(0).getProvinceCode() + ","
                        + sd.getAddresses().get(0).getCountryCode() + ","
                        + sd.getAddresses().get(0).getPostal() + ","
                        + sd.getAddresses().get(0).getAddressTypeCode() + ","
                );
            }
            schoolDetails.add(sd);
            counter++;
        }
        return schoolDetails;
    }*/

    /*public String getAccessToken() {
        String token = "";
        try {
            OAuthClient client = new OAuthClient(new URLConnectionClient());
            OAuthClientRequest request = OAuthClientRequest.tokenLocation(constants.getTokenUrl())
                    .setGrantType(GrantType.CLIENT_CREDENTIALS)
                    .setClientId(constants.getInstituteClientId())
                    .setClientSecret(constants.getInstituteClientSecret())
                    .buildBodyMessage();

            token = client.accessToken(request, OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class).getAccessToken();
        } catch (Exception exn) {
            exn.printStackTrace();
        }
        return token;
    }*/
}
