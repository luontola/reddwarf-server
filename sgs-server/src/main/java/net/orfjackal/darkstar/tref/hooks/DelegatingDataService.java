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

package net.orfjackal.darkstar.tref.hooks;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.service.DataService;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 */
public class DelegatingDataService implements DataService {

    private final DataService target;

    public DelegatingDataService(DataService target) {
        this.target = target;
    }

    // automatically generated delegate methods

    public long getLocalNodeId() {
        return target.getLocalNodeId();
    }

    public ManagedObject getServiceBinding(String name) {
        return target.getServiceBinding(name);
    }

    public ManagedObject getServiceBindingForUpdate(String name) {
        return target.getServiceBindingForUpdate(name);
    }

    public void setServiceBinding(String name, Object object) {
        target.setServiceBinding(name, object);
    }

    public void removeServiceBinding(String name) {
        target.removeServiceBinding(name);
    }

    public String nextServiceBoundName(String name) {
        return target.nextServiceBoundName(name);
    }

    public ManagedReference<?> createReferenceForId(BigInteger id) {
        return target.createReferenceForId(id);
    }

    public BigInteger nextObjectId(BigInteger objectId) {
        return target.nextObjectId(objectId);
    }

    public ManagedObject getBinding(String name) {
        return target.getBinding(name);
    }

    public ManagedObject getBindingForUpdate(String name) {
        return target.getBindingForUpdate(name);
    }

    public void setBinding(String name, Object object) {
        target.setBinding(name, object);
    }

    public void removeBinding(String name) {
        target.removeBinding(name);
    }

    public String nextBoundName(String name) {
        return target.nextBoundName(name);
    }

    public void removeObject(Object object) {
        target.removeObject(object);
    }

    public void markForUpdate(Object object) {
        target.markForUpdate(object);
    }

    public <T> ManagedReference<T> createReference(T object) {
        return target.createReference(object);
    }

    public BigInteger getObjectId(Object object) {
        return target.getObjectId(object);
    }

    public String getName() {
        return target.getName();
    }

    public void ready() throws Exception {
        target.ready();
    }

    public void shutdown() {
        target.shutdown();
    }
}
