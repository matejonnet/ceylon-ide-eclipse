/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License version 2.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package com.redhat.ceylon.eclipse.core.model.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IDependent;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

import com.redhat.ceylon.compiler.loader.AbstractModelLoader;
import com.redhat.ceylon.compiler.loader.mirror.AnnotationMirror;
import com.redhat.ceylon.compiler.loader.mirror.ClassMirror;
import com.redhat.ceylon.compiler.loader.mirror.FieldMirror;
import com.redhat.ceylon.compiler.loader.mirror.MethodMirror;
import com.redhat.ceylon.compiler.loader.mirror.PackageMirror;
import com.redhat.ceylon.compiler.loader.mirror.TypeMirror;
import com.redhat.ceylon.compiler.loader.mirror.TypeParameterMirror;

public class JDTClass implements ClassMirror {

    private ReferenceBinding klass;
    private LookupEnvironment lookupEnvironment;
    
    private PackageMirror pkg;
    private TypeMirror superclass;
    private List<MethodMirror> methods;
    private List<TypeMirror> interfaces;
    private Map<String, AnnotationMirror> annotations;
    private List<TypeParameterMirror> typeParams;
    private List<FieldMirror> fields;
    private String qualifiedName;
    private String simpleName;
    private boolean superClassSet = false;
    private List<ClassMirror> innerClasses;
    

    public JDTClass(ReferenceBinding klass, LookupEnvironment lookupEnvironment) {
        this.klass = klass;
        this.lookupEnvironment = lookupEnvironment;
    }

    @Override
    public AnnotationMirror getAnnotation(String type) {
        if (annotations == null) {
            annotations = JDTUtils.getAnnotations(klass.getAnnotations(), lookupEnvironment);
        }
        return annotations.get(type);
    }

    @Override
    public boolean isPublic() {
        if (klass != null) {
            return klass.isPublic();
        }
        return true;
    }

    @Override
    public String getQualifiedName() {
        if (qualifiedName == null) {
            qualifiedName = JDTUtils.getFullyQualifiedName(klass);
        }
        return qualifiedName;
    }

    @Override
    public String getName() {
        if (simpleName == null) {
            simpleName = new String(klass.sourceName());
        }
        return simpleName;
    }

    @Override
    public PackageMirror getPackage() {
        if (pkg == null) {
            pkg = new JDTPackage(klass.getPackage());
        }
        return pkg;
    }

    @Override
    public boolean isInterface() {
        return klass.isInterface();
    }

    @Override
    public boolean isAbstract() {
        return klass.isAbstract();
    }

    @Override
    public List<MethodMirror> getDirectMethods() {
        if (methods == null) {
            MethodBinding[] directMethods;
            directMethods = klass.methods();
            methods = new ArrayList<MethodMirror>(directMethods.length);
            for(MethodBinding method : directMethods) {
                methods.add(new JDTMethod(method, lookupEnvironment));
            }
        }
        return methods;
    }

    @Override
    public TypeMirror getSuperclass() {
        if (! superClassSet) {
            if (klass.isInterface() || "java.lang.Object".equals(getQualifiedName())) {
                superclass = null;
            } else {
                ReferenceBinding superClassBinding = klass.superclass();
                if (superClassBinding != null) {
                    superClassBinding = JDTUtils.inferTypeParametersFromSuperClass(klass,
                            superClassBinding, lookupEnvironment);
                    superclass = new JDTType(superClassBinding, lookupEnvironment);
                }
            }
            superClassSet = true;
        }
        return superclass;
    }

    @Override
    public List<TypeMirror> getInterfaces() {
        if (interfaces == null) {
            ReferenceBinding[] superInterfaces = klass.superInterfaces();
            interfaces = new ArrayList<TypeMirror>(superInterfaces.length);
            for(ReferenceBinding superInterface : superInterfaces)
                interfaces.add(new JDTType(superInterface, lookupEnvironment));
        }
        return interfaces;
    }

