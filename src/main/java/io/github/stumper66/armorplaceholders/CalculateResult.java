package io.github.stumper66.armorplaceholders;

import org.jetbrains.annotations.NotNull;

public class CalculateResult {
    public CalculateResult(final double result, final @NotNull String info){
        this.result = result;
        this.info = info;
    }
    public final double result;
    public final @NotNull String info;
}
