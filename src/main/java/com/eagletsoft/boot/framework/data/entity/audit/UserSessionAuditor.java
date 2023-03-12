package com.eagletsoft.boot.framework.data.entity.audit;

import com.eagletsoft.boot.framework.common.session.UserSession;
import org.springframework.data.domain.AuditorAware;

import java.io.Serializable;
import java.util.Optional;

public class UserSessionAuditor implements AuditorAware<Serializable> {
    @Override
    public Optional<Serializable> getCurrentAuditor() {
        return Optional.of(UserSession.getUserInterface().getId());
    }
}
