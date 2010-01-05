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
import com.sun.sgs.service.data.SerializationHook;
import java.util.Properties;
import net.orfjackal.darkstar.tref.hooks.DelegatingDataService;
import net.orfjackal.darkstar.tref.hooks.HookedDataService;
import net.orfjackal.darkstar.tref.hooks.ManagedObjectReplacementHook;
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
        EntityReferenceFactory referenceFactory = new EntityReferenceAdapterFactory();
        TransparentReferenceFactory trefFactory = new TransparentReferenceFactoryImpl(providerFor(referenceFactory));
        SerializationReplacer serializationReplacer = new ReplaceEntitiesWithTransparentReferences(trefFactory, entityApi);

        SerializationHook serializationHook = new TrefSerializationHook(serializationReplacer);
        ManagedObjectReplacementHook replacementHook = new TrefMoReplacementHook(entityApi);

        DataServiceImpl dataService = new DataServiceImpl(properties, systemRegistry, txnProxy);
        dataService.setSerializationHook(serializationHook);
        return new HookedDataService(dataService, replacementHook);
    }

    private static <T> Provider<T> providerFor(final T target) {
        return new Provider<T>() {
            public T get() {
                return target;
            }
        };
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
