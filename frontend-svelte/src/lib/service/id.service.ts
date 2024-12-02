import type {GeometryMessage} from "$lib/model/geometry-message.model";

export namespace IdService {
    export function messageToRowId(message: GeometryMessage) {
        return "message-" + message.id;
    }

    export function rowToMessageId(row: HTMLTableRowElement) {
        return Number.parseInt(row.id.replace("message-", ""));
    }
}