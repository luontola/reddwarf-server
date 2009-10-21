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

import com.sun.sgs.app.*;
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import net.orfjackal.dimdwarf.entities.EntityReferenceFactory;

/**
 * @author Esko Luontola
 */
public class EntityReferenceAdapterFactory implements EntityReferenceFactory {

    public <T> EntityReference<T> createReference(T entity) {
        ManagedReference<T> ref = AppContext.getDataManager().createReference(entity);
        return new EntityReferenceAdapter<T>(ref);
    }
}
