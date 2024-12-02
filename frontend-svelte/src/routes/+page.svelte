<script lang="ts">
    import Map from "$lib/map.svelte"
    import Table from "$lib/table.svelte"

    if (import.meta.env.MODE === 'development') {
        // @ts-ignore
        // window.addGeometry('MyPoint', 'POINT (10 20)')
        // // @ts-ignore
        // window.addGeometry('MyPolygon', 'POLYGON ((5 15, 15 15, 15 5, 5 5, 5 15))')
        // // @ts-ignore
        // window.addGeometry('MyLinestring', 'MULTILINESTRING ((0 10, 10 10, 10 0, 0 0, 0 10))')
        // // @ts-ignore
        // window.addGeometry('MyPolygon', 'POLYGON ((5 15, 15 15, 15 5, 5 5, 5 15))')
        // // @ts-ignore
        // window.addGeometry('MyPolygon', 'POLYGON ((5 15, 15 15, 15 5, 5 5, 5 15))')
        window.addGeometry('MyPolygon', 'POLYGON ((150000 150000, 150000 160000, 160000 160000, 160000 150000))', 31370)
    }
</script>

<Map/>
<Table/>