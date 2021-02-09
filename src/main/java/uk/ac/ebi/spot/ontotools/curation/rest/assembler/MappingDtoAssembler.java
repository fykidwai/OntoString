package uk.ac.ebi.spot.ontotools.curation.rest.assembler;

import uk.ac.ebi.spot.ontotools.curation.domain.Mapping;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.MappingDto;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MappingDtoAssembler {

    public static MappingDto assemble(Mapping mapping) {
        return new MappingDto(mapping.getId(),
                mapping.getEntityId(),
                OntologyTermDtoAssembler.assemble(mapping.getOntologyTerm()),
                mapping.isReviewed(),
                mapping.getStatus(),
                mapping.getReviews() != null ? mapping.getReviews().stream().map(ReviewDtoAssembler::assemble).collect(Collectors.toList()) : new ArrayList<>(),
                ProvenanceDtoAssembler.assemble(mapping.getCreated()));
    }

}
