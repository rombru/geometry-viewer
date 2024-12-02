import {Feature} from "ol";
import type {StyleType} from "$lib/enum/style-type.enum";

export interface GeometryMessage {
    id: number,
    name: string;
    srid: number;
    wkt: string;
    zIndex: number;
    color?: string;
    style?: StyleType;
    feature?: Feature;
}