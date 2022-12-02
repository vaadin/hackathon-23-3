window.Vaadin.Flow._spreadsheet_collab = {

    init(spreadsheet) {
        if(!spreadsheet.__collab) {
            spreadsheet.__collab = {};
        }

    },

    onSelect(spreadsheet, id, row, col, color, name) {
        this.registerOnScrollListener(spreadsheet);

        this.onDeselect(spreadsheet, id);

        let query = `.col${col}.row${row}`;
        let cell = spreadsheet.shadowRoot.querySelector(query);

        if(cell) {
            let bcr = cell.getBoundingClientRect();

            let overlayDiv = document.createElement('div');
            let oStyle = overlayDiv.style;
            oStyle.border = `2px solid ${color}`;
            oStyle.pointerEvents = 'none';
            oStyle.position = 'absolute';
            oStyle.left = bcr.x - 1 + "px";
            oStyle.top = bcr.y - 1 + "px";
            oStyle.width = bcr.width - 3 + "px";
            oStyle.height = bcr.height - 3 + "px";
            oStyle.zIndex = 2;

            let nameDiv = document.createElement("div");
            nameDiv.textContent = name;
            let nStyle = nameDiv.style;
            nStyle.pointerEvents = 'none';
            nStyle.position = 'absolute';
            nStyle.left = oStyle.left;
            nStyle.top = oStyle.top;
            nStyle.backgroundColor = color;
            nStyle.padding = "2px";
            nStyle.fontSize = "0.5rem";
            nStyle.color = "white";
            nStyle.zIndex = 2;

            spreadsheet.shadowRoot.append(overlayDiv, nameDiv);

            // readjust ourselves so that we look good
            nStyle.top = (bcr.y - nameDiv.getBoundingClientRect().height) + "px";

            spreadsheet.__collab[id] = {};
            spreadsheet.__collab[id].overlay = overlayDiv;
            spreadsheet.__collab[id].name = nameDiv;
            spreadsheet.__collab[id].cell = cell;
        }
    },

    onDeselect(spreadsheet, id) {
        if (spreadsheet.__collab[id]) {
            spreadsheet.__collab[id].overlay.remove();
            spreadsheet.__collab[id].name.remove();

            delete spreadsheet.__collab[id];
        }
    },

    registerOnScrollListener(spreadsheet) {
        // not yet working well
        if(!spreadsheet.__collab_selectListenerRegistered) {
            let area = spreadsheet.shadowRoot.querySelector("div.bottom-right-pane.sheet");
            if (area) {
                spreadsheet.__collab_selectListenerRegistered = true;
                area.addEventListener("scroll", e => this.onScroll(spreadsheet))
            }
        }
    },

    onScroll(spreadsheet) {
        // not yet working well
        for (const [key, collabElement] of Object.entries(spreadsheet.__collab)) {
            let bcr = collabElement.cell.getBoundingClientRect();

            let oStyle = collabElement.overlay.style;
            oStyle.left = bcr.x - 1 + "px";
            oStyle.top = bcr.y - 1 + "px";
            oStyle.width = bcr.width - 3 + "px";
            oStyle.height = bcr.height - 3 + "px";

            let nStyle = collabElement.name.style;
            nStyle.left = oStyle.left;
            nStyle.top = (bcr.y - collabElement.name.getBoundingClientRect().height) + "px";
        }
    },

}