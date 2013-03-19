/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.griffon.ast;

import griffon.plugins.scaffolding.ScaffoldingUtils;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static griffon.util.GriffonNameUtils.*;
import static org.codehaus.griffon.ast.GriffonASTUtils.*;

/**
 * Handles generation of code for the {@code @CommandObject} annotation.
 * <p/>
 *
 * @author Andres Almiray
 */
public class SwingCommandObjectASTTransformation extends CommandObjectASTTransformation {
    private static final String PROPERTY = "Property";
    private static final String VALUE = "Value";
    private static final String VALUE_ARG = "value";
    private static final String SET_VALUE = "setValue";
    private static final String SET_PRIMITIVE = "setPrimitive";

    private static final Map<ClassNode, ClassNode> SUPPORTED_ATOM_TYPES = new LinkedHashMap<ClassNode, ClassNode>();

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes  the ast nodes
     * @param source the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        checkNodesForAnnotationAndType(nodes[0], nodes[1]);

        if (SUPPORTED_ATOM_TYPES.isEmpty()) {
            for (Map.Entry<Class, Class> entry : ScaffoldingUtils.initializeAtomTypes().entrySet()) {
                SUPPORTED_ATOM_TYPES.put(makeClassSafe(entry.getKey()), makeClassSafe(entry.getValue()));
            }
        }

        ClassNode classNode = (ClassNode) nodes[1];

        List<PropertyNode> properties = new ArrayList<PropertyNode>(classNode.getProperties());
        for (PropertyNode propertyNode : properties) {
            if (propertyNode.isStatic() || propertyNode.isSynthetic() || propertyNode.isDynamicTyped()
                || !propertyNode.isPublic() || !isPropertyTypeSupported(propertyNode))
                continue;

            Expression initialValue = propertyNode.getField().getInitialValueExpression();
            String propertyName = propertyNode.getField().getName();
            classNode.removeField(propertyName);
            classNode.getProperties().remove(propertyNode);
            ClassNode propertyType = SUPPORTED_ATOM_TYPES.get(propertyNode.getType());

            FieldNode fieldNode = classNode.addField(
                propertyName,
                ACC_FINAL | ACC_PRIVATE,
                makeClassSafe(propertyType),
                ctor(makeClassSafe(propertyType), NO_ARGS));

            injectMethod(classNode, new MethodNode(
                getGetterName(propertyName),
                ACC_PUBLIC,
                makeClassSafe(propertyNode.getType()),
                params(),
                ClassNode.EMPTY_ARRAY,
                returns(call(field(fieldNode), uncapitalize(propertyNode.getType().getNameWithoutPackage()) + VALUE, NO_ARGS))
            ));

            injectMethod(classNode, new MethodNode(
                getSetterName(propertyName),
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                params(param(propertyNode.getType(), VALUE_ARG)),
                ClassNode.EMPTY_ARRAY,
                stmnt(call(field(fieldNode), SET_VALUE, args(var(VALUE_ARG))))
            ));

            injectMethod(classNode, new MethodNode(
                uncapitalize(propertyName) + PROPERTY,
                ACC_PUBLIC,
                makeClassSafe(propertyType),
                params(),
                ClassNode.EMPTY_ARRAY,
                returns(field(fieldNode))
            ));

            if (propertyNode.getType().getTypeClass().isPrimitive()) {
                classNode.addObjectInitializerStatements(
                    stmnt(call(field(classNode, propertyName), SET_PRIMITIVE, args(constx(true))))
                );
            }
            if (initialValue != null) {
                classNode.addObjectInitializerStatements(
                    stmnt(call(THIS, getSetterName(propertyName), args(initialValue)))
                );
            }
        }
    }

    private static boolean isPropertyTypeSupported(PropertyNode propertyNode) {
        return SUPPORTED_ATOM_TYPES.keySet().contains(propertyNode.getType());
    }
}
