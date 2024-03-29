/*
* Copyright (c) 2021 PSPACE, inc. KSAN Development Team ksan@pspace.co.kr
* KSAN is a suite of free software: you can redistribute it and/or modify it under the terms of
* the GNU General Public License as published by the Free Software Foundation, either version 
* 3 of the License.  See LICENSE for details
*
* 본 프로그램 및 관련 소스코드, 문서 등 모든 자료는 있는 그대로 제공이 됩니다.
* KSAN 프로젝트의 개발자 및 개발사는 이 프로그램을 사용한 결과에 따른 어떠한 책임도 지지 않습니다.
* KSAN 개발팀은 사전 공지, 허락, 동의 없이 KSAN 개발에 관련된 모든 결과물에 대한 LICENSE 방식을 변경 할 권리가 있습니다.
*/

package com.pspace.ifsmover.rest;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.pspace.ifsmover.rest.handler.GW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private final static String VERSION = "0.1.6";

    private static ScheduledExecutorService serviceEmptyTrash = null;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            if (args[0].equals("-v")) {
                System.out.println("ifsmoverRest version : " + VERSION);
                System.exit(0);
            }
        }

        findIfsMover();
        // serviceEmptyTrash = Executors.newSingleThreadScheduledExecutor();
        // serviceEmptyTrash.scheduleAtFixedRate(new DeleteConfigFile(), 0, 1, TimeUnit.DAYS);

        logger.info("ifsmoverRest Start ...");
        GW gw = new GW();
        try {
            gw.init();
            gw.start();
            gw.join();
        } catch (IllegalStateException e) {
            PrintStack.logging(logger, e);
        } catch (Exception e) {
            PrintStack.logging(logger, e);
        } finally {
            gw.stop();
        }
    }

    private static void findIfsMover() {
        File file = new File(RestConfig.getInstance().getPath() + "/ifs_mover");
        if (!file.exists()) {
            System.out.println();
            System.out.println("Can't find ifs_mover");
            System.out.println("ifsmover path : " + RestConfig.getInstance().getPath());
            System.out.println("Check /usr/local/pspace/etc/moverrest/ifsmoverRest.conf - ifsmover_path");
            System.exit(-1);
        }
    }
}
