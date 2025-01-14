package uk.ac.ebi.spot.ontotools.curation.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.ontotools.curation.constants.CurationConstants;
import uk.ac.ebi.spot.ontotools.curation.constants.ProjectRole;
import uk.ac.ebi.spot.ontotools.curation.domain.Project;
import uk.ac.ebi.spot.ontotools.curation.domain.auth.User;
import uk.ac.ebi.spot.ontotools.curation.exception.BadRequestException;
import uk.ac.ebi.spot.ontotools.curation.rest.assembler.ProjectContextDtoAssembler;
import uk.ac.ebi.spot.ontotools.curation.rest.assembler.ProjectDtoAssembler;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectContextDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectContextGraphRestrictionDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectDto;
import uk.ac.ebi.spot.ontotools.curation.service.EntityService;
import uk.ac.ebi.spot.ontotools.curation.service.JWTService;
import uk.ac.ebi.spot.ontotools.curation.service.ProjectService;
import uk.ac.ebi.spot.ontotools.curation.system.GeneralCommon;
import uk.ac.ebi.spot.ontotools.curation.util.GraphRestrictionUtil;
import uk.ac.ebi.spot.ontotools.curation.util.HeadersUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Arrays;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS)
public class ProjectContextsController {

    private static final Logger log = LoggerFactory.getLogger(ProjectContextsController.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private GraphRestrictionUtil graphRestrictionUtil;

    /**
     * POST /v1/projects/{projectId}/contexts
     */
    @PostMapping(value = "/{projectId}" + CurationConstants.API_CONTEXTS,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto createProjectContext(@PathVariable String projectId,
                                           @RequestBody @Valid ProjectContextDto projectContextDto, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to create project context [{}]: {}", user.getEmail(), projectId, projectContextDto.getName());
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN}));
        if (projectContextDto.getGraphRestriction() != null) {
            ProjectContextGraphRestrictionDto newGraphRestriction = graphRestrictionUtil.enrichGraphRestriction(projectContextDto.getGraphRestriction());
            projectContextDto = new ProjectContextDto(projectContextDto.getName(),
                    projectContextDto.getDescription(),
                    projectContextDto.getDatasources(),
                    projectContextDto.getOntologies(),
                    projectContextDto.getPreferredMappingOntologies(),
                    newGraphRestriction);
        }

        Project project = projectService.createProjectContext(ProjectContextDtoAssembler.disassemble(projectContextDto), projectId, user);
        return ProjectDtoAssembler.assemble(project);
    }

    /**
     * PUT /v1/projects/{projectId}/contexts
     */
    @PutMapping(value = "/{projectId}" + CurationConstants.API_CONTEXTS,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ProjectDto updateProjectContext(@RequestBody @Valid ProjectContextDto projectContextDto, @PathVariable String projectId, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to update project context [{}]: {}", user.getEmail(), projectId, projectContextDto.getName());
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN}));
        ProjectContextGraphRestrictionDto newGraphRestriction = projectContextDto.getGraphRestriction();
        if (newGraphRestriction != null) {
            newGraphRestriction = graphRestrictionUtil.enrichGraphRestriction(newGraphRestriction);
        }

        Project updated = projectService.updateProjectContext(ProjectContextDtoAssembler.disassemble(
                new ProjectContextDto(projectContextDto.getName(),
                        projectContextDto.getDescription(),
                        projectContextDto.getDatasources(),
                        projectContextDto.getOntologies(),
                        projectContextDto.getPreferredMappingOntologies(),
                        newGraphRestriction)
        ), projectId, user);
        return ProjectDtoAssembler.assemble(updated);
    }

    /**
     * DELETE /v1/projects/{projectId}/contexts/{contextName}
     */
    @DeleteMapping(value = "/{projectId}" + CurationConstants.API_CONTEXTS + "/{contextName}",
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteProjectContext(@PathVariable String projectId, @PathVariable String contextName, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to delete project context [{}]: {}", user.getEmail(), projectId, contextName);
        projectService.verifyAccess(projectId, user, Arrays.asList(new ProjectRole[]{ProjectRole.ADMIN}));
        if (contextName.equalsIgnoreCase(CurationConstants.CONTEXT_DEFAULT)) {
            log.error("Cannot delete DEFAULT context for any project.");
            throw new BadRequestException("Cannot delete DEFAULT context for any project.");
        }
        projectService.deleteProjectContext(contextName, projectId, user);
        entityService.moveEntities(projectId, contextName, CurationConstants.CONTEXT_DEFAULT);
    }

}
