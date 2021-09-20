package io.github.pulsebeat02.ezmediacore.random;

import java.io.Serial;
import java.lang.ref.WeakReference;
import java.util.Random;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A random with a delegate, preventing {@link Random#setSeed(long)} and locked to be used by a
 * single thread.
 */
public final class AssertingRandom extends Random {

  @Serial
  private static final long serialVersionUID = -1552213382473062718L;
  private final Random delegate;
  private final WeakReference<Thread> ownerRef;
  private final String ownerName;
  private final StackTraceElement[] allocationStack;

  /**
   * Track out-of-context use of this {@link Random} instance. This introduces memory barriers and
   * scheduling side-effects but there's no other way to do it in any other way and sharing randoms
   * across threads or test cases is very bad and worth tracking.
   */
  private volatile boolean valid = true;

  /** Enable paranoid mode when assertions are enabled. */
  private static final boolean assertionsEnabled = AssertingRandom.class.desiredAssertionStatus();

  /**
   * Creates an instance to be used by <code>owner</code> thread and delegating to <code>delegate
   * </code> until {@link #destroy()}ed.
   */
  public AssertingRandom(final Thread owner, final Random delegate) {
    // Must be here, the only Random constructor. Has side-effects on setSeed, see below.
    super(0);

    this.delegate = delegate;
    this.ownerRef = new WeakReference<>(owner);
    this.ownerName = owner.toString();
    this.allocationStack = Thread.currentThread().getStackTrace();
  }

  @Override
  protected int next(final int bits) {
    throw new RuntimeException("Shouldn't be reachable.");
  }

  @Override
  public boolean nextBoolean() {
    this.checkValid();
    return this.delegate.nextBoolean();
  }

  @Override
  public void nextBytes(final byte[] bytes) {
    this.checkValid();
    this.delegate.nextBytes(bytes);
  }

  @Override
  public double nextDouble() {
    this.checkValid();
    return this.delegate.nextDouble();
  }

  @Override
  public float nextFloat() {
    this.checkValid();
    return this.delegate.nextFloat();
  }

  @Override
  public double nextGaussian() {
    this.checkValid();
    return this.delegate.nextGaussian();
  }

  @Override
  public int nextInt() {
    this.checkValid();
    return this.delegate.nextInt();
  }

  @Override
  public int nextInt(final int n) {
    this.checkValid();
    return this.delegate.nextInt(n);
  }

  @Override
  public long nextLong() {
    this.checkValid();
    return this.delegate.nextLong();
  }

  @Override
  public void setSeed(final long seed) {
    // This is an interesting case of observing uninitialized object from an instance method
    // (this method is called from the superclass constructor).
    if (seed == 0 && this.delegate == null) {
      return;
    }

    throw noSetSeed();
  }

  @Override
  public String toString() {
    this.checkValid();
    return this.delegate.toString();
  }

  @Override
  public boolean equals(final Object obj) {
    this.checkValid();
    return this.delegate.equals(obj);
  }

  @Override
  public int hashCode() {
    this.checkValid();
    return this.delegate.hashCode();
  }

  /** This object will no longer be usable after this method is called. */
  public void destroy() {
    this.valid = false;
  }

  /* */
  private void checkValid() {
    // Fastpath if assertions are disabled.
    if (!isVerifying()) {
      return;
    }

    if (!this.valid) {
      throw new IllegalStateException(
          "This Random instance has been invalidated and "
              + "is probably used out of its allowed context (test or suite).");
    }

    final Thread owner = this.ownerRef.get();
    if (owner == null || Thread.currentThread() != owner) {
      final Throwable allocationEx =
          new Throwable(
              "Original allocation stack for this Random ("
                  + "allocated by "
                  + this.ownerName
                  + ")");
      allocationEx.setStackTrace(this.allocationStack);
      throw new IllegalStateException(
          "This Random was created for/by another thread ("
              + this.ownerName
              + ")."
              + " Random instances must not be shared (acquire per-thread). Current thread: "
              + Thread.currentThread(),
          allocationEx);
    }
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    this.checkValid();
    throw new CloneNotSupportedException("Don't clone test Randoms.");
  }

  /**
   * @return Return <code>true</code> if this class is verifying sharing and lifecycle assertions.
   * @see "https://github.com/randomizedtesting/randomizedtesting/issues/234"
   */
  public static boolean isVerifying() {
    return assertionsEnabled;
  }

  @Contract(value = " -> new", pure = true)
  static @NotNull RuntimeException noSetSeed() {
    return new RuntimeException(
        "Class prevents changing the seed of its random generators to assure repeatability"
            + " of tests. If you need a mutable instance of Random, create a new (local) instance,"
            + " preferably with the initial seed aquired from this Random instance.");
  }

  // Overriding this has side effects on the GC; let's not be paranoid.
  /* protected void finalize() throws Throwable { super.finalize(); } */
}
