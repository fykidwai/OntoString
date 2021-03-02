
import { Button, CircularProgress, createStyles, darken, FormGroup, lighten, makeStyles, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Theme, WithStyles, withStyles } from "@material-ui/core";
import React, { ChangeEvent } from "react";
import { useState, useEffect } from "react";
import { getAuthHeaders, getToken, isLoggedIn } from "../../auth";
import Project from "../../dto/Project";
import ProjectList from "./ProjectList";

interface Props {
    project:Project
    onUpdateProject:(project:Project)=>void
}

interface State {
}

class ProjectForm extends React.Component<Props, State> {

    constructor(props:Props) {

        super(props)

    }

    render() {

        return <form noValidate autoComplete='off'>
            <FormGroup>
                <TextField label="Name" fullWidth onChange={this.onChangeName} />
                <TextField label="Description" fullWidth onChange={this.onChangeDescription} />
            </FormGroup>
        </form>
    }

    onChangeName = (e:ChangeEvent<HTMLInputElement>) => {
        this.props.onUpdateProject({ ...this.props.project, name: e.target.value })
    }

    onChangeDescription = (e:ChangeEvent<HTMLInputElement>) => {
        this.props.onUpdateProject({ ...this.props.project, description: e.target.value })
    }

}

export default ProjectForm

