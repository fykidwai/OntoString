package uk.ac.ebi.spot.ontotools.curation.rest.dto.oxo;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class OXORequestDtoTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(OXORequestDto.class)
                .verify();
    }

}