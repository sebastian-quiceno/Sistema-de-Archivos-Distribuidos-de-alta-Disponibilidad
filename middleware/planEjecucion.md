# Plan de ejecución: Corrección y mejora de GetFileUseCase y CheckFilesUseCase

## Objetivo

Implementar las seis recomendaciones identificadas durante la revisión de `GetFileUseCase` y `CheckFilesUseCase`, corrigiendo problemas de seguridad, gestión de recursos, separación de responsabilidades, consistencia de datos y escalabilidad.

El objetivo es mejorar la implementación respetando los principios SOLID, especialmente SRP, OCP y DIP, sin introducir complejidad ni abstracciones innecesarias.

## Instrucciones generales

Antes de comenzar:

1. Analiza la estructura actual del proyecto y las convenciones arquitectónicas existentes.
2. Revisa las implementaciones, interfaces, entidades, repositorios y servicios involucrados.
3. Identifica las dependencias entre componentes y determina cómo implementar cada cambio sin romper funcionalidades existentes.
4. No asumas que los ejemplos de código de este documento deben copiarse literalmente. Adáptalos a la arquitectura y al lenguaje del proyecto.
5. No realices cambios fuera del alcance de este plan, salvo que sean estrictamente necesarios para completar alguna corrección.
6. No elimines funcionalidades existentes ni introduzcas cambios incompatibles sin justificación.

## Fase 1: Corregir la validación de propiedad en GetFileUseCase

**Prioridad: Crítica**

### Tareas

- Revisar cómo `GetFileUseCase` valida actualmente que un archivo pertenece al usuario autenticado.
- Corregir cualquier condición lógica invertida o validación incorrecta.
- Encapsular la comprobación de propiedad dentro de la entidad correspondiente (`File` o `Folder`), evitando acceder directamente a relaciones internas desde el caso de uso.
- Implementar un método como `belongsToUser(UUID userId)`, adaptándolo al modelo de dominio.
- Garantizar que los valores nulos no provoquen `NullPointerException`.
- Lanzar `FileDoesNotBelongToUserException` cuando el archivo no pertenezca al usuario autenticado.

### Criterios de aceptación

- Un usuario únicamente puede obtener archivos de su propiedad.
- Los archivos pertenecientes a otros usuarios son rechazados.
- Los archivos sin carpeta o sin propietario asociado no generan errores inesperados.

## Fase 2: Corregir la gestión de streams en getChunk y lookOverIntegrity

**Prioridad: Alta**

### Tareas

- Analizar el ciclo de vida de los `InputStream` utilizados en `ChunkStorageService`.
- Revisar especialmente los métodos `getChunk`, `lookOverIntegrity` y el flujo de reconstrucción (`mergeFile`).
- Garantizar que todos los streams utilizados exclusivamente para auditoría o verificación se cierren correctamente mediante `try-with-resources`.
- Evitar consumir un stream antes de que pueda utilizarse para reconstruir o entregar el archivo.
- Si es necesario verificar la integridad durante una descarga, implementar una estrategia que permita calcular el hash sin consumir anticipadamente el stream que necesita el consumidor.
- Evitar lecturas duplicadas innecesarias y fugas de conexiones o descriptores.

### Criterios de aceptación

- Los streams se cierran correctamente cuando corresponde.
- La reconstrucción del archivo no produce archivos vacíos, incompletos o corruptos por consumo anticipado de streams.
- La verificación de integridad funciona correctamente.
- Se contemplan errores durante la lectura y el cierre de recursos.

## Fase 3: Segregar la lógica de integridad y curación

**Prioridad: Alta**

### Tareas

- Analizar las responsabilidades actuales de `ChunkStorageService` y `ChunkReplicationService`.
- Extraer la lógica de auditoría de integridad y auto-recuperación de `ChunkStorageService`.
- Crear un servicio dedicado, denominado `FileIntegrityService`, que centralice la verificación de integridad y la curación de chunks.
- Mantener `ChunkStorageService` enfocado en las operaciones de almacenamiento y recuperación de chunks.
- Reducir el acoplamiento entre almacenamiento, replicación y verificación de integridad.
- Revisar el flujo de curación para garantizar que, cuando se detecte un chunk original dañado, este también se incluya entre los fragmentos que deben curarse.
- Revisar la generación y el nombrado de claves en `healChunk`, garantizando que la copia de un chunk no termine eliminándose accidentalmente por una operación posterior.
- Garantizar que las operaciones de curación sean consistentes y no provoquen pérdida de datos.

### Criterios de aceptación

- La lógica de integridad está separada de la lógica de almacenamiento.
- La curación contempla correctamente los chunks originales dañados.
- Las claves utilizadas durante la replicación y curación son consistentes.
- No se eliminan accidentalmente los archivos recién copiados.

## Fase 4: Introducir un índice de partición en Chunk

**Prioridad: Alta**

### Tareas

- Incorporar un atributo `partNumber` en la entidad `Chunk`.
- Revisar los mecanismos de creación y persistencia de chunks para garantizar que cada fragmento reciba su posición correcta dentro del archivo.
- Actualizar los repositorios necesarios para recuperar los chunks originales ordenados por su índice.
- Garantizar que `getOriginalsChunks(fileId)` devuelva los fragmentos en orden ascendente.

