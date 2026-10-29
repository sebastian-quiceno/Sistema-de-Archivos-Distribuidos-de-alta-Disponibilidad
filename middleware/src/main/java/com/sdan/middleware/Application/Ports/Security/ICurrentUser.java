package com.sdan.middleware.Application.Ports.Security;

import java.util.List;
import java.util.UUID;

//TODO: revisar si esta bien implementada la interfaz
public interface ICurrentUser {
    UUID getUserId();
}
