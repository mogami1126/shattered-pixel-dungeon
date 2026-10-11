package com.shatteredpixel.shatteredpixeldungeon.mmo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Locale;

/** Non-negative scientific game number: 15 significant digits, unbounded decimal exponent.
 * Health ratios and boss stage coordinates stay separate from this magnitude. */
public final class GameNumber implements Comparable<GameNumber> {
    public static final GameNumber ZERO = new GameNumber(0, BigInteger.ZERO);
    public static final GameNumber ONE = new GameNumber(1, BigInteger.ZERO);
    private final double mantissa;
    private final BigInteger exponent;

    private GameNumber(double m, BigInteger e) {
        if (!Double.isFinite(m) || m < 0) throw new IllegalArgumentException("Invalid magnitude");
        if (m == 0) { mantissa = 0; exponent = BigInteger.ZERO; return; }
        int shift = (int)Math.floor(Math.log10(m));
        double normalized = m / Math.pow(10, shift);
        // Subnormal doubles cannot be normalized with an underflowing power.
        if (!Double.isFinite(normalized)) { normalized = m * 1e300; shift = -300;
            int s = (int)Math.floor(Math.log10(normalized)); normalized /= Math.pow(10, s); shift += s; }
        if (normalized >= 10) { normalized /= 10; shift++; }
        mantissa = normalized; exponent = e.add(BigInteger.valueOf(shift));
    }
    public static GameNumber of(double value) { return new GameNumber(value, BigInteger.ZERO); }
    public static GameNumber of(BigInteger value) {
        if (value.signum() <= 0) return ZERO;
        String s = value.toString(); int take = Math.min(15, s.length());
        return new GameNumber(Double.parseDouble(s.substring(0, take)) / Math.pow(10, take-1), BigInteger.valueOf(s.length()-1));
    }
    public static GameNumber power(double base, BigInteger power) {
        if (!(base > 0) || !Double.isFinite(base)) throw new IllegalArgumentException("Invalid base");
        BigDecimal log = BigDecimal.valueOf(Math.log10(base)).multiply(new BigDecimal(power));
        BigInteger whole = log.setScale(0, java.math.RoundingMode.FLOOR).toBigIntegerExact();
        return new GameNumber(Math.pow(10, log.subtract(new BigDecimal(whole)).doubleValue()), whole);
    }
    public GameNumber times(double n) { return times(of(n)); }
    public GameNumber times(GameNumber n) { return new GameNumber(mantissa*n.mantissa, exponent.add(n.exponent)); }
    public GameNumber divide(GameNumber n) {
        if (n.mantissa == 0) throw new ArithmeticException("Division by zero");
        return new GameNumber(mantissa/n.mantissa, exponent.subtract(n.exponent));
    }
    public GameNumber plus(GameNumber n) {
        if (mantissa == 0) return n; if (n.mantissa == 0) return this;
        GameNumber a = compareTo(n) >= 0 ? this : n, b = a == this ? n : this;
        BigInteger d = a.exponent.subtract(b.exponent);
        if (d.compareTo(BigInteger.valueOf(16)) > 0) return a;
        return new GameNumber(a.mantissa + b.mantissa*Math.pow(10, -d.intValue()), a.exponent);
    }
    public GameNumber minus(GameNumber n) {
        if (compareTo(n) <= 0) return ZERO;
        if (n.mantissa == 0) return this;
        BigInteger d = exponent.subtract(n.exponent);
        if (d.compareTo(BigInteger.valueOf(16)) > 0) return this;
        return new GameNumber(Math.max(0, mantissa-n.mantissa*Math.pow(10, -d.intValue())), exponent);
    }
    public GameNumber max(GameNumber n) { return compareTo(n) >= 0 ? this : n; }
    public double bounded(double maximum) {
        if (mantissa == 0 || exponent.compareTo(BigInteger.valueOf(-320)) < 0) return 0;
        if (exponent.compareTo(BigInteger.valueOf(308)) > 0) return maximum;
        return Math.min(maximum, mantissa*Math.pow(10, exponent.intValue()));
    }
    public int compareTo(GameNumber n) {
        if (mantissa == 0 || n.mantissa == 0) return Double.compare(mantissa, n.mantissa);
        int c = exponent.compareTo(n.exponent); return c != 0 ? c : Double.compare(mantissa, n.mantissa);
    }
    public String encode() { return mantissa + ":" + exponent; }
    public static GameNumber decode(String s) {
        if (s == null || s.isEmpty()) return ONE;
        String[] parts = s.split(":"); return new GameNumber(Double.parseDouble(parts[0]), new BigInteger(parts[1]));
    }
    public String toString() {
        if (mantissa == 0) return "0";
        if (exponent.compareTo(BigInteger.ZERO) >= 0 && exponent.compareTo(BigInteger.valueOf(6)) < 0)
            return String.format(Locale.ROOT, "%.1f", bounded(1e6));
        return String.format(Locale.ROOT, "%.3f", mantissa) + "e" + exponent;
    }
}
