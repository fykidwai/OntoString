package uk.ac.ebi.spot.ontotools.curation.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.ontotools.curation.constants.EntityStatus;
import uk.ac.ebi.spot.ontotools.curation.domain.mapping.Entity;
import uk.ac.ebi.spot.ontotools.curation.domain.Provenance;
import uk.ac.ebi.spot.ontotools.curation.domain.Project;
import uk.ac.ebi.spot.ontotools.curation.domain.auth.User;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.dataimport.ImportDataElementDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.dataimport.ImportDataPackageDto;
import uk.ac.ebi.spot.ontotools.curation.service.DataImportService;
import uk.ac.ebi.spot.ontotools.curation.service.EntityService;
import uk.ac.ebi.spot.ontotools.curation.service.MatchmakerService;
import uk.ac.ebi.spot.ontotools.curation.service.ProjectService;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class DataImportServiceImpl implements DataImportService {

    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private MatchmakerService matchmakerService;

    private ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() {
        this.objectMapper = new ObjectMapper();
    }

    @Async(value = "applicationTaskExecutor")
    @Override
    public void importData(String fileData, String projectId, String sourceId, User user) {
        log.info("[{} | {}] Importing data from file.", projectId, sourceId);
        Provenance provenance = new Provenance(user.getName(), user.getEmail(), DateTime.now());
        Project project = projectService.retrieveProject(projectId, user);
        try {
            ImportDataPackageDto importDataPackageDto = this.objectMapper.readValue(fileData, new TypeReference<ImportDataPackageDto>() {
            });

            log.info("Received {} entries to import.", importDataPackageDto.getData().size());
            long sTime = System.currentTimeMillis();
            log.info("Creating entities ...");
            int count = 0;
            for (ImportDataElementDto importDataElementDto : importDataPackageDto.getData()) {
                entityService.createEntity(new Entity(null, importDataElementDto.getText(), importDataElementDto.getBaseId(), importDataElementDto.getBaseField(),
                        sourceId, provenance, EntityStatus.UNMAPPED));
                count++;
                if (count % 100 == 0) {
                    log.info(" -- [{} | {}] Progress: {} of {}", projectId, sourceId, count, importDataPackageDto.getData().size());
                }
            }
            long eTime = System.currentTimeMillis();
            log.info("{} entities created [{}s]", count, (eTime - sTime) / 1000);
            matchmakerService.runMatchmaking(sourceId, project);
        } catch (IOException e) {
            log.error("Unable to deserialize import data file: {}", e.getMessage(), e);
        }
    }
}