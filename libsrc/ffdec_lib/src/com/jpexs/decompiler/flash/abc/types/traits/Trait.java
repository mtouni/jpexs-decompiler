/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.List;

public abstract class Trait implements Cloneable, Serializable {

    private static final int[] EMPTY_METADATA_ARRAY = new int[0];

    public int name_index;

    public int kindType;

    public int kindFlags;

    public int[] metadata = EMPTY_METADATA_ARRAY;

    public long fileOffset;

    public byte[] bytes;

    public static final int ATTR_Final = 0x1;

    public static final int ATTR_Override = 0x2;

    public static final int ATTR_Metadata = 0x4;

    public static final int TRAIT_SLOT = 0;

    public static final int TRAIT_METHOD = 1;

    public static final int TRAIT_GETTER = 2;

    public static final int TRAIT_SETTER = 3;

    public static final int TRAIT_CLASS = 4;

    public static final int TRAIT_FUNCTION = 5;

    public static final int TRAIT_CONST = 6;

    public abstract void delete(ABC abc, boolean d);

    public GraphTextWriter getModifiers(ABC abc, boolean isStatic, GraphTextWriter writer) {
        if ((kindFlags & ATTR_Override) > 0) {
            writer.appendNoHilight("override ");
        }
        Multiname m = getName(abc);
        if (m != null) {
            String nsname = "";
            for (ABCContainerTag abcTag : abc.getAbcTags()) {
                if (m.namespace_index == -1) {
                    break;
                }
                DottedChain dc = abcTag.getABC().nsValueToName(abc.constants.getNamespace(m.namespace_index).getName(abc.constants, true));
                nsname = dc.getLast();

                if (nsname == null) {
                    break;
                }
                if (!nsname.isEmpty()) {
                    break;
                }
            }

            Namespace ns = m.getNamespace(abc.constants);

            if (nsname != null) {
                String identifier = IdentifiersDeobfuscation.printIdentifier(true, nsname);
                if (identifier != null && !identifier.isEmpty()) {
                    writer.appendNoHilight(identifier).appendNoHilight(" ");
                }
            }
            if (ns != null) {
                String nsPrefix = ns.getPrefix(abc);
                if (nsPrefix != null && !nsPrefix.isEmpty()) {
                    writer.appendNoHilight(nsPrefix).appendNoHilight(" ");
                }
            }
        }
        if (isStatic) {
            if ((this instanceof TraitSlotConst) && ((TraitSlotConst) this).isNamespace()) {
                //static is automatic
            } else {
                writer.appendNoHilight("static ");
            }
        }
        if ((kindFlags & ATTR_Final) > 0) {
            if (!isStatic) {
                writer.appendNoHilight("final ");
            }
        }
        return writer;
    }

    @Override
    public String toString() {
        return "name_index=" + name_index + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
    }

    public GraphTextWriter toString(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        writer.appendNoHilight(abc.constants.getMultiname(name_index).toString(abc.constants, fullyQualifiedNames) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata));
        return writer;
    }

    public void convert(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
    }

    public GraphTextWriter toStringPackaged(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = ns.getName(abc.constants, false).toPrintableString(true);
            writer.appendNoHilight("package");
            if (!nsname.isEmpty()) {
                writer.appendNoHilight(" " + nsname); //assume not null name
            }
            writer.startBlock();
            toString(parent, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            writer.endBlock();
            writer.newLine();
        }
        return writer;
    }

    public void convertPackaged(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        Namespace ns = abc.constants.getMultiname(name_index).getNamespace(abc.constants);
        if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
            String nsname = ns.getName(abc.constants, false).toPrintableString(true);
            convert(parent, path + nsname, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        }
    }

    public GraphTextWriter toStringHeader(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        toString(parent, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
        return writer;
    }

    public void convertHeader(Trait parent, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        convert(parent, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
    }

    public Multiname getName(ABC abc) {
        if (name_index == 0) {
            return null;
        } else {
            return abc.constants.getMultiname(name_index);
        }
    }

    public abstract int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException;

    public ClassPath getPath(ABC abc) {
        Multiname name = getName(abc);
        Namespace ns = name.getNamespace(abc.constants);
        DottedChain packageName = ns.getName(abc.constants, false);
        String objectName = name.getName(abc.constants, null, true);
        return new ClassPath(packageName, objectName); //assume not null name
    }

    @Override
    public Trait clone() {
        try {
            Trait ret = (Trait) super.clone();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}
