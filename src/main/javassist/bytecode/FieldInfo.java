/*
 * Javassist, a Java-bytecode translator toolkit.
 * Copyright (C) 1999-2004 Shigeru Chiba. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License.  Alternatively, the contents of this file may be used under
 * the terms of the GNU Lesser General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */

package javassist.bytecode;

import javassist.bytecode.annotation.AnnotationGroup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

/**
 * <code>field_info</code> structure.
 *
 * @see javassist.CtField#getFieldInfo()
 */
public final class FieldInfo {
    ConstPool constPool;
    int accessFlags;
    int name;
    int descriptor;
    LinkedList attribute;       // may be null.
    AnnotationGroup runtimeInvisible;
    AnnotationGroup runtimeVisible;

    private FieldInfo(ConstPool cp) {
        constPool = cp;
        accessFlags = 0;
        attribute = null;
    }

    /**
     * Constructs a <code>field_info</code> structure.
     *
     * @param cp                a constant pool table
     * @param fieldName         field name
     * @param desc              field descriptor
     *
     * @see Descriptor
     */
    public FieldInfo(ConstPool cp, String fieldName, String desc) {
        this(cp);
        name = cp.addUtf8Info(fieldName);
        descriptor = cp.addUtf8Info(desc);
    }

    FieldInfo(ConstPool cp, DataInputStream in) throws IOException {
        this(cp);
        read(in);
    }

    /**
     * Returns the constant pool table used
     * by this <code>field_info</code>.
     */
    public ConstPool getConstPool() {
        return constPool;
    }

    /**
     * Returns the field name.
     */
    public String getName() {
        return constPool.getUtf8Info(name);
    }

    /**
     * Sets the field name.
     */
    public void setName(String newName) {
        name = constPool.addUtf8Info(newName);
    }

    /**
     * Returns the access flags.
     *
     * @see AccessFlag
     */
    public int getAccessFlags() {
        return accessFlags;
    }

    /**
     * Sets the access flags.
     *
     * @see AccessFlag
     */
    public void setAccessFlags(int acc) {
        accessFlags = acc;
    }

    /**
     * Returns the field descriptor.
     *
     * @see Descriptor
     */
    public String getDescriptor() {
        return constPool.getUtf8Info(descriptor);
    }

    /**
     * Sets the field descriptor.
     *
     * @see Descriptor
     */
    public void setDescriptor(String desc) {
        if (!desc.equals(getDescriptor()))
            descriptor = constPool.addUtf8Info(desc);
    }

    /**
     * Returns all the attributes.
     *
     * @return a list of <code>AttributeInfo</code> objects.
     * @see AttributeInfo
     */
    public List getAttributes() {
        if (attribute == null)
            attribute = new LinkedList();

        return attribute;
    }

    /**
     * Returns the attribute with the specified name.
     *
     * @param name      attribute name
     */
    public AttributeInfo getAttribute(String name) {
        return AttributeInfo.lookup(attribute, name);
    }

    /**
     * Appends an attribute.  If there is already an attribute with
     * the same name, the new one substitutes for it.
     */
    public void addAttribute(AttributeInfo info) {
        if (attribute == null)
            attribute = new LinkedList();

        AttributeInfo.remove(attribute, info.getName());
        attribute.add(info);
    }

    /**
     * Create an empty (null) attribute "RuntimeInvisibleAnnotations"
     * Usually used so that you can start adding annotations to a particular thing
     */
    public void createRuntimeInvisibleGroup() {
        if (runtimeInvisible == null) {
            AttributeInfo attr =
                new AttributeInfo(constPool, "RuntimeInvisibleAnnotations");
            addAttribute(attr);
            runtimeInvisible = new AnnotationGroup(attr);
        }
    }

    /**
     * Create an empty (null) attribute "RuntimeVisibleAnnotations"
     * Usually used so that you can start adding annotations to a particular thing
     */
    public void createRuntimeVisibleGroup() {
        if (runtimeVisible == null) {
            AttributeInfo attr =
                new AttributeInfo(constPool, "RuntimeVisibleAnnotations");
            addAttribute(attr);
            runtimeVisible = new AnnotationGroup(attr);
        }
    }

    /**
     * Return access object for getting info about annotations
     * This returns runtime invisible annotations as pertains to the
     * CLASS RetentionPolicy
     * @return
     */
    public AnnotationGroup getRuntimeInvisibleAnnotations() {
        if (runtimeInvisible != null)
            return runtimeInvisible;
        AttributeInfo invisible = getAttribute("RuntimeInvisibleAnnotations");
        if (invisible == null)
            return null;
        runtimeInvisible = new AnnotationGroup(invisible);
        return runtimeInvisible;
    }

    /**
     * Return access object for getting info about annotations
     * This returns runtime visible annotations as pertains to the
     * RUNTIME RetentionPolicy
     * @return
     */
    public AnnotationGroup getRuntimeVisibleAnnotations() {
        if (runtimeVisible != null)
            return runtimeVisible;
        AttributeInfo visible = getAttribute("RuntimeVisibleAnnotations");
        if (visible == null)
            return null;
        runtimeVisible = new AnnotationGroup(visible);
        return runtimeVisible;
    }

    private void read(DataInputStream in) throws IOException {
        accessFlags = in.readUnsignedShort();
        name = in.readUnsignedShort();
        descriptor = in.readUnsignedShort();
        int n = in.readUnsignedShort();
        attribute = new LinkedList();
        for (int i = 0; i < n; ++i)
            attribute.add(AttributeInfo.read(constPool, in));
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(accessFlags);
        out.writeShort(name);
        out.writeShort(descriptor);
        if (attribute == null)
            out.writeShort(0);
        else {
            out.writeShort(attribute.size());
            AttributeInfo.writeAll(attribute, out);
        }
    }
}
