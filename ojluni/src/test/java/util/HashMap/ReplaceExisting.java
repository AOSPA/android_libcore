/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

/**
 * @test
 * @bug 8025173
 * @summary Verify that replacing the value for an existing key does not
 * corrupt active iterators, in particular due to a resize() occurring and
 * not updating modCount.
 * @run main ReplaceExisting
 */
package test.java.util.HashMap;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ReplaceExisting {
    /* Number of entries required to trigger a resize for cap=16, load=0.75*/
    private static int ENTRIES = 13;

    @Test
    public void test() {
        for (int i = 0; i <= ENTRIES; i++) {
            HashMap<Integer,Integer> hm = prepHashMap();
            testItr(hm, i);
        }
    }

    /* Prepare a HashMap that will resize on next put() */
    private static HashMap<Integer,Integer> prepHashMap() {
        HashMap<Integer,Integer> hm = new HashMap<>(16, 0.75f);
        // Add items to one more than the resize threshold
        for (int i = 0; i < ENTRIES; i++) {
            hm.put(i*10, i*10);
        }
        return hm;
    }

    /* Iterate hm for elemBeforePut elements, then call put() to replace value
     * for existing key.  With bug 8025173, this will also cause a resize, but
     * not increase the modCount.
     * Finish the iteration to check for a corrupt iterator.
     */
    private static void testItr(HashMap<Integer,Integer> hm, int elemBeforePut) {
        if (elemBeforePut > hm.size()) {
            Assert.fail("elemBeforePut must be <= HashMap size. elemBeforePut:" +
                elemBeforePut + " " + "HashMap.size:" + hm.size());
        }
        // Create a copy of the keys
        HashSet<Integer> keys = new HashSet<>(hm.size());
        keys.addAll(hm.keySet());

        HashSet<Integer> collected = new HashSet<>(hm.size());

        // Run itr for elemBeforePut items, collecting returned elems
        Iterator<Integer> itr = hm.keySet().iterator();
        for (int i = 0; i < elemBeforePut; i++) {
            Integer retVal = itr.next();
            Assert.assertTrue(collected.add(retVal),
                "Corrupt iterator: key " + retVal + " already encountered");
        }

        // Do put() to replace entry (and resize table when bug present)
        Assert.assertNotNull(hm.put(0, 100),
            "Error in test: expected key 0 to be in the HashMap");

        // Finish itr + collecting returned elems
        while(itr.hasNext()) {
            Integer retVal = itr.next();
            Assert.assertTrue(collected.add(retVal),
                "Corrupt iterator: key " + retVal + " already encountered");
        }

        // Compare returned elems to original copy of keys
        Assert.assertEquals (collected, keys,
            "Collected keys do not match original set of keys");
    }
}
