package hu.bme.mit.spaceship;

import java.util.Random;

/**
* Class storing and managing the torpedoes of a ship
*
* (Deliberately contains bugs.)
*/
public class TorpedoStore {

  // rate of failing to fire torpedoes [0.0, 1.0]
  private double FAILURE_RATE = 0.0; //NOSONAR

  private int torpedoCount = 0;

  private final Random random = new Random();

  public TorpedoStore(int numberOfTorpedoes){
    this.torpedoCount = numberOfTorpedoes;

    // update failure rate if it was specified in an environment variable
    String failureEnv = System.getenv("IVT_RATE");
    if (failureEnv != null){
      try {
        FAILURE_RATE = Double.parseDouble(failureEnv);
      } catch (NumberFormatException nfe) {
        FAILURE_RATE = 0.0;
      }
    }
  }

  public TorpedoStore(int numberOfTorpedos, double failureRate) {
    this(numberOfTorpedos);
    this.FAILURE_RATE = failureRate;
  }

  public boolean fire(int numberOfTorpedoes){
    if(numberOfTorpedoes < 1 || numberOfTorpedoes > this.torpedoCount){
      throw new IllegalArgumentException("The number of torpedoes to fire must be at least one and cannot be more than the currently stored number of torpedoes.");
    }

    boolean success = false;

    // simulate random overheating of the launcher bay which prevents firing
    double r = random.nextDouble();

    if (r >= FAILURE_RATE) {
      // successful firing
      this.torpedoCount -= numberOfTorpedoes;
      success = true;
    } else {
      // simulated failure
      success = false;
    }

    return success;
  }

  public boolean isEmpty(){
    return this.torpedoCount <= 0;
  }

  public int getTorpedoCount() {
    return this.torpedoCount;
  }
}
