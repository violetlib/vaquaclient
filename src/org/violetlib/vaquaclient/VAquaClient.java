/*
 * Copyright (c) 2015-2020 Alan Snyder.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the license agreement. For details see
 * accompanying license terms.
 */

package org.violetlib.vaquaclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.jetbrains.annotations.*;

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
      Indicate whether the installed look and feel is VAqua or the standard Aqua look and feel.
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
            } catch (IOException ex) {
            }
        }

        return "Unknown";
    }
}
