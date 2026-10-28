package com.sdan.middleware.Application.Ports.Security;

import com.sdan.middleware.Domain.Entities.User;

public interface ITokenGenerator {
    //TENER EN CUENTA: es mejor solicitar un dto antes que la entidad
    String Generate(User user);
}
