<script lang="ts">
    import Icon from "$lib/icon.svelte";
    import Delete from "virtual:icons/mono-icons/delete";
    import type {GeometryMessage} from "$lib/model/geometry-message.model";
    import {StyleType} from "$lib/enum/style-type.enum.js";
    import type {Type} from "ol/geom/Geometry";
    import {StyleService} from "$lib/service/style.service";
    import {MapService} from "$lib/service/map.service";
    import {geometryMessageList} from "$lib/store/geometry-message-list.svelte";
    import SiDragIndicatorFill from 'virtual:icons/si/drag-indicator-fill';
    import {IdService} from "$lib/service/id.service";
    import type {Feature} from "ol";

    const {message, onDragStart}: { message: GeometryMessage, onDragStart: (evt: DragEvent) => void } = $props()
    const type: StyleType = $derived(message?.style)!;
    const color: string = $derived(message.color)!;
    const styleTypeList = $derived(getTypeListByGeometryType(message?.feature?.getGeometry()?.getType()));

    function getTypeListByGeometryType(geometryType?: Type) {
        switch (geometryType) {
            case 'LineString':
            case 'LinearRing':
                return [StyleType.Line, StyleType.Arrow];
            case 'MultiLineString':
            case 'Point':
            case 'MultiPoint':
            case 'Polygon':
            case 'Circle':
            case 'MultiPolygon':
            case 'GeometryCollection':
            default:
                return [];
        }
    }

    function selectStyleType(styleType: StyleType) {
        if (message.feature) {
            StyleService.setStyle(message, styleType)
        }
        if (document.activeElement) {
            const target = document.activeElement as HTMLElement;
            target.blur()
        }
    }

    function deleteRow() {
        MapService.removeGeometry(message);
        geometryMessageList.splice(geometryMessageList.indexOf(message), 1);
    }

    function zoomTo(feature?: Feature) {
        if (feature) {
            MapService.zoomToFeature(feature);
        }
    }
</script>

<tr id="{IdService.messageToRowId(message)}">
    <td>
        <button class="cursor-grab rounded p-0.5 bg-slate-100 hover:bg-slate-200 align-bottom" draggable="true"
                ondragstart={(evt) => onDragStart(evt)}>
            <SiDragIndicatorFill class="pointer-events-none"></SiDragIndicatorFill>
        </button>
    </td>
    <td>
        {#if styleTypeList.length > 0}
            <div class="dropdown align-text-bottom">
                <div tabindex="0" role="button" class="hover:bg-slate-100 rounded p-0.5">
                    <Icon {color} {type}/>
                </div>

                <ul class="dropdown-content menu bg-base-100 rounded-box z-[1] w-16 shadow">
                    {#each styleTypeList as styleType}
                        <li>
                            <button onclick={() => selectStyleType(styleType)}>
                                <span tabindex="0" role="button"><Icon {color} type={styleType}/></span>
                            </button>
                        </li>
                    {/each}
                </ul>
            </div>
        {:else}
            <Icon {color} {type}/>
        {/if}
    </td>
    <td>
        <button class="cursor-pointer hover:bg-slate-100 active:bg-slate-200 p-0.5 rounded overflow-hidden whitespace-nowrap text-ellipsis max-w-24" onclick={() => zoomTo(message.feature)}
                title="{message.name}">{message.name}</button>
    </td>
    <td>
        <button onclick={() => deleteRow()} class="align-text-bottom">
            <Delete class="cursor-pointer hover:text-red-500"/>
        </button>
    </td>
</tr>