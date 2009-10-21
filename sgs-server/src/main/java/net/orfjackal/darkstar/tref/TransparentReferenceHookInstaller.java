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

import com.google.inject.Provider;
import com.sun.sgs.impl.hook.HookLocator;
import com.sun.sgs.service.data.ManagedObjectReplacementHook;
import com.sun.sgs.service.data.SerializationHook;
import net.orfjackal.dimdwarf.api.internal.EntityApi;
import net.orfjackal.dimdwarf.api.internal.TransparentReference;
import net.orfjackal.dimdwarf.entities.EntityReferenceFactory;
import net.orfjackal.dimdwarf.entities.tref.ReplaceEntitiesWithTransparentReferences;
import net.orfjackal.dimdwarf.entities.tref.TransparentReferenceFactory;
import net.orfjackal.dimdwarf.entities.tref.TransparentReferenceFactoryImpl;
import net.orfjackal.dimdwarf.serial.MetadataBuilder;
import net.orfjackal.dimdwarf.serial.SerializationReplacer;

/**
 * @author Esko Luontola
 */
public class TransparentReferenceHookInstaller {

    private final TrefMoReplacementHook managedObjectReplacementHook;
    private final TrefSerializationHook serializationHook;

    public TransparentReferenceHookInstaller() {
        EntityApi entityApi = new DarkstarEntityApi();
        EntityReferenceFactory referenceFactory = new EntityReferenceAdapterFactory();
        TransparentReferenceFactory trefFactory = new TransparentReferenceFactoryImpl(providerFor(referenceFactory));
        SerializationReplacer serializationReplacer = new ReplaceEntitiesWithTransparentReferences(trefFactory, entityApi);

        managedObjectReplacementHook = new TrefMoReplacementHook(entityApi);
        serializationHook = new TrefSerializationHook(serializationReplacer);
    }

    private static <T> Provider<T> providerFor(final T target) {
        return new Provider<T>() {
            public T get() {
                return target;
            }
        };
    }

    public void install() {
        HookLocator.setManagedObjectReplacementHook(managedObjectReplacementHook);
        HookLocator.setSerializationHook(serializationHook);
    }

    public void uninstall() {
        HookLocator.setManagedObjectReplacementHook(null);
        HookLocator.setSerializationHook(null);
    }


    private static class TrefSerializationHook implements SerializationHook {

        private final SerializationReplacer replacer;
        private final MetadataBuilder nullMetadataBuilder = new MetadataBuilder() {
            public void append(Class<?> key, Object value) {
            }
        };

        public TrefSerializationHook(SerializationReplacer replacer) {
            this.replacer = replacer;
        }

        public Object replaceObject(Object topLevelObject, Object object) {
            return replacer.replaceSerialized(topLevelObject, object, nullMetadataBuilder);
        }

        public Object resolveObject(Object object) {
            return replacer.resolveDeserialized(object, nullMetadataBuilder);
        }
    }

    private static class TrefMoReplacementHook implements ManagedObjectReplacementHook {
        private final EntityApi entityApi;

        public TrefMoReplacementHook(EntityApi entityApi) {
            this.entityApi = entityApi;
        }

        public <T> T replaceManagedObject(T object) {
            if (entityApi.isTransparentReference(object)) {
                return unwrapProxy(object);
            }
            return object;
        }

        @SuppressWarnings({"unchecked"})
        private <T> T unwrapProxy(T object) {
            TransparentReference proxy = (TransparentReference) object;
            return (T) proxy.getEntity$TREF();
        }
    }

}
