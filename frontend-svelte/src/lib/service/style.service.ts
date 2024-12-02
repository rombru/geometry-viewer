import {Fill, RegularShape, Stroke, Style} from "ol/style";
import CircleStyle from "ol/style/Circle";
import type {Feature} from "ol";
import {type Geometry, type GeometryCollection, type LinearRing, type LineString} from "ol/geom";
import {Point} from "ol/geom";
import {StyleType} from "$lib/enum/style-type.enum";
import type {StyleLike} from "ol/style/Style";
import type {GeometryMessage} from "$lib/model/geometry-message.model";
import {ColorService} from "$lib/service/color.service";

export namespace StyleService {

    export function updateZIndex(message: GeometryMessage) {
        const style = message.feature?.getStyle()
        if (style instanceof Array) {
            style.forEach(s => s.setZIndex(message.zIndex));
        } else if (style instanceof Style) {
            style.setZIndex(message.zIndex);
        }
        message.feature?.setStyle(style);
    }

    export function setStyle(message: GeometryMessage, styleType?: StyleType) {
        if (!message.feature) throw Error(`StyleService: Feature cannot be undefined.`);
        const feature = message.feature
        const geometry = feature.getGeometry();
        styleType = styleType ?? getDefaultStyleType(geometry);
        const color = message.color ?? getDefaultColor();
        const zIndex = message.zIndex;
        feature.setStyle(createStyleForFeature(feature, zIndex, styleType, color))
        message.style = styleType
        message.color = color
    }

    function createStyleForFeature(feature: Feature, zIndex: number, type: StyleType, color: string): StyleLike {
        const geometry = feature?.getGeometry()

        if (!geometry) {
            return [];
        } else if (type === StyleType.GeometryCollection) {
            const collection = geometry as GeometryCollection;
            return collection.getGeometries().map((geom) => {
                return createStyleForType(geom, getDefaultStyleType(geom), color, zIndex);
            }).flat()
        } else {
            return createStyleForType(geometry, type, color, zIndex)
        }
    }

    function createStyleForType(geom: Geometry, type: StyleType, color: string, zIndex: number): Style | Style[] {
        switch (type) {
            case StyleType.Point:
                return createPointStyle(color, zIndex)
            case StyleType.Line:
                return createLineStyle(color, zIndex)
            case StyleType.Arrow:
                return createArrowStyle(geom as LineString | LinearRing, color, zIndex)
            case StyleType.Polygon:
                return createPolygonStyle(color, zIndex)
            default:
                return createDefaultStyle(color, zIndex)
        }
    }

    function createPointStyle(color: string, zIndex: number): Style {
        return new Style({
            image: new CircleStyle({
                radius: 7,
                fill: new Fill({color}),
                stroke: new Stroke({color, width: 2}),
            }),
            zIndex
        });
    }

    function createLineStyle(color: string, zIndex: number): Style {
        return new Style({
            stroke: new Stroke({
                color,
                width: 3,
            }),
            zIndex
        });
    }

    function createArrowStyle(geometry: LineString | LinearRing, color: string, zIndex: number): Style[] {
        const coordinates = geometry.getCoordinates();
        const start = coordinates[coordinates.length - 2];
        const end = coordinates[coordinates.length - 1];
        const rotation = -Math.atan2(end[1] - start[1], end[0] - start[0]);

        return [
            new Style({
                stroke: new Stroke({
                    color,
                    width: 3,
                }),
                zIndex
            }),
            new Style({
                geometry: new Point(geometry.getLastCoordinate()),
                image: new RegularShape({
                    points: 3,               // Triangle shape
                    radius: 10,              // Size of the arrowhead
                    displacement: [-6, 0],
                    angle: Math.PI / 2,
                    fill: new Fill({
                        color,     // Arrowhead color matching line
                    }),
                    rotation: rotation, // Adjust rotation based on line direction
                    rotateWithView: true,
                }),
                zIndex
            })];
    }

    function createPolygonStyle(color: string, zIndex: number): Style {
        return new Style({
            fill: new Fill({
                color,
            }),
            stroke: new Stroke({
                color,
                width: 2,
            }),
            zIndex
        });
    }

    function createDefaultStyle(color: string, zIndex: number): Style {
        return new Style({
            stroke: new Stroke({
                color,
                width: 1,
            }),
            zIndex
        });
    }

    function getDefaultColor() {
        return ColorService.get() as string;
    }

    function getDefaultStyleType(geometry?: Geometry): StyleType {
        const geometryType = geometry?.getType()

        switch (geometryType) {
            case 'Point':
            case 'MultiPoint':
                return StyleType.Point;
            case 'LineString':
            case 'LinearRing':
            case 'MultiLineString':
                return StyleType.Line;
            case 'Polygon':
            case 'Circle':
            case 'MultiPolygon':
                return StyleType.Polygon;
            case 'GeometryCollection':
                return StyleType.GeometryCollection;
            default:
                return StyleType.Line;
        }
    }
}