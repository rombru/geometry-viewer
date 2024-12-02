<script lang="ts">
    import TableRow from "$lib/table-row.svelte";
    import {geometryMessageList} from "$lib/store/geometry-message-list.svelte";
    import {IdService} from "$lib/service/id.service.js";
    import {StyleService} from "$lib/service/style.service";

    let table: HTMLTableElement;
    let draggedRowId: number;

    function onDragOver(evt: DragEvent) {
        evt.preventDefault();
        if (evt && evt.dataTransfer) {
            evt.dataTransfer.dropEffect = "move";
        }
        const rows = table.querySelectorAll("tr").values()
            .map(el => {
                const bounding = el.getBoundingClientRect();
                const offset = evt.clientY - bounding.top;
                const absOffset = Math.abs(offset);
                return {el, bounding, offset, absOffset};
            });
        const target = rows.reduce((r1, r2) => r1.absOffset < r2.absOffset ? r1 : r2);
        const targetId = IdService.rowToMessageId(target.el);

        if (targetId !== draggedRowId) {
            const row = target.el;
            const bounding = row.getBoundingClientRect();
            const offset = evt.clientY - bounding.top;
            const draggedRowIndex = geometryMessageList.findIndex(message => message.id === draggedRowId);
            const draggedRowMessage = geometryMessageList.splice(draggedRowIndex, 1)[0];
            const rowIndex = geometryMessageList.findIndex(message => message.id === targetId);
            if (offset > row.offsetHeight / 2) {
                geometryMessageList.splice(rowIndex + 1, 0, draggedRowMessage);
            } else {
                geometryMessageList.splice(rowIndex, 0, draggedRowMessage);
            }

            for (let i = rowIndex + 1; i >= 0; i--) {
                geometryMessageList[i].zIndex = geometryMessageList.length - i;
                StyleService.updateZIndex(geometryMessageList[i]);
            }
        }
    }

    function onDragStart(evt: DragEvent) {
        const target = evt.target as HTMLElement;
        const row = target?.closest("tr") as HTMLTableRowElement;
        if (evt && evt.dataTransfer) {
            draggedRowId = IdService.rowToMessageId(row);
            evt.dataTransfer.effectAllowed = "move";

            const clone = row.cloneNode(true) as HTMLElement;
            clone.getElementsByClassName("dropdown-content")[0]?.remove();
            const rect = row.getBoundingClientRect();
            clone.style.top = Math.max(0, rect.top) + "px";
            clone.style.left = Math.max(0, rect.left) + "px";
            clone.style.width = rect.width + "px";
            clone.style.height = rect.height + "px";
            clone.style.position = "absolute";
            clone.style.pointerEvents = "none";
            document.body.appendChild(clone);
            setTimeout(function () {
                document.body.removeChild(clone);
            });

            evt.dataTransfer.setDragImage(clone, 0, 0);
        }
    }

    function onDragEnter() {
        table.style.pointerEvents = "none"
    }

    function onDragEnd() {
        table.style.pointerEvents = "auto"
    }
</script>

<div class="top-3 right-3 absolute z-10">
    <div class="rounded overflow-visible bg-white mt-3">
        <div ondragend={onDragEnd} ondragenter={onDragEnter} ondragover={onDragOver} role="table">
            <table bind:this={table} class="table table-sm">
                <tbody id="table-body">
                {#each geometryMessageList as message}
                    <TableRow {onDragStart} {message}/>
                {/each}
                </tbody>
            </table>
        </div>
    </div>
</div>