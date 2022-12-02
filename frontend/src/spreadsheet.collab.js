window.Vaadin.Flow._spreadsheet_collab = {

    init(spreadsheet) {
        if(!spreadsheet.__collab_overlays) {
            spreadsheet.__collab_overlays = {};
            spreadsheet.__collab_names = {};
        }
    },

    onSelect(spreadsheet, id, row, col, color, name) {
        let query = `.col${col}.row${row}`;
        let cell = spreadsheet.shadowRoot.querySelector(query);

        if(cell) {
            debugger;
            let bcr = cell.getBoundingClientRect();

            let overlayDiv = document.createElement('div');
            let oStyle = overlayDiv.style;
            oStyle.border = `2px solid ${color}`;
            oStyle.pointerEvents = 'none';
            oStyle.position = 'absolute';
            oStyle.left = bcr.x + "px";
            oStyle.top = bcr.y + "px";
            oStyle.width = bcr.width - 3 + "px";
            oStyle.height = bcr.height - 3 + "px";

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

            spreadsheet.shadowRoot.append(overlayDiv, nameDiv);

            // readjust ourselves so that we look good
            nStyle.top = (bcr.y - nameDiv.getBoundingClientRect().height) + "px";



            spreadsheet.__collab_overlays[id] = overlayDiv;
            spreadsheet.__collab_names[id] = nameDiv;
        }
    },

    onDeselect(spreadsheet, id) {
        let overlay = spreadsheet.__collab_overlays[id];
        if (overlay) {
            overlay.remove();
        }

        let name = spreadsheet.__collab_names[id];
        if (name) {
            name.remove();
        }
    },

}