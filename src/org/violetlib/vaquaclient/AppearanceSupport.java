package org.violetlib.vaquaclient;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Map;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.jetbrains.annotations.*;

/**
  Indirect support for appearance objects. Uses VAqua (if installed)
*/

public class AppearanceSupport
{
    private static @Nullable Class<?> vAppearancesClass;
    private static boolean hasFailed;
    private static boolean hasWarned;

    public static @Nullable Boolean isEffectiveAppearanceDark()
    {
        Object a = getEffectiveAppearance();
        return a != null ? isDark(a) : null;
    }

    public static @Nullable Color getEffectiveAppearanceSystemColor(@NotNull String name)
    {
        Object a = getEffectiveAppearance();
        return a != null ? getSystemColor(a, name) : null;
    }

    private static @Nullable Boolean isDark(@NotNull Object appearance)
    {
        try {
            Method m = findInterfaceMethod(appearance.getClass().getMethod("isDark"));
            return (Boolean) m.invoke(appearance);
        } catch (Exception ex) {
            return null;
        }
    }

    private static @Nullable Color getSystemColor(@NotNull Object appearance, @NotNull String name)
    {
        try {
            Map<?,?> colors = getColors(appearance);
            return colors != null ? (Color) colors.get(name) : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static @Nullable Map<?,?> getColors(@NotNull Object appearance)
    {
        try {
            Method m = findInterfaceMethod(appearance.getClass().getMethod("getColors"));
            return (Map) m.invoke(appearance);
        } catch (Exception ex) {
            return null;
        }
    }

    private static @NotNull Method findInterfaceMethod(@Nullable Method m)
      throws NoSuchMethodException
    {
        if (m != null) {
            for (Class<?> bc : m.getDeclaringClass().getInterfaces()) {
                try {
                    return bc.getMethod(m.getName(), m.getParameterTypes());
                } catch (NoSuchMethodException ignore) {
                }
            }
        }
        throw new NoSuchMethodException();
    }

    private static @Nullable Object getEffectiveAppearance()
    {
        Class<?> c = getVAppearances();
        return c != null ? getEffectiveAppearance(c) : null;
    }

    private static @Nullable Object getEffectiveAppearance(@NotNull Class<?> c)
    {
        try {
            Method m = c.getMethod("getApplicationEffectiveAppearance");
            return m.invoke(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private static @Nullable Class<?> getVAppearances()
    {
        if (vAppearancesClass != null || hasFailed) {
            return vAppearancesClass;
        }

        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf != null) {
            if (laf.getClass().getName().equals("org.violetlib.aqua.AquaLookAndFeel")) {
                ClassLoader classLoader = laf.getClass().getClassLoader();
                try {
                    vAppearancesClass = Class.forName("org.violetlib.vappearances.VAppearances", true, classLoader);
                    System.err.println("Loaded VAppearances using VAqua class loader");
                    return vAppearancesClass;
                } catch (Exception ex) {
                    System.err.println("Did not find VAppearances using VAqua class loader");
                    hasFailed = true;
                    return null;
                }
            }
            // If the look and feel is not VAqua, assume VAqua will not be loaded and load VAppearances directly, if
            // possible.
            try {
                vAppearancesClass = Class.forName("org.violetlib.vappearances.VAppearances");
                System.err.println("Loaded VAppearances because look and feel is " + laf.getName());
                return vAppearancesClass;
            } catch (Exception ex) {
                System.err.println("Did not find VAppearances using application class loader");
                hasFailed = true;
                return null;
            }
        }

        // If there is no look and feel, return null for now.
        if (!hasWarned) {
            hasWarned = true;
            System.err.println("VAquaClient: attempt to access VAppearances before a LAF has been installed");
            new Exception().printStackTrace();
        }
        return null;
    }
}
