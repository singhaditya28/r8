// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming;

import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexReference;
import com.android.tools.r8.graph.DexString;
import java.util.Set;

public interface MemberNamingStrategy {

  DexString next(DexMethod method, MethodNamingState.InternalState internalState);

  DexString next(DexField field, FieldNamingState.InternalState internalState);

  boolean bypassDictionary();

  boolean breakOnNotAvailable(DexReference source, DexString name);

  Set<DexReference> noObfuscation();
}
