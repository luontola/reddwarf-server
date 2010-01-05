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
import com.sun.sgs.impl.service.data.DataServiceImpl;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.service.DataService;
import com.sun.sgs.service.TransactionProxy;
import com.sun.sgs.service.data.ManagedReferenceFactory;
import com.sun.sgs.service.data.SerializationHook;
import com.sun.sgs.service.data.SerializationHookFactory;
import java.util.Properties;
import net.orfjackal.darkstar.tref.hooks.DelegatingDataService;
import net.orfjackal.darkstar.tref.hooks.HookedDataService;
import net.orfjackal.darkstar.tref.hooks.ManagedObjectReplacementHook;
import net.orfjackal.dimdwarf.api.internal.EntityApi;
import net.orfjackal.dimdwarf.api.internal.TransparentReference;
import net.orfjackal.dimdwarf.entities.EntityReferenceFactory;
import net.orfjackal.dimdwarf.entities.tref.ReplaceEntitiesWithTransparentReferences;
import net.orfjackal.dimdwarf.entities.tref.TransparentReferenceFactoryImpl;
import net.orfjackal.dimdwarf.serial.MetadataBuilder;
import net.orfjackal.dimdwarf.serial.SerializationReplacer;

/**
 * @author Esko Luontola
 */
public class TransparentReferenceDataService extends DelegatingDataService {

    public TransparentReferenceDataService(Properties properties,
                                           ComponentRegistry systemRegistry,
                                           TransactionProxy txnProxy) throws Exception {
        super(init(properties, systemRegistry, txnProxy));
    }

    private static DataService init(Properties properties,
                                    ComponentRegistry systemRegistry,
                                    TransactionProxy txnProxy) throws Exception {
        EntityApi entityApi = new DarkstarEntityApi();
        DataServiceImpl dataService = new DataServiceImpl(properties, systemRegistry, txnProxy);
        dataService.setSerializationHookFactory(new TrefSerializationHookFactory(entityApi));
        return new HookedDataService(dataService, new TrefMoReplacementHook(entityApi));
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

    private static class TrefSerializationHookFactory implements SerializationHookFactory {

        private final EntityApi entityApi;
        private final ThreadLocalProvider<EntityReferenceFactory> threadLocalReferenceFactory;
        private final TransparentReferenceFactoryImpl trefFactory;

        public TrefSerializationHookFactory(EntityApi entityApi) {
            this.entityApi = entityApi;
            threadLocalReferenceFactory = new ThreadLocalProvider<EntityReferenceFactory>();
            trefFactory = new TransparentReferenceFactoryImpl(threadLocalReferenceFactory);
        }

        @Override public SerializationHook create(ManagedReferenceFactory referenceFactory) {
            threadLocalReferenceFactory.set(new EntityReferenceAdapterFactory(referenceFactory));
            SerializationReplacer serializationReplacer = new ReplaceEntitiesWithTransparentReferences(trefFactory, entityApi);
            return new TrefSerializationHook(serializationReplacer);
        }
    }

    private static class ThreadLocalProvider<T> implements Provider<T> {
        
        private final ThreadLocal<T> threadLocal = new ThreadLocal<T>();

        public void set(T value) {
            threadLocal.set(value);
        }

        @Override public T get() {
            return threadLocal.get();
        }
    }
}
