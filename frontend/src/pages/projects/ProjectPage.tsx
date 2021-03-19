
import { Breadcrumbs, CircularProgress, Link, Typography } from "@material-ui/core";
import React from "react";
import { Redirect } from "react-router-dom";
import { get } from "../../api";
import { getAuthHeaders, isLoggedIn } from "../../auth";
import Provenance from "../../components/Provenance";
import Project from "../../dto/Project";
import EntityList from "../entities/EntityList";

interface Props {
    id:string
}

interface State {
    project:Project|null
}

export default class ProjectPage extends React.Component<Props, State> {

    constructor(props:Props) {
        super(props)

        this.state = {
            project: null
        }
    }

    componentDidMount() {
        this.fetchProject()
    }

    render() {

        let { project } = this.state

        if (!isLoggedIn()) {
            return <Redirect to='/login' />
        }

        if(project === null) {
            return <CircularProgress />
        }

        return <div>
            <Breadcrumbs>
                <Link color="inherit" href="/">
                    Projects
                </Link>
                <Typography color="textPrimary">{project.name}</Typography>
            </Breadcrumbs>
            { project.created && <Provenance provenance={project.created} label="Created by" /> }
            <h1>{project.name}</h1>
            <Typography variant='subtitle1'>{project.description}</Typography>
            <br/>
            <h2>Entities</h2>
            <EntityList projectId={project.id as string} />
        </div>
    }

    async fetchProject() {

        let { id } = this.props

        await this.setState(prevState => ({ ...prevState, project: null }))

        let project = await get<Project>(`/v1/projects/${id}`)

        this.setState(prevState => ({ ...prevState, project }))
    }
}
