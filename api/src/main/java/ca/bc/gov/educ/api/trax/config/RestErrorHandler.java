package ca.bc.gov.educ.api.trax.config;

import ca.bc.gov.educ.api.trax.exception.ApiError;
import ca.bc.gov.educ.api.trax.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.trax.exception.GradBusinessRuleException;
import ca.bc.gov.educ.api.trax.util.ApiResponseMessage.MessageTypeEnum;
import ca.bc.gov.educ.api.trax.util.ApiResponseModel;
import ca.bc.gov.educ.api.trax.util.GradValidation;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@ControllerAdvice
public class RestErrorHandler extends ResponseEntityExceptionHandler {

	GradValidation validation;

	@Autowired
	public RestErrorHandler(GradValidation validation) {
		this.validation = validation;
	}

	/**
	 * Override method handles exception thrown by @Valid validation
	 * on controller methods (for example)
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		ApiError apiError = new ApiError(BAD_REQUEST);
		apiError.setMessage(errors.toString());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(value = { IllegalArgumentException.class, IllegalStateException.class })
	protected ResponseEntity<Object> handleConflict(RuntimeException ex) {
        log.error("Illegal argument ERROR IS: {}", ex.getClass().getName(), ex);
		ApiResponseModel<?> response = ApiResponseModel.ERROR(null, ex.getLocalizedMessage());
		validation.ifErrors(response::addErrorMessages);
		validation.ifWarnings(response::addWarningMessages);
		validation.clear();
		return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(value = { JpaObjectRetrievalFailureException.class, DataRetrievalFailureException.class, EntityNotFoundException.class })
	protected ResponseEntity<Object> handleEntityNotFound(RuntimeException ex) {
        // no need to log EntityNotFoundExceptions as they are expected
		if(!(ex instanceof EntityNotFoundException)){
			log.error("JPA ERROR IS: {}", ex.getClass().getName(), ex);
		}
		ApiError apiError = new ApiError(NOT_FOUND);
		apiError.setMessage(ex.getMessage());
		return buildResponseEntity(apiError);
	}

	@ExceptionHandler(value = { AccessDeniedException.class })
	protected ResponseEntity<Object> handleAuthorizationErrors(AccessDeniedException ex) {
        log.error("Authorization error EXCEPTION IS: {}", ex.getClass().getName());
		String message = "You are not authorized to access this resource.";
		validation.clear();
		return new ResponseEntity<>(ApiResponseModel.ERROR(null, message), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(value = { GradBusinessRuleException.class })
	protected ResponseEntity<Object> handleGradBusinessException(GradBusinessRuleException ex) {
		ApiResponseModel<?> response = ApiResponseModel.ERROR(null);
		validation.ifErrors(response::addErrorMessages);
		validation.ifWarnings(response::addWarningMessages);
		if (response.getMessages().isEmpty()) {
			response.addMessageItem(ex.getLocalizedMessage(), MessageTypeEnum.ERROR);
		}
		validation.clear();
		return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(value = { OptimisticEntityLockException.class })
	protected ResponseEntity<Object> handleOptimisticEntityLockException(OptimisticEntityLockException ex) {
		return getGenericUncaughtExceptionResponse(ex);
	}
	
	@ExceptionHandler(value = { DataIntegrityViolationException.class })
	protected ResponseEntity<Object> handleSQLException(DataIntegrityViolationException ex) {

        log.error("DATA INTEGRITY VIOLATION IS: {}", ex.getClass().getName(), ex);
		String msg = ex.getLocalizedMessage();
		Throwable cause = ex.getCause();
		if (cause instanceof ConstraintViolationException) {
			ConstraintViolationException contraintViolation = (ConstraintViolationException) cause;
			if ("23000".equals(contraintViolation.getSQLState())) {
				// primary key violation - probably inserting a duplicate record
				msg = (ex.getRootCause() != null) ? ex.getRootCause().getMessage() : "";
			}
		}
		ApiResponseModel<?> response = ApiResponseModel.ERROR(null, msg);
		validation.ifErrors(response::addErrorMessages);
		validation.ifWarnings(response::addWarningMessages);
		validation.clear();
		return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(value = { Exception.class })
	protected ResponseEntity<Object> handleUncaughtException(Exception ex) {
		return getGenericUncaughtExceptionResponse(ex);
	}

	private ResponseEntity<Object> getGenericUncaughtExceptionResponse(Exception ex) {
        log.error("EXCEPTION IS: {}", ex.getClass().getName(), ex);
		ApiResponseModel<?> response = ApiResponseModel.ERROR(null);
		validation.ifErrors(response::addErrorMessages);
		validation.ifWarnings(response::addWarningMessages);
		if (!validation.hasErrors()) {
			response.addMessageItem(ex.getLocalizedMessage(), MessageTypeEnum.ERROR);
		}
		validation.clear();
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Build response entity response entity.
	 *
	 * @param apiError the api error
	 * @return the response entity
	 */
	private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}
}
