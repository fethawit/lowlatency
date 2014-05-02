package featherdev.mgoa.screens;

import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import featherdev.lwbd.Beat;
import featherdev.lwbd.BeatDetector;
import featherdev.lwbd.BeatsProcessor;
import featherdev.lwbd.LwbdDecoder;
import featherdev.lwbd.decoders.GdxMp3Decoder;
import featherdev.lwbd.decoders.GdxOggDecoder;
import featherdev.mgoa.Mgoa;
import featherdev.mgoa.subsystems.MusicPlayer;

/**
 * music processing & map gen
 */

public class LoadScreen extends UiScreen {
	
	class LoadingThread extends Thread {

		@Override
		public void run() {

			LinkedList<Beat> rawbeats = null;
			LinkedList<Beat> beatmap  = null;
			
			FileHandle audiofile = game.song.getHandle();
			LwbdDecoder decoder = null;
			
			String extension = audiofile.extension().toLowerCase();
			
			System.out.println("[+] Initializing audio analysis system");
			if (extension.contains("mp3"))
				decoder = new GdxMp3Decoder(audiofile);
			else if (extension.contains("ogg"))
				decoder = new GdxOggDecoder(audiofile);
			else
				onLoadComplete(false);
			
			
			rawbeats = BeatDetector.detectBeats(decoder, BeatDetector.SENSITIVITY_AGGRESSIVE);
			beatmap  = BeatsProcessor.thinBeats(rawbeats, 120);
			beatmap  = BeatsProcessor.dropWeakBeats(beatmap, game.difficulty.minBeatEnergy);
			
			game.rawmap = rawbeats;
			game.beatmap = beatmap;
			System.out.println("[+] Audio analysis complete.");
			
			onLoadComplete(true);
		}
	}

	Label status;
	LoadingThread loadThread;
	enum LoadScreenState { LOADING, DYING }
	LoadScreenState state;
	
	public LoadScreen(){
		game = Mgoa.instance();
		loadThread = new LoadingThread();
		
		stage = new Stage();
		Table root = new Table();
		status = new Label("Loading . . .", game.skin);	
		root.add(status).left();
		root.setFillParent(true);
		stage.addActor(root);
		stage.getRoot().getColor().a = 0;
		state = LoadScreenState.LOADING;
		
		Gdx.input.setInputProcessor(stage);
	}
	
	private void onLoadComplete(boolean success){
		if (success){
			status.setText("Done.");
			stage.addAction(Actions.sequence(Actions.delay(1), Actions.fadeOut(1f)));
			state = LoadScreenState.DYING;
		}
		else
			System.out.println("Load failed.");
	}
	
	public void render(float delta) {
		
		switch (state){
		case LOADING:
			// update
			game.lights.update(delta);
			stage.act(delta);

			// draw
			game.lights.draw(null);
			stage.draw();
			break;
		case DYING:
			// update
			game.lights.update(delta);
			stage.act(delta);

			// draw
			game.lights.draw(null);
			stage.draw();
			
			if (stage.getRoot().getColor().a == 0){
				MusicPlayer.instance().pause();
				if (game.visualizer)
					game.setScreen(new VisualizerScreen());
				else
					game.setScreen(new ReadyScreen());
				dispose();
			}
			break;
		}
	}
	public void show() {
		loadThread.start();
		super.show();
	}
}
