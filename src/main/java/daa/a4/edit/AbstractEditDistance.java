package daa.a4.edit;

import java.util.Objects;

/** Holds the two counters and small helpers shared by the three versions. */
abstract class AbstractEditDistance implements EditDistance {

    protected long steps;
    protected long allocatedCells;

    protected final void start(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        steps = 0;
        allocatedCells = 0;
    }

    protected static int min3(int x, int y, int z) {
        return Math.min(x, Math.min(y, z));
    }

    @Override
    public long steps() {
        return steps;
    }

    @Override
    public long allocatedCells() {
        return allocatedCells;
    }
}
