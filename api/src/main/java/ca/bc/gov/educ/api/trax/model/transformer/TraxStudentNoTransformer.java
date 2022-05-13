package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.TraxStudentNo;
import ca.bc.gov.educ.api.trax.model.entity.TraxStudentNoEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TraxStudentNoTransformer {

    @Autowired
    ModelMapper modelMapper;

    public TraxStudentNo transformToDTO(TraxStudentNoEntity traxStudentNoEntity) {
        return modelMapper.map(traxStudentNoEntity, TraxStudentNo.class);
    }

    public TraxStudentNo transformToDTO(Optional<TraxStudentNoEntity> traxStudentNoEntityOptional) {
        TraxStudentNoEntity entity;
        if (traxStudentNoEntityOptional.isPresent()) {
            entity = traxStudentNoEntityOptional.get();
        } else {
            entity = new TraxStudentNoEntity();
        }

        return modelMapper.map(entity, TraxStudentNo.class);
    }

    public List<TraxStudentNo> transformToDTO(Iterable<TraxStudentNoEntity> traxStudentNoEntities) {
        List<TraxStudentNo> traxStudentNoList = new ArrayList<>();

        for (TraxStudentNoEntity traxStudentNoEntity : traxStudentNoEntities) {
            TraxStudentNo traxStudentNo = modelMapper.map(traxStudentNoEntity, TraxStudentNo.class);
            traxStudentNoList.add(traxStudentNo);
        }

        return traxStudentNoList;
    }

    public TraxStudentNoEntity transformToEntity(TraxStudentNo traxStudentNo) {
        return modelMapper.map(traxStudentNo, TraxStudentNoEntity.class);
    }
}
