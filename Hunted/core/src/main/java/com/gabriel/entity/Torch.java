package com.gabriel.entity;

import box2dLight.ConeLight;
import box2dLight.PositionalLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.gabriel.Param;
import com.gabriel.manager.Sounds;
import com.gabriel.manager.Sprites;
import com.gabriel.manager.Physics;

/**
 * Created by Tim on 02/01/2017.
 */
public class Torch extends EntityBase {

  public boolean isOn = false;
  private boolean isPartial;
  private Vector2 lightPos;
  private Vector2 lightEffectPos;
  private boolean needsSecondLight;
  private float torchAngle;

  public int nLight = 0;
  public PositionalLight[] torchLight = {null,null};
  public float[] torchDistanceRef = {0,0};
  private float torchDistanceCurrent;
//  private float torchDistanceTarget;
  Color primaryTorchType;

  // TODO static torches

  public Torch(int x, int y) {
    super (x,y);
  }

  public Torch(float x, float y, float lX, float lY, float sX, float sY, boolean partial, float a, Color c) {
    // x and y are main directional light
    // lX and lY are particle effect and effect light
    // sX and sY are the location of the sensor to turn the light on
    super((int)x, (int)y);
    torchAngle = a * (float)(180f/Math.PI); // Degrees needed below - WTF?
    primaryTorchType = c;
    isPartial = partial;
    addTorchSensor(sX,sY);
    lightEffectPos = new Vector2(lX, lY);
    lightPos = new Vector2(x,y);
    // If the actual light is not in the same position as its effect - or the actual light is partial, need another
    needsSecondLight = (isPartial || (body != null && body.getPosition().dst(lightEffectPos) < 1e-4));
  }

  public void addTorchSensor(float x, float y) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody;
    bodyDef.position.set(x, y);
    body = Physics.getInstance().world.createBody(bodyDef);
    body.setUserData(this);
    bodyDef.type = BodyDef.BodyType.StaticBody;
    CircleShape circleShape = new CircleShape();
    circleShape.setRadius(1f);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = circleShape;
    fixtureDef.filter.categoryBits = Param.SENSOR_ENTITY; // I am a
    fixtureDef.filter.maskBits = Param.SENSOR_COLLIDES; // I collide with
    fixtureDef.isSensor = true;
    body.createFixture(fixtureDef);
    circleShape.dispose();
  }

  public void addTorchToEntity(int rays, float range, float distance, Color c, boolean xRay, Vector2 loc) {
    torchLight[nLight] = new ConeLight(Physics.getInstance().rayHandler,
      rays,
      c,
      distance,
      loc != null ? loc.x : 0f, loc != null ? loc.y : 0f, torchAngle, range); // Degrees? WTF?
    torchDistanceRef[nLight] = distance; // Should we need to reset
    torchLight[nLight].setContactFilter(Param.TORCH_ENTITY, (short)0, Param.TORCH_COLLIDES); // I am a, 0, I collide with
    torchLight[nLight].setXray(xRay);
    ++nLight;
  }

  public void modTorch(float percent) {
    torchLight[0].setDistance( torchDistanceRef[0] * percent );
  }

  public void flicker() {
    //if (Math.abs(torchDistanceCurrent - torchDistanceTarget) < 1e-3) {
    //  torchDistanceTarget = torchDistanceRef + ((float)Utility.r.nextGaussian() * Param.TORCH_FLICKER);
    //}
    torchDistanceCurrent = torchDistanceRef[0];// rchDistanceCurrent + (0.1f * (torchDistanceTarget - torchDistanceCurrent));
    torchLight[0].setDistance(torchDistanceCurrent);
  }

  public void doCollision(boolean doSound) {
    if (isOn) return;
    isOn = true;
    if (doSound) Sounds.getInstance().ignite();
    float range = isPartial ? 90f : 180f;
    float distance = Param.WALL_TORCH_STRENGTH;
    addTorchToEntity(Param.RAYS, range, distance,  primaryTorchType, false, lightPos);
    torchLight[0].setStaticLight(true);
    Physics.getInstance().litTorches.add(this);
    Sprites.getInstance().addFlameEffect(lightEffectPos);
    if (needsSecondLight) {
      addTorchToEntity(Param.RAYS_SMALL, 180f, Param.SMALL_TORCH_STRENGTH, Param.WALL_FLAME_SPOT,  true, lightEffectPos);
      torchLight[1].setStaticLight(true);
    }
  }

}
