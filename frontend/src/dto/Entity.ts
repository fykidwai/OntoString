import MappingSuggestion from "./MappingSuggestion";
import Provenance from "./Provenance";
import Source from "./Source";
import Mapping from "./Mapping";

export default interface Entity {
    id: string
    name: string
    mappingStatus: string
    source: Source
    upstreamId: string
    upstreamField: string
    mappingSuggestions:MappingSuggestion[]
    mappings:Mapping[]
    created:Provenance
}
