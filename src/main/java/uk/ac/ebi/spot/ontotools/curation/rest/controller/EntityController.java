package uk.ac.ebi.spot.ontotools.curation.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.ontotools.curation.constants.CurationConstants;
import uk.ac.ebi.spot.ontotools.curation.constants.ProjectRole;
import uk.ac.ebi.spot.ontotools.curation.domain.Source;
import uk.ac.ebi.spot.ontotools.curation.domain.auth.User;
import uk.ac.ebi.spot.ontotools.curation.domain.mapping.Entity;
import uk.ac.ebi.spot.ontotools.curation.domain.mapping.Mapping;
import uk.ac.ebi.spot.ontotools.curation.domain.mapping.MappingSuggestion;
import uk.ac.ebi.spot.ontotools.curation.rest.assembler.EntityDtoAssembler;
import uk.ac.ebi.spot.ontotools.curation.rest.assembler.SourceDtoAssembler;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.EntityDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.RestResponsePage;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.SourceDto;
import uk.ac.ebi.spot.ontotools.curation.service.*;
import uk.ac.ebi.spot.ontotools.curation.system.GeneralCommon;
import uk.ac.ebi.spot.ontotools.curation.util.HeadersUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS)
public class EntityController {

    private static final Logger log = LoggerFactory.getLogger(EntityController.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private MappingSuggestionsService mappingSuggestionsService;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private AuditEntryService auditEntryService;

    /**
     * GET /v1/projects/{projectId}/entities?search=<search>
     */
    @GetMapping(value = "/{projectId}" + CurationConstants.API_ENTITIES,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RestResponsePage<EntityDto> getEntities(@PathVariable String projectId,
                                                   @RequestParam(value = CurationConstants.PARAM_SEARCH, required = false) String prefix,
                                                   @RequestParam(value = CurationConstants.PARAM_CONTEXT, required = false) String context,
                                                   @PageableDefault(size = 20, page = 0) Pageable pageable, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to retrieve entities: {} | {} | {}", user.getEmail(), projectId, prefix, context);
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN, ProjectRole.CONTRIBUTOR, ProjectRole.CONSUMER}));
        List<Source> sources = sourceService.getSources(projectId);
        Map<String, SourceDto> sourceMap = new HashMap<>();
        for (Source source : sources) {
            sourceMap.put(source.getId(), SourceDtoAssembler.assemble(source));
        }

        Page<Entity> entities = entityService.retrieveEntitiesForProject(projectId, prefix, context, pageable);
        List<String> entityIds = entities.get().map(Entity::getId).collect(Collectors.toList());
        Map<String, Mapping> mappings = mappingService.retrieveMappingsForEntities(entityIds, projectId, context);
        Map<String, List<MappingSuggestion>> mappingSuggestions = mappingSuggestionsService.retrieveMappingSuggestionsForEntities(entityIds, projectId, context);
        log.info("Assembling results ...");
        List<EntityDto> entityDtos = new ArrayList<>();
        for (Entity entity : entities.getContent()) {
            entityDtos.add(EntityDtoAssembler.assemble(entity, sourceMap.get(entity.getSourceId()),
                    mappings.get(entity.getId()),
                    mappingSuggestions.get(entity.getId()),
                    new ArrayList<>()));
        }
        log.info("Returning results ...");
        return new RestResponsePage<>(entityDtos, pageable, entities.getTotalElements());
    }

    /**
     * GET /v1/projects/{projectId}/entities/{entityId}
     */
    @GetMapping(value = "/{projectId}" + CurationConstants.API_ENTITIES + "/{entityId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public EntityDto getEntity(@PathVariable String projectId, @PathVariable String entityId, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to retrieve entity: {} | {}", user.getEmail(), projectId, entityId);
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN, ProjectRole.CONTRIBUTOR, ProjectRole.CONSUMER}));
        Entity entity = entityService.retrieveEntity(entityId);
        Source source = sourceService.getSource(entity.getSourceId(), projectId);
        Mapping mapping = mappingService.retrieveMappingForEntity(entityId);
        Map<String, List<MappingSuggestion>> mappingSuggestions = mappingSuggestionsService.retrieveMappingSuggestionsForEntities(Arrays.asList(new String[]{entityId}), projectId, entity.getContext());
        return EntityDtoAssembler.assemble(entity, SourceDtoAssembler.assemble(source),
                mapping, mappingSuggestions.get(entityId),
                auditEntryService.retrieveAuditEntries(entity.getId()));
    }
}
