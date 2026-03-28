/*
 * Copyright (c) 2020-2026 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.vaquaclient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
  Indirect support for VAppearances. Uses VAqua and its associated VAppearances (if installed).
*/

public class AppearanceSupport
{
    private static @Nullable Class<?> vAppearancesClass;
    private static boolean hasFailed;
    private static boolean hasWarned;

    private AppearanceSupport()
    {
    }

    /**
      Indicate whether the current application effective appearance is a dark appearance.
      @return true if the appearance is dark, false if the appearance is light, or null if the information is not
      available.
    */

    public static @Nullable Boolean isEffectiveAppearanceDark()
    {
        Object a = getEffectiveAppearance();
        return a != null ? isDark(a) : null;
    }

    /**
      Indicate whether the current application effective appearance is a high contrast appearance.
      @return true if the appearance is high contrast, false if the appearance is not high contrast, or null if the
      information is not available.
    */

    public static @Nullable Boolean isEffectiveAppearanceHighContrast()
    {
        Object a = getAppearanceSettings();
        return a != null ? isHighContrast(a) : null;
    }

    /**
      Indicate whether the current application effective appearance is a tinted appearance.
      @return true if the appearance is tinted, false if the appearance is not tinted, or null if the information is not
      available.
    */

    public static @Nullable Boolean isEffectiveAppearanceTinted()
    {
        Object a = getAppearanceSettings();
        return a != null ? isTinted(a) : null;
    }

    /**
      Return a named system color appropriate for the current application effective appearance.
      @return the color, or null if not available.
    */

    public static @Nullable Color getEffectiveAppearanceSystemColor(@NotNull String name)
    {
        Object a = getEffectiveAppearance();
        return a != null ? getSystemColor(a, name) : null;
    }

    /**
      Return a non-modifiable map that provides the current values of the system colors associated with the current
      application effective appearance.
    */

    public static @Nullable Map<String,Color> getEffectiveAppearanceSystemColors()
    {
        Object a = getEffectiveAppearance();
        return a != null ? getColors(a) : null;
    }

    /**
      Register a responder to be called when the application effective appearance or a related appearance setting may
      have changed.
      @param r The responder.
      @return true if successful, false otherwise.
    */

    public static boolean addEffectiveAppearanceChangeResponder(@NotNull Runnable r)
    {
        ChangeListener listener = e -> r.run();
        Class<?> c = getVAppearances();
        if (c == null) {
            return false;
        }
        try {
            Method m = c.getMethod("addEffectiveAppearanceChangeListener", ChangeListener.class);
            m.invoke(null, listener);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
      Indicate whether the specified component currently has a dark appearance.
      @return true if the appearance is dark, false if the appearance is light, or null if the information is not
      available.
    */

    public static @Nullable Boolean isDark(@NotNull Component c)
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf.getClass().getName().equals("org.violetlib.aqua.AquaLookAndFeel")) {
            return isDark(findAppearanceName(c));
        }
        return null;
    }

    /**
      Return a non-modifiable map that provides the current values of the system colors associated with the specified
      component.
    */

    public static @Nullable Map<String,Color> getColors(@NotNull Component c)
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf.getClass().getName().equals("org.violetlib.aqua.AquaLookAndFeel")) {
            return getColors(findAppearanceName(c));
        }
        return null;
    }

    /**
      Set or clear the application appearance.

      @param appearanceName If not null and valid, this appearance is installed as the application appearance. If null,
      the application appearance is cleared, which means that the application effective appearance will be the system
      appearance.
      @throws UnsupportedOperationException if this operation could not be performed.
    */

    public static void setApplicationAppearance(@Nullable String appearanceName)
      throws UnsupportedOperationException
    {
        Class<?> c = getVAppearances();
        if (c == null) {
            throw new UnsupportedOperationException("VAppearances is not available");
        }
        setApplicationAppearance(c, appearanceName);

    }

    private static void setApplicationAppearance(@NotNull Class<?> c, @Nullable String appearanceName)
      throws UnsupportedOperationException
    {
        try {
            Method m = c.getMethod("setApplicationAppearance", String.class);
            m.invoke(null, appearanceName);
        } catch (InvocationTargetException ex) {
            // The appearance is not defined
            throw new UnsupportedOperationException(ex.getMessage());
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Unable to set the application appearance");
        }
    }

    private static @Nullable String findAppearanceName(@NotNull Component c)
    {
        try {
            LookAndFeel laf = UIManager.getLookAndFeel();
            Method m = laf.getClass().getMethod("getComponentAppearance", Component.class);
            Object o = m.invoke(null, c);
            if (o instanceof String) {
                return (String) o;
            }
        } catch (Exception ignore) {
        }

        while (c != null) {
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                Object o = jc.getClientProperty("Aqua.appearanceName");
                if ("NSAppearanceNameAqua".equals(o)) {
                    return (String) o;
                }
                if ("NSAppearanceNameDarkAqua".equals(o)) {
                    return (String) o;
                }
            }
            c = c.getParent();
        }

        Object o = getEffectiveAppearance();
        return o != null ? getAppearanceName(o) : null;
    }

    private static @Nullable Boolean isDark(@Nullable String s)
    {
        if ("NSAppearanceNameAqua".equals(s)) {
            return false;
        }
        if ("NSAppearanceNameDarkAqua".equals(s)) {
            return true;
        }
        return null;
    }

    private static @Nullable Map<String,Color> getColors(@Nullable String s)
    {
        if (s == null) {
            return null;
        }

        Object appearance = getAppearance(s);
        if (appearance == null) {
            return null;
        }
        return getColors(appearance);
    }

    private static @Nullable String getAppearanceName(@NotNull Object appearance)
    {
        try {
            Method m = findInterfaceMethod(appearance.getClass().getMethod("getName"));
            return (String) m.invoke(appearance);
        } catch (Exception ex) {
            return null;
        }
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
            Map<String,Color> colors = getColors(appearance);
            return colors != null ? colors.get(name) : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static @Nullable Map<String,Color> getColors(@NotNull Object appearance)
    {
        try {
            Method m = findInterfaceMethod(appearance.getClass().getMethod("getColors"));
            return (Map) m.invoke(appearance);
        } catch (Exception ex) {
            return null;
        }
    }

    private static @Nullable Boolean isHighContrast(@NotNull Object settings)
    {
        try {
            Method m = settings.getClass().getMethod("isIncreaseContrast");
            return (Boolean) m.invoke(settings);
        } catch (Exception ex) {
            return null;
        }
    }

    private static @Nullable Boolean isTinted(@NotNull Object settings)
    {
        try {
            Method m = settings.getClass().getMethod("getTintedOption");
            Integer n = (Integer) m.invoke(settings);
            return n != 0;
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

    private static @Nullable Object getAppearance(@NotNull String name)
    {
        Class<?> c = getVAppearances();
        return c != null ? getAppearance(c, name) : null;
    }

    private static @Nullable Object getAppearance(@NotNull Class<?> c, @NotNull String name)
    {
        try {
            Method m = c.getMethod("getAppearance", String.class);
            return m.invoke(null, name);
        } catch (Exception ex) {
            return null;
        }
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

    private static @Nullable Object getAppearanceSettings()
    {
        Class<?> c = getVAppearances();
        return c != null ? getAppearanceSettings(c) : null;
    }

    private static @Nullable Object getAppearanceSettings(@NotNull Class<?> c)
    {
        try {
            Method m = c.getMethod("getAppearanceSettings");
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
