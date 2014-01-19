package com.sawtoothdev.mgoa.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.sawtoothdev.mgoa.MainGame;
import com.sawtoothdev.mgoa.game.GameWorld;
import com.sawtoothdev.mgoa.game.GameWorld.WorldState;

/**
 * What are we here for, anyway?
 * 
 * @author albatross
 */

public class GameScreen implements Screen {

	private enum GameScreenState { INITIALIZED, RUNNING, DONE, PAUSED };
	private final GameWorld gameWorld;
	private GameScreenState state;
	
	public GameScreen() {
		gameWorld = new GameWorld();

		// IT BEGINS
		state = GameScreenState.INITIALIZED;
	}

	@Override
	public void render(float delta) {

		switch (state) {

		case INITIALIZED:
			break;
		case RUNNING:
			gameWorld.update(delta);
			gameWorld.draw(MainGame.gfx.sysSB);

			// end condition
			if (gameWorld.getState() == WorldState.FINISHED)
				this.state = GameScreenState.DONE;
			break;
		case DONE:
			MainGame.game.setScreen(new FinishScreen());
			break;
		case PAUSED:
			Gdx.app.log("gamescreen", "paused update");
		default:
			break;
		}
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void show() {
		
		if (state == GameScreenState.INITIALIZED) {
			gameWorld.start();
			state = GameScreenState.RUNNING;
		}
		else if (state == GameScreenState.PAUSED)
			resume();
	}

	@Override
	public void hide() {

	}

	@Override
	public void pause() {
		Gdx.app.log("game screen", "pausing");
		gameWorld.pause();
		this.state = GameScreenState.PAUSED;
		MainGame.game.setScreen(new PausedScreen(this));
	}

	@Override
	public void resume() {
		Gdx.app.log("game screen", "resuming");
		gameWorld.unpause();
		this.state = GameScreenState.RUNNING;
	}

	@Override
	public void dispose() {

	}

}
