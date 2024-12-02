import type {GeometryMessage} from "$lib/model/geometry-message.model";
import {DEFAULT_DATA_PROJECTION} from "$lib/constant/default-projections";

export const geometryMessageList: GeometryMessage[] = $state([])

const defaultZIndex = 0;
let id = 0;

function addGeometry(name: string, wkt: string, srid: number = DEFAULT_DATA_PROJECTION) {
    const zIndex = geometryMessageList.length;
    geometryMessageList.unshift({id, name, zIndex, srid, wkt});
    id++;
}

// @ts-ignore
window["addGeometry"] = addGeometry;

// @ts-ignore
for (const initialGeometryMessage of window["initialGeometryMessageList"]) {
    addGeometry(initialGeometryMessage.name, initialGeometryMessage.wkt, initialGeometryMessage.srid);
}