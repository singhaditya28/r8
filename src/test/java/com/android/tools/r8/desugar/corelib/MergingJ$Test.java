// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.desugar.corelib;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

import com.android.tools.r8.L8;
import com.android.tools.r8.L8Command;
import com.android.tools.r8.OutputMode;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.desugar.corelib.corelibjdktests.Jdk11CoreLibTestBase;
import com.android.tools.r8.utils.AndroidApiLevel;
import com.android.tools.r8.utils.codeinspector.CodeInspector;
import java.nio.file.Path;
import org.junit.Test;

public class MergingJ$Test extends Jdk11CoreLibTestBase {

  @Test
  public void testMergingJ$() throws Exception {
    Path mergerInputPart1 = buildSplitDesugaredLibraryPart1();
    Path mergerInputPart2 = buildSplitDesugaredLibraryPart2();
    CodeInspector codeInspectorOutput = null;
    try {
      codeInspectorOutput =
          testForD8().addProgramFiles(mergerInputPart1, mergerInputPart2).compile().inspector();
    } catch (Exception e) {
      if (e.getCause().getMessage().contains("Merging dex file containing classes with prefix")) {
        // TODO(b/138278440): Forbid to merge j$ classes in a Google3 compliant way.
        // In Google 3 the Dex merger is used to merge the Bazel desugared core library.
        // The Dex merger has to be able to merge multiple classes with the prefix j$ for this case.
        // The following should therefore not raise:
        // "Merging dex file containing classes with prefix j$. is not allowed."
        fail();
      }
      throw e;
    }
    CodeInspector codeInspectorSplit1 = new CodeInspector(mergerInputPart1);
    CodeInspector codeInspectorSplit2 = new CodeInspector(mergerInputPart2);
    assertNotNull(codeInspectorOutput);
    assertTrue(codeInspectorOutput.allClasses().size() > codeInspectorSplit1.allClasses().size());
    assertTrue(codeInspectorOutput.allClasses().size() > codeInspectorSplit2.allClasses().size());
  }

  private Path buildSplitDesugaredLibraryPart1() throws Exception {
    Path output = temp.newFolder().toPath().resolve("merger-input.zip");
    L8.run(
        L8Command.builder()
            .addLibraryFiles(ToolHelper.getAndroidJar(AndroidApiLevel.P))
            .addProgramFiles(ToolHelper.getDesugarJDKLibs())
            .addSpecialLibraryConfiguration("default")
            .setMinApiLevel(AndroidApiLevel.B.getLevel())
            .setOutput(output, OutputMode.DexIndexed)
            .build());
    return output;
  }

  private Path buildSplitDesugaredLibraryPart2() throws Exception {
    Path output = temp.newFolder().toPath().resolve("merger-input-split.zip");
    L8.run(
        L8Command.builder()
            .addLibraryFiles(ToolHelper.getAndroidJar(AndroidApiLevel.P))
            .addProgramFiles(JDK_11_JAVA_BASE_EXTENSION_COMPILED_FILES)
            .addClasspathFiles(ToolHelper.getDesugarJDKLibs())
            .addSpecialLibraryConfiguration("default")
            .setMinApiLevel(AndroidApiLevel.B.getLevel())
            .setOutput(output, OutputMode.DexIndexed)
            .build());
    return output;
  }
}