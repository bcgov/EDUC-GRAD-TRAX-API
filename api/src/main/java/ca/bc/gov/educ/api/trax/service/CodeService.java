package ca.bc.gov.educ.api.trax.service;

import ca.bc.gov.educ.api.trax.model.dto.GradCountry;
import ca.bc.gov.educ.api.trax.model.dto.GradProvince;
import ca.bc.gov.educ.api.trax.model.entity.GradCountryEntity;
import ca.bc.gov.educ.api.trax.model.entity.GradProvinceEntity;
import ca.bc.gov.educ.api.trax.model.transformer.GradCountryTransformer;
import ca.bc.gov.educ.api.trax.model.transformer.GradProvinceTransformer;
import ca.bc.gov.educ.api.trax.repository.GradCountryRepository;
import ca.bc.gov.educ.api.trax.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.trax.util.EducGradTraxApiConstants;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Optional;
import ca.bc.gov.educ.api.trax.util.RestUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CodeService {

	@Autowired
	private GradCountryTransformer gradCountryTransformer;

	@Autowired
	private GradProvinceTransformer gradProvinceTransformer;

	@Autowired
	private GradCountryRepository gradCountryRepository;

	@Autowired
	private GradProvinceRepository gradProvinceRepository;

	@Autowired
	GradValidation validation;

	@Autowired
	private EducGradTraxApiConstants constants;

	@Autowired
	private WebClient webClient;

	@Autowired
	private RestUtils restUtils;

	private static Logger logger = LoggerFactory.getLogger(CodeService.class);
	private static final String EXCEPTION_MSG = "Exception: %s";
	private static final String CREATED_BY="createdBy";
	private static final String CREATED_TIMESTAMP="createdTimestamp";

	@Transactional
	public List<GradCountry> getAllCountryCodeList() {
		return gradCountryTransformer.transformToDTO(gradCountryRepository.findAll());
	}

	@Transactional
	public GradCountry getSpecificCountryCode(String countryCode) {
		Optional<GradCountryEntity> entity = gradCountryRepository.findById(StringUtils.toRootUpperCase(countryCode));
		if (entity.isPresent()) {
			return gradCountryTransformer.transformToDTO(entity);
		} else {
			return null;
		}
	}

	@Transactional
	public List<GradProvince> getAllProvinceCodeList() {
		return gradProvinceTransformer.transformToDTO(gradProvinceRepository.findAll());
	}

	@Transactional
	public GradProvince getSpecificProvinceCode(String provCode) {
		Optional<GradProvinceEntity> entity = gradProvinceRepository.findById(StringUtils.toRootUpperCase(provCode));
		if (entity.isPresent()) {
			return gradProvinceTransformer.transformToDTO(entity);
		} else {
			return null;
		}
	}
}
