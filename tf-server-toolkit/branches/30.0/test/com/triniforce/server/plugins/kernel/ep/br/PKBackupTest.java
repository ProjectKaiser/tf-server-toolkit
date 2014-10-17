/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.extensions.IPKRootExtensionPoint;
import com.triniforce.extensions.PKRootExtensionPoint;
import com.triniforce.server.plugins.kernel.ep.br.Entries.Entry1_MyClass1;
import com.triniforce.utils.ApiStack;

public class PKBackupTest extends TFTestCase {
    
    @Override
    public void test() throws Exception {
        //test init/finit
        {
            //one record will go to error log
            incExpectedLogErrorCount(1);
            
            IPKRootExtensionPoint rep = new PKRootExtensionPoint();
            PKEPBackupRestore br = new PKEPBackupRestore();
            rep.putExtensionPoint(br);
            PKBackup pkb = new PKBackup(br, m_archive);
            
            Mockery ctx = new Mockery();
            final Sequence seqInit = ctx.sequence("seqInit");
            final IPKEPBackupRestoreEntry bre1 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre1");
            final IPKEPBackupRestoreEntry bre2 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre2");
            br.putExtension("bre1", bre1);
            br.putExtension("bre2", bre2);
            
            ctx.checking(new Expectations(){{
                exactly(1).of(bre1).initBackup(                
                );inSequence(seqInit);
                exactly(1).of(bre2).initBackup(                
                );inSequence(seqInit);                
            }});
            pkb.init();
            ctx.assertIsSatisfied();
            
            //finit

            ctx.checking(new Expectations(){{
                exactly(1).of(bre2).finitBackup(                
                );inSequence(seqInit);will(throwException(new RuntimeException("Must not be shown")));
                exactly(1).of(bre1).finitBackup(                
                );inSequence(seqInit);                
            }});
            pkb.finit();
            ctx.assertIsSatisfied();
        }
    }

