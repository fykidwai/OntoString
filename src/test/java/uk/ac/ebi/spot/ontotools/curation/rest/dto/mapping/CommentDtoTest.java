package uk.ac.ebi.spot.ontotools.curation.rest.dto.mapping;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class CommentDtoTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(CommentDto.class)
                .verify();
    }

}