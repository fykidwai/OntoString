import Provenance from "./Provenance";

export default interface MappingSuggestion {
    id:string
    entityId:string
    ontologyTerm: {
        curie:string
        iri:string
        label:string
        status:string
    }
    created: Provenance
}
