// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import static com.android.tools.r8.optimize.MemberRebindingAnalysis.isMemberVisibleFromOriginalContext;

import com.android.tools.r8.graph.AppInfo;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.ir.analysis.AbstractError;
import java.util.Collections;
import java.util.List;

public abstract class FieldInstruction extends Instruction {

  private MemberType type;
  private final DexField field;

  protected FieldInstruction(DexField field, Value dest, Value value) {
    this(field, dest, Collections.singletonList(value));
  }

  protected FieldInstruction(DexField field, Value dest, List<Value> inValues) {
    super(dest, inValues);
    assert field != null;
    this.field = field;
    this.type = MemberType.fromDexType(field.type);
  }

  public MemberType getType() {
    return type;
  }

  public DexField getField() {
    return field;
  }

  @Override
  public boolean isFieldInstruction() {
    return true;
  }

  @Override
  public FieldInstruction asFieldInstruction() {
    return this;
  }

  @Override
  public AbstractError instructionInstanceCanThrow(
      AppView<? extends AppInfo> appView, DexType context) {
    // Not applicable for D8.
    if (!appView.enableWholeProgramOptimizations()) {
      return AbstractError.top();
    }

    // TODO(b/123857022): Should be possible to use definitionFor().
    DexEncodedField resolvedField = appView.appInfo().resolveField(getField());
    // * NoSuchFieldError (resolution failure).
    if (resolvedField == null) {
      return AbstractError.specific(appView.dexItemFactory().noSuchFieldErrorType);
    }
    // * IncompatibleClassChangeError (instance-* for static field and vice versa).
    if (resolvedField.isStaticMember()) {
      if (isInstanceGet() || isInstancePut()) {
        return AbstractError.specific(appView.dexItemFactory().icceType);
      }
    } else {
      if (isStaticGet() || isStaticPut()) {
        return AbstractError.specific(appView.dexItemFactory().icceType);
      }
    }
    // * IllegalAccessError (not visible from the access context).
    if (!isMemberVisibleFromOriginalContext(
        appView, context, resolvedField.field.holder, resolvedField.accessFlags)) {
      return AbstractError.specific(appView.dexItemFactory().illegalAccessErrorType);
    }
    // * NullPointerException (null receiver).
    if (isInstanceGet() || isInstancePut()) {
      Value receiver = inValues.get(0);
      if (receiver.isAlwaysNull(appView) || receiver.typeLattice.isNullable()) {
        return AbstractError.specific(appView.dexItemFactory().npeType);
      }
    }
    // May trigger <clinit> that may have side effects.
    if (field.holder.classInitializationMayHaveSideEffects(
        appView,
        // Types that are a super type of `context` are guaranteed to be initialized already.
        type -> appView.isSubtype(context, type).isTrue())) {
      return AbstractError.top();
    }

    return AbstractError.bottom();
  }

  @Override
  public boolean hasInvariantOutType() {
    // TODO(jsjeon): what if the target field is known to be non-null?
    return true;
  }
}
