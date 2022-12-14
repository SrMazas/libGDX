package com.gabriel.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.gabriel.HuntedGame;
import com.gabriel.Param;
import com.gabriel.Utility;
import com.gabriel.entity.Tile;
import com.gabriel.screen.GameScreen;
import com.gabriel.world.WorldGen;

import java.util.HashSet;

/**
 * Created by Tim on 08/01/2017.
 */
public class GameState {

  public boolean fullscreen = false;
  public int frame = 0;
  public boolean[] switchStatus = new boolean[Param.KEY_ROOMS + 1];
  public float[] progress = new float[Param.KEY_ROOMS + 1];
  public float aiCooldown = 0;
  public float scoreDelay = 0f;
  public boolean webEffect = false;
  public GameScreen theGameScreen = null;
  public HuntedGame game = null;
  public HashSet<Tile> waypoints; // Known good AI destinations
  public boolean userControl;
  public boolean movementOn;

  public boolean gameIsWon = false;
  public boolean showingScore = false;

  private boolean chaseOn = false;
  private float chaseVolume = 100;

  private boolean unlockSound = false;

  private static GameState ourInstance;

  public static GameState getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new GameState(); }

  private GameState() {

  }

  public void toggleFullScreen() {
    GameState.getInstance().fullscreen = !GameState.getInstance().fullscreen;
    if (GameState.getInstance().fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    else Gdx.graphics.setWindowedMode(Param.DISPLAY_X, Param.DISPLAY_Y);
  }

  public void reset() {
    resetInternal();
    theGameScreen.reset();
    WorldGen.getInstance().generateWorld();
    userControl = true;
  }

  private void resetInternal() {
    for (int i = 0; i < Param.KEY_ROOMS + 1; ++i) {
      progress[i] = 0;
      switchStatus[i] = HuntedGame.debug; // Should be false normally
    }
    aiCooldown = 0;
    webEffect = false;
    frame = 0;
    scoreDelay = 0f;
    userControl = false;
    gameIsWon = false;
    showingScore = false;
    waypoints = new HashSet<Tile>();
  }

  private void doSwitchLogic(int i, float delta) {
    if (switchStatus[i]) {
      progress[i] += (i == 0 ? delta/2f : delta); // Twice as long to unlock final
      if (progress[i] >= Param.SWITCH_TIME) { // finished unlocking
        progress[i] = Param.SWITCH_TIME;
        unlockSound = false;
        Sounds.getInstance().unlockStop();
      } else if (!unlockSound) {
        unlockSound = true;
        Sounds.getInstance().unlock();
      }
    } else if (progress[i] > 0) {
      progress[i] -= delta;
      if (progress[i] < 0) progress[i] = 0;
      if (unlockSound) {
        unlockSound = false;
        Sounds.getInstance().unlockStop();
      }
    }
  }

  public void updatePhysics(float delta) {

    // Update cooldown
    if (aiCooldown > 0) aiCooldown -= delta;

    // Timer for end of game treasure
    if (gameIsWon && !showingScore) {
      scoreDelay += delta;
      if (scoreDelay > 4f) {
        showingScore = true;
        Sprites.getInstance().treasurePile.setPosition(-2 * Param.TILE_SIZE, -Param.TILE_SIZE);
        //for (int i=0; i<100; ++i) Sprites.getInstance().treasurePile.addToPile(Utility.r.nextInt(Param.N_TREASURE));
      }
    }

    // Update logic for key switches
    boolean allKeys = true;
    Vector2 playerPos = Sprites.getInstance().getPlayer().getBody().getPosition();
    float minDist = 999f;
    int minSwitch = 0;
    for (int i = 1; i < Param.KEY_ROOMS + 1; ++i) {
      float dist = Sprites.getInstance().keySwitch[i].getBody().getPosition().dst(playerPos);
      if (dist < minDist) {
        minDist = dist;
        minSwitch = i;
      }
      if (progress[i] < Param.SWITCH_TIME) {
        allKeys = false;
        doSwitchLogic(i, delta);
      }
    }
    // Sounds
    if (minDist <= Param.BIGBAD_SENSE_DISTANCE && progress[minSwitch] >= Param.SWITCH_TIME) {
      Sounds.getInstance().machineNoise((Param.BIGBAD_SENSE_DISTANCE - minDist) / Param.BIGBAD_SENSE_DISTANCE);
    } else {
      Sounds.getInstance().machineNoise(0);
    }
    // Compass
    if (Sprites.getInstance().getBigBad().distanceFromPlayer <= 2f*Param.BIGBAD_SENSE_DISTANCE) {
      float pointAngle = Utility.getTargetAngle(Sprites.getInstance().getBigBad().getBody().getPosition(),
        Sprites.getInstance().getPlayer().getBody().getPosition());
      Sprites.getInstance().compass.setDesiredArrow(pointAngle, 4, 0.75f, .1f);
    } else if (minDist <= 2f*Param.BIGBAD_SENSE_DISTANCE) {
      float pointAngle = Utility.getTargetAngle(Sprites.getInstance().keySwitch[minSwitch].getBody().getPosition(),
        Sprites.getInstance().getPlayer().getBody().getPosition());
      Sprites.getInstance().compass.setDesiredArrow(pointAngle, minSwitch, 0.1f, 2f);
    } else {
      Sprites.getInstance().compass.setDesiredArrow((float)Math.PI/2f, 0, 0.1f, 2f);
    }

    // Update logic for exit switch
    if (allKeys && progress[0] < Param.SWITCH_TIME) doSwitchLogic(0, delta);

    musicLogic(delta);
  }

  private void musicLogic(float delta) {

    if (!chaseOn && Sprites.getInstance().getBigBad().musicSting) {
      chaseOn = true;
      Sounds.getInstance().startChaseMusic();
    }
    if (chaseOn) {
      if (!Sprites.getInstance().getBigBad().musicSting) chaseVolume -= delta;
      else chaseVolume = Param.CHASE_VOL_MAX;

      if (chaseVolume <= 0) chaseVolume = 0;
      Sounds.getInstance().chaseVolume(chaseVolume/Param.CHASE_VOL_MAX);
      if (chaseVolume == 0) {
        Sounds.getInstance().endChase();
        chaseOn = false;
      }
    }
  }

  public void dispose() {
    ourInstance = null;
  }

}
