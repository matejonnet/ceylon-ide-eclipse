package com.redhat.ceylon.eclipse.core.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.redhat.ceylon.cmr.api.JDKUtils;
import com.redhat.ceylon.compiler.typechecker.context.PhasedUnit;
import com.redhat.ceylon.compiler.typechecker.context.PhasedUnits;
import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.model.ExternalUnit;
import com.redhat.ceylon.compiler.typechecker.model.IntersectionType;
import com.redhat.ceylon.compiler.typechecker.model.ProducedType;
import com.redhat.ceylon.compiler.typechecker.model.TypeDeclaration;
import com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration;
import com.redhat.ceylon.compiler.typechecker.model.UnionType;
import com.redhat.ceylon.compiler.typechecker.model.Unit;
import com.redhat.ceylon.compiler.typechecker.tree.Tree;
import com.redhat.ceylon.compiler.typechecker.tree.Visitor;
import com.redhat.ceylon.eclipse.core.model.SourceFile;

public class UnitDependencyVisitor extends Visitor {
    
    private final PhasedUnit phasedUnit;
    private final PhasedUnits phasedUnits;
    private final List<PhasedUnits> phasedUnitsOfDependencies;
    private Set<Declaration> alreadyDone;
    
    public UnitDependencyVisitor(PhasedUnit phasedUnit, PhasedUnits phasedUnits, 
    		List<PhasedUnits> phasedUnitsOfDependencies) {
        this.phasedUnit = phasedUnit;
        this.phasedUnits = phasedUnits;
        this.phasedUnitsOfDependencies = phasedUnitsOfDependencies;
        alreadyDone = new HashSet<Declaration>();
    }
    
    private String getSrcFolderRelativePath(Unit u) {
        return u.getPackage().getQualifiedNameString().replace('.', '/') + 
                "/" + u.getFilename();
    }

    private void storeDependency(Declaration d) {
        if (d!=null && (d instanceof UnionType || 
                        d instanceof IntersectionType || 
                        !alreadyDone.contains(d))) {
            if (!(d instanceof UnionType || 
                        d instanceof IntersectionType)) {
                alreadyDone.add(d);
            }
            if (d instanceof TypeDeclaration) {
                TypeDeclaration td = (TypeDeclaration) d;
                storeDependency(td.getExtendedTypeDeclaration());
                for (TypeDeclaration st: td.getSatisfiedTypeDeclarations()) {
                    storeDependency(st);
                }
                List<TypeDeclaration> caseTypes = td.getCaseTypeDeclarations();
                if (caseTypes!=null) {
                    for (TypeDeclaration ct: caseTypes) {
                        storeDependency(ct);
                    }
                }
            }
            if (d instanceof TypedDeclaration) {
                //TODO: is this really necessary?
                storeDependency(((TypedDeclaration) d).getTypeDeclaration());
            }
            Declaration rd = d.getRefinedDeclaration();
            if (rd!=d) {
                storeDependency(rd); //this one is needed for default arguments, I think
            }
            Unit declarationUnit = d.getUnit();
            if (declarationUnit != null) {
            	String moduleName = declarationUnit.getPackage().getModule().getNameAsString();
            	if (!moduleName.equals("ceylon.language") && 
            			!JDKUtils.isJDKModule(moduleName)
            			&& !JDKUtils.isOracleJDKModule(moduleName)) { 
            	    //TODO: also filter out src archives from external repos
            	    //      Now with specialized units we could do : 
            	    //         if (unit instanceOf ProjectSourceFile 
            	    //             || unit instanceOf JavaCompilationUnit)
            	    //      Might be necesary though to manage a specific case 
            	    //      for cross-project dependencies when they will be managed 
            	    //      and not from source archives anymore
            		Unit currentUnit = phasedUnit.getUnit();
            		String currentUnitPath = phasedUnit.getUnitFile().getPath();
            		String currentUnitName = currentUnit.getFilename();
            		String dependedOnUnitName = declarationUnit.getFilename();
            		String currentUnitPackage = currentUnit.getPackage().getNameAsString();
            		String dependedOnPackage = currentUnit.getPackage().getNameAsString();
            		if (!dependedOnUnitName.equals(currentUnitName) ||
            				!dependedOnPackage.equals(currentUnitPackage)) {
            			if (! (declarationUnit instanceof SourceFile)) {
            				//TODO: this does not seem to work for cross-project deps
            				declarationUnit.getDependentsOf().add(currentUnitPath);
            			} 
            			else {
            				String dependedOnUnitRelPath = getSrcFolderRelativePath(declarationUnit);
            				PhasedUnit dependedOnPhasedUnit = phasedUnits.getPhasedUnitFromRelativePath(dependedOnUnitRelPath);
            				if (dependedOnPhasedUnit != null && dependedOnPhasedUnit.getUnit() != null) {
            					dependedOnPhasedUnit.getUnit().getDependentsOf().add(currentUnitPath);
            				} else {
            				    // This case is only for cross-project dependencies managed by source archives
            					for (PhasedUnits phasedUnitsOfDependency : phasedUnitsOfDependencies) {
            						dependedOnPhasedUnit = phasedUnitsOfDependency.getPhasedUnitFromRelativePath(dependedOnUnitRelPath);
            						if (dependedOnPhasedUnit != null && dependedOnPhasedUnit.getUnit() != null) {
            							dependedOnPhasedUnit.getUnit().getDependentsOf().add(currentUnitPath);
            							break;
            						}
            					}
            				}
            			}
            		}
            	}
            }
        }
    }
    
    @Override
    public void visit(Tree.MemberOrTypeExpression that) {
        storeDependency(that.getDeclaration());
        super.visit(that);
    }
        
    @Override
    public void visit(Tree.NamedArgument that) {
        //TODO: is this really necessary?
        storeDependency(that.getParameter());
        super.visit(that);
    }
        
    @Override
    public void visit(Tree.SequencedArgument that) {
        //TODO: is this really necessary?
        storeDependency(that.getParameter());
        super.visit(that);
    }
        
    @Override
    public void visit(Tree.PositionalArgument that) {
        //TODO: is this really necessary?
        storeDependency(that.getParameter());
        super.visit(that);
    }
        
    @Override
    public void visit(Tree.Type that) {
        ProducedType tm = that.getTypeModel();
        if (tm!=null) {
            storeDependency(tm.getDeclaration());
        }
        super.visit(that);
    }
        
    @Override
    public void visit(Tree.ImportMemberOrType that) {
        storeDependency(that.getDeclarationModel());
        super.visit(that);
    }
        
    @Override
    public void visit(Tree.TypeArguments that) {
        //TODO: is this really necessary?
        List<ProducedType> tms = that.getTypeModels();
        if (tms!=null) {
            for (ProducedType pt: tms) {
                storeDependency(pt.getDeclaration());
            }
        }
        super.visit(that);
    }
        
    @Override
    public void visit(Tree.Term that) {
        //TODO: is this really necessary?
        ProducedType tm = that.getTypeModel();
        if (tm!=null) {
            storeDependency(tm.getDeclaration());
        }
        super.visit(that);
    }
    
}
