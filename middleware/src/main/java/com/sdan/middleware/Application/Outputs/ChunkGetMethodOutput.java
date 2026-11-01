package com.sdan.middleware.Application.Outputs;

import java.io.InputStream;
import java.util.UUID;

// Record para saber si el chunk es valido o no
// Si el archivo esta sano, retorna el bloque junto con un atributo bool para saber que fue bien
// Si el archivo esta corrupto, no retorna el bloque y dice que esta mal
public record ChunkGetMethodOutput(
        UUID chunkId,
        InputStream chunkFile,
        boolean corrupted
) {
}
