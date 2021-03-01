
import Review from './Review'
import Comment from './Comment'
import Provenance from './Provenance'

export default interface Mapping {
    id:string
    entityId:string
    ontologyTerm: {
        curie:string
        iri:string
        label:string
        status:string
    }
    reviewed:boolean
    status:string
    reviews:Review[]
    comments:Comment[]
    created: Provenance
}
