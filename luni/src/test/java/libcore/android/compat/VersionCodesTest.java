/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.android.compat;

import dalvik.annotation.compat.VersionCodes;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VersionCodesTest {

    /**
     * Ensure the consistent values of @link VersionCodes#UPSIDE_DOWN_CAKE} and
     * {@link android.os.Build.VERSION_CODES#UPSIDE_DOWN_CAKE}.
     *.
     */
    @Test
    public void testUPSIDE_DOWN_CAKE() {
        String field = "UPSIDE_DOWN_CAKE";
        int frameworkValue = getFrameworksSdkCode(field);
        Assert.assertEquals(frameworkValue, getLibcoreSdkCode(field));
    }

    private static int getFrameworksSdkCode(String fieldName) {
        try {
            Class<?> clazz = Class.forName("android.os.Build$VERSION_CODES");
            return clazz.getField(fieldName).getInt(null);
        } catch (ReflectiveOperationException e) {
            // If the field is not found, we use Assume API to skip this test.
            Assume.assumeNoException(e);
            // Throw RuntimeException in order to compile this, but previous statement should throw.
            throw new RuntimeException();
        }
    }

    private static int getLibcoreSdkCode(String fieldName) {
        try {
            // Use reflection to access the API to avoid constant inlining by javac.
            return VersionCodes.class.getField(fieldName).getInt(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
