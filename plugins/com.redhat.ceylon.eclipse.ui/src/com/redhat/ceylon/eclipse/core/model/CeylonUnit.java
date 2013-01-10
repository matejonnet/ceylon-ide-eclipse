package com.redhat.ceylon.eclipse.core.model;

import java.lang.ref.WeakReference;

import com.redhat.ceylon.compiler.typechecker.context.PhasedUnit;
import com.redhat.ceylon.eclipse.core.typechecker.IdePhasedUnit;

public abstract class CeylonUnit extends IdeUnit {
    
    public CeylonUnit() {
        phasedUnitRef = null;
    }
    
    protected WeakReference<IdePhasedUnit> phasedUnitRef;
    
    final protected void createPhasedUnitRef(IdePhasedUnit phasedUnit) {
        phasedUnitRef = new WeakReference<IdePhasedUnit>(phasedUnit);
    }
    
    protected abstract void setPhasedUnitIfNecessary();
    
    public IdePhasedUnit getPhasedUnit() {
        setPhasedUnitIfNecessary();
        return phasedUnitRef.get();
    }
}
