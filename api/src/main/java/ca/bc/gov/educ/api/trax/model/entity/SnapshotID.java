package ca.bc.gov.educ.api.trax.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class SnapshotID implements Serializable {
    private Integer gradYear;
    private String pen;

    public SnapshotID(int gradYear, String pen) {
        this.gradYear = gradYear;
        this.pen = pen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnapshotID that = (SnapshotID) o;
        return Objects.equals(gradYear, that.gradYear) && Objects.equals(pen, that.pen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradYear, pen);
    }
}
