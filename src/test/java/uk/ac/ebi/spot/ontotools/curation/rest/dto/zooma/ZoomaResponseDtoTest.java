package uk.ac.ebi.spot.ontotools.curation.rest.dto.zooma;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ZoomaResponseDtoTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(ZoomaResponseDto.class)
                .verify();
    }

}