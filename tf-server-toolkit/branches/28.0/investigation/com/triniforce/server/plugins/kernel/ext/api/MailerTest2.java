/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ext.api;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.TFUtils;

/*


test.properties must have the following settings:

Mailer.host=mail
Mailer.port=25
Mailer.user=gmp
Mailer.password=
Mailer.useTLS=0
Mailer.from=gmp@sigma-soft.spb.ru
Mailer.to=maxim.ge@gmail.com
 
 */

public class MailerTest2 extends BasicServerRunningTestCase {
    public class MailerSettings implements IMailerSettings {

        private String m_smtpHost;
        private int m_smtpPort;
        private String m_smtpUser;
        private String m_smtpPassword;
        private boolean m_useTLS = false;

        public MailerSettings() {
            m_smtpHost = getMustHaveTestProperty("Mailer.host");
            m_smtpPort = Integer
                    .parseInt(getMustHaveTestProperty("Mailer.port"));
            m_smtpUser = getMustHaveTestProperty("Mailer.user");
            m_smtpPassword = getMustHaveTestProperty("Mailer.password");
            m_useTLS = TFUtils.equals(1,
                    getMustHaveTestProperty("Mailer.useTLS"));
        }

        public void loadSettings() {
        }

        public String getSmtpHost() {
            return m_smtpHost;
        }

        public int getSmtpPort() {
            return m_smtpPort;
        }

        public String getSmtpUser() {
            return m_smtpUser;
        }

        public String getSmtpPassword() {
            return m_smtpPassword;
        }

        public boolean useTLS() {
            return m_useTLS;
        }

    }
    
    boolean registered;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IThrdWatcherRegistrator twr = ApiStack
                .getInterface(IThrdWatcherRegistrator.class);
        twr.registerThread(Thread.currentThread(), null);
        registered = true;
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(registered){
            IThrdWatcherRegistrator twr = ApiStack
                .getInterface(IThrdWatcherRegistrator.class);
            twr.unregisterThread(Thread.currentThread());
            registered = false;
        }
        super.tearDown();
    }

    @Override
    public void test() throws Exception {

        String from = getMustHaveTestProperty("Mailer.from");
        String to = getMustHaveTestProperty("Mailer.to");

        Mailer mailer = new Mailer();
        Api api = new Api();

        // from rambler by SSLq
        api.setIntfImplementor(IMailerSettings.class, new MailerSettings());
        ApiStack.pushApi(api);
        try {
            mailer.send(from, to, "Mailer test subject", "Mailer test");
        } finally {
            ApiStack.popApi();
        }
    }

}
