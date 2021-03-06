/*
 * Copyright 2005 Sun Microsystems, Inc.
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

package org.apache.roller.planet.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Test planet Entry functionality.
 */
public class EntryFunctionalTests  {
    
    public static Log log = LogFactory.getLog(EntryFunctionalTests.class);
    
    private Planet testPlanet = null;
    private PlanetGroup testGroup1 = null;
    private Subscription testSub1 = null;
    private Subscription testSub2 = null;
    private SubscriptionEntry testEntry1 = null;
    private SubscriptionEntry testEntry2 = null;
    private SubscriptionEntry testEntry3 = null;
    

    @BeforeEach
    public void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();

        log.info("ENTERED");
        
        testPlanet = TestUtils.setupPlanet("entryFuncTestPlanet");
        testGroup1 = TestUtils.setupGroup(testPlanet, "entryFuncTestGroup");
        testSub1 = TestUtils.setupSubscription("entryFuncTestSub1");
        testSub2 = TestUtils.setupSubscription("entryFuncTestSub2");
        testEntry1 = TestUtils.setupEntry(testSub1, "entryFuncTestEntry1");
        testEntry2 = TestUtils.setupEntry(testSub2, "entryFuncTestEntry2");
        testEntry3 = TestUtils.setupEntry(testSub2, "entryFuncTestEntry3");
        
        // now associate both subscriptions with the test group
        testGroup1.getSubscriptions().add(testSub1);
        testSub1.getGroups().add(testGroup1);
        
        testGroup1.getSubscriptions().add(testSub2);
        testSub2.getGroups().add(testGroup1);
        
        WebloggerFactory.getWeblogger().getPlanetManager().saveGroup(testGroup1);
        WebloggerFactory.getWeblogger().flush();
        
        log.info("EXITED");
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        log.info("ENTERED");
        
        TestUtils.teardownSubscription(testSub1.getId());
        TestUtils.teardownSubscription(testSub2.getId());
        TestUtils.teardownGroup(testGroup1.getId());
        TestUtils.teardownPlanet(testPlanet.getId());
        
        log.info("EXITED");
    }
    

    @Test
    public void testEntryLookups() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        // by id
        SubscriptionEntry entry = mgr.getEntryById(testEntry1.getId());
        assertNotNull(entry);
        assertEquals("entryFuncTestEntry1", entry.getPermalink());
        
        // by subscription
        Subscription sub = mgr.getSubscriptionById(testSub2.getId());
        assertEquals(2, sub.getEntries().size());
        
        // by subscription through manager
        assertEquals(2, mgr.getEntries(sub, 0, 10).size());
        
        // by group
        PlanetGroup group = mgr.getGroupById(testGroup1.getId());
        assertEquals(3, mgr.getEntries(group, 0, 10).size());
        
        // by group with timeframe constraint
        assertEquals(0, mgr.getEntries(group, new Date(), null, 0, 10).size());
    }
    
    @Test
    public void testDeleteEntries() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Subscription sub = mgr.getSubscriptionById(testSub2.getId());
        
        // make sure entries are there
        assertEquals(2, sub.getEntries().size());
        
        // purge entries
        mgr.deleteEntries(sub);
        TestUtils.endSession(true);
        
        // verify
        sub = mgr.getSubscriptionById(testSub2.getId());
        assertEquals(0, sub.getEntries().size());
    }
    
}
