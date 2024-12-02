<script lang="ts">
    import {MapService} from "$lib/service/map.service";
    import {geometryMessageList} from "$lib/store/geometry-message-list.svelte";
    import {ProjectionService} from "$lib/service/projection.service";
    import {DEFAULT_DATA_PROJECTION, DEFAULT_OSM_PROJECTION} from "$lib/constant/default-projections";

    $effect(() => {
        ProjectionService.registerProjections([DEFAULT_OSM_PROJECTION, DEFAULT_DATA_PROJECTION])
        MapService.createMap('map')
    });

    $effect(() => {
        for (const message of geometryMessageList.toReversed()) {
            if (!message.feature) {
                MapService.addGeometry(message);
            }
        }
    })
</script>

<div id="map"></div>