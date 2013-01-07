package com.redhat.ceylon.eclipse.core.model;

import java.util.List;

import org.antlr.runtime.CommonToken;

import com.redhat.ceylon.compiler.typechecker.TypeChecker;
import com.redhat.ceylon.compiler.typechecker.analyzer.ModuleManager;
import com.redhat.ceylon.compiler.typechecker.context.PhasedUnit;
import com.redhat.ceylon.compiler.typechecker.io.VirtualFile;
import com.redhat.ceylon.compiler.typechecker.model.Package;
import com.redhat.ceylon.compiler.typechecker.tree.Tree.CompilationUnit;

public class CeylonSourceFile extends PhasedUnit {

    private TypeChecker typeChecker;

    public CeylonSourceFile(VirtualFile unitFile, VirtualFile srcDir,
            CompilationUnit cu, Package p, ModuleManager moduleManager,
            TypeChecker typeChecker, List<CommonToken> tokenStream) {
        super(unitFile, srcDir, cu, p, moduleManager, typeChecker.getContext(), tokenStream);
        this.typeChecker = typeChecker;
    }
    
    public CeylonSourceFile(PhasedUnit other) {
        super(other);
        if (other instanceof CeylonSourceFile) {
            typeChecker = ((CeylonSourceFile) other).typeChecker;
        }
    }
}
