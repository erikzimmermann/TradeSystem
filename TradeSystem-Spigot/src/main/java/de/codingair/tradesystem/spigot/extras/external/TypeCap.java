package de.codingair.tradesystem.spigot.extras.external;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.function.Function;

public class TypeCap {
    private final Class<? extends Number> type;
    private final Function<BigDecimal, BigDecimal> mapper;

    public TypeCap(@NotNull Class<? extends Number> type, @NotNull Function<BigDecimal, BigDecimal> mapper) {
        this.type = type;
        this.mapper = mapper;
    }

    @NotNull
    public BigDecimal apply(@NotNull BigDecimal value) {
        return mapper.apply(value);
    }

    @NotNull
    public Class<? extends Number> getType() {
        return type;
    }

    public boolean isByte() {
        return type == Byte.class;
    }

    public boolean isShort() {
        return type == Short.class;
    }

    public boolean isInteger() {
        return type == Integer.class;
    }

    public boolean isLong() {
        return type == Long.class;
    }

    public boolean isNumber() {
        return isByte() || isShort() || isInteger() || isLong();
    }

    public boolean isFloat() {
        return type == Float.class;
    }

    public boolean isDouble() {
        return type == Double.class;
    }

    public boolean isDecimal() {
        return isFloat() || isDouble();
    }

    public boolean isBigDecimal() {
        return type == BigDecimal.class;
    }
}
