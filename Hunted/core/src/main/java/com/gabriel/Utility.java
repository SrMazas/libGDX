package com.gabriel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gabriel.manager.Textures;

import java.util.Random;

/**
 * Created by Tim on 31/12/2016.
 */
public class Utility {

  public static Random r = new Random();

  public static Integer xyToID(int x, int y) {
    assert (y < 2048);
    return (2048 * x) + y;
  }

  static public boolean prob(int chanceOfPass) {
    return (r.nextInt(100) + 1) <= chanceOfPass;
  }

  static public boolean prob(float chanceOfPass) {
    return (r.nextFloat() <= chanceOfPass);
  }

  static public boolean getOutOfBound(int x, int y) {
    return x < 0 || y < 0 || x >= Param.TILE_X || y >= Param.TILE_Y;
  }

  static public float getTargetAngle(float targetX, float targetY, Vector2 from) {
    float targetAngle = (float) Math.atan2(targetY - from.y, targetX - from.x);
    if (targetAngle < 0) targetAngle += (float)2*Math.PI;
    return targetAngle;
  }

  static public float getTargetAngle(Vector2 target, Vector2 from) {
    float targetAngle = (float) Math.atan2(target.y - from.y, target.x - from.x);
    if (targetAngle < 0) targetAngle += (float)2*Math.PI;
    return targetAngle;
  }

  static public float clampSignedAngle(float angle) {
    return angle + (float)((angle > Math.PI) ? -2*Math.PI : (angle < -Math.PI) ? 2*Math.PI : 0);
  }

  static public ParticleEffect getNewFlameEffect() {
    if (!HuntedGame.particles) return null;
    ParticleEffect effect = new ParticleEffect();
    effect.load(Gdx.files.internal("flame.p"), Textures.getInstance().getAtlas());
    effect.scaleEffect(0.2f);
    effect.start();
    return effect;
  }
}
