import React, {useState, useEffect} from 'react';
import { Button } from '@hilla/react-components/Button.js';
import { RadioGroup } from '@hilla/react-components/RadioGroup.js';
import { RadioButton } from '@hilla/react-components/RadioButton.js';
import * as cn from 'classnames';
import { GameEndpoint } from 'Frontend/generated/endpoints';
import GameLevel from 'Frontend/generated/com/example/application/model/GameLevel';
import TileMap from './TileMap';
import Game from 'Frontend/generated/com/example/application/model/Game';
import Tile from 'Frontend/generated/com/example/application/model/Tile';

const GameView = (props: any) => {
    
    const [selectedLevel, setSelectedLevel] = useState<GameLevel>();
    const [isGameStarted, setIsGameStarted] = useState(false);
    const [game, setGame] = useState<Game | null>(null);

    useEffect(() => {
        fetchExistingGame();
    },[]);
    const startTheGame = () => {
        
        GameEndpoint.createGame(selectedLevel)
        .then(response => {
            if(response && response.id){
                const url = new URL(window.location.href);
                url.searchParams.set('id', response.id);
                window.history.replaceState(null, "", url); // or pushState
                setGame(response);
            }
            
        }).catch(error => {
            console.error(error);
        })
    };

    const fetchExistingGame = () => {
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        if(id){
            GameEndpoint.getCurrent(id)
            .then(response => {
                if(response !== null && response !== undefined){
                    setGame(response);
                    setSelectedLevel(response.level);
                    setIsGameStarted(true);
                }
            }).catch(error => {
    
            });
        }
        
    }

    const updateGame = (tile1: Tile, tile2: Tile) => {
        if(!game){
            return;
        }
        const tiles = game.tiles;
        if(!tiles){
            return;
        }
        const foundTile1 = tiles.find(f => f && f.id === tile1.id);
        const foundTile2 = tiles.find(f => f && f.id === tile2.id);
        if(foundTile1 && foundTile2){
            foundTile1.visible = true;
            foundTile2.visible = true;
        }
        GameEndpoint.update(game).then(res => console.log('Game is updated'));
    }
    const onTileClick = () =>{
        if(!game){
            return;
        }
        game.clickCount = game?.clickCount + 1;
        setGame(game)
        GameEndpoint.update(game)
        .then(() => {console.log("Game is updated"); });
    }
    return (
        <>
            <div className={'game-view'}>
                <div className="toolbar">
                    <RadioGroup label="Select level" theme="horizontal" onValueChanged={(e: any) => setSelectedLevel(e.target.value)}>
                        <RadioButton value="EASY" label="Easy" />
                        <RadioButton value="MEDIUM" label="Medium" />
                        <RadioButton value="HARD" label="Hard" />
                    </RadioGroup>
                    <Button onClick={startTheGame}> {!isGameStarted ? "Start" : "Restart"} </Button>
                    {game && <span>Total Click Count : {game.clickCount}</span> }
                </div>
                <hr/>
                <div className="tile-map-container">
                    {game !== null ? 
                        <TileMap tiles={game.tiles} matrixSize={game.matrixSize} userMatchedTwoTiles={updateGame} onTileClick={onTileClick} />
                    : null}
                </div>
            </div>
            
            
        </>
    )

};
export default GameView;