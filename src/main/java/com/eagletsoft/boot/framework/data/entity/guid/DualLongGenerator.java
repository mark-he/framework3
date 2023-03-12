package com.eagletsoft.boot.framework.data.entity.guid;

import com.eagletsoft.boot.framework.common.utils.ApplicationUtils;
import com.eagletsoft.boot.framework.common.utils.SnowFlakeGenerator;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDHexGenerator;
import org.springframework.core.env.Environment;

import java.io.Serializable;

public class DualLongGenerator extends UUIDHexGenerator {

    private SnowFlakeGenerator snowFlakeGenerator;

    public DualLongGenerator() {
        Environment env = ApplicationUtils.getBean(Environment.class);
        int workerId = Integer.parseInt(env.getProperty("server.worker.id"));
        int datacenterId = Integer.parseInt(env.getProperty("server.datacenter.id"));
        snowFlakeGenerator = new SnowFlakeGenerator(datacenterId, workerId);
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        if(object instanceof IdAssignable) {
            IdAssignable identifiable = (IdAssignable) object;
            Serializable id = identifiable.getId();
            if(id != null) {
                return id;
            }
        }
        return snowFlake();
    }

    public long snowFlake() {
        return snowFlakeGenerator.nextId();
    }

}
