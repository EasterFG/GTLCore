package org.gtlcore.gtlcore.api.recipe;

public enum RecipeCacheStrategy {

    /**
     * Full caching - cache everything including ME pattern machine internals
     * Used for normal recipe processing
     */
    FULL_CACHE(true, true),

    /**
     * Partial caching - cache recipe-> handlePart mapping but not ME pattern internals
     * Used for dummy recipes that need parallel calculation but shouldn't pollute ME recipe queue
     */
    HANDLE_PART_CACHE_ONLY(true, false),

    /**
     * No caching - skip all caching operations
     * Used for temporary validation recipes
     */
    NO_CACHE(false, false);

    public final boolean cacheToHandlePartMap;  // Cache recipe->handler in machine
    public final boolean cacheToMEInternal;  // Cache recipe in MEPatternPartMachine internal

    RecipeCacheStrategy(boolean cacheToHandlePartMap, boolean cacheToMEInternal) {
        this.cacheToHandlePartMap = cacheToHandlePartMap;
        this.cacheToMEInternal = cacheToMEInternal;
    }
}
