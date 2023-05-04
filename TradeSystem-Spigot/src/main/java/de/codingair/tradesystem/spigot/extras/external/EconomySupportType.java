package de.codingair.tradesystem.spigot.extras.external;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

/**
 * A helper class for {@link EconomyIcon#getMaxSupportedValue()}.
 */
public class EconomySupportType {
    private static final BigDecimal MAX_BYTE = BigDecimal.valueOf(Byte.MAX_VALUE);
    private static final BigDecimal MIN_BYTE = BigDecimal.valueOf(Byte.MIN_VALUE);
    private static final BigDecimal MAX_SHORT = BigDecimal.valueOf(Short.MAX_VALUE);
    private static final BigDecimal MIN_SHORT = BigDecimal.valueOf(Short.MIN_VALUE);
    private static final BigDecimal MAX_INTEGER = BigDecimal.valueOf(Integer.MAX_VALUE);
    private static final BigDecimal MIN_INTEGER = BigDecimal.valueOf(Integer.MIN_VALUE);
    private static final BigDecimal MAX_LONG = BigDecimal.valueOf(Long.MAX_VALUE);
    private static final BigDecimal MIN_LONG = BigDecimal.valueOf(Long.MIN_VALUE);

    private static final BigDecimal MAX_FLOAT = BigDecimal.valueOf(Float.MAX_VALUE);
    private static final BigDecimal MIN_FLOAT = BigDecimal.valueOf(Float.MIN_VALUE);
    private static final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);
    private static final BigDecimal MIN_DOUBLE = BigDecimal.valueOf(Double.MIN_VALUE);

    public static final TypeCap BYTE = new TypeCap(Byte.class, d -> d.min(MAX_BYTE).max(MIN_BYTE).setScale(0, RoundingMode.DOWN));
    public static final TypeCap SHORT = new TypeCap(Short.class, d -> d.min(MAX_SHORT).max(MIN_SHORT).setScale(0, RoundingMode.DOWN));
    public static final TypeCap INTEGER = new TypeCap(Integer.class, d -> d.min(MAX_INTEGER).max(MIN_INTEGER).setScale(0, RoundingMode.DOWN));
    public static final TypeCap LONG = new TypeCap(Long.class, d -> d.min(MAX_LONG).max(MIN_LONG).setScale(0, RoundingMode.DOWN));

    public static final TypeCap FLOAT = new TypeCap(Float.class, EconomySupportType::toFloat);
    public static final TypeCap DOUBLE = new TypeCap(Double.class, EconomySupportType::toDouble);

    public static final TypeCap BIG_DECIMAL = new TypeCap(BigDecimal.class, d -> d);

    private EconomySupportType() {
    }

    @NotNull
    private static BigDecimal toFloat(@NotNull BigDecimal value) {
        if (Double.isInfinite(value.floatValue())) return value.floatValue() < 0 ? MIN_FLOAT : MAX_FLOAT;
        else return BigDecimal.valueOf(value.floatValue());
    }

    @NotNull
    private static BigDecimal toDouble(@NotNull BigDecimal value) {
        if (Double.isInfinite(value.doubleValue())) return value.doubleValue() < 0 ? MIN_DOUBLE : MAX_DOUBLE;
        else return BigDecimal.valueOf(value.doubleValue());
    }
}
