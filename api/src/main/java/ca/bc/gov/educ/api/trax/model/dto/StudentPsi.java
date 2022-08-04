package ca.bc.gov.educ.api.trax.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class StudentPsi {

	private String pen;
	private String psiCode;
    private Character psiStatus;
    private String psiYear;

	@Override
	public String toString() {
		return "StudentPsi{" +
				"pen='" + pen + '\'' +
				", psiCode='" + psiCode + '\'' +
				", psiStatus='" + psiStatus + '\'' +
				", psiYear='" + psiYear + '\'' +
				'}';
	}
}
