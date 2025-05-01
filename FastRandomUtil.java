public final class FastRandomUtil {
    private static final ThreadLocal<FastRandom> LOCAL =
            ThreadLocal.withInitial(FastRandom::new);


    public static FastRandom get() {
        return LOCAL.get();
    }

    public static int nextInt() {
        return get().nextInt();
    }

    public static int nextInt(int bound) {
        return get().nextInt(bound);
    }

    public static long nextLong() {
        return get().nextLong();
    }

    public static long nextLong(long bound) {
        return get().nextLong(bound);
    }

    public static float nextFloat() {
        return get().nextFloat();
    }

    public static double nextDouble() {
        return get().nextDouble();
    }

    public static boolean nextBoolean() {
        return get().nextBoolean();
    }

    public static double nextGaussian() {
        return get().nextGaussian();
    }

    /**
     * Shuffles a List in-place.
     */
    public static <T> void shuffle(List<T> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = nextInt(i + 1);
            Collections.swap(list, i, j);
        }
    }

    /**
     * Picks one element at random.
     */
    public static <T> T choose(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException("List is empty");
        return list.get(nextInt(list.size()));
    }

    /**
     * Reseeds the thread-local generator.
     */
    public static void reseed(long seed) {
        get().setSeed(seed);
    }
}
