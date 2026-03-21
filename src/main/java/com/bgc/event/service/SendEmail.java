package com.bgc.event.service;

import com.bgc.event.entity.User;

public interface SendEmail {
    void sendHtmlEmail(User user) throws Exception;
}
