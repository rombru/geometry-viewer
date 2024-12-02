import {register} from "ol/proj/proj4";
import proj4 from 'proj4';
import spatial_reference from "$lib/constant/spatial-reference";

export namespace ProjectionService {
    const projections: number[] = [];

    export function registerProjections(srid: number[]) {
        projections.push(...srid)
        proj4.defs(spatial_reference.filter(x => projections.indexOf(x.srid) !== -1).map(x => [`${x.srid}`, x.proj4text]));
        register(proj4);
    }
}