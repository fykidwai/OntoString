package uk.ac.ebi.spot.ontotools.curation.rest.dto.mapping;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class ExportOntologyTermsDtoTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(ExportOntologyTermsDto.class)
                .verify();
    }

}