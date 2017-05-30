// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package trywithresources;

public class TryWithResourcesNotDesugaredTests extends TryWithResources {
  @Override
  boolean desugaredCodeRunningOnJvm() {
    return false;
  }

  public static void main(String[] args) throws Exception {
    new TryWithResourcesNotDesugaredTests().test();
  }
}
