package ca.bc.gov.educ.api.trax.model.transformer;

import ca.bc.gov.educ.api.trax.model.dto.Psi;
import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PsiTransformer {

    @Autowired
    ModelMapper modelMapper;

    public Psi transformToDTO (PsiEntity psiEntity) {
        Psi psi = modelMapper.map(psiEntity, Psi.class);
        return psi;
    }

    public Psi transformToDTO ( Optional<PsiEntity> schoolEntity ) {
        PsiEntity cae = new PsiEntity();

        if (schoolEntity.isPresent())
            cae = schoolEntity.get();

        Psi school = modelMapper.map(cae, Psi.class);
        return school;
    }

	public List<Psi> transformToDTO (Iterable<PsiEntity> schoolEntities ) {

        List<Psi> schoolList = new ArrayList<Psi>();

        for (PsiEntity schoolEntity : schoolEntities) {
            Psi school = new Psi();
            school = modelMapper.map(schoolEntity, Psi.class);            
            schoolList.add(school);
        }

        return schoolList;
    }

    public PsiEntity transformToEntity(Psi school) {
        PsiEntity schoolEntity = modelMapper.map(school, PsiEntity.class);
        return schoolEntity;
    }
}
