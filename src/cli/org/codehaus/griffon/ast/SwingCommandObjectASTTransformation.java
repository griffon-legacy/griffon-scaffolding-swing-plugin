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

import griffon.plugins.scaffolding.AtomicValue;
import griffon.plugins.scaffolding.atoms.*;
import griffon.plugins.scaffolding.atoms.StringValue;
import griffon.util.ApplicationClassLoader;
import griffon.util.CollectionUtils;
import griffon.util.RunnableWithArgs;
import griffon.util.RunnableWithArgsClosure;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.SourceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;

import static griffon.util.GriffonExceptionHandler.sanitize;
import static griffon.util.GriffonNameUtils.*;
import static org.codehaus.griffon.ast.GriffonASTUtils.*;
import static org.codehaus.groovy.runtime.ResourceGroovyMethods.eachLine;

/**
 * Handles generation of code for the {@code @CommandObject} annotation.
 * <p/>
 *
 * @author Andres Almiray
 */
public class SwingCommandObjectASTTransformation extends CommandObjectASTTransformation {
    private static final Logger LOG = LoggerFactory.getLogger(SwingCommandObjectASTTransformation.class);
    private static final String PROPERTY = "Property";
    private static final String VALUE = "Value";
    private static final String VALUE_ARG = "value";
    private static final String SET_VALUE = "setValue";

    private static Map<ClassNode, ClassNode> SUPPORTED_ATOM_TYPES = CollectionUtils.<ClassNode, ClassNode>map()
        .e(makeClassSafe(BigDecimal.class), makeClassSafe(BigDecimalValue.class))
        .e(makeClassSafe(BigInteger.class), makeClassSafe(BigIntegerValue.class))
        .e(makeClassSafe(Boolean.class), makeClassSafe(BooleanValue.class))
        .e(makeClassSafe(Byte.class), makeClassSafe(ByteValue.class))
        .e(makeClassSafe(Calendar.class), makeClassSafe(CalendarValue.class))
        .e(makeClassSafe(Date.class), makeClassSafe(DateValue.class))
        .e(makeClassSafe(Double.class), makeClassSafe(DoubleValue.class))
        .e(makeClassSafe(Float.class), makeClassSafe(FloatValue.class))
        .e(makeClassSafe(Integer.class), makeClassSafe(IntegerValue.class))
        .e(makeClassSafe(Long.class), makeClassSafe(LongValue.class))
        .e(makeClassSafe(Short.class), makeClassSafe(ShortValue.class))
        .e(makeClassSafe(String.class), makeClassSafe(StringValue.class))
        .e(makeClassSafe(Boolean.TYPE), makeClassSafe(BooleanValue.class))
        .e(makeClassSafe(Byte.TYPE), makeClassSafe(ByteValue.class))
        .e(makeClassSafe(Double.TYPE), makeClassSafe(DoubleValue.class))
        .e(makeClassSafe(Float.TYPE), makeClassSafe(FloatValue.class))
        .e(makeClassSafe(Integer.TYPE), makeClassSafe(IntegerValue.class))
        .e(makeClassSafe(Long.TYPE), makeClassSafe(LongValue.class))
        .e(makeClassSafe(Short.TYPE), makeClassSafe(ShortValue.class));

    static {
        initializeAtomTypes();
    }

    private static void initializeAtomTypes() {
        Enumeration<URL> urls = null;

        try {
            urls = ApplicationClassLoader.get().getResources("META-INF/services/" + AtomicValue.class.getName());
        } catch (IOException ioe) {
            return;
        }

        if (urls == null) return;

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reading " + AtomicValue.class.getName() + " definitions from " + url);
            }

            try {
                eachLine(url, new RunnableWithArgsClosure(new RunnableWithArgs() {
                    @Override
                    public void run(Object[] args) {
                        String line = (String) args[0];
                        if (line.startsWith("#") || isBlank(line)) return;
                        try {
                            String[] parts = line.trim().split("=");
                            Class targetType = loadClass(parts[0].trim());
                            Class atomicValueClass = loadClass(parts[1].trim());
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Registering " + atomicValueClass.getName() + " as AtomicValue for " + targetType.getName());
                            }
                            SUPPORTED_ATOM_TYPES.put(makeClassSafe(targetType), makeClassSafe(atomicValueClass));
                        } catch (Exception e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("Could not load AtomicValue with " + line, sanitize(e));
                            }
                        }
                    }
                }));
            } catch (IOException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Could not load AtomicValue definitions from " + url, sanitize(e));
                }
            }
        }
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassNotFoundException cnfe = null;

        ClassLoader cl = CommandObjectASTTransformation.class.getClassLoader();
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            cnfe = e;
        }

        cl = Thread.currentThread().getContextClassLoader();
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            cnfe = e;
        }

        if (cnfe != null) throw cnfe;
        return null;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes  the ast nodes
     * @param source the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        checkNodesForAnnotationAndType(nodes[0], nodes[1]);

        ClassNode classNode = (ClassNode) nodes[1];

        List<PropertyNode> properties = new ArrayList<PropertyNode>(classNode.getProperties());
        for (PropertyNode propertyNode : properties) {
            if (propertyNode.isStatic() || propertyNode.isSynthetic() || propertyNode.isDynamicTyped()
                || !propertyNode.isPublic() || !isPropertyTypeSupported(propertyNode))
                continue;

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
        }
    }

    private static boolean isPropertyTypeSupported(PropertyNode propertyNode) {
        return SUPPORTED_ATOM_TYPES.keySet().contains(propertyNode.getType());
    }
}
