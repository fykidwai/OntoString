package uk.ac.ebi.spot.ontotools.curation.rest.controller;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.ontotools.curation.constants.AuditEntryConstants;
import uk.ac.ebi.spot.ontotools.curation.constants.CurationConstants;
import uk.ac.ebi.spot.ontotools.curation.constants.EntityStatus;
import uk.ac.ebi.spot.ontotools.curation.constants.ProjectRole;
import uk.ac.ebi.spot.ontotools.curation.domain.MetadataEntry;
import uk.ac.ebi.spot.ontotools.curation.domain.Provenance;
import uk.ac.ebi.spot.ontotools.curation.domain.auth.User;
import uk.ac.ebi.spot.ontotools.curation.domain.mapping.Entity;
import uk.ac.ebi.spot.ontotools.curation.domain.mapping.Mapping;
import uk.ac.ebi.spot.ontotools.curation.domain.mapping.OntologyTerm;
import uk.ac.ebi.spot.ontotools.curation.rest.assembler.MappingDtoAssembler;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.mapping.MappingCreationDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.mapping.MappingDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.mapping.OntologyTermDto;
import uk.ac.ebi.spot.ontotools.curation.service.*;
import uk.ac.ebi.spot.ontotools.curation.system.GeneralCommon;
import uk.ac.ebi.spot.ontotools.curation.util.HeadersUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS)
public class MappingsController {

    private static final Logger log = LoggerFactory.getLogger(MappingsController.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private MappingSuggestionsService mappingSuggestionsService;

    @Autowired
    private OntologyTermService ontologyTermService;

    /**
     * GET /v1/projects/{projectId}/mappings?entityId=<entityId>
     */
    @GetMapping(value = "/{projectId}" + CurationConstants.API_MAPPINGS,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public MappingDto getMapping(@PathVariable String projectId, @RequestParam(value = CurationConstants.PARAM_ENTITY_ID) String entityId, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to retrieve mappings: {} | {}", user.getEmail(), projectId, entityId);
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN, ProjectRole.CONTRIBUTOR, ProjectRole.CONSUMER}));
        Entity entity = entityService.retrieveEntity(entityId);
        Mapping mapping = mappingService.retrieveMappingForEntity(entity.getId());
        if (mapping == null) {
            return null;
        }
        return MappingDtoAssembler.assemble(mapping);
    }

    /**
     * POST /v1/projects/{projectId}/mappings
     */
    @PostMapping(value = "/{projectId}" + CurationConstants.API_MAPPINGS,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MappingDto createMapping(@PathVariable String projectId, @RequestBody @Valid MappingCreationDto mappingCreationDto, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to create mapping: {} | {} | {}", user.getEmail(), projectId, mappingCreationDto.getEntityId(), mappingCreationDto.getOntologyTerms());
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN, ProjectRole.CONTRIBUTOR}));

        Provenance provenance = new Provenance(user.getName(), user.getEmail(), DateTime.now());
        Entity entity = entityService.retrieveEntity(mappingCreationDto.getEntityId());
        /**
         * Check if a mapping to this term already exists
         */
        Mapping existingMapping = mappingService.retrieveMappingForEntity(entity.getId());
        if (existingMapping != null) {
            log.warn("Entity [{}] already has a mapping.", entity.getName());
            return MappingDtoAssembler.assemble(existingMapping);
        }

        List<String> curies = mappingCreationDto.getOntologyTerms().stream().map(OntologyTermDto::getCurie).collect(Collectors.toList());
        List<OntologyTerm> ontologyTerms = ontologyTermService.retrieveTermByCuries(curies);

        /**
         * Create new mapping.
         */
        Mapping created = mappingService.createMapping(entity, ontologyTerms, provenance);

        /**
         * Updating mapping status to MANUAL.
         */
        entityService.updateMappingStatus(entity, EntityStatus.MANUALLY_MAPPED);

        return MappingDtoAssembler.assemble(created);
    }

    /**
     * PUT /v1/projects/{projectId}/mappings/{mappingId}
     */
    @PutMapping(value = "/{projectId}" + CurationConstants.API_MAPPINGS + "/{mappingId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public MappingDto updateMapping(@PathVariable String projectId, @PathVariable String mappingId, @RequestBody @NotEmpty @Valid MappingDto mappingDto, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to update mapping [{} | {}]: {}", user.getEmail(), projectId, mappingId, mappingDto.getOntologyTerms());
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN, ProjectRole.CONTRIBUTOR}));

        Provenance provenance = new Provenance(user.getName(), user.getEmail(), DateTime.now());
        Map<String, OntologyTerm> ontologyTermMap = new LinkedHashMap<>();
        List<OntologyTerm> newTerms = new ArrayList<>();
        for (OntologyTermDto ontologyTermDto : mappingDto.getOntologyTerms()) {
            OntologyTerm ontologyTerm = ontologyTermService.retrieveTermByCurie(ontologyTermDto.getCurie());
            ontologyTermMap.put(ontologyTerm.getId(), ontologyTerm);
            newTerms.add(ontologyTerm);
        }
        Mapping existing = mappingService.retrieveMappingById(mappingId);
        List<String> existingOntoIds = existing.getOntologyTermIds();
        List<OntologyTerm> oldTerms = new ArrayList<>();
        for (String oId : existingOntoIds) {
            if (!ontologyTermMap.containsKey(oId)) {
                oldTerms.add(ontologyTermService.retrieveTermById(oId));
            }
        }

        Mapping updated = mappingService.updateMapping(mappingId, newTerms, oldTerms, provenance);

        /**
         * Updating mapping status to MANUAL.
         */
        Entity entity = entityService.retrieveEntity(updated.getEntityId());
        entityService.updateMappingStatus(entity, EntityStatus.MANUALLY_MAPPED);

        updated.setOntologyTerms(newTerms);
        return MappingDtoAssembler.assemble(updated);
    }

    /**
     * DELETE /v1/projects/{projectId}/mappings/{mappingId}
     */
    @DeleteMapping(value = "/{projectId}" + CurationConstants.API_MAPPINGS + "/{mappingId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteMapping(@PathVariable String projectId, @PathVariable String mappingId,
                              HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to delete mapping [{}]: {}", user.getEmail(), projectId, mappingId);
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN, ProjectRole.CONTRIBUTOR}));

        Provenance provenance = new Provenance(user.getName(), user.getEmail(), DateTime.now());
        Mapping mapping = mappingService.retrieveMappingById(mappingId);
        Entity entity = entityService.retrieveEntity(mapping.getEntityId());
        List<String> ontoTermIds = mapping.getOntologyTermIds();

        Map<String, OntologyTerm> ontologyTermMap = new LinkedHashMap<>();
        List<MetadataEntry> metadata = new ArrayList<>();
        for (String ontoTermId : ontoTermIds) {
            OntologyTerm ontologyTerm = ontologyTermService.retrieveTermById(ontoTermId);
            ontologyTermMap.put(ontoTermId, ontologyTerm);
            metadata.add(new MetadataEntry(ontologyTerm.getIri(), ontologyTerm.getLabel(), AuditEntryConstants.REMOVED.name()));
        }

        /**
         * Delete mapping.
         */
        mappingService.deleteMapping(mappingId, provenance, metadata);
        entity = entityService.updateMappingStatus(entity, EntityStatus.SUGGESTIONS_PROVIDED);

        /**
         * Re-create mapping suggestions.
         */
        for (String ontoTermId : ontologyTermMap.keySet()) {
            mappingSuggestionsService.createMappingSuggestion(entity, ontologyTermMap.get(ontoTermId), provenance);
        }

    }

}
