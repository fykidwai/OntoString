package uk.ac.ebi.spot.ontotools.curation;

import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.ac.ebi.spot.ontotools.curation.constants.CurationConstants;
import uk.ac.ebi.spot.ontotools.curation.constants.IDPConstants;
import uk.ac.ebi.spot.ontotools.curation.constants.ProjectRole;
import uk.ac.ebi.spot.ontotools.curation.domain.mapping.Entity;
import uk.ac.ebi.spot.ontotools.curation.repository.EntityRepository;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.SourceDto;
import uk.ac.ebi.spot.ontotools.curation.service.MatchmakerService;
import uk.ac.ebi.spot.ontotools.curation.system.GeneralCommon;

import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {IntegrationTest.MockTaskExecutorConfig.class,
        IntegrationTest.MockMatchmakerServiceConfig.class})
public class DataImportTest extends IntegrationTest {

    private ProjectDto projectDto;

    private SourceDto sourceDto;

    @Autowired
    private EntityRepository entityRepository;

    @Autowired
    private MatchmakerService matchmakerService;

    @Override
    public void setup() throws Exception {
        super.setup();
        projectDto = super.createProject("New Project", user1, null, null, null, 0, null);
        sourceDto = super.createSource(projectDto.getId());
        doNothing().when(matchmakerService).runMatchmaking(eq(sourceDto.getId()), any());
    }

    @Test
    public void shouldImportJSON() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() +
                CurationConstants.API_SOURCES + "/" + sourceDto.getId() + CurationConstants.API_UPLOAD;

        InputStream fileAsStream = new ClassPathResource("import_test.json").getInputStream();
        MockMultipartFile testFile = new MockMultipartFile("file", "import_test.json",
                ContentType.APPLICATION_JSON.getMimeType(), fileAsStream);

        mockMvc.perform(MockMvcRequestBuilders.multipart(endpoint)
                .file(testFile)
                .header(IDPConstants.JWT_TOKEN, "token1"))
                .andExpect(status().isCreated());

        assertEquals(12443, entityRepository.findAll().size());
        for (Entity entity : entityRepository.findAll()) {
            assertEquals(CurationConstants.CONTEXT_DEFAULT, entity.getContext());
        }
    }

    @Test
    public void shouldImportCSV() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() +
                CurationConstants.API_SOURCES + "/" + sourceDto.getId() + CurationConstants.API_UPLOAD;

        InputStream fileAsStream = new ClassPathResource("import_test.csv").getInputStream();
        MockMultipartFile testFile = new MockMultipartFile("file", "import_test.csv",
                ContentType.APPLICATION_JSON.getMimeType(), fileAsStream);

        mockMvc.perform(MockMvcRequestBuilders.multipart(endpoint)
                .file(testFile)
                .header(IDPConstants.JWT_TOKEN, "token1"))
                .andExpect(status().isCreated());

        assertEquals(3, entityRepository.findAll().size());
        for (Entity entity : entityRepository.findAll()) {
            assertEquals(CurationConstants.CONTEXT_DEFAULT, entity.getContext());
        }
    }

    @Test
    public void shouldNotImportData() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() +
                CurationConstants.API_SOURCES + "/" + sourceDto.getId() + CurationConstants.API_UPLOAD;

        InputStream fileAsStream = new ClassPathResource("import_test.json").getInputStream();
        MockMultipartFile testFile = new MockMultipartFile("file", "import_test.json",
                ContentType.APPLICATION_JSON.getMimeType(), fileAsStream);

        mockMvc.perform(MockMvcRequestBuilders.multipart(endpoint)
                .file(testFile)
                .header(IDPConstants.JWT_TOKEN, "token2"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotImportDataAsConsumer() throws Exception {
        String endpoint = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS + "/" + projectDto.getId() +
                CurationConstants.API_SOURCES + "/" + sourceDto.getId() + CurationConstants.API_UPLOAD;
        userService.addUserToProject(super.user2, projectDto.getId(), Arrays.asList(new ProjectRole[]{ProjectRole.CONSUMER}));

        InputStream fileAsStream = new ClassPathResource("import_test.json").getInputStream();
        MockMultipartFile testFile = new MockMultipartFile("file", "import_test.json",
                ContentType.APPLICATION_JSON.getMimeType(), fileAsStream);

        mockMvc.perform(MockMvcRequestBuilders.multipart(endpoint)
                .file(testFile)
                .header(IDPConstants.JWT_TOKEN, "token2"))
                .andExpect(status().isNotFound());
    }
}
