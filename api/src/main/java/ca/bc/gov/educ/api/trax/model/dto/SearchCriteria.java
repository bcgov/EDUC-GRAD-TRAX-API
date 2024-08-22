package ca.bc.gov.educ.api.trax.model.dto;

import ca.bc.gov.educ.api.trax.filter.FilterOperation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchCriteria {
    /**
     * The Key.
     */
    @NotNull
    String key;
    /**
     * The Operation.
     */
    @NotNull
    FilterOperation operation;
    /**
     * The Value.
     */
    String value;
    /**
     * The Value type.
     */
    @NotNull
    ValueType valueType;
    /**
     * The Condition. ENUM to hold and AND OR
     */
    Condition condition;

}
