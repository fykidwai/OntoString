package uk.ac.ebi.spot.ontotools.curation.rest.dto.mapping;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class MappingDtoTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(MappingDto.class)
                .verify();
    }

}