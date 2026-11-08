package com.sdan.middleware.Application.Outputs;

import java.io.InputStream;

public record FileDownloadOutput(
        InputStream stream,
        String fileName
) {}

