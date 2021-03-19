
import { Breadcrumbs, CircularProgress, Link, Typography } from "@material-ui/core";
import React from "react";
import { Redirect } from "react-router-dom";
import { post, get, put } from "../../api";
import { getAuthHeaders, isLoggedIn } from "../../auth";
import EntityStatusBox from "../../components/EntityStatusBox";
import MappingStatusBox from "../../components/MappingStatusBox";
import Entity from "../../dto/Entity";
import Mapping, { CreateMapping } from "../../dto/Mapping";
import MappingSuggestion from "../../dto/MappingSuggestion";
import Project from "../../dto/Project";
import MappingSuggestionList from "./MappingSuggestionList";

interface Props {
    projectId:string
    entityId:string
}

interface State {
    project:Project|null
    entity:Entity|null
}

export default class EntityPage extends React.Component<Props, State> {

    constructor(props:Props) {
        super(props)

        this.state = {
            entity: null,
            project: null
        }
    }

    componentDidMount() {
        this.fetch()
    }

    render() {

        let { project, entity } = this.state

        if (!isLoggedIn()) {
            return <Redirect to='/login' />
        }

        if(!project || !entity) {
            return <CircularProgress />
        }

        return <div>
            <Breadcrumbs>
                <Link color="inherit" href="/">
                    Projects
                </Link>
                <Link color="inherit" href={`/projects/${project.id!}`}>
                    {project.name}
                </Link>
                <Link color="inherit" href={`/projects/${project.id!}`}>
                    Entities
                </Link>
                <Typography color="textPrimary">{entity.name}</Typography>
            </Breadcrumbs>
            <h1>{entity.name} <EntityStatusBox status={entity.mappingStatus} /></h1>
            <h2>Suggested Mappings</h2>
            <MappingSuggestionList project={project} entity={entity} onClickSuggestion={this.onClickSuggestion} />
        </div>
    }

    async fetch() {

        let { projectId, entityId } = this.props

        await this.setState(prevState => ({ ...prevState, project: null, entity: null }))

        let [ project, entity ] = await Promise.all([
            get<Project>(`/v1/projects/${projectId}`),
            get<Entity>(`/v1/projects/${projectId}/entities/${entityId}`)
        ])

        this.setState(prevState => ({ ...prevState, project, entity }))
    }

    onClickSuggestion = (suggestion:MappingSuggestion) => {

        let entity = this.state.entity!

        let term = suggestion.ontologyTerm

        if(entity.mapping) {

            let wasSelected = entity.mapping.ontologyTerms.filter(t => t.iri === term.iri).length > 0

            if(wasSelected) {

                this.updateMapping({
                    ...entity.mapping,
                    ontologyTerms: entity.mapping.ontologyTerms.filter(t => t.iri !== term.iri)
                })

            } else {

                this.updateMapping({
                    ...entity.mapping,
                    ontologyTerms: [
                        ...entity.mapping.ontologyTerms,
                        suggestion.ontologyTerm
                    ]
                })
                
            }

        } else {

            this.createMapping({
                entityId: entity.id,
                ontologyTerms: [
                    suggestion.ontologyTerm
                ]
            })

        }


    }

    private async createMapping(mapping:CreateMapping) {

        let { projectId, entityId } = this.props

        await post(`/v1/projects/${projectId}/entities/${entityId}/mapping`, mapping)
    }

    private async updateMapping(mapping:Mapping) {

        let { projectId, entityId } = this.props

        await put(`/v1/projects/${projectId}/entities/${entityId}/mapping`, [mapping])
    }
}
