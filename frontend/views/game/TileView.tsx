import { Button } from '@hilla/react-components/Button.js';
import Tile from 'Frontend/generated/com/example/application/model/Tile';




type TileProps = {
    onTileSelected(tile: Tile): unknown; 
    tile: Tile;
    lastClickedTileId: String | null | undefined;
}

const TileView = (props: TileProps) => {
    const {tile} = props;

    let text = null;
    if(tile.visible === true){
        text = tile.value;
    }
    const onTileClick = () => {
        if(tile.visible){
            console.log('Tile is already clicked');
        }else{
            props.onTileSelected(tile);
        }
    }

    const inlineStyle = {
        gridRow: tile.row + 1,
        gridColumn: tile.col + 1
    }
    return (
        <div className={"tile-container " + (tile.visible ? 'selected ' : '')} style={inlineStyle}>
            <Button disabled={tile.visible} className={"tile" + (props.lastClickedTileId === tile.id ? " current-selection" :'')} onClick={onTileClick} >{text}</Button>
        </div>
    )

};

export default TileView;