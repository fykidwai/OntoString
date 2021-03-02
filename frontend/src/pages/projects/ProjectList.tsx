
import { Button, CircularProgress, createStyles, darken, lighten, makeStyles, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Theme, WithStyles, withStyles } from "@material-ui/core";
import React from "react";
import { useState, useEffect } from "react";
import { getAuthHeaders, getToken, isLoggedIn } from "../../auth";
import Project from "../../dto/Project";
import CreateProjectDialog from "./CreateProjectDialog";

const styles = (theme:Theme) => createStyles({
    tableRow: {
        "&": {
            cursor: 'pointer'
        },
        "&:hover": {
            backgroundColor: lighten(theme.palette.primary.light, 0.85)
        }
    }
})

interface Props extends WithStyles<typeof styles> {
}

interface State {
    projects:Project[]|null
}

class ProjectList extends React.Component<Props, State> {

    constructor(props:Props) {

        super(props)

        this.state = {
            projects: null
        }


    }

    componentDidMount() {
        this.fetchProjects()
    }

    render() {

        let { projects } = this.state
        let { classes } = this.props

        if(projects === null) {
            return <CircularProgress />
        }

        return <TableContainer component={Paper}>
        <Table aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell align="left">Description</TableCell>
              <TableCell align="left">Created by</TableCell>
              <TableCell align="left">Created</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {projects.map((project:Project) => (
              <TableRow className={classes.tableRow} key={project.name}>
                <TableCell component="th" scope="row">
                    {project.name}
                </TableCell>
                <TableCell align="left">
                    {project.description}
                </TableCell>
                <TableCell align="left">
                    {project.created!.user.name}
                </TableCell>
                <TableCell align="left">
                    {project.created!.timestamp}
                </TableCell>
              </TableRow>
            ))}
            <TableRow>
                <TableCell colSpan={4} align="right">
                    <CreateProjectDialog onCreate={this.onCreateProject} />
                </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>
    }

    async fetchProjects() {

        let res = await fetch(process.env.REACT_APP_APIURL + '/v1/projects', {
            headers: { ...getAuthHeaders() }
        })

        let projects = await res.json()

        this.setState(prevState => ({ ...prevState, projects }))
    }

    onCreateProject = async (project:Project) => {

        let res = await fetch(process.env.REACT_APP_APIURL + '/v1/projects', {
            method: 'POST',
            headers: { ...getAuthHeaders() },
            body: JSON.stringify(project)
        })

        if(res.status === 200) {
            this.fetchProjects()
        } else {
            console.log('Error creating projects: ' + res.statusText)
        }
    }
}

export default withStyles(styles)(ProjectList)

