/*
 * Copyright (c) 2015-2025 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.vaquaclient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**

*/

public class VAquaClient
{
    private VAquaClient() {}

    /**
      Return an identification of this release of VAquaClient.
    */

    public static @NotNull String getReleaseName()
    {
        return getStringResource("RELEASE.txt");
    }

    /**
      Return an identification of this build of VAquaClient.
    */

    public static @NotNull String getBuildID()
    {
        return getStringResource("BUILD.txt");
    }

    /**
      Write a description of this version of VAquaClient to standard error.
    */

    public static void showVersion()
    {
        System.err.println("VAquaClient: release " + getReleaseName() + ", build " + getBuildID());
    }

    /**
      Indicate whether VAqua is the installed look and feel.
    */

    public static boolean isVAquaInstalled()
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        return laf != null && laf.getClass().getName().startsWith("org.violetlib.aqua.");
    }

    /**
      Indicate whether the VAqua or the standard Aqua look and feel is the installed look and feel.
    */

    public static boolean isAquaInstalled()
    {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf != null) {
            String className = laf.getClass().getName();
            return className.startsWith("com.apple.laf.") || className.startsWith("org.violetlib.aqua.");
        }
        return false;
    }

    /**
      Return an identification of installed release of VAqua. This method returns null if VAqua is not the installed
      look and feel.
    */

    public static @Nullable String getVAquaReleaseName()
    {
        try {
            LookAndFeel laf = UIManager.getLookAndFeel();
            Method m = laf.getClass().getMethod("getReleaseName");
            Object o = m.invoke(null);
            if (o instanceof String) {
                return (String) o;
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
      Return an identification of installed build of VAqua. This method returns null if VAqua is not the installed
      look and feel.
    */

    public static @Nullable String getVAquaBuildID()
    {
        try {
            LookAndFeel laf = UIManager.getLookAndFeel();
            Method m = laf.getClass().getMethod("getBuildID");
            Object o = m.invoke(null);
            if (o instanceof String) {
                return (String) o;
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
      Identify the UI version. The UI version normally matches the OS release, except where macOS is supporting
      backward compatibility.

      @return an integer version, or zero if VAqua is not the installed look and feel or the UI version is not
      available. A UI version is an integer with the decimal form MMmm, where MM is the major release number and mm is
      the minor release number. For example, 2600 represents the first release of macOS 26 (Tahoe).
    */

    public static int getUIVersion()
    {
        try {
            LookAndFeel laf = UIManager.getLookAndFeel();
            Method m = laf.getClass().getMethod("getUIVersion");
            Object o = m.invoke(null);
            if (o instanceof Integer) {
                return (Integer) o;
            }
        } catch (Exception ignore) {
        }
        return 0;
    }

    private static @NotNull String getStringResource(@NotNull String name)
    {
        InputStream s = VAquaClient.class.getResourceAsStream(name);
        if (s != null) {
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(s));
                StringBuilder sb = new StringBuilder();
                for (; ; ) {
                    int ch = r.read();
                    if (ch < 0) {
                        break;
                    }
                    sb.append((char) ch);
                }
                return sb.toString();
            } catch (IOException ignore) {
            }
        }

        return "Unknown";
    }
}
