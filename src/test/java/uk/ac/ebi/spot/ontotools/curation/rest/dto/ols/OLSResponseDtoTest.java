package uk.ac.ebi.spot.ontotools.curation.rest.dto.ols;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class OLSResponseDtoTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(OLSResponseDto.class)
                .verify();
    }

}