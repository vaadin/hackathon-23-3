import React, {useState} from 'react';
import Tile from "Frontend/generated/com/example/application/model/Tile";
import TileView from "./TileView";


type TileMapProps = {
    tiles: Array<Tile | undefined>;
    matrixSize: number;
    userMatchedTwoTiles(tile1: Tile, tile2: Tile) : unknown;
    onTileClick(): unknown;
}

const TileMap = (props: TileMapProps) => {
    const {matrixSize, tiles, userMatchedTwoTiles, onTileClick} = props;

    const [lastSelectedTile, setLastSelectedTile] = useState<Tile | null>(null);
    

    const onTileSelected =(tile: Tile) => {
        
        if(lastSelectedTile !== null){
            if(tile.value === lastSelectedTile.value){
                userMatchedTwoTiles(tile, lastSelectedTile);
                setLastSelectedTile(null);
                
            }else{
                lastSelectedTile.visible = false;
                setLastSelectedTile(tile);
                tile.visible = true;
            }
        }else{
            tile.visible = true;
            setLastSelectedTile(tile);
        }
        onTileClick();
    }
    const style = {
        height: matrixSize * 110,
        width: matrixSize * 110
    };
    return (
        <div className="tile-map" style={style}> 
            {tiles.map(item => item && <TileView lastClickedTileId={lastSelectedTile && lastSelectedTile.id} tile={item} key={item.id} onTileSelected={onTileSelected} />)}
        </div>
    )

    
};
export default TileMap;