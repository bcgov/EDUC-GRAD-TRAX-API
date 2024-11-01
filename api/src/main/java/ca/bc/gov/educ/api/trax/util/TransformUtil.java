package ca.bc.gov.educ.api.trax.util;

import ca.bc.gov.educ.api.trax.exception.TraxAPIRuntimeException;

import java.beans.Expression;
import java.beans.Statement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.util.StringUtils.capitalize;

/**
 * The type Transform util.
 */
public class TransformUtil {

    private TransformUtil() {
    }

    /**
     * Is uppercase field boolean.
     *
     * @param clazz     the clazz
     * @param fieldName the field name
     * @return the boolean
     */
    public static boolean isUppercaseField(Class<?> clazz, String fieldName) {
        var superClazz = clazz;
        while (!superClazz.equals(Object.class)) {
            try {
                Field field = superClazz.getDeclaredField(fieldName);
                return field.getAnnotation(UpperCase.class) != null;
            } catch (NoSuchFieldException e) {
                superClazz = superClazz.getSuperclass();
            }
        }
        return false;
    }

    private static <T> void transformFieldToUppercase(Field field, T claz) {
        if (!field.getType().equals(String.class)) {
            return;
        }

        if (field.getAnnotation(UpperCase.class) != null) {
            try {
                var fieldName = capitalize(field.getName());
                var expr = new Expression(claz, "get" + fieldName, new Object[0]);
                var entityFieldValue = (String) expr.getValue();
                if (entityFieldValue != null) {
                    var stmt = new Statement(claz, "set" + fieldName, new Object[]{entityFieldValue.toUpperCase()});
                    stmt.execute();
                }
            } catch (Exception ex) {
                throw new TraxAPIRuntimeException(ex.getMessage());
            }
        }

    }
}
