/*
 * Copyright 1990-2006 Sun Microsystems, Inc. All Rights Reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 only,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 or visit www.sun.com if you need additional information or have
 * any questions.
 */

package sun.applet;


import java.net.URL;
import java.awt.Image;
import sun.misc.Ref;
import java.util.Hashtable;
import java.applet.AudioClip;


/**
 * Part of this class still remains only to support legacy, 100%-impure
 * applications such as HotJava 1.0.1.
 */
public class AppletResourceLoader {

    /*
     * Where we store AppletAudioClips.  This will be the audio 
     * hashtable for the browser or appletviewer.  We'll put ONLY
     * objects of type AudioClip into this hashtable.
     */

    static private Hashtable audioHash = new Hashtable();

    public static Image getImage(URL url) {
        return AppletViewer.getCachedImage(url);
    }

    public static Ref getImageRef(URL url) {
        return AppletViewer.getCachedImageRef(url);
    }

    public static void flushImages() {
        AppletViewer.flushImageCache();
    }

    public static synchronized AudioClip getAudioClip(URL url) {
        System.getSecurityManager().checkConnect(url.getHost(), url.getPort());
	AudioClip clip = (AudioClip)audioHash.get(url);
	if (clip == null) {
	    audioHash.put(url, clip = new AppletAudioClip(url));
	}
        return clip;
    }

    public static void flushAudioClips() {
	audioHash = new Hashtable();
    }
}
