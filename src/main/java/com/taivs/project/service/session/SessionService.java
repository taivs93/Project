package com.taivs.project.service.session;

import com.taivs.project.entity.Session;
import com.taivs.project.entity.User;

public interface SessionService {

    public Session createSession(User user);
}