### Criterios de aceptación

- Cada chunk tiene un índice que representa su posición dentro del archivo.
- Los chunks se recuperan siempre en orden ascendente.
- La reconstrucción produce archivos idénticos al original.
- La solución no depende del orden de inserción ni del comportamiento particular del motor de base de datos.

## Fase 5: Mejorar la paginación y resiliencia de CheckFilesUseCase

**Prioridad: Alta**

### Tareas

- Revisar la implementación actual de `CheckFilesUseCase`.
- Reemplazar la recuperación completa mediante `fileRepository.getAll()` por un mecanismo de procesamiento paginado o por lotes.
- Incorporar el método necesario en el repositorio, por ejemplo `findPage(page, size)`, respetando las convenciones existentes.
- Procesar los archivos progresivamente para evitar cargar todos los registros en memoria.
- Aislar la comprobación de cada archivo mediante un manejo individual de excepciones.
- Garantizar que un error durante la auditoría de un archivo no interrumpa la comprobación de los siguientes.
- Crear un objeto de resultado, por ejemplo `IntegrityCheckReport`, que permita conocer el resultado de la ejecución.
- El reporte debe contemplar, como mínimo:
  - Cantidad de archivos verificados.
  - Cantidad de archivos íntegros.
  - Cantidad de archivos curados correctamente.
  - Cantidad de archivos con errores.
  - Información suficiente para identificar los archivos que presentaron problemas.

### Criterios de aceptación

- La auditoría funciona mediante procesamiento paginado.
- Un error individual no interrumpe el procesamiento general.
- El resultado permite conocer el estado global de la auditoría.
- El consumo de memoria no depende de la cantidad total de archivos almacenados.
- Se contemplan repositorios vacíos, última página y errores durante el procesamiento.

## Fase 6: Aplicar inversión de dependencias (DIP) y mejorar extensibilidad (OCP)

**Prioridad: Media**

### Tareas

- Analizar las dependencias actuales de los casos de uso y los servicios involucrados.
- Identificar dependencias concretas que deban sustituirse por abstracciones.
- Evaluar la creación de las siguientes interfaces:
  - `IFileReconstructionService`, implementada por `FileReconstructionService`.
  - `IFileIntegrityService`, implementada por `FileIntegrityService`.
- Garantizar que los casos de uso dependan de contratos y no de implementaciones concretas cuando corresponda.
- Configurar correctamente la inyección de dependencias.
- Evitar crear interfaces innecesarias para componentes que no requieren variabilidad ni aislamiento.
- Garantizar que sea posible sustituir las estrategias de reconstrucción o verificación sin modificar la lógica de los casos de uso.

### Criterios de aceptación

- Los casos de uso dependen de abstracciones apropiadas.
- Las implementaciones concretas se resuelven mediante inyección de dependencias.
- La arquitectura permite extender las estrategias sin modificar innecesariamente el código existente.

## Fase 7: Validación e integración final

**Prioridad: Obligatoria**

### Tareas

- Crear o actualizar pruebas para todos los cambios implementados.
- Ejecutar las pruebas de integración relacionadas con almacenamiento, replicación, reconstrucción y auditoría.
- Verificar que los flujos de subida, descarga, reconstrucción y recuperación de archivos continúen funcionando correctamente.
- Comprobar que no existan errores de compilación ni problemas en la inyección de dependencias.
- Revisar que las modificaciones respeten las convenciones y la arquitectura del proyecto.
- Corregir cualquier regresión introducida durante la implementación.

### Criterios de aceptación

- El proyecto compila correctamente.
- Todas las pruebas relevantes pasan.
- Los seis puntos del plan están implementados o cuentan con una justificación técnica documentada si alguno no puede completarse.
- No existen regresiones conocidas en los flujos existentes.

## Entrega final

Al terminar, proporciona un informe que incluya:

1. **Cambios realizados:** descripción de las modificaciones implementadas, agrupadas por fase.
2. **Archivos modificados:** listado de archivos creados, modificados o eliminados y su propósito.
3. **Decisiones técnicas:** explicación breve de las decisiones relevantes y cualquier desviación respecto al plan original.
4. **Pruebas ejecutadas:** pruebas realizadas y sus resultados.
5. **Pendientes:** problemas que no pudieron resolverse, limitaciones encontradas o recomendaciones adicionales.

## Consideraciones finales

- Ejecuta las fases en el orden establecido, respetando sus dependencias.
- Prioriza la corrección y la integridad de los datos sobre la velocidad de implementación.
- No consideres una tarea terminada únicamente porque el código compile; verifica su comportamiento.
- No ocultes errores ni marques como completadas tareas que no hayan sido verificadas.
- Mantén los cambios enfocados, coherentes y alineados con los principios SOLID.
- Si durante la implementación descubres un problema adicional que afecta directamente la seguridad, integridad o funcionamiento de estos casos de uso, documenta el hallazgo y resuélvelo si es necesario para completar correctamente el objetivo.

