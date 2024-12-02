import {type Feature, Map, View} from "ol";
import TileLayer from "ol/layer/Tile";
import {OSM} from "ol/source";
import {fromLonLat} from "ol/proj";
import VectorSource from "ol/source/Vector";
import VectorLayer from "ol/layer/Vector";
import {WKT} from "ol/format";
import type {GeometryMessage} from "$lib/model/geometry-message.model";
import {ProjectionService} from "$lib/service/projection.service";
import {DEFAULT_OSM_PROJECTION} from "$lib/constant/default-projections";
import {StyleService} from "$lib/service/style.service";

export namespace MapService {
    let map: Map;
    const vectorSource = new VectorSource();
    const vectorLayer = new VectorLayer({source: vectorSource});
    const wktFormat = new WKT();

    export function createMap(target: string) {
        map = new Map({
            target: target,
            layers: [
                new TileLayer({source: new OSM()}),
                vectorLayer
            ],
            view: new View({
                center: fromLonLat([4.3517, 50.8503]),
                zoom: 14,
            }),
        });
    }

    export function addGeometry(message: GeometryMessage) {
        ProjectionService.registerProjections([message.srid])

        message.feature = wktFormat.readFeature(message.wkt, {
            dataProjection: message.srid.toString(),
            featureProjection: DEFAULT_OSM_PROJECTION.toString(),
        })
        StyleService.setStyle(message);
        vectorSource.addFeature(message.feature);

        zoomToFeature(message.feature);
    }

    export function removeGeometry(message: GeometryMessage) {
        if (message.feature) {
            vectorSource.removeFeature(message.feature)
        }
    }

    export function zoomToFeature(feature: Feature) {
        const extent = feature.getGeometry()?.getExtent()
        if (extent) {
            map.getView().fit(extent, {padding: [50, 50, 50, 50]});
        }
    }
}