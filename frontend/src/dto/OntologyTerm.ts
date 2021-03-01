
/*

On creation:

curie, iri and label are mandatory NOT EMPTY
In responses:

status is auto-populated
*/

export default interface OntologyTerm {
    curie:string
    iri:string
    label:string
    status:string
    description:string
    crossRefs:string
}