    @Override
    public List<TypeParameterMirror> getTypeParameters() {
        if (typeParams == null) {
            TypeVariableBinding[] typeParameters = klass.typeVariables();
            typeParams = new ArrayList<TypeParameterMirror>(typeParameters.length);
            for(TypeVariableBinding parameter : typeParameters)
                typeParams.add(new JDTTypeParameter(parameter, lookupEnvironment));
        }
        return typeParams;
    }

    private boolean isAnnotationPresent(Class<?> clazz) {
        return getAnnotation(clazz.getName()) != null;
    }
    
    @Override
    public boolean isCeylonToplevelAttribute() {
        return isAnnotationPresent(com.redhat.ceylon.compiler.java.metadata.Attribute.class);
    }

    @Override
    public boolean isCeylonToplevelObject() {
        return isAnnotationPresent(com.redhat.ceylon.compiler.java.metadata.Object.class);
    }

    @Override
    public boolean isCeylonToplevelMethod() {
        return isAnnotationPresent(com.redhat.ceylon.compiler.java.metadata.Method.class);
    }

    public boolean isCeylon() {
        return isAnnotationPresent(com.redhat.ceylon.compiler.java.metadata.Ceylon.class);
    }

    @Override
    public List<FieldMirror> getDirectFields() {
        if (fields == null) {
            FieldBinding[] directFields = klass.fields();
            fields = new ArrayList<FieldMirror>(directFields.length);
            for(FieldBinding field : directFields)
                fields.add(new JDTField(field, lookupEnvironment));
        }
        return fields;
    }

    @Override
    public boolean isInnerClass() {
        return getAnnotation(AbstractModelLoader.CEYLON_CONTAINER_ANNOTATION) != null || klass.isMemberType();
    }
    
    @Override
    public ClassMirror getEnclosingClass() {
    	//TODO: is this correct?
    	ReferenceBinding enclosingType = klass.enclosingType();
		return enclosingType==null ? null : new JDTClass(enclosingType, lookupEnvironment);
    }

    @Override
    public List<ClassMirror> getDirectInnerClasses() {
        if (innerClasses == null) {
            ReferenceBinding[] memberTypeBindings = klass.memberTypes();
            innerClasses = new ArrayList<ClassMirror>(memberTypeBindings.length);
            for(ReferenceBinding memberTypeBinding : memberTypeBindings) {
                ReferenceBinding classBinding = (ReferenceBinding) memberTypeBinding;
                innerClasses.add(new JDTClass(classBinding, lookupEnvironment));
            }
        }
        return innerClasses;
    }

    @Override
    public boolean isStatic() {
        return klass.isStatic();
    }
    
    public String getFileName() {
        char[] fileName = klass.getFileName();
        int start = CharOperation.lastIndexOf('/', fileName) + 1;
        if (start == 0 || start < CharOperation.lastIndexOf('\\', fileName))
            start = CharOperation.lastIndexOf('\\', fileName) + 1;

        return new String(CharOperation.subarray(fileName, start, -1));
    }

    public boolean isBinary() {
        return klass.isBinaryBinding();
    }

    @Override
    public boolean isLoadedFromSource() {
        return false;
    }

	@Override
	public boolean isAnonymous() {
		return klass.isAnonymousType();
	}

    @Override
    public boolean isJavaSource() {
        return (klass instanceof SourceTypeBinding) && new String(((SourceTypeBinding) klass).getFileName()).endsWith(".java");
    }
    
    public String getFullPath() {
        char[] fileName = klass.getFileName();
        int jarFileEntrySeparatorIndex = CharOperation.indexOf(IDependent.JAR_FILE_ENTRY_SEPARATOR, fileName);
        if (jarFileEntrySeparatorIndex > 0) {
            char[] jarPart = CharOperation.subarray(fileName, 0, jarFileEntrySeparatorIndex);
            IJavaElement jarPackageFragmentRoot = JavaCore.create(new String(jarPart));
            String jarPath = jarPackageFragmentRoot.getPath().toOSString();
            char[] entryPart = CharOperation.subarray(fileName, jarFileEntrySeparatorIndex + 1, fileName.length);
            return new StringBuilder(jarPath).append('!').append(entryPart).toString();
        }
        String result = new String(fileName);
        return result;
    }
}
