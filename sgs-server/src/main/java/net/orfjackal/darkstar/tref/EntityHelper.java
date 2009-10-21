/*
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * This file is part of Project Darkstar Server.
 *
 * Project Darkstar Server is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation and
 * distributed hereunder to you.
 *
 * Project Darkstar Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.orfjackal.darkstar.tref;

import com.sun.sgs.app.AppContext;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.util.Objects;

import javax.annotation.Nullable;
import java.math.BigInteger;

/**
 * For transparent references to work correctly, all subclasses of {@link net.orfjackal.dimdwarf.api.internal.EntityObject} should
 * define their {@link #equals(Object)} and {@link #hashCode()} methods as follows:
 * <pre><code>
 * public boolean equals(Object obj) {
 *     return EntityHelper.equals(this, obj);
 * }
 * public int hashCode() {
 *     return EntityHelper.hashCode(this);
 * }
 * </code></pre>
 *
 * @author Esko Luontola
 */
public class EntityHelper {

    private static final EntityApi entityApi = new DarkstarEntityApi();

    private EntityHelper() {
    }

    public static boolean equals(@Nullable Object obj1, @Nullable Object obj2) {
        Object id1 = getId(obj1);
        Object id2 = getId(obj2);
        return Objects.safeEquals(id1, id2);
    }

    public static int hashCode(@Nullable Object obj) {
        Object id = getId(obj);
        return id.hashCode();
    }

    @Nullable
    private static BigInteger getId(@Nullable Object obj) {
        if (entityApi.isTransparentReference(obj)) {
            return ((TransparentReference) obj).getEntityReference$TREF().getEntityId().toBigInteger();
        } else if (entityApi.isEntity(obj)) {
            return AppContext.getDataManager().createReference(obj).getId();
        } else {
            return null;
        }
    }
}
