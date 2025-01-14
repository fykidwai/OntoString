package uk.ac.ebi.spot.ontotools.curation;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.ac.ebi.spot.ontotools.curation.constants.CurationConstants;
import uk.ac.ebi.spot.ontotools.curation.constants.IDPConstants;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectContextDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectContextGraphRestrictionDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectDto;
import uk.ac.ebi.spot.ontotools.curation.system.GeneralCommon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectContextsControllerTest extends IntegrationTest {

    private ProjectDto projectDto;

    public void setup() throws Exception {
        super.setup();
        projectDto = super.createProject("New Project", user1,
                Arrays.asList(new String[]{"cttv", "sysmicro", "atlas", "ebisc", "uniprot", "gwas", "cbi", "clinvar-xrefs"}),
                Arrays.asList(new String[]{"efo", "mondo", "hp", "ordo", "orphanet"}),
                "efo", 0, null);
    }


    /**
     * POST /v1/projects/{projectId}/contexts
     */
    @Test
    public void shouldCreateContext() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS;
        ProjectContextDto newProjectContextDto = new ProjectContextDto("species_mouse", "",
                Arrays.asList(new String[]{"sysmicro", "atlas", "ebisc", "uniprot", "gwas", "cbi", "clinvar-xrefs"}),
                Arrays.asList(new String[]{"efo", "mondo", "mp"}),
                Arrays.asList(new String[]{"efo"}),
                new ProjectContextGraphRestrictionDto(
                        Arrays.asList(new String[]{"EFO:0005802"}),
                        null,
                        Arrays.asList(new String[]{"rdfs:subClassOf"}),
                        false,
                        false
                ));

        String response = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token1")
                .content(super.mapper.writeValueAsString(newProjectContextDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProjectDto projectResponse = mapper.readValue(response, new TypeReference<>() {
        });

        assertEquals(2, projectResponse.getContexts().size());
        Map<String, String> contexts = new HashMap<>();
        for (ProjectContextDto projectContextDto : projectResponse.getContexts()) {
            contexts.put(projectContextDto.getName(), "");
        }
        assertTrue(contexts.containsKey(CurationConstants.CONTEXT_DEFAULT));
        assertTrue(contexts.containsKey(newProjectContextDto.getName()));
        for (ProjectContextDto projectContextDto : projectResponse.getContexts()) {
            if (projectContextDto.getName().equalsIgnoreCase(newProjectContextDto.getName())) {
                assertEquals(newProjectContextDto.getDatasources().size(), projectContextDto.getDatasources().size());
                assertEquals(newProjectContextDto.getOntologies().size(), projectContextDto.getOntologies().size());
                assertEquals(newProjectContextDto.getPreferredMappingOntologies().size(), projectContextDto.getPreferredMappingOntologies().size());
                ProjectContextGraphRestrictionDto pcgr = newProjectContextDto.getGraphRestriction();
                ProjectContextGraphRestrictionDto actual = projectContextDto.getGraphRestriction();
                assertEquals(pcgr.getClasses(), actual.getClasses());
                assertFalse(actual.getIris().isEmpty());
            }
        }
    }

    /**
     * PUT /v1/projects/{projectId}/contexts
     */
    @Test
    public void shouldUpdateContext() throws Exception {
        ProjectContextDto projectContextDto = projectDto.getContexts().get(0);
        ProjectContextDto updated = new ProjectContextDto(projectContextDto.getName(),
                projectContextDto.getDescription(),
                Arrays.asList(new String[]{"sysmicro", "atlas", "ebisc", "uniprot", "gwas", "cbi", "clinvar-xrefs"}),
                Arrays.asList(new String[]{"efo", "mondo", "mp"}),
                Arrays.asList(new String[]{"efo"}), null);

        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS;
        String response = mockMvc.perform(put(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token1")
                .content(super.mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProjectDto projectResponse = mapper.readValue(response, new TypeReference<>() {
        });

        assertEquals(1, projectResponse.getContexts().size());
        assertEquals(CurationConstants.CONTEXT_DEFAULT, projectResponse.getContexts().get(0).getName());
        assertEquals(updated.getDatasources().size(), projectResponse.getContexts().get(0).getDatasources().size());
        assertEquals(updated.getOntologies().size(), projectResponse.getContexts().get(0).getOntologies().size());
        assertEquals(updated.getPreferredMappingOntologies().size(), projectResponse.getContexts().get(0).getPreferredMappingOntologies().size());
    }

    /**
     * DELETE /v1/projects/{projectId}/contexts/{contextName}
     */
    @Test
    public void shouldDeleteContext() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS;
        ProjectContextDto newProjectContextDto = new ProjectContextDto("species_mouse", "",
                Arrays.asList(new String[]{"sysmicro", "atlas", "ebisc", "uniprot", "gwas", "cbi", "clinvar-xrefs"}),
                Arrays.asList(new String[]{"efo", "mondo", "mp"}),
                Arrays.asList(new String[]{"efo"}), null);

        String response = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token1")
                .content(super.mapper.writeValueAsString(newProjectContextDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProjectDto projectResponse = mapper.readValue(response, new TypeReference<>() {
        });

        assertEquals(2, projectResponse.getContexts().size());

        endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS + "/" + newProjectContextDto.getName();
        mockMvc.perform(delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token1"))
                .andExpect(status().isOk());

        endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId();
        response = mockMvc.perform(get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProjectDto actual = mapper.readValue(response, new TypeReference<>() {
        });
        assertEquals(1, actual.getContexts().size());
        assertEquals(CurationConstants.CONTEXT_DEFAULT, actual.getContexts().get(0).getName());
    }

    /**
     * DELETE /v1/projects/{projectId}/contexts/{contextName}
     */
    @Test
    public void shouldNotDeleteDefaultContext() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS + "/" + CurationConstants.CONTEXT_DEFAULT;
        mockMvc.perform(delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * POST /v1/projects/{projectId}/contexts
     */
    @Test
    public void shouldNotCreateContext() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS;
        ProjectContextDto newProjectContextDto = new ProjectContextDto("species_mouse", "",
                Arrays.asList(new String[]{"sysmicro", "atlas", "ebisc", "uniprot", "gwas", "cbi", "clinvar-xrefs"}),
                Arrays.asList(new String[]{"efo", "mondo", "mp"}),
                Arrays.asList(new String[]{"efo"}), null);

        mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token2")
                .content(super.mapper.writeValueAsString(newProjectContextDto)))
                .andExpect(status().isNotFound());
    }

    /**
     * PUT /v1/projects/{projectId}/contexts
     */
    @Test
    public void shouldNotUpdateContext() throws Exception {
        ProjectContextDto projectContextDto = projectDto.getContexts().get(0);
        ProjectContextDto updated = new ProjectContextDto(projectContextDto.getName(),
                projectContextDto.getDescription(),
                Arrays.asList(new String[]{"sysmicro", "atlas", "ebisc", "uniprot", "gwas", "cbi", "clinvar-xrefs"}),
                Arrays.asList(new String[]{"efo", "mondo", "mp"}),
                Arrays.asList(new String[]{"efo"}), null);

        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS;
        mockMvc.perform(put(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token2")
                .content(super.mapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());
    }

    /**
     * DELETE /v1/projects/{projectId}/contexts/{contextName}
     */
    @Test
    public void shouldNotDeleteContext() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS;
        ProjectContextDto newProjectContextDto = new ProjectContextDto("species_mouse", "",
                Arrays.asList(new String[]{"sysmicro", "atlas", "ebisc", "uniprot", "gwas", "cbi", "clinvar-xrefs"}),
                Arrays.asList(new String[]{"efo", "mondo", "mp"}),
                Arrays.asList(new String[]{"efo"}), null);

        String response = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token1")
                .content(super.mapper.writeValueAsString(newProjectContextDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProjectDto projectResponse = mapper.readValue(response, new TypeReference<>() {
        });

        assertEquals(2, projectResponse.getContexts().size());

        endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() + CurationConstants.API_CONTEXTS + "/" + newProjectContextDto.getName();
        mockMvc.perform(delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDPConstants.JWT_TOKEN, "token2"))
                .andExpect(status().isNotFound());
    }
}
