package uk.ac.ebi.spot.ontotools.curation.rest.controller;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.ontotools.curation.constants.CurationConstants;
import uk.ac.ebi.spot.ontotools.curation.domain.Project;
import uk.ac.ebi.spot.ontotools.curation.domain.Provenance;
import uk.ac.ebi.spot.ontotools.curation.domain.auth.User;
import uk.ac.ebi.spot.ontotools.curation.exception.AuthorizationException;
import uk.ac.ebi.spot.ontotools.curation.rest.assembler.ProjectDtoAssembler;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectContextGraphRestrictionDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectCreationDto;
import uk.ac.ebi.spot.ontotools.curation.rest.dto.project.ProjectDto;
import uk.ac.ebi.spot.ontotools.curation.service.JWTService;
import uk.ac.ebi.spot.ontotools.curation.service.ProjectService;
import uk.ac.ebi.spot.ontotools.curation.service.UserService;
import uk.ac.ebi.spot.ontotools.curation.system.GeneralCommon;
import uk.ac.ebi.spot.ontotools.curation.util.GraphRestrictionUtil;
import uk.ac.ebi.spot.ontotools.curation.util.HeadersUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + CurationConstants.API_PROJECTS)
public class ProjectsController {

    private static final Logger log = LoggerFactory.getLogger(ProjectsController.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private GraphRestrictionUtil graphRestrictionUtil;

    /**
     * POST /v1/projects
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto createProject(@RequestBody @Valid ProjectCreationDto projectCreationDto, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        if (!user.isSuperUser()) {
            log.error("Attempt to create a project by a non-super user: {}", user.getEmail());
            throw new AuthorizationException("Attempt to create a project by a non-super user: " + user.getEmail());
        }
        log.info("[{}] Request to create project: {}", user.getEmail(), projectCreationDto.getName());
        if (projectCreationDto.getGraphRestriction() != null) {
            ProjectContextGraphRestrictionDto newGraphRestriction = graphRestrictionUtil.enrichGraphRestriction(projectCreationDto.getGraphRestriction());
            projectCreationDto = new ProjectCreationDto(projectCreationDto.getName(),
                    projectCreationDto.getDescription(),
                    projectCreationDto.getNumberOfReviewsRequired(),
                    projectCreationDto.getDatasources(),
                    projectCreationDto.getOntologies(),
                    projectCreationDto.getPreferredMappingOntologies(),
                    newGraphRestriction);
        }

        Project created = projectService.createProject(ProjectDtoAssembler.disassemble(projectCreationDto, new Provenance(user.getName(), user.getEmail(), DateTime.now())), user);
        return ProjectDtoAssembler.assemble(created);
    }

    /**
     * GET /v1/projects
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectDto> getProjects(HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to retrieve projects.", user.getEmail());

        List<Project> projects = projectService.retrieveProjects(user);
        log.info("Found {} projects for user: {}", projects.size(), user.getEmail());
        return projects.stream().map(ProjectDtoAssembler::assemble).collect(Collectors.toList());
    }

    /**
     * GET /v1/projects/{projectId}
     */
    @GetMapping(value = "/{projectId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ProjectDto getProject(@PathVariable String projectId, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        log.info("[{}] Request to retrieve project: {}", user.getEmail(), projectId);
        Project project = projectService.retrieveProject(projectId, user);
        return ProjectDtoAssembler.assemble(project);
    }

    /**
     * PUT /v1/projects/{projectId}
     */
    @PutMapping(value = "/{projectId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ProjectDto updateProject(@RequestBody @Valid ProjectDto projectDto, @PathVariable String projectId, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        if (!user.isSuperUser()) {
            log.error("Attempt to create a project by a non-super user: {}", user.getEmail());
            throw new AuthorizationException("Attempt to create a project by a non-super user: " + user.getEmail());
        }
        log.info("[{}] Request to update project [{}]: {}", user.getEmail(), projectId, projectDto.getName());
        Project updated = projectService.updateProject(ProjectDtoAssembler.disassemble(projectDto), projectId, user);
        return ProjectDtoAssembler.assemble(updated);
    }

    /**
     * DELETE /v1/projects/{projectId}
     */
    @DeleteMapping(value = "/{projectId}",
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteProject(@PathVariable String projectId, HttpServletRequest request) {
        User user = jwtService.extractUser(HeadersUtil.extractJWT(request));
        if (!user.isSuperUser()) {
            log.error("Attempt to create a project by a non-super user: {}", user.getEmail());
            throw new AuthorizationException("Attempt to create a project by a non-super user: " + user.getEmail());
        }
        log.info("[{}] Request to delete project: {}", user.getEmail(), projectId);
        projectService.deleteProject(projectId, user);
        userService.removeProjectFromUser(user, projectId);
    }
}
