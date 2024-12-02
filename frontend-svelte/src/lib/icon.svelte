<script lang="ts">
    import ArrowRight from "virtual:icons/mono-icons/arrow-right";
    import IcOutlineHorizontalRule from "virtual:icons/ic/outline-horizontal-rule";
    import TablerPointFilled from "virtual:icons/tabler/point-filled";
    import MaterialSymbolsCropSquareOutline from "virtual:icons/material-symbols/crop-square-outline";
    import LucideSquareStack from 'virtual:icons/lucide/square-stack';
    import {StyleType} from "$lib/enum/style-type.enum.js";

    const {type, color}: { type: StyleType, color: string } = $props()
</script>

{#if type === StyleType.Point}
    <TablerPointFilled style="color: {color}"/>
{:else if type === StyleType.Line}
    <IcOutlineHorizontalRule style="color: {color}"/>
{:else if type === StyleType.Arrow}
    <ArrowRight style="color: {color}"/>
{:else if type === StyleType.Polygon}
    <MaterialSymbolsCropSquareOutline style="color: {color}"/>
{:else if type === StyleType.GeometryCollection}
    <LucideSquareStack style="color: {color}"/>
{/if}