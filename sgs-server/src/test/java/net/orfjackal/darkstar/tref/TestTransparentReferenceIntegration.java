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

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.auth.Identity;
import com.sun.sgs.impl.kernel.StandardProperties;
import com.sun.sgs.kernel.TransactionScheduler;
import com.sun.sgs.service.DataService;
import com.sun.sgs.test.util.SgsTestNode;
import com.sun.sgs.test.util.TestAbstractKernelRunnable;
import java.io.Serializable;
import java.util.Properties;
import net.orfjackal.dimdwarf.api.internal.EntityApi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Esko Luontola
 */
public class TestTransparentReferenceIntegration extends Assert {

    private SgsTestNode serverNode;
    private TransactionScheduler txnScheduler;
    private DataService dataService;
    private Identity taskOwner;

    private final EntityApi entityApi = new DarkstarEntityApi();

    @Before
    public void setUp() throws Exception {
        Properties props = SgsTestNode.getDefaultProperties("TestTransparentReferenceIntegration", null, null);
        props.setProperty(StandardProperties.DATA_SERVICE, TransparentReferenceDataService.class.getName());

        serverNode = new SgsTestNode("TestTransparentReferenceIntegration", null, props);
        txnScheduler = serverNode.getSystemRegistry().getComponent(TransactionScheduler.class);
        dataService = serverNode.getDataService();
        taskOwner = serverNode.getProxy().getCurrentOwner();
    }

    @After
    public void tearDown() throws Exception {
        serverNode.shutdown(true);
    }


    @Test
    public void directly_referenced_managed_objects_are_replaced_with_proxies() throws Exception {
        txnScheduler.runTask(new TestAbstractKernelRunnable() {
            public void run() throws Exception {
                MyInterface a = new MyObject("A");
                MyInterface b = new MyObject("B");
                a.setOther(b);
                dataService.setBinding("a", a);
                assertEquals("A", a.getName());
                assertEquals("B", b.getName());
                assertIsEntity(a);
                assertIsEntity(b);
            }
        }, taskOwner);
        txnScheduler.runTask(new TestAbstractKernelRunnable() {
            public void run() throws Exception {
                MyInterface a = (MyInterface) dataService.getBinding("a");
                MyInterface b = (MyInterface) a.getOther();
                assertEquals("A", a.getName());
                assertEquals("B", b.getName());
                assertIsEntity(a);
                assertIsProxy(b);
            }
        }, taskOwner);
    }

    @Test
    public void cyclic_managed_object_graphs_must_not_cause_infinite_recursion_on_serialization() throws Exception {
        txnScheduler.runTask(new TestAbstractKernelRunnable() {
            public void run() throws Exception {
                MyInterface a = new MyObject("A");
                MyInterface b = new MyObject("B");
                a.setOther(b);
                b.setOther(a);
                dataService.setBinding("a", a);
            }
        }, taskOwner);
    }


    private void assertIsEntity(Object obj) {
        assertTrue(entityApi.isEntity(obj));
        assertFalse(entityApi.isTransparentReference(obj));
    }

    private void assertIsProxy(Object obj) {
        assertFalse(entityApi.isEntity(obj));
        assertTrue(entityApi.isTransparentReference(obj));
    }


    private static class MyObject implements MyInterface, ManagedObject, Serializable {

        private final String name;
        private Object other;

        public MyObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Object getOther() {
            return other;
        }

        public void setOther(Object other) {
            this.other = other;
        }
    }

    private interface MyInterface {

        String getName();

        Object getOther();

        void setOther(Object other);
    }
}
