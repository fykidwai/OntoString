package uk.ac.ebi.spot.ontotools.curation.rest.dto.ols;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OLSResponseDto implements Serializable {

    private static final long serialVersionUID = 1823555008334670305L;

    @JsonProperty("_embedded")
    private final OLSEmbeddedDto embedded;

    @JsonProperty("page")
    private final OLSPageDto page;

    @JsonCreator
    public OLSResponseDto(@JsonProperty("_embedded") OLSEmbeddedDto embedded,
                          @JsonProperty("page") OLSPageDto page) {
        this.embedded = embedded;
        this.page = page;
    }

    public OLSEmbeddedDto getEmbedded() {
        return embedded;
    }

    public OLSPageDto getPage() {
        return page;
    }
}
