package com.sdan.middleware.Application.Inputs.Commands;

import com.sdan.middleware.Domain.ValueObjects.UserName;

public record SignUpUserCommand(
        UserName userName,
        String password
) {}
