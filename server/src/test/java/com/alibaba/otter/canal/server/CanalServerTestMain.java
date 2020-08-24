package com.alibaba.otter.canal.server;

import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.core.CanalInstanceGenerator;
import com.alibaba.otter.canal.instance.manager.CanalInstanceWithManager;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.server.netty.CanalServerWithNetty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * CanalServerTestMain
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/8/24 16:45
 */
public class CanalServerTestMain {

    protected static final String ZK_CLUSTER_ADDRESS      = "127.0.0.1:2181";
    protected static final String DESTINATION   = "example";
    protected static final String DETECTING_SQL = "select 1";
    protected static final String MYSQL_ADDRESS = "127.0.0.1";
    protected static final String USERNAME      = "canal";
    protected static final String PASSWORD      = "canal";
    protected static final String FILTER        = ".\\*\\\\\\\\..\\*";
    /** 默认 500s 后关闭 */
    protected static final long RUN_TIME = 120 * 1000;
    private final ByteBuffer header        = ByteBuffer.allocate(4);
    private CanalServerWithNetty nettyServer;
    public static void main(String[] args) {
        CanalServerTestMain test = new CanalServerTestMain();
        try {
            test.setUp();
            System.out.println("start");
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.out.println("sleep");
            try {
                Thread.sleep(RUN_TIME);
            } catch (Throwable ee) {
            }
            test.tearDown();
            System.out.println("end");
        }
    }
    public void setUp() {
        CanalServerWithEmbedded embeddedServer = new CanalServerWithEmbedded();
        embeddedServer.setCanalInstanceGenerator(new CanalInstanceGenerator() {
            public CanalInstance generate(String destination) {
                Canal canal = buildCanal();
                return new CanalInstanceWithManager(canal, FILTER);
            }
        });
        nettyServer = CanalServerWithNetty.instance();
        nettyServer.setEmbeddedServer(embeddedServer);
        nettyServer.setPort(11111);
        nettyServer.start();
        // 启动 instance
        embeddedServer.start("example");
    }
    public void tearDown() {
        nettyServer.stop();
    }
    private Canal buildCanal() {
        Canal canal = new Canal();
        canal.setId(1L);
        canal.setName(DESTINATION);
        canal.setDesc("test");
        CanalParameter parameter = new CanalParameter();
        //parameter.setZkClusters(Arrays.asList(ZK_CLUSTER_ADDRESS));
        parameter.setMetaMode(CanalParameter.MetaMode.MEMORY);
        parameter.setHaMode(CanalParameter.HAMode.HEARTBEAT);
        parameter.setIndexMode(CanalParameter.IndexMode.MEMORY);
        parameter.setStorageMode(CanalParameter.StorageMode.MEMORY);
        parameter.setMemoryStorageBufferSize(32 * 1024);
        parameter.setSourcingType(CanalParameter.SourcingType.MYSQL);
        parameter.setDbAddresses(Arrays.asList(new InetSocketAddress(MYSQL_ADDRESS, 3306),
                new InetSocketAddress(MYSQL_ADDRESS, 3306)));
        parameter.setDbUsername(USERNAME);
        parameter.setDbPassword(PASSWORD);
        parameter.setSlaveId(1234L);
        parameter.setDefaultConnectionTimeoutInSeconds(30);
        parameter.setConnectionCharset("UTF-8");
        parameter.setConnectionCharsetNumber((byte) 33);
        parameter.setReceiveBufferSize(8 * 1024);
        parameter.setSendBufferSize(8 * 1024);
        parameter.setDetectingEnable(false);
        parameter.setDetectingIntervalInSeconds(10);
        parameter.setDetectingRetryTimes(3);
        parameter.setDetectingSQL(DETECTING_SQL);
        canal.setCanalParameter(parameter);
        return canal;
    }

}