    File m_archive;
    File m_tmpFolder;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_tmpFolder = getTmpFolder(this);
        m_archive = new File(m_tmpFolder, "archive.zip");
    }

    void createArchiveEntry1Entry2(){
        IPKRootExtensionPoint rep = new PKRootExtensionPoint();
        PKEPBackupRestore br = new PKEPBackupRestore();
        rep.putExtensionPoint(br);
        final IPKEPBackupRestoreEntry bre1 = new Entries.Entry1_MyClass1();
        final IPKEPBackupRestoreEntry bre2 = new Entries.Entry2_MyClass1_MyClass2();
        br.putExtension("bre1", bre1);
        br.putExtension("bre2", bre2);
        PKBackup pkb = new PKBackup(br, m_archive);

        try {
            pkb.init();
            pkb.process();
        } finally {
            pkb.finit();
        }
    }
        
    public void testRestoreInitFinit2x2(){
        //two registered entries and two entries in the archive
        
        IPKRootExtensionPoint rep = new PKRootExtensionPoint();
        PKEPBackupRestore br = new PKEPBackupRestore();
        rep.putExtensionPoint(br);
        Mockery ctx = new Mockery();
        final Sequence seqInit = ctx.sequence("seqInit");
        final IPKEPBackupRestoreEntry bre1 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre1");
        final IPKEPBackupRestoreEntry bre2 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre2");
        br.putExtension("bre1", bre1);
        br.putExtension("bre2", bre2);
        
        //create empty archive
        {
            createArchiveEntry1Entry2();
        }

        PKRestore pkr = new PKRestore(br, m_archive);
        ctx.checking(new Expectations(){{
            exactly(1).of(bre1).initRestore(
                    with(any(IRestoreStorage.class))                
            );will(returnValue(null));inSequence(seqInit);
            exactly(1).of(bre2).initRestore(
                    with(any(IRestoreStorage.class))                
            );will(returnValue(null));inSequence(seqInit);                
        }});

        try {
            pkr.init();
            ctx.assertIsSatisfied();
        } finally {
            pkr.finit();
        }
    }
    
    public void testRestoreInitFinit3x2(){
        //three registered entries and two entries in the archive
        
        IPKRootExtensionPoint rep = new PKRootExtensionPoint();
        PKEPBackupRestore br = new PKEPBackupRestore();
        rep.putExtensionPoint(br);
        Mockery ctx = new Mockery();
        final Sequence seqInit = ctx.sequence("seqInit");
        final IPKEPBackupRestoreEntry bre1 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre1");
        final IPKEPBackupRestoreEntry bre2 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre2");
        final IPKEPBackupRestoreEntry bre3 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre3");
        br.putExtension("bre1", bre1);
        br.putExtension("bre2", bre2);
        br.putExtension("bre3", bre3);
        
        //create empty archive
        {
            createArchiveEntry1Entry2();
        }

        PKRestore pkr = new PKRestore(br, m_archive);
        ctx.checking(new Expectations(){{
            exactly(1).of(bre1).initRestore(
                    with(any(IRestoreStorage.class))                
            );will(returnValue(null));inSequence(seqInit);
            exactly(1).of(bre2).initRestore(
                    with(any(IRestoreStorage.class))                
            );will(returnValue(null));inSequence(seqInit);                
        }});
        try {
            pkr.init();
            ctx.assertIsSatisfied();
        } finally {
            pkr.finit();
        }
    }

    public void testRestoreInitFinit1x2(){
        //one registered entries and two entries in the archive
        
        IPKRootExtensionPoint rep = new PKRootExtensionPoint();
        PKEPBackupRestore br = new PKEPBackupRestore();
        rep.putExtensionPoint(br);
        Mockery ctx = new Mockery();
        final Sequence seqInit = ctx.sequence("seqInit");
        final IPKEPBackupRestoreEntry bre2 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre2");
        br.putExtension("bre2", bre2);
        
        //create empty archive
        {
            createArchiveEntry1Entry2();
        }

        PKRestore pkr = new PKRestore(br, m_archive);
        ctx.checking(new Expectations(){{
            exactly(1).of(bre2).initRestore(
                    with(any(IRestoreStorage.class))                
            );will(returnValue(null));inSequence(seqInit);                
        }});
        try {
            pkr.init();
            ctx.assertIsSatisfied();
        } finally {
            pkr.finit();
        }
    }

   
    public void testRestoreInitFailure(){
        //two registered entries and two entries in the archive
        
        IPKRootExtensionPoint rep = new PKRootExtensionPoint();
        PKEPBackupRestore br = new PKEPBackupRestore();
        rep.putExtensionPoint(br);
        Mockery ctx = new Mockery();
        final Sequence seqInit = ctx.sequence("seqInit");
        final IPKEPBackupRestoreEntry bre1 = ctx.mock(IPKEPBackupRestoreEntry.class, "bre1");
        br.putExtension("bre1", bre1);
        
        //create empty archive
        {
            createArchiveEntry1Entry2();
        }

        PKRestore pkr = new PKRestore(br, m_archive);
        ctx.checking(new Expectations(){{
            exactly(1).of(bre1).initRestore(
                    with(any(IRestoreStorage.class))                
            );will(returnValue("Oops"));inSequence(seqInit);                    
        }});

        try{
            try {
                pkr.init();                
                fail();
            } catch (EEntryMayNotBeRestored e) {
                trace(e);
                assertEquals("Oops", e.getReason());
            }
        }finally{
            pkr.finit();
        }
        ctx.assertIsSatisfied();
    }
    
    public void testRestoreEntry4_TempFolder(){
        PKEPBackupRestore br = new PKEPBackupRestore();
        final IPKEPBackupRestoreEntry bre1 = new Entries.Entry4_TempFolder();
        br.putExtension("bre1", bre1);
        
        PKBackup pb = new PKBackup(br, m_archive);
        pb.init_process_finit();
        
        PKRestore pr = new PKRestore(br, m_archive);
        pr.init_process_finit();
        
    }
    
    public void testRestoreEntry1Entry2(){
        createArchiveEntry1Entry2();
        PKEPBackupRestore br = new PKEPBackupRestore();
        final IPKEPBackupRestoreEntry bre1 = new Entries.Entry1_MyClass1();
        final IPKEPBackupRestoreEntry bre2 = new Entries.Entry2_MyClass1_MyClass2();
        br.putExtension("bre1", bre1);
        br.putExtension("bre2", bre2);

        PKRestore pkr = new PKRestore(br, m_archive);
        TestProps tp = new TestProps();
        ApiStack.pushInterface(TestProps.class, tp);
        try {
            pkr.init();
            pkr.process();
        } finally {
            pkr.finit();
            ApiStack.popInterface(1);
        }
        
        
        {
            Entries.Entry1_MyClass1 emc1 = (Entry1_MyClass1) tp
                    .getObject(Entries.Entry1_MyClass1.class);
            emc1.assertEquals();
        }
        {
            Entries.Entry2_MyClass1_MyClass2 emc2 = (Entries.Entry2_MyClass1_MyClass2) tp
                    .getObject(Entries.Entry2_MyClass1_MyClass2.class);
            emc2.assertEquals();
        }
    }
    
}