/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package test.java.net.InetAddress;

/**
 * @test
 * @bug 8135305
 * @key intermittent
 * @summary ensure we can't ping external hosts via loopback if
 */

import java.io.*;
import java.net.*;
import java.util.*;
import org.testng.annotations.Test;
import static org.testng.Assert.fail;

public class IsReachableViaLoopbackTest {
    @Test
    public void testReachableViaLoopback() {
        try {
            InetAddress addr = InetAddress.getByName("localhost");
            InetAddress remoteAddr = InetAddress.getByName("bugs.openjdk.java.net");
            if (!addr.isReachable(10000))
                fail("Localhost should always be reachable");
            NetworkInterface inf = NetworkInterface.getByInetAddress(addr);
            if (inf != null) {
                if (!addr.isReachable(inf, 20, 10000)) {
                    fail("Localhost should always be reachable");
                } else {
                }
                if (remoteAddr.isReachable(inf, 20, 10000)) {
                    fail(remoteAddr + " is reachable");
                } else {
                }
            }

        } catch (IOException e) {
           fail("Unexpected exception:" + e);
        }
    }
}