@ThreadSafe
public class FastRandom implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    // Xorshift64* constants
    private long seed;

    // For gaussian
    private boolean hasNextGaussian = false;
    private double nextGaussian;

    /**
     * Creates a new generator seeded by System.nanoTime().
     */
    public FastRandom() {
        this(System.nanoTime());
    }

    /**
     * Creates a new generator with the given seed.
     */
    public FastRandom(long seed) {
        setSeed(seed);
    }

    /**
     * Returns the current seed (state).
     */
    public synchronized long getSeed() {
        return seed;
    }

    /**
     * Sets a new seed. Reset gaussian cache.
     */
    public synchronized void setSeed(long seed) {
        if (seed == 0) seed = 0xdeadbeefcafebabeL;
        this.seed = seed;
        hasNextGaussian = false;
    }

    /**
     * Return a clone with identical state.
     */
    @Override
    public synchronized FastRandom clone() {
        FastRandom c = new FastRandom(seed);
        c.hasNextGaussian = this.hasNextGaussian;
        c.nextGaussian = this.nextGaussian;
        return c;
    }

    /**
     * Core xorshift64* step.
     */
    protected synchronized long nextLongRaw() {
        long x = seed;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        seed = x;
        return x;
    }

    /**
     * Returns the next nbits of randomness.
     */
    protected int next(int nbits) {
        long x = nextLongRaw();
        return (int)(x & ((1L<<nbits)-1));
    }

    public int nextInt() {
        return next(32);
    }

    public int nextInt(int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        // reject-sampling to avoid bias
        int m = bound - 1;
        if ((bound & m) == 0) {
            return (int)((bound * (long)next(31)) >> 31);
        }
        int u;
        do {
            u = next(31);
        } while (u - (u % bound) + m < 0);
        return u % bound;
    }

    public long nextLong() {
        return nextLongRaw();
    }

    public long nextLong(long bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        long r = nextLongRaw() >>> 1;
        long m = bound - 1;
        if ((bound & m) == 0L) {
            return (r * bound) >> 63;
        }
        long u = r;
        while (u - (u % bound) + m < 0) {
            u = nextLongRaw() >>> 1;
        }
        return u % bound;
    }

    public float nextFloat() {
        return next(24) / ((float)(1 << 24));
    }

    public double nextDouble() {
        long high = ((long)next(26)) << 27;
        long low  = next(27);
        return (high + low) / (double)(1L << 53);
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    /**
     * Box-Muller transform for gaussian.
     */
    public synchronized double nextGaussian() {
        if (hasNextGaussian) {
            hasNextGaussian = false;
            return nextGaussian;
        }
        double u1, u2, s;
        do {
            u1 = 2.0 * nextDouble() - 1.0;
            u2 = 2.0 * nextDouble() - 1.0;
            s = u1*u1 + u2*u2;
        } while (s >= 1.0 || s == 0.0);
        double multiplier = Math.sqrt(-2.0 * Math.log(s) / s);
        nextGaussian = u2 * multiplier;
        hasNextGaussian = true;
        return u1 * multiplier;
    }

    /**
     * Advance the internal state by n steps (for jump-ahead).
     */
    public synchronized void skip(long steps) {
        for (long i = 0; i < steps; i++) {
            nextLongRaw();
        }
    }
}